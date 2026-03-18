package com.ddd4j.cloud.mq.impl;

import com.ddd4j.cloud.core.contract.MQEvent;
import com.ddd4j.cloud.core.contract.exception.ServiceException;
import com.ddd4j.cloud.core.kit.JsonKit;
import com.ddd4j.cloud.mq.core.MQClient;
import com.ddd4j.cloud.mq.core.MQFilter;
import com.ddd4j.cloud.mq.core.MQListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.StreamEntryID;
import redis.clients.jedis.params.XReadGroupParams;
import redis.clients.jedis.resps.StreamEntry;
import redis.clients.jedis.resps.StreamGroupInfo;

/**
 * Redis Stream 客户端实现
 */
@Slf4j(topic = "### BASE-MQ : redisStreamClient ###")
@Component
public final class RedisStreamClient implements MQClient {
    BlockingQueue<MQEvent> SENDING_MSGS = new LinkedBlockingQueue<>();
    private AtomicBoolean started = new AtomicBoolean(false);
    // 使用 ThreadLocal 来管理每个线程的 Jedis 实例
    private final ThreadLocal<Jedis> jedisThreadLocal = ThreadLocal.withInitial(this::createJedis);

    private Jedis createJedis() {
        String[] hostAndPort = config().getServer().split(":");
        String host = hostAndPort[0];
        int port = Integer.parseInt(hostAndPort[1]);
        String username = config().getUsername();
        String password = config().getPassword();

        Jedis jedis = new Jedis(host, port);
        if (password != null && !password.isEmpty()) {
            if (username != null && !username.isEmpty()) {
                jedis.auth(username, password);
            } else {
                jedis.auth(password);
            }
        }
        return jedis;
    }

    private Jedis jedis() {
        Jedis jedis = jedisThreadLocal.get();
        if (jedis == null || !jedis.isConnected()) {
            jedisThreadLocal.set(createJedis());
        }
        return jedisThreadLocal.get();
    }

    @Override
    public String impl() {
        return "redisStream";
    }

    @Override
    public String concat() {
        return ":";
    }

    private void run() {
        Executors.newSingleThreadExecutor().submit(() -> {
            log.info("MQ publisher start");
            while (!Thread.currentThread().isInterrupted()) {
                String payload = null;
                String streamKey = null;
                try {
                    MQEvent mqEvent = SENDING_MSGS.take();
                    payload = serialization().serialize(mqEvent);
                    String namespace = mqEvent.getNamespace() != null ? mqEvent.getNamespace() : config().getNamespace();
                    String concat = mqEvent.getConcat() != null ? mqEvent.getConcat() : ":";
                    String tag = (mqEvent.getTag() == null || mqEvent.getTag().isEmpty()) ? "" : (concat + mqEvent.getTag());
                    // channel=namespace:topic:tag或namespace:topic或topic
                    streamKey = namespace + concat + mqEvent.getTopic() + tag;
                    // streamKey=namespace:topic:tag或namespace:topic或topic
                    jedis().xadd(streamKey, StreamEntryID.NEW_ENTRY, Collections.singletonMap("payload", payload));
                    log.info("Publish MQ [{}]: {}", streamKey, payload);
                } catch (Exception e) {
                    log.error("Publish MQ [{}]: {} failed!", streamKey, payload, e);
                }
            }
            log.info("MQ publisher stopped");
        });
    }

    @Override
    public Consumer<MQEvent> initProducer() {
        if (started.compareAndSet(false, true)) {
            run();
        }
        return mqEvent -> SENDING_MSGS.offer(mqEvent);
    }

    @Override
    public boolean initConsumer(MQListener mqListener) throws Exception {
        // streamKey=namespace:topic:tag或namespace:topic或topic
        List<String> topics = new ArrayList<>();
        if (mqListener.getTags() != null && !mqListener.getTags().isEmpty()) {
            Set<String> tags = MQFilter.findIncludes(mqListener.getTags());
            if (!tags.isEmpty()) {
                for (String tag : tags) {
                    topics.add(mqListener.getNamespace() + concat() + mqListener.getTopic() + concat() + tag);
                }
            }
        } else {
            topics.add(mqListener.getNamespace() + concat() + mqListener.getTopic());
        }

        Map<String, StreamEntryID> streamKeys = new HashMap<>();
        try {
            Jedis jedis = jedis();
            // 创建消费组（忽略已存在）
            for (String topic : topics) {
                try {
                    if (!jedis.exists(topic)) {
                        jedis.xadd(topic, StreamEntryID.NEW_ENTRY, Collections.singletonMap("payload", "{}"));
                    }
                    List<StreamGroupInfo> streamGroupInfos = jedis.xinfoGroups(topic);
                    List<String> streamGroupInfoGroupNames = streamGroupInfos.stream().map(StreamGroupInfo::getName).collect(Collectors.toList());
                    if (!streamGroupInfoGroupNames.contains(mqListener.getGroup())) {
                        // 创建消费者组，使用$表示最后一个条目的ID，MKSTREAM选项设置为true以确保如果Stream不存在则创建它
                        jedis.xgroupCreate(topic, mqListener.getGroup(), new StreamEntryID("0-0"), true);
                    }
                    streamKeys.put(topic, StreamEntryID.UNRECEIVED_ENTRY);
                } catch (Exception e) {
                    log.info("Create consumer group failed: key={}, groupname={}", topic, mqListener.getGroup(), e);
                }
            }
            if (streamKeys.isEmpty()) {
                return false;
            }
        } catch (Exception e) {
            log.error("Create consumer group failed!", e);
        }
        Executors.newSingleThreadExecutor().submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Jedis jedis = jedis();
                    // 从消费组中读取消息
                    List<Map.Entry<String, List<StreamEntry>>> messages = jedis.xreadGroup(mqListener.getGroup(),
                            mqListener.getMethod().getName(),
                            XReadGroupParams.xReadGroupParams().count(1).block(1000),
                            streamKeys);
                    if (messages == null || messages.isEmpty()) {
                        // 没有消息，可以选择在这里短暂休眠，避免CPU占用过高
                        TimeUnit.MILLISECONDS.sleep(1000);
                        continue;
                    }

                    for (Map.Entry<String, List<StreamEntry>> entry : messages) {
                        for (StreamEntry streamEntry : entry.getValue()) {
                            String payload = streamEntry.getFields().get("payload");
                            MQEvent mqEvent = mqListener.getDeserialize().apply(payload);
                            if (mqEvent == null) {
                                jedis.xack(entry.getKey(), mqListener.getGroup(), streamEntry.getID());
                                log.warn("Consume MQ [{}] failed: the mqEvent is null", mqListener.key());
                                continue;
                            }
                            try {
                                if (config().isAutoAck()) {
                                    jedis.xack(entry.getKey(), mqListener.getGroup(), streamEntry.getID());
                                }
                                // 消费消息
                                consume(mqListener, mqEvent);
                                // 确认消息
                                if (!config().isAutoAck()) {
                                    jedis.xack(entry.getKey(), mqListener.getGroup(), streamEntry.getID());
                                }
                            } catch (Throwable e) {
                                if (e instanceof ServiceException) {
                                    log.error("Consume MQ [{}] failed: {} => {}", mqListener.key(), e.getMessage(), JsonKit.toJson(mqEvent));
                                } else {
                                    log.error("Consume MQ [{}] failed: {}", mqListener.key(), mqEvent, e);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("Consume MQ [{}] failed!", mqListener.key(), e);
                    try {
                        TimeUnit.MILLISECONDS.sleep(5000);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });

        return true;
    }

}