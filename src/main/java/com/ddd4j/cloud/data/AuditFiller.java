package com.ddd4j.cloud.data;

import com.ddd4j.cloud.context.AppContext;
import com.ddd4j.cloud.data.annotation.OnCreate;
import com.ddd4j.cloud.data.annotation.OnCreateBy;
import com.ddd4j.cloud.data.annotation.OnUpdate;
import com.ddd4j.cloud.data.annotation.OnUpdateBy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 审计字段填充，发生在 MybatisRepository 写入时（显式、无 MyBatis-Plus 内部机制参与）：
 * <ul>
 *   <li>{@link OnCreate}：插入时填充当前时间；{@link OnUpdate}：插入和更新时填充当前时间，
 *       类型支持 LocalDateTime / LocalDate / Date / Long（毫秒时间戳）</li>
 *   <li>{@link OnCreateBy}：插入时填充当前用户；{@link OnUpdateBy}：插入和更新时填充当前用户，
 *       类型为 String，取自 {@code AppContext.userId()}，上下文无用户（系统任务）时保持原值</li>
 * </ul>
 * 字段已有值时不覆盖。
 *
 * @author Jensen
 */
public final class AuditFiller {

    private static final Map<Class<?>, List<Field>> TIME_CREATE_FIELDS = new ConcurrentHashMap<>();
    private static final Map<Class<?>, List<Field>> TIME_UPDATE_FIELDS = new ConcurrentHashMap<>();
    private static final Map<Class<?>, List<Field>> USER_CREATE_FIELDS = new ConcurrentHashMap<>();
    private static final Map<Class<?>, List<Field>> USER_UPDATE_FIELDS = new ConcurrentHashMap<>();

    private AuditFiller() {
    }

    public static void onInsert(Object model) {
        // 插入时尊重显式赋值（导入、订正场景可回填历史时间/操作人）
        fill(model, OnCreate.class, TIME_CREATE_FIELDS, AuditFiller::now, false);
        fill(model, OnCreateBy.class, USER_CREATE_FIELDS, AuditFiller::currentUser, false);
        fill(model, OnUpdate.class, TIME_UPDATE_FIELDS, AuditFiller::now, false);
        fill(model, OnUpdateBy.class, USER_UPDATE_FIELDS, AuditFiller::currentUser, false);
    }

    public static void onUpdate(Object model) {
        // 更新时无条件覆盖：update_time/update_by 记录的就是"这次是谁改的"，旧值没有保留意义
        fill(model, OnUpdate.class, TIME_UPDATE_FIELDS, AuditFiller::now, true);
        fill(model, OnUpdateBy.class, USER_UPDATE_FIELDS, AuditFiller::currentUser, true);
    }

    private static void fill(Object model, Class<? extends Annotation> annotation,
                             Map<Class<?>, List<Field>> cache, Function<Class<?>, Object> valueFor,
                             boolean overwrite) {
        for (Field field : annotatedFields(model.getClass(), annotation, cache)) {
            try {
                if (!overwrite && field.get(model) != null) {
                    continue;
                }
                Object value = valueFor.apply(field.getType());
                if (value == null) {
                    continue;
                }
                field.set(model, value);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("审计字段填充失败: " + field.getName(), e);
            }
        }
    }

    // 当前用户；无用户上下文返回 null 表示跳过填充
    private static Object currentUser(Class<?> type) {
        return type == String.class ? AppContext.userId() : null;
    }

    private static Object now(Class<?> type) {
        if (type == LocalDate.class) {
            return LocalDate.now();
        }
        if (type == Date.class) {
            return new Date();
        }
        if (type == Long.class || type == long.class) {
            return System.currentTimeMillis();
        }
        return LocalDateTime.now();
    }

    // 按类缓存注解字段，避免每次写入都反射扫描
    private static List<Field> annotatedFields(Class<?> clazz, Class<? extends Annotation> annotation,
                                               Map<Class<?>, List<Field>> cache) {
        return cache.computeIfAbsent(clazz, key -> scan(key, annotation));
    }

    private static List<Field> scan(Class<?> clazz, Class<? extends Annotation> annotation) {
        List<Field> result = new ArrayList<>();
        for (Class<?> current = clazz; current != null && current != Object.class; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (field.isAnnotationPresent(annotation)) {
                    field.setAccessible(true);
                    result.add(field);
                }
            }
        }
        return result;
    }
}
