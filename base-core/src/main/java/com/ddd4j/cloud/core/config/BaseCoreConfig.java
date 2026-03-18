package com.ddd4j.cloud.core.config;

import com.ddd4j.cloud.core.kit.Operator;
import com.ddd4j.cloud.core.kit.OperatorRouter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy(exposeProxy = true)
public class BaseCoreConfig {

    @Autowired(required = false)
    public void initOperatorRouter(Map<String, OperatorRouter> routerMap, ApplicationContext applicationContext) {
        if (routerMap != null && !routerMap.isEmpty()) {
            routerMap.values().forEach(router -> {
                Class<Operator> operatorClass = router.getOperatorClass();
                Map<String, Operator> beans = applicationContext.getBeansOfType(operatorClass);

                Map<Object, Operator> tmpMap = new HashMap<>(beans.size());

                beans.forEach((beanName, operator) -> {
                    Class<? extends Operator> aClass = operator.getClass();
                    if (!aClass.isInterface()) {
                        router.checkOperator(operator);
                        tmpMap.put(operator.getName(), operator);
                    }
                });

                router.setOperatorMap(Collections.unmodifiableMap(tmpMap));
            });

        }
    }
}