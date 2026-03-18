package com.ddd4j.cloud.core.contract;

import com.ddd4j.cloud.core.context.SpringContext;
import com.ddd4j.cloud.core.context.ThreadContext;
import com.ddd4j.cloud.core.contract.constant.ContextConstants;
import org.springframework.context.ApplicationEvent;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.*;

/**
 * 领域事件
 * 1. 异步事件透传线程变量
 * 2. 租户策略
 * 3. 条件策略
 */
public abstract class DomainEvent<T> extends ApplicationEvent {
    // 监听者能否执行的条件，用于控制事件监听器能否执行（策略模式）
    private Collection<?> supports;
    // 结果，适用于同步返回
    private Object result;

    /**
     * 领域事件构造器
     *
     * @param source 事件源
     */
    public DomainEvent(T source) {
        super(source);
    }

    /**
     * 领域事件构造器
     *
     * @param source   事件源
     * @param supports 支持执行的条件，配合supports方法使用
     */
    public DomainEvent(T source, Collection supports) {
        super(source);
        this.supports = supports;
    }

    /**
     * 领域事件构造器
     *
     * @param source  事件源
     * @param support 支持执行的条件，配合supports方法使用
     */
    public DomainEvent(T source, Object support) {
        super(source);
        this.supports = Collections.singleton(support);
    }

    /**
     * 获取事件源
     *
     * @return 事件源
     */
    public <R> R get() {
        return (R) source();
    }

    /**
     * 获取事件源
     *
     * @return 事件源
     */
    public T source() {
        return (T) super.getSource();
    }

    /**
     * 设置结果，适用于同步返回
     *
     * @param result
     */
    public void result(Object result) {
        this.result = result;
    }

    /**
     * 租户判断
     * 使用方式：监听方法标注@EventListener(condition = "#event.tenantIn('xxx', 'xxx')")
     *
     * @param tenantIds 指定租户ID才能订阅
     * @return 该租户能否监听
     */
    public boolean tenantIn(String... tenantIds) {
        if (tenantIds == null) return false;
        return Arrays.asList(tenantIds).contains(ThreadContext.get(ContextConstants.TENANT_ID));
    }

    /**
     * 条件判断（策略模式）
     * 使用方式：监听方法标注@EventListener(condition = "#event.supports('xxx', 'xxx')")
     *
     * @param supports 支持的类型
     * @param <S>      任意类型
     * @return 该条件下能否监听
     */
    public <S> boolean supports(S... supports) {
        if (this.supports == null || supports == null) return false;
        List<S> supportList = Arrays.asList(supports);
        for (Object support : this.supports) {
            if (supportList.contains(support)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 立即发布事件
     * 使用方便，副作用：会降低代码可读性
     * 建议使用原生的SpringContext.getApplicationContext().publishEvent()方法
     */
    public <R> R publish() {
        SpringContext.ctx().publishEvent(this);
        return (R) result;
    }

    /**
     * 定时发布事件
     *
     * @param sendTime 发送时间
     */
    public void publishAt(Date sendTime) {
        ThreadPoolTaskScheduler taskScheduler = SpringContext.getBean("taskScheduler", ThreadPoolTaskScheduler.class);
        taskScheduler.schedule(this::publish, sendTime);
    }

    /**
     * 延迟发布事件
     *
     * @param delayMillis 延迟毫秒数
     */
    public void publishIn(long delayMillis) {
        publishAt(new Date(System.currentTimeMillis() + delayMillis));
    }
}