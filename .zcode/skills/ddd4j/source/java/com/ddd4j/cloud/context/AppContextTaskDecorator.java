package com.ddd4j.cloud.context;

import org.springframework.core.task.TaskDecorator;

/**
 * 异步任务上下文装饰器：提交任务时捕获 {@link AppContext} 快照，
 * 执行时恢复、结束后清理。装配到 Spring 默认任务执行器后，
 * {@code @Async} 方法（含异步事件监听器）可直接读到用户/租户/traceId。
 *
 * @author Jensen
 */
public class AppContextTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        return AppContext.wrap(runnable);
    }
}
