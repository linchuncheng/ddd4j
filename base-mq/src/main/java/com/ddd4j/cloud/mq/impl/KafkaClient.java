package com.ddd4j.cloud.mq.impl;

import cn.hutool.core.thread.GlobalThreadPool;
import com.ddd4j.cloud.core.contract.MQEvent;
import com.ddd4j.cloud.core.contract.exception.ServiceException;
import com.ddd4j.cloud.core.kit.JsonKit;
import com.ddd4j.cloud.mq.core.MQClient;
import com.ddd4j.cloud.mq.core.MQFilter;
import com.ddd4j.cloud.mq.core.MQListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.function.Consumer;

/**
 * Kafka客户端实现
 */
@Slf4j(topic = "### BASE-MQ : kafkaClient ###")
public final class KafkaClient implements MQClient {

    @Override
    public String impl() {
        return "kafka";
    }

    @Override
    public Consumer<MQEvent> initProducer() {
        // 创建 Producer 配置
        Properties props = new Properties();
        props.put("bootstrap.servers", config().getServer()); // Kafka broker 地址
        props.put("acks", "all");
        props.put("retries", config().getRetries());// 失败重试次数
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        // 创建KafkaProducer实例
        Producer<String, String> producer = new KafkaProducer<>(props);
        return mqEvent -> {
            // 定义具体的MQ事件发布逻辑
            String payload = serialization().serialize(mqEvent);
            String namespace = mqEvent.getNamespace() != null ? mqEvent.getNamespace() : config().getNamespace();
            String concat = mqEvent.getConcat() != null ? mqEvent.getConcat() : "_";
            String topic = namespace + concat + mqEvent.getTopic();
            log.info("Publish MQ [{}]: {}", topic, payload);
            try {
                // topic使用下划线的方式拼接
                producer.send(new ProducerRecord<>(topic, payload));
            } catch (Exception e) {
                log.error("Publish MQ [{}]: {} failed!", topic, payload, e);
            }
        };
    }

    @Override
    public boolean initConsumer(MQListener mqListener) throws Exception {
        Properties props = new Properties();
        props.put("bootstrap.servers", config().getServer());
        props.put("group.id", mqListener.getGroup()); // 消费者组
        props.put("enable.auto.commit", config().isAutoAck());
        props.put("auto.offset.reset", "earliest"); // 从最早的偏移量开始消费
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(mqListener.getNamespace() + concat() + mqListener.getTopic()));
        GlobalThreadPool.submit(() -> {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, String> record : records) {
                    // 处理消息
                    // Kafka没有标签过滤机制，所以自己实现一遍
                    MQEvent mqEvent = mqListener.getDeserialize().apply(record.value());
                    if (mqEvent == null) {
                        consumer.commitSync(); // 手动提交偏移量
                        log.warn("Consume MQ [{}] failed: the mqEvent is null", mqListener.key());
                        continue;
                    }
                    if (!MQFilter.matchExps(mqEvent.getTag(), mqListener.getTags())) {
                        continue;
                    }
                    try {
                        consume(mqListener, mqEvent);
                        if (!config().isAutoAck()) {
                            consumer.commitSync(); // 手动提交偏移量
                        }
                    } catch (Throwable e) {
                        if (e instanceof ServiceException) {
                            log.error("Consume MQ failed: {} => {}", e.getMessage(), JsonKit.toJson(mqEvent));
                            if (!config().isAutoAck()) {
                                consumer.commitSync(); // 手动提交偏移量
                            }
                        } else {
                            log.error("Consume MQ failed: {}", mqEvent, e);
                        }
                    }
                }
            }
        });
        return true;
    }

}