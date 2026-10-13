package com.ddd4j.cloud.web;

import com.ddd4j.cloud.context.AppContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

/**
 * 上下文拦截器：从请求头解析用户 / 租户 / 链路ID写入 AppContext 与 MDC，
 * 并把 traceId 回写到响应头；请求结束后清理，避免线程复用串数据。
 * <p>
 * 日志输出建议在 pattern 中加入 %X{traceId}。
 *
 * @author Jensen
 */
public class ContextInterceptor implements HandlerInterceptor {

    private final String userIdHeader;
    private final String tenantIdHeader;
    private final String traceIdHeader;

    public ContextInterceptor(String userIdHeader, String tenantIdHeader, String traceIdHeader) {
        this.userIdHeader = userIdHeader;
        this.tenantIdHeader = tenantIdHeader;
        this.traceIdHeader = traceIdHeader;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        AppContext context = AppContext.current();
        String userId = normalize(request.getHeader(userIdHeader));
        if (userId != null) {
            context.setUserId(userId);
        }
        String tenantId = normalize(request.getHeader(tenantIdHeader));
        if (tenantId != null) {
            context.setTenantId(tenantId);
        }
        String traceId = request.getHeader(traceIdHeader);
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }
        context.setTraceId(traceId);
        MDC.put("traceId", traceId);
        response.setHeader(traceIdHeader, traceId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AppContext.clear();
        MDC.clear();
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
