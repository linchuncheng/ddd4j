package com.ddd4j.cloud.context;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 应用上下文：当前请求的用户 / 租户 / 链路ID，基于 ThreadLocal。
 * <p>
 * 由 web 层 ContextInterceptor 在请求入口写入、请求结束清理；
 * 自定义线程池场景用 {@link #snapshot()} + {@link #restore(AppContext)}
 * 或 {@link #wrap(Runnable)} 显式传递，不做任何隐式继承。
 *
 * @author Jensen
 */
public final class AppContext {

    private static final ThreadLocal<AppContext> HOLDER = new ThreadLocal<>();

    private Long userId;
    private Long tenantId;
    private String traceId;
    private final Map<String, Object> attributes = new HashMap<>(4);

    private AppContext() {
    }

    public static AppContext current() {
        AppContext context = HOLDER.get();
        if (context == null) {
            context = new AppContext();
            HOLDER.set(context);
        }
        return context;
    }

    public static Long userId() {
        AppContext context = HOLDER.get();
        return context == null ? null : context.userId;
    }

    public static Long tenantId() {
        AppContext context = HOLDER.get();
        return context == null ? null : context.tenantId;
    }

    public static String traceId() {
        AppContext context = HOLDER.get();
        return context == null ? null : context.traceId;
    }

    public static void setUserId(Long userId) {
        current().userId = userId;
    }

    public static void setTenantId(Long tenantId) {
        current().tenantId = tenantId;
    }

    public static void setTraceId(String traceId) {
        current().traceId = traceId;
    }

    public static Object getAttribute(String key) {
        AppContext context = HOLDER.get();
        return context == null ? null : context.attributes.get(key);
    }

    public static void setAttribute(String key, Object value) {
        current().attributes.put(key, value);
    }

    // 复制当前上下文，用于提交任务前保存
    public static AppContext snapshot() {
        AppContext context = HOLDER.get();
        if (context == null) {
            return null;
        }
        AppContext snapshot = new AppContext();
        snapshot.userId = context.userId;
        snapshot.tenantId = context.tenantId;
        snapshot.traceId = context.traceId;
        snapshot.attributes.putAll(context.attributes);
        return snapshot;
    }

    // 在当前线程恢复上下文快照，传 null 等价于 clear
    public static void restore(AppContext snapshot) {
        if (snapshot == null) {
            HOLDER.remove();
        } else {
            HOLDER.set(snapshot);
        }
    }

    public static void clear() {
        HOLDER.remove();
    }

    // 包装任务：执行时使用快照上下文，结束后清理，适用于自定义线程池
    public static Runnable wrap(Runnable task) {
        AppContext snapshot = snapshot();
        return () -> {
            restore(snapshot);
            try {
                task.run();
            } finally {
                clear();
            }
        };
    }

    public static <T> Supplier<T> wrap(Supplier<T> task) {
        AppContext snapshot = snapshot();
        return () -> {
            restore(snapshot);
            try {
                return task.get();
            } finally {
                clear();
            }
        };
    }
}
