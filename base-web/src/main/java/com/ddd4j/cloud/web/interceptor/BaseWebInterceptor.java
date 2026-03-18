package com.ddd4j.cloud.web.interceptor;

import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 基础拦截器
 *
 * @func 实现该接口，需要加载到Spring容器
 */
public abstract class BaseWebInterceptor implements HandlerInterceptor {
    // @return 拦截的请求路径
    public String[] pathPatterns() {
        return new String[]{"/**"};
    }

    // @return 不拦截的请求路径
    public String[] excludePathPatterns() {
        return new String[]{"/error"};
    }
}