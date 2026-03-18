package com.ddd4j.cloud.web.interceptor;

import com.ddd4j.cloud.core.kit.JsonKit;
import com.ddd4j.cloud.web.config.BaseWebProperties;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 日志Web拦截器
 *
 * @func 打印请求路径、请求方法
 */
@Order(-600)
public class WebLogInterceptor extends BaseWebInterceptor {
    @Autowired
    BaseWebProperties baseWebProperties;
    final ThreadLocal<LocalDateTime> beginTime = new ThreadLocal<>();

    @Override
    public String[] pathPatterns() {
        return baseWebProperties.getLog().getIncludes().split(",");
    }

    @Override
    public String[] excludePathPatterns() {
        return baseWebProperties.getLog().getExcludes().split(",");
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler.getClass() == ResourceHttpRequestHandler.class) {
            return Boolean.TRUE;
        }
        HandlerMethod method = (HandlerMethod) handler;
        String className = method.getBeanType().getSimpleName();
        String methodName = method.getMethod().getName();
        Logger logger = LoggerFactory.getLogger("==> Request");
        logger.info("{} {} -> {}.{}() request param：{},request header：{}", request.getMethod(), request.getRequestURI(), className, methodName, JsonKit.toJson(request.getParameterMap()), JsonKit.toJson(getAllHeaders(request)));
        beginTime.set(LocalDateTime.now());
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        try {
            Logger logger = LoggerFactory.getLogger(String.format("<== Response in %s ms", Duration.between(beginTime.get(), LocalDateTime.now()).toMillis()));
            logger.info("{} {}", request.getMethod(), request.getRequestURI());
        } catch (Exception ignored) {
        } finally {
            beginTime.remove();
        }

    }

    /**
     * 获取所有请求头
     *
     * @param request 请求
     * @return 请求头信息
     */
    public Map<String, String> getAllHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = headerNames.nextElement();
            String value = request.getHeader(key);
            headers.put(key, value);
        }
        return headers;
    }

}