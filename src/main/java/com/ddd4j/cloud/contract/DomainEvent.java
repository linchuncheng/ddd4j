package com.ddd4j.cloud.contract;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * 领域事件基类：进程内事件，经 Spring {@code ApplicationEventPublisher} 发布，
 * 由应用层 {@code @EventListener} 处理。
 * <p>
 * 发布方式（显式注入，不用静态定位器）：
 * <pre>
 * &#64;RequiredArgsConstructor
 * public class UserAppService {
 *     private final ApplicationEventPublisher publisher;
 *
 *     public Long create(UserCreateCmd cmd) {
 *         User user = ...;
 *         publisher.publishEvent(new UserCreatedEvent(user));
 *         return user.getId();
 *     }
 * }
 * </pre>
 * 处理方式：应用层 {@code @EventListener} 同步处理；{@code @Async} 异步处理
 * （框架自动传播 {@code AppContext}，异步处理器内可直接取到用户/租户/traceId）；
 * 事务内发布、提交后处理用 {@code @TransactionalEventListener}。
 *
 * @author Jensen
 */
@Getter
public abstract class DomainEvent<T> {

    // 事件ID，用于链路追踪与幂等
    private final String eventId;
    // 事件载荷：通常是聚合根或其快照
    private final T payload;
    // 发生时间
    private final Instant occurredOn;

    protected DomainEvent(T payload) {
        this.eventId = UUID.randomUUID().toString().replace("-", "");
        this.payload = payload;
        this.occurredOn = Instant.now();
    }

    // 载荷快捷访问（与 3.x 习惯一致）
    public T get() {
        return payload;
    }
}
