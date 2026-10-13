package com.ddd4j.cloud.data;

import java.util.function.Supplier;

/**
 * 租户开关：{@link Query#ignoreTenant()} 通过它传递给 SQL 层的租户拦截器。
 * 仅框架内部使用，业务代码请通过 Query.ignoreTenant() 表达意图。
 *
 * @author Jensen
 */
public final class TenantManager {

    private static final ThreadLocal<Boolean> IGNORE = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private TenantManager() {
    }

    public static boolean isIgnored() {
        return IGNORE.get();
    }

    public static <T> T ignoring(Supplier<T> task) {
        IGNORE.set(Boolean.TRUE);
        try {
            return task.get();
        } finally {
            IGNORE.remove();
        }
    }
}
