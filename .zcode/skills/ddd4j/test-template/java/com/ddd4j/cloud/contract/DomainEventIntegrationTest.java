package com.ddd4j.cloud.contract;

import com.ddd4j.cloud.context.AppContext;
import com.ddd4j.cloud.context.SpringContext;
import com.ddd4j.cloud.testsupport.User;
import com.ddd4j.cloud.testsupport.UserCreatedEvent;
import com.ddd4j.cloud.testsupport.UserEventHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 领域事件集成测试：同步监听、异步监听 + AppContext 传播与清理
 */
@SpringBootTest(classes = com.ddd4j.cloud.testsupport.TestApplication.class)
class DomainEventIntegrationTest {

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private UserEventHandler handler;

    @Autowired
    @Qualifier("applicationTaskExecutor")
    private ThreadPoolTaskExecutor taskExecutor;

    @BeforeEach
    void setUp() {
        AppContext.clear();
        AppContext.current().setUserId("u-9");
        AppContext.current().setTenantId("t-9");
        handler.reset();
    }

    @AfterEach
    void tearDown() {
        AppContext.clear();
    }

    private UserCreatedEvent event() {
        User user = new User();
        user.setUsername("event-user");
        user.setAge(1);
        return new UserCreatedEvent(user);
    }

    @Test
    void eventCarriesPayloadAndMeta() {
        UserCreatedEvent event = event();
        assertThat(event.get()).isSameAs(event.getPayload());
        assertThat(event.getPayload().getUsername()).isEqualTo("event-user");
        assertThat(event.getEventId()).isNotBlank();
        assertThat(event.getOccurredOn()).isNotNull();
    }

    @Test
    void syncHandlerReceivesPayloadOnSameThread() {
        UserCreatedEvent event = event();
        publisher.publishEvent(event);
        assertThat(handler.getLastSyncPayload()).isSameAs(event.getPayload());
    }

    @Test
    void asyncHandlerCarriesAppContext() throws Exception {
        publisher.publishEvent(event());

        assertThat(handler.awaitAsync(5)).isTrue();
        assertThat(handler.getAsyncUserId()).isEqualTo("u-9");
        assertThat(handler.getAsyncTenantId()).isEqualTo("t-9");
        assertThat(handler.getAsyncPayloadCount()).isEqualTo(1);
    }

    @Test
    void publishInstanceMethodUsesSpringContext() {
        UserCreatedEvent event = event();
        event.publish();
        assertThat(handler.getLastSyncPayload()).isSameAs(event.getPayload());
    }

    @Test
    void springContextExposesBeans() {
        assertThat(SpringContext.getBean(UserEventHandler.class)).isSameAs(handler);
        assertThat(SpringContext.ctx()).isNotNull();
    }

    @Test
    void executorDoesNotLeakContextBetweenTasks() throws Exception {
        publisher.publishEvent(event());
        assertThat(handler.awaitAsync(5)).isTrue();

        // 模拟一个没有上下文的提交方：上个异步任务执行完后已被装饰器清理，
        // 其上下文不得残留在池化线程上泄漏给后续任务
        AppContext.clear();
        AtomicReference<String> leakedUserId = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        taskExecutor.submit(() -> {
            leakedUserId.set(AppContext.userId());
            latch.countDown();
        });

        assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(leakedUserId.get()).isNull();
    }
}
