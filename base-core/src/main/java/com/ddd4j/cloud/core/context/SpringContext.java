package com.ddd4j.cloud.core.context;

import com.ddd4j.cloud.core.contract.constant.ContextConstants;
import com.ddd4j.cloud.core.kit.BizAssert;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Primary;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Spring上下文：显式获取SpringBean、注册Bean；SpringEvent事件发布
 *
 * @author Jensen
 * @公众号 架构师修行录
 */
@Slf4j(topic = "### BASE-CORE : SpringContext ###")
@Primary
@Order(PriorityOrdered.HIGHEST_PRECEDENCE)
public class SpringContext implements ApplicationContextAware, ApplicationRunner {
    // 应用启动完成的信号
    public static final CountDownLatch APP_START_SIGNAL = new CountDownLatch(1);
    // Spring上下文初始化完成的信号
    public static final CountDownLatch APPLICATION_CONTEXT_START_SIGNAL = new CountDownLatch(1);
    private static ApplicationContext APPLICATION_CONTEXT;
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();

    public SpringContext() {
        log.debug("Loading SpringContext");
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTraceElements) {
            if (element.getMethodName().equals("main")) {
                try {
                    String projectPackage = Class.forName(element.getClassName()).getPackage().getName();
                    BaseContext.inject(ContextConstants.PROJECT_PACKAGE, projectPackage);
                    log.debug("PROJECT_PACKAGE: {}", projectPackage);
                } catch (ClassNotFoundException e) {
                    log.error("Cannot find class: {}", element.getClassName());
                }
                break;
            }
        }
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        SpringContext.APPLICATION_CONTEXT = applicationContext;
        APPLICATION_CONTEXT_START_SIGNAL.countDown();
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 通知等待的线程初始化已完成
        APP_START_SIGNAL.countDown();
    }

    public static void onAppStarted(Consumer<ApplicationContext> then) {
        EXECUTOR_SERVICE.submit(() -> {
            try {
                APP_START_SIGNAL.await();
                then.accept(APPLICATION_CONTEXT);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("应用初始化时线程被中断", e);
            } catch (Exception e) {
                log.error("执行应用启动后置逻辑失败", e);
            }
        });
    }

    // 获取ApplicationContext，需等待ApplicationContext初始化完成
    @SneakyThrows
    public static ApplicationContext ctx() {
        return APPLICATION_CONTEXT;
    }

    public static <T> T getBean(@NonNull String name) {
        BizAssert.hasValue(name, "获取的Bean名称不能为空");
        return (T) ctx().getBean(name);
    }

    public static <T> T getBean(@NonNull String name, @NonNull Class<T> clazz) {
        BizAssert.hasValue(name, "获取的Bean名称不能为空");
        return ctx().getBean(name, clazz);
    }

    public static <T> Map<String, T> getBeansOfType(Class<T> clazz) {
        return ctx().getBeansOfType(clazz);
    }

    public static <T> T getBean(@NonNull Class<T> clazz) {
        return ctx().getBean(clazz);
    }

    public static <T> Collection<T> getBeans(@NonNull Class<T> clazz) {
        Map<String, T> beansOfType = ctx().getBeansOfType(clazz);
        if (beansOfType.isEmpty()) return new ArrayList<>();
        return beansOfType.values();
    }

    // 获取aop代理对象
    @SuppressWarnings("unchecked")
    public static <T> T proxy(T invoker) {
        return (T) AopContext.currentProxy();
    }

    public static Environment getEnv() {
        return ctx().getEnvironment();
    }

}