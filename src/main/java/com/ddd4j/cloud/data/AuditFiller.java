package com.ddd4j.cloud.data;

import com.ddd4j.cloud.data.annotation.OnCreate;
import com.ddd4j.cloud.data.annotation.OnUpdate;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 审计字段填充：标注 {@link OnCreate} 的字段在仓储插入时填充当前时间，
 * 标注 {@link OnUpdate} 的字段在插入和更新时填充当前时间。
 * 字段已有值时不覆盖。
 * 字段类型支持 LocalDateTime / LocalDate / Date / Long（毫秒时间戳）。
 * <p>
 * 填充发生在 MybatisRepository 写入时（显式、无 MyBatis-Plus 内部机制参与）。
 *
 * @author Jensen
 */
public final class AuditFiller {

    private static final Map<Class<?>, List<Field>> CREATE_FIELDS = new ConcurrentHashMap<>();
    private static final Map<Class<?>, List<Field>> UPDATE_FIELDS = new ConcurrentHashMap<>();

    private AuditFiller() {
    }

    public static void onInsert(Object model) {
        fill(model, OnCreate.class, CREATE_FIELDS);
        fill(model, OnUpdate.class, UPDATE_FIELDS);
    }

    public static void onUpdate(Object model) {
        fill(model, OnUpdate.class, UPDATE_FIELDS);
    }

    private static void fill(Object model, Class<? extends Annotation> annotation, Map<Class<?>, List<Field>> cache) {
        for (Field field : annotatedFields(model.getClass(), annotation, cache)) {
            try {
                if (field.get(model) != null) {
                    continue;
                }
                field.set(model, now(field.getType()));
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("审计字段填充失败: " + field.getName(), e);
            }
        }
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
