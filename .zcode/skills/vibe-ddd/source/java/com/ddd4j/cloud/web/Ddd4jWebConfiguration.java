package com.ddd4j.cloud.web;

import com.ddd4j.cloud.config.Ddd4jProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 装配：上下文拦截器 / 响应包装 / 全局异常处理。
 * 仅在 Servlet Web 应用中激活，可通过 ddd4j.web.enabled=false 关闭。
 *
 * @author Jensen
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(name = "ddd4j.web.enabled", havingValue = "true", matchIfMissing = true)
public class Ddd4jWebConfiguration implements WebMvcConfigurer {

    private final Ddd4jProperties properties;

    public Ddd4jWebConfiguration(Ddd4jProperties properties) {
        this.properties = properties;
    }

    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    @Bean
    public WebResponseAdvice webResponseAdvice() {
        return new WebResponseAdvice();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        Ddd4jProperties.Web web = properties.getWeb();
        registry.addInterceptor(new ContextInterceptor(web.getUserIdHeader(), web.getTenantIdHeader(), web.getTraceIdHeader()))
                .addPathPatterns("/**");
    }
}
