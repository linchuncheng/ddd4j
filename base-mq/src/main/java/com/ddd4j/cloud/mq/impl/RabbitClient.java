package com.ddd4j.cloud.mq.impl;

import cn.hutool.core.thread.GlobalThreadPool;
import com.ddd4j.cloud.core.contract.MQEvent;
import com.ddd4j.cloud.core.contract.exception.ServiceException;
import com.ddd4j.cloud.core.kit.JsonKit;
import com.ddd4j.cloud.mq.core.MQClient;
import com.ddd4j.cloud.mq.core.MQFilter;
import com.ddd4j.cloud.mq.core.MQListener;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

@Component
@Slf4j(topic = "### BASE-MQ : rabbitClient ###")
public final class RabbitClient implements MQClient {
    private static Connection CONNECTION;

    @Override
    public String impl() {
        return "rabbit";
    }

    @Override
    public String concat() {
        return ".";
    }

    @Override
    public Consumer<MQEvent> initProducer() {
        try {
            com.rabbitmq.client.Channel channel = connection().createChannel();
            return mqEvent -> {
                // 定义具体的MQ事件发布逻辑
                String namespace = mqEvent.getNamespace() != null ? mqEvent.getNamespace() : config().getNamespace();
                String payload = serialization().serialize(mqEvent);
                String tag = mqEvent.getTag() != null ? mqEvent.getConcat() + mqEvent.getTag() : "";
                // 路由键=namespace.topic或namespace.topic.tag
                String routingKey = namespace + concat() + mqEvent.getTopic() + tag;
                try {
                    channel.basicPublish(config().getExchange(), routingKey, null, payload.getBytes());
                    log.info("Publish MQ [{}]: {}", routingKey, payload);
                } catch (Exception e) {
                    log.error("Publish MQ [{}]: {} failed!", routingKey, payload, e);
                }
            };
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean initConsumer(MQListener mqListener) throws Exception {
        // 队列名=group.namespace.topic.className.methodName
        String queue = mqListener.getGroup() + concat() + mqListener.getNamespace() + concat() + mqListener.getMethod().getDeclaringClass().getSimpleName() + concat() + mqListener.getMethod().getName();
        List<String> routingKeys = new ArrayList<>();
        if (mqListener.getTags() != null && !mqListener.getTags().isEmpty()) {
            // 通过多个tags绑定到对应的路由键
            Set<String> tags = MQFilter.findIncludes(mqListener.getTags());
            if (!tags.isEmpty()) {
                // 路由键=namespace.topic.tag1、namespace.topic.tag2，TODO 这里只能用或||的关系，通配符*和非-是不能生效的
                for (String tag : tags) {
                    String routingKey = mqListener.getNamespace() + concat() + mqListener.getTopic() + concat() + tag;
                    routingKeys.add(routingKey);
                }
            }
        } else {
            // 路由键=namespace.topic
            String routingKey = mqListener.getNamespace() + concat() + mqListener.getTopic();
            routingKeys.add(routingKey);
        }
        try (com.rabbitmq.client.Channel channel = connection().createChannel()) {
            channel.queueDeclare(queue, true, false, false, null);
            // 队列绑定到多个路由器
            for (String routingKey : routingKeys) {
                channel.queueBind(queue, config().getExchange(), routingKey);
            }
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                MQEvent mqEvent = mqListener.getDeserialize().apply(message);
                if (mqEvent == null) {
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    log.warn("Consume MQ [{}] failed: the mqEvent is null", mqListener.key());
                    return;
                }
                if (!MQFilter.matchExps(mqEvent.getTag(), mqListener.getTags())) {
                    return;
                }
                try {
                    consume(mqListener, mqEvent);
                    if (!config().isAutoAck()) {
                        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    }
                } catch (Throwable e) {
                    if (e instanceof ServiceException) {
                        log.error("Consume MQ failed: {} => {}", e.getMessage(), JsonKit.toJson(mqEvent));
                        if (!config().isAutoAck()) {
                            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                        }
                    } else {
                        log.error("Consume MQ failed: {}", mqEvent, e);
                    }
                }
            };
            GlobalThreadPool.submit(() -> {
                try {
                    channel.basicConsume(queue, config().isAutoAck(), deliverCallback, consumerTag -> {
                    });
                } catch (Exception e) {
                }
                return channel;
            });
        }
        return true;
    }

    private Connection connection() {
        if (CONNECTION == null) {
            ConnectionFactory factory = new ConnectionFactory();
            String[] hostAndPort = config().getServer().split(":");
            factory.setHost(hostAndPort[0]);
            factory.setPort(Integer.parseInt(hostAndPort[1]));
            factory.setUsername(config().getUsername());
            factory.setPassword(config().getPassword());
            try {
                CONNECTION = factory.newConnection();
            } catch (IOException | TimeoutException e) {
                throw new RuntimeException(e);
            }
        }
        return CONNECTION;
    }
}