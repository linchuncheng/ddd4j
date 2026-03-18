package com.ddd4j.cloud.data.kit;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Slf4j
@UtilityClass
public class ReflectKit extends ReflectionUtils {
    public static Reflections REFLECTIONS;
    public static String[] SCAN_PACKAGES;

    public Reflections build(String... scanPackages) {
        SCAN_PACKAGES = scanPackages;
        Collection<URL> urls = new ArrayList<>();
        for (String basePackage : scanPackages) {
            urls.addAll(ClasspathHelper.forPackage(basePackage));
        }
        // 初始化 Reflections
        REFLECTIONS = new Reflections(new ConfigurationBuilder().setUrls(urls).setScanners(new TypeAnnotationsScanner(), new MethodAnnotationsScanner()));
        return REFLECTIONS;
    }

    // 扫描指定类型注解
    public List<Method> scanMethods(Class<? extends Annotation>... annotations) {
        List<Method> annotatedMethods = new ArrayList<>();
        for (Class<? extends Annotation> annotation : annotations) {
            // 获取所有标记了 annotation 注解的工程类
            Set<Method> method = ReflectKit.REFLECTIONS.getMethodsAnnotatedWith(annotation);
            annotatedMethods.addAll(method);
            log.info("Found {} @{} methods in: {}", method.size(), annotation.getSimpleName(), ReflectKit.SCAN_PACKAGES);
        }
        return annotatedMethods;
    }

    public <T> Class<T> getSuperClassGenericType(final Class<?> clazz, final int index) {
        Type genType = clazz.getGenericSuperclass();
        if (!(genType instanceof ParameterizedType)) {
            throw new RuntimeException(String.format("Warn: %s's superclass not ParameterizedType", clazz.getSimpleName()));
        } else {
            Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
            if (index < params.length && index >= 0) {
                if (!(params[index] instanceof Class)) {
                    log.warn(String.format("Warn: %s not set the actual class on superclass generic parameter", clazz.getSimpleName()));
                    throw new RuntimeException(String.format("Warn: %s's superclass not ParameterizedType", clazz.getSimpleName()));
                } else {
                    return (Class<T>) params[index];
                }
            } else {
                log.warn(String.format("Warn: Index: %s, Size of %s's Parameterized Type: %s .", index, clazz.getSimpleName(), params.length));
                throw new RuntimeException(String.format("Warn: %s's superclass not ParameterizedType", clazz.getSimpleName()));
            }
        }
    }

    // 判断一个对象的所有字段是否为null
    public static boolean allFieldsNull(@NonNull Object obj) {
        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();
        try {
            for (Field field : fields) {
                // 跳过 static 字段
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                field.setAccessible(true); // 确保可以访问私有字段
                Object value = field.get(obj);
                if (value != null) {
                    return false; // 如果某个字段不为 null，则直接返回 false
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Error accessing fields", e);
        }

        return true;
    }
}