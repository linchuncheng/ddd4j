package com.ddd4j.cloud.context;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Spring 上下文静态桥：供脱离依赖注入场景的便捷访问（如领域事件 publish()）。
 * <p>
 * 由 ddd4j 自动装配注册；仅在容器启动后可用，启动前调用会抛出 IllegalStateException。
 * 业务代码优先使用构造注入，此工具留给框架便捷方法与少数无法注入的场景。
 *
 * @author Jensen
 */
public final class SpringContext implements ApplicationContextAware {

    private static volatile ApplicationContext context;

    public SpringContext() {
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContext.context = applicationContext;
    }

    public static ApplicationContext ctx() {
        ApplicationContext current = context;
        if (current == null) {
            throw new IllegalStateException("Spring 容器尚未就绪，SpringContext 不可用");
        }
        return current;
    }

    public static <T> T getBean(Class<T> type) {
        return ctx().getBean(type);
    }

    public static <T> T getBean(String name, Class<T> type) {
        return ctx().getBean(name, type);
    }

    public static Object getBean(String name) {
        return ctx().getBean(name);
    }

    public static void publishEvent(Object event) {
        ctx().publishEvent(event);
    }
}
