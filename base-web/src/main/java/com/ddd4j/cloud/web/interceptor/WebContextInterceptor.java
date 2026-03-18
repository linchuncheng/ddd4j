package com.ddd4j.cloud.web.interceptor;

import com.ddd4j.cloud.core.context.ThreadContext;
import com.ddd4j.cloud.core.contract.constant.ContextConstants;
import com.ddd4j.cloud.web.utils.RequestContext;

import java.util.Locale;

import org.springframework.core.annotation.Order;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 上下文Web拦截器
 *
 * @func 获取参数请求头，设到线程上下文供后续使用
 */
@Slf4j
@Order(-400)
public class WebContextInterceptor extends BaseWebInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        ThreadContext.set(ContextConstants.REQUEST_PARAMS, RequestContext.getParams());
        ThreadContext.set(ContextConstants.TENANT_ID, request.getHeader(ContextConstants.TENANT_ID));
        ThreadContext.set(ContextConstants.APP_ID, request.getHeader(ContextConstants.APP_ID));
        ThreadContext.set(ContextConstants.ROLE, request.getHeader(ContextConstants.ROLE));
        ThreadContext.set(ContextConstants.CLIENT_TYPE, request.getHeader(ContextConstants.CLIENT_TYPE));
        ThreadContext.set(ContextConstants.AUTHORIZATION, request.getHeader(ContextConstants.AUTHORIZATION));
        ThreadContext.set(ContextConstants.ACCEPT_LANGUAGE, request.getHeader(ContextConstants.ACCEPT_LANGUAGE));
        String acceptLanguage = request.getHeader(ContextConstants.ACCEPT_LANGUAGE);
        // 解析第一个语言选项（如 "en-US,en;q=0.9" -> "en-US"）
        if (acceptLanguage != null && !acceptLanguage.isEmpty()) {
            ThreadContext.set(ContextConstants.LOCALE, Locale.forLanguageTag(acceptLanguage.split(",")[0].replace('-', '_')));
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        ThreadContext.clear();
    }

}