package com.ddd4j.cloud.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class AppContextTest {

    @AfterEach
    void tearDown() {
        AppContext.clear();
    }

    @Test
    void setAndRead() {
        AppContext.current().setUserId("u-1");
        AppContext.current().setTenantId("t-2");
        AppContext.current().setTraceId("t-123");

        assertThat(AppContext.userId()).isEqualTo("u-1");
        assertThat(AppContext.tenantId()).isEqualTo("t-2");
        assertThat(AppContext.traceId()).isEqualTo("t-123");
    }

    @Test
    void emptyContextReturnsNull() {
        assertThat(AppContext.userId()).isNull();
        assertThat(AppContext.tenantId()).isNull();
        assertThat(AppContext.traceId()).isNull();
    }

    @Test
    void snapshotAndRestore() {
        AppContext.current().setTenantId("t-7");
        AppContext.current().setAttribute("k", "v");
        AppContext snapshot = AppContext.snapshot();

        AppContext.clear();
        assertThat(AppContext.tenantId()).isNull();

        AppContext.restore(snapshot);
        assertThat(AppContext.tenantId()).isEqualTo("t-7");
        assertThat(AppContext.getAttribute("k")).isEqualTo("v");
    }

    @Test
    void restoreNullClearsContext() {
        AppContext.current().setTenantId("t-7");
        AppContext.restore(null);
        assertThat(AppContext.tenantId()).isNull();
    }

    @Test
    void wrapCarriesContextAcrossThreads() throws Exception {
        AppContext.current().setTenantId("42");
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            AtomicReference<String> seen = new AtomicReference<>();
            executor.submit(AppContext.wrap(() -> seen.set(AppContext.tenantId()))).get(5, TimeUnit.SECONDS);
            assertThat(seen.get()).isEqualTo("42");
            // 任务结束后不应污染池化线程
            AtomicReference<String> afterTask = new AtomicReference<>();
            executor.submit(() -> afterTask.set(AppContext.tenantId())).get(5, TimeUnit.SECONDS);
            assertThat(afterTask.get()).isNull();
        } finally {
            executor.shutdownNow();
        }
    }
}
