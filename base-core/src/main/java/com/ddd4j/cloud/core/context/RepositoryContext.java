package com.ddd4j.cloud.core.context;

import com.ddd4j.cloud.core.contract.Model;
import com.ddd4j.cloud.core.contract.Query;
import com.ddd4j.cloud.core.contract.annotation.DAO;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 仓储上下文
 */
@Slf4j
public class RepositoryContext implements ApplicationListener<ApplicationStartedEvent> {
    public static Map<Object, Class<? extends Model>> MODEL_CLASS_MAPPING = new ConcurrentHashMap<>();
    public static Map<Class<?>, Class<Query>> QUERY_CLASS_MAPPING = new ConcurrentHashMap<>();
    public static Map<Class<?>, Class<?>> ENTITY_CLASS_MAPPING = new ConcurrentHashMap<>();
    public static Map<Class<?>, Object> DAO_MAPPING = new ConcurrentHashMap<>();

    public static Class<Model> modelClass(Object any) {
        if (any instanceof Class<?>) {
            return (Class<Model>) MODEL_CLASS_MAPPING.get(any);
        } else if (any instanceof String) {
            return (Class<Model>) MODEL_CLASS_MAPPING.get(any);
        } else {
            return (Class<Model>) MODEL_CLASS_MAPPING.get(any.getClass());
        }
    }

    public static Class<Query> queryClass(Class<?> mappingClass) {
        return QUERY_CLASS_MAPPING.get(mappingClass);
    }

    public static Class<?> entityClass(Class<?> mappingClass) {
        return ENTITY_CLASS_MAPPING.get(mappingClass);
    }

    public static <DAO> DAO dao(Object any) {
        if (any instanceof Class<?>) {
            return (DAO) DAO_MAPPING.get(any);
        } else {
            return (DAO) DAO_MAPPING.get(any.getClass());
        }
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        Reflections reflections = new Reflections(new ConfigurationBuilder().forPackages("").setScanners(new TypeAnnotationsScanner()));
        // 找出所有注解了@DAO 的接口
        Set<Class<?>> daoClasses = reflections.getTypesAnnotatedWith(DAO.class);
        for (Class<?> daoClass : daoClasses) {
            DAO daoAnnotation = daoClass.getDeclaredAnnotation(DAO.class);
            Object dao = SpringContext.getBean(daoClass);
            Class<?> entityClass = daoAnnotation.entity();
            Class<? extends Model> modelClass = daoAnnotation.model();
            Class<? extends Query> queryClass = daoAnnotation.query();
            ENTITY_CLASS_MAPPING.put(queryClass, entityClass);
            ENTITY_CLASS_MAPPING.put(modelClass, entityClass);

            // 首字母设为小写
            String modelName = modelClass.getSimpleName().toLowerCase().charAt(0) + modelClass.getSimpleName().substring(1);
            MODEL_CLASS_MAPPING.put(modelName, modelClass);
            MODEL_CLASS_MAPPING.put(queryClass, modelClass);
            MODEL_CLASS_MAPPING.put(entityClass, modelClass);

            DAO_MAPPING.put(modelClass, dao);
            DAO_MAPPING.put(queryClass, dao);
            DAO_MAPPING.put(entityClass, dao);
        }
    }
}