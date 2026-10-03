package com.ddd4j.cloud.testsupport;

import com.ddd4j.cloud.context.AppContext;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 事件处理器样例：同步监听 + 异步监听（验证上下文自动传播）
 */
@Component
public class UserEventHandler {

    private User lastSyncPayload;
    private final List<User> asyncPayloads = new Vector<>();
    private final AtomicReference<String> asyncUserId = new AtomicReference<>();
    private final AtomicReference<String> asyncTenantId = new AtomicReference<>();
    private volatile CountDownLatch asyncLatch = new CountDownLatch(1);

    // 同步监听：与发布同线程
    @EventListener
    public void onUserCreated(UserCreatedEvent event) {
        this.lastSyncPayload = event.get();
    }

    // 异步监听：默认任务执行器，框架装饰器自动携带 AppContext
    @Async
    @EventListener
    public void onUserCreatedAsync(UserCreatedEvent event) {
        asyncUserId.set(AppContext.userId());
        asyncTenantId.set(AppContext.tenantId());
        asyncPayloads.add(event.get());
        asyncLatch.countDown();
    }

    public void reset() {
        this.lastSyncPayload = null;
        asyncUserId.set(null);
        asyncTenantId.set(null);
        asyncPayloads.clear();
        this.asyncLatch = new CountDownLatch(1);
    }

    public User getLastSyncPayload() {
        return lastSyncPayload;
    }

    public boolean awaitAsync(long seconds) throws InterruptedException {
        return asyncLatch.await(seconds, TimeUnit.SECONDS);
    }

    public String getAsyncUserId() {
        return asyncUserId.get();
    }

    public String getAsyncTenantId() {
        return asyncTenantId.get();
    }

    public int getAsyncPayloadCount() {
        return asyncPayloads.size();
    }
}
