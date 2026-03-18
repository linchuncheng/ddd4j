package com.ddd4j.cloud.mq.impl;

import com.ddd4j.cloud.core.contract.MQEvent;
import com.ddd4j.cloud.core.contract.exception.ServiceException;
import com.ddd4j.cloud.core.kit.JsonKit;
import com.ddd4j.cloud.mq.core.MQClient;
import com.ddd4j.cloud.mq.core.MQFilter;
import com.ddd4j.cloud.mq.core.MQListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Redis Pubsub客户端实现
 */
@Slf4j(topic = "### BASE-MQ : redisClient ###")
@Component
public final class RedisClient implements MQClient {
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
        return "redis";
    }

    @Override
    public String concat() {
        return ":";
    }

    private void run() {
        Executors.newSingleThreadExecutor().submit(() -> {
            log.info("MQ publisher start");
            while (!Thread.currentThread().isInterrupted()) {
                String channel = null;
                String payload = null;
                try {
                    MQEvent mqEvent = SENDING_MSGS.take();
                    payload = serialization().serialize(mqEvent);
                    String namespace = mqEvent.getNamespace() != null ? mqEvent.getNamespace() : config().getNamespace();
                    String tag = (mqEvent.getTag() == null || mqEvent.getTag().isEmpty()) ? "" : (concat() + mqEvent.getTag());
                    // channel=namespace:topic:tag或namespace:topic或topic
                    channel = namespace + concat() + mqEvent.getTopic() + tag;
                    jedis().publish(channel, payload);
                    log.info("Publish MQ [{}]: {}", channel, payload);
                } catch (Exception e) {
                    log.error("Publish MQ [{}]: {} failed!", channel, payload, e);
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
        // channel=namespace:topic:tag或namespace:topic或topic，TODO 暂不支持通配符*和非-
        List<String> channels = new ArrayList<>();
        if (mqListener.getTags() != null && !mqListener.getTags().isEmpty()) {
            Set<String> tags = MQFilter.findIncludes(mqListener.getTags());
            if (!tags.isEmpty()) {
                for (String tag : tags) {
                    channels.add(mqListener.getNamespace() + concat() + mqListener.getTopic() + concat() + tag);
                }
            }
        } else {
            channels.add(mqListener.getNamespace() + concat() + mqListener.getTopic());
        }
        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                jedis().subscribe(new JedisPubSub() {
                    @Override
                    public void onMessage(String channel, String message) {
                        MQEvent mqEvent = mqListener.getDeserialize().apply(message);
                        if (mqEvent == null) {
                            log.warn("Consume MQ [{}] failed: the mqEvent is null", mqListener.key());
                            return;
                        }
                        String group = String.format("ChannelGroupLock:%s:%s", mqListener.key(), mqEvent.getMsgId());
                        try {
                            // 按组消费：Redis本身没有消费组的概念，这里我们锁住某条消息的消费资格10秒钟，以实现同消费组（如多实例）只能由一个消费者消费
//                    if (!singleton().exists(lockKey)) {
//                        singleton().setex(lockKey, 10L, lockKey);
                            // 本条消息在该消费者组未消费
                            consume(mqListener, mqEvent);
//                    }
                        } catch (Throwable e) {
                            if (e instanceof ServiceException) {
                                log.error("Consume MQ [{}] failed: {} => {}", group, e.getMessage(), JsonKit.toJson(mqEvent));
                            } else {
                                log.error("Consume MQ [{}] failed: {}", group, mqEvent, e);
                            }
                        }
                    }

                    @Override
                    public void onSubscribe(String channel, int subscribedChannels) {
                        log.info("Subscribed channel: {}", channel);
                    }

                    @Override
                    public void onUnsubscribe(String channel, int subscribedChannels) {
                        log.info("Unsubscribed channel: {}", channel);
                    }
                }, channels.toArray(new String[0]));
            } catch (Exception e) {
            }
        });
        return true;
    }

}