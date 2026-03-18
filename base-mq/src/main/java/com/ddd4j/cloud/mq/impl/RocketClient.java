package com.ddd4j.cloud.mq.impl;

import com.ddd4j.cloud.core.context.SpringContext;
import com.ddd4j.cloud.core.contract.MQEvent;
import com.ddd4j.cloud.core.contract.exception.ServiceException;
import com.ddd4j.cloud.core.kit.JsonKit;
import com.ddd4j.cloud.mq.core.MQClient;
import com.ddd4j.cloud.mq.core.MQListener;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.acl.common.AclClientRPCHook;
import org.apache.rocketmq.acl.common.SessionCredentials;
import org.apache.rocketmq.client.AccessChannel;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.common.RemotingHelper;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Rocket客户端实现
 */
@Slf4j(topic = "### BASE-MQ : rocketClient ###")
public final class RocketClient implements MQClient {
    private static List<Supplier> LISTENERS = new ArrayList<>();

    @Override
    public String impl() {
        return "rocket";
    }

    @Override
    public Consumer<MQEvent> initProducer() {
        try {
            // 创建 DefaultMQProducer 实例
            DefaultMQProducer rocketProducer;
            if (!config().getUsername().isEmpty() && !config().getPassword().isEmpty()) {
                // 需要鉴权
                rocketProducer = new DefaultMQProducer(new AclClientRPCHook(new SessionCredentials(config().getUsername(), config().getPassword())));
            } else {
                rocketProducer = new DefaultMQProducer();
            }
            // 生产者组
            rocketProducer.setProducerGroup(SpringContext.getEnv().getProperty("spring.application.name"));
            // 设置接入方式为阿里云，在使用云上消息轨迹的时候，需要设置此项；如果不开启消息轨迹功能，则不需要运行此项。
            rocketProducer.setAccessChannel(AccessChannel.CLOUD);
            // 5.3.0版本及以上SDK开启消息轨迹除需设置AccessChannel外，需要增加，EnableTrace参数
            rocketProducer.setEnableTrace(true);
            rocketProducer.setNamesrvAddr(config().getServer());
            // 启动rocketProducer实例
            rocketProducer.start();
            DefaultMQProducer finalRocketProducer = rocketProducer;
            return mqEvent -> {
                // 定义具体的MQ事件发布逻辑
                String message = serialization().serialize(mqEvent);
                String tags = mqEvent.getTag() == null ? "" : mqEvent.getTag();
                try {
                    finalRocketProducer.send(new Message(mqEvent.getTopic(), tags, message.getBytes(RemotingHelper.DEFAULT_CHARSET)));
                    log.info("Publish MQ [{}]: {}", mqEvent.getNamespace() + concat() + mqEvent.getTopic() + concat() + tags, message);
                } catch (Exception e) {
                    log.error("Publish MQ [{}]: {} failed!", mqEvent.getNamespace() + concat() + mqEvent.getTopic() + concat() + tags, message, e);
                }
            };
        } catch (MQClientException e) {
            log.error("创建Rocket生产者失败", e);
        }
        return null;
    }

    @Override
    public boolean initConsumer(MQListener mqListener) throws Exception {
        DefaultMQPushConsumer defaultMQPushConsumer;
        if (!config().getUsername().isEmpty() && !config().getPassword().isEmpty()) {
            // 需要鉴权的情况
            defaultMQPushConsumer = new DefaultMQPushConsumer(new AclClientRPCHook(new SessionCredentials(config().getUsername(), config().getPassword())));
        } else {
            defaultMQPushConsumer = new DefaultMQPushConsumer();
        }
        log.info("Listening consumer group: {}", mqListener.getGroup());
        defaultMQPushConsumer.setConsumerGroup(mqListener.getGroup());
        // 设置接入方式为阿里云，在使用云上消息轨迹的时候，需要设置此项；如果不开启消息轨迹功能，则不需要运行此项。
        defaultMQPushConsumer.setAccessChannel(AccessChannel.CLOUD);
        // 5.3.0版本及以上SDK开启消息轨迹除需设置AccessChannel外，需要增加，EnableTrace参数
        defaultMQPushConsumer.setEnableTrace(true);
        defaultMQPushConsumer.setNamesrvAddr(config().getServer());
        defaultMQPushConsumer.subscribe(mqListener.getTopic(), mqListener.getTags());
        // 创建MessageListenerConcurrently实例
        defaultMQPushConsumer.registerMessageListener((MessageListenerConcurrently) (messageExts, context) -> {
            // 获取方法参数
            for (MessageExt messageExt : messageExts) {
                MQEvent mqEvent = null;
                try {
                    // 反序列化为MQEvent对象
                    mqEvent = mqListener.getDeserialize().apply(new String(messageExt.getBody(), StandardCharsets.UTF_8));
                    if (mqEvent != null) {
                        consume(mqListener, mqEvent);
                    } else {
                        log.warn("Consume MQ [{}] failed: the mqEvent is null", mqListener.key());
                    }
                } catch (Throwable e) {
                    if (e instanceof ServiceException) {
                        log.error("Consume MQ failed: {} => {}", e.getMessage(), JsonKit.toJson(mqEvent));
                    } else {
                        log.error("Consume MQ failed: {}", mqEvent, e);
                    }
                }
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });
        LISTENERS.add(() -> defaultMQPushConsumer);
        return true;
    }

    @Override
    @SneakyThrows
    public void start() {
        if (!Objects.equals(config().getImpl(), impl())) return;
        // 在此统一启动所有消费者
        for (Supplier<DefaultMQPushConsumer> listener : LISTENERS) {
            listener.get().start();
        }
    }
}