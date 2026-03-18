package com.ddd4j.cloud.mq.core;

import com.ddd4j.cloud.core.context.BaseContext;
import com.ddd4j.cloud.core.context.SpringContext;
import com.ddd4j.cloud.core.context.ThreadContext;
import com.ddd4j.cloud.core.contract.MQEvent;
import com.ddd4j.cloud.core.contract.annotation.MQEventListener;
import com.ddd4j.cloud.core.contract.constant.ContextConstants;
import com.ddd4j.cloud.mq.config.BaseMQProperties;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * MQClient接口，MQ实现类实现该接口做差异化实现
 */
public interface MQClient {
    // MQ实现
    String impl();

    default String concat() {
        return config().getConcat();
    }

    // MQ配置
    default BaseMQProperties config() {
        return SpringContext.getBean(BaseMQProperties.class);
    }

    // 整体初始化
    default void init() {
        if (!Objects.equals(config().getImpl(), impl())) return;
        Logger log = LoggerFactory.getLogger("### BASE-MQ : " + impl() + "Client ###");
        // 初始化MQ事件发布器
        if (!BaseContext.contains("MQEventPublisher")) {
            log.info("Initializing MQEventPublisher");
            Consumer<MQEvent> producer = initProducer();
            if (producer != null) {
                BaseContext.inject("MQEventPublisher", producer);
            }
        }
        // 初始化MQ事件监听器
        log.info("Initializing MQEventListener");
        String[] beanDefinitionNames = SpringContext.ctx().getBeanDefinitionNames();
        List<MQListener> mqListeners = new ArrayList<>();
        for (String beanName : beanDefinitionNames) {
            try {
                // 找出所有Bean下注解了@MQEventListener的方法
                Method[] listenerMethods = AopUtils.getTargetClass(SpringContext.getBean(beanName)).getDeclaredMethods();
                for (Method method : listenerMethods) {
                    MQEventListener mqEventListener = AnnotationUtils.findAnnotation(method, MQEventListener.class);
                    if (mqEventListener != null) {
                        // 消费者组，不设置默认按Spring工程的应用名隔离，当然也可以自定义
                        String group = mqEventListener.group().isEmpty() ? SpringContext.getEnv().getProperty("spring.application.name") + "_" + method.getName() : mqEventListener.group();
                        String namespace = mqEventListener.namespace().isEmpty() ? config().getNamespace() : mqEventListener.namespace();
                        // 取出首个参数，用于接收MQ事件后自动解析到该类型
                        mqListeners.add(MQListener.builder().method(method).group(group)
                                .namespace(namespace).topic(mqEventListener.topic()).tags(mqEventListener.tags()).concat(concat())
                                .supports(Arrays.asList(mqEventListener.supports()))
                                .deserialize(message -> serialization().deserialize(message, (Class<MQEvent>) method.getParameterTypes()[0]))
                                .build());
                    }
                }
            } catch (Exception ignore) {
            }
        }
        for (MQListener mqListener : mqListeners) {
            try {
                // 具体创建消费者的逻辑，不同MQ实现不一样
                mqListener.setSuccess(initConsumer(mqListener));
            } catch (Exception e) {
                log.error("Listen MQ [{}] failed!", mqListener.key(), e);
            }
        }
        log.info("Listening MQ: {}", mqListeners.stream().filter(MQListener::isSuccess).map(MQListener::key).collect(Collectors.joining(",")));
    }

    // 初始化生产者
    Consumer<MQEvent> initProducer();

    // 初始化消费者
    boolean initConsumer(MQListener mqListener) throws Exception;

    // 启动
    default void start() {
    }

    // 序列化器，默认取配置了的实现了MQSerialization的Bean
    default MQEventSerialization serialization() {
        return SpringContext.getBean(config().getSerialization());
    }

    // 消费MQ事件
    default void consume(@NonNull MQListener mqListener, @NonNull MQEvent mqEvent) throws Throwable {
        if (!mqEvent.supports(mqListener.getSupports())) {
            return;
        }
        Logger log = LoggerFactory.getLogger("### BASE-MQ : " + impl() + "Client ###");
        log.info("Consume MQ [{}]: {}", mqListener.key(), serialization().<String>serialize(mqEvent));
        try {
            ThreadContext.set(ContextConstants.TENANT_ID, mqEvent.getTenantId());
            // MQ事件持久化
            if (config().isPersist()) {
                try {
                    MQEventStorer mqEventStorer = SpringContext.getBean(MQEventStorer.class);
                    mqEventStorer.store(mqEvent);
                } catch (Exception e) {
                    log.error("Persist MQ failed [{}]: {}", mqListener.key(), serialization().<String>serialize(mqEvent), e);
                }
            }
            // 解析为MQEvent后，具体调用该方法的位置
            mqListener.getMethod().invoke(SpringContext.getBean(mqListener.getMethod().getDeclaringClass()), mqEvent);
        } catch (Exception e) {
            if (e.getCause() != null && e.getCause().getCause() != null) {
                throw e.getCause().getCause();
            } else if (e.getCause() != null) {
                throw e.getCause();
            } else {
                throw e;
            }
        } finally {
            ThreadContext.clear();
        }
    }

}