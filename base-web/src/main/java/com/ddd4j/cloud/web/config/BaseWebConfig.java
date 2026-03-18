package com.ddd4j.cloud.web.config;

import com.ddd4j.cloud.core.config.BaseCoreProperties;
import com.ddd4j.cloud.core.context.SpringContext;
import com.ddd4j.cloud.core.kit.JsonKit;
import com.ddd4j.cloud.web.core.GlobalExceptionAdvice;
import com.ddd4j.cloud.web.core.GlobalFeignErrorAdvice;
import com.ddd4j.cloud.web.core.GlobalRequestAdvice;
import com.ddd4j.cloud.web.core.GlobalResponseRAdvice;
import com.ddd4j.cloud.web.interceptor.BaseWebInterceptor;
import com.ddd4j.cloud.web.utils.BaseWebSocketServer;
import com.ddd4j.cloud.web.utils.LocalDateFormatter;
import com.ddd4j.cloud.web.utils.LocalDateTimeFormatter;
import com.ddd4j.cloud.web.utils.LocalTimeFormatter;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j(topic = "### BASE-WEB : BaseWebConfig ###")
@EnableConfigurationProperties(BaseWebProperties.class)
@RequiredArgsConstructor
public class BaseWebConfig implements WebMvcConfigurer {
    final List<BaseWebInterceptor> baseWebInterceptors;
    final BaseCoreProperties baseCoreProperties;
    final List<BaseWebSocketServer> baseWebSocketServers;

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addFormatter(new DateFormatter(baseCoreProperties.getDateTimePattern()));
        registry.addFormatter(new LocalDateTimeFormatter(baseCoreProperties.getDateTimePattern()));
        registry.addFormatter(new LocalTimeFormatter(baseCoreProperties.getTimePattern()));
        registry.addFormatter(new LocalDateFormatter(baseCoreProperties.getDatePattern()));
    }

    @Bean
    public ObjectMapper mvcObjectMapper() {
        log.debug("Loading mvcObjectMapper");
        return JsonKit.buildObjectMapper(baseCoreProperties.getDatePattern(), baseCoreProperties.getDateTimePattern(), baseCoreProperties.getTimePattern());
    }

    @Bean
    @ConditionalOnMissingBean
    public MappingJackson2HttpMessageConverter jackson2HttpMessageConverter() {
        log.debug("Loading jackson2HttpMessageConverter");
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setDefaultCharset(Charset.defaultCharset());
        converter.setObjectMapper(mvcObjectMapper());
        return converter;
    }

    @Bean
    @ConditionalOnMissingBean
    public SpringContext springContext() {
        return new SpringContext();
    }

    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionAdvice globalRestExceptionAdvice() {
        log.debug("Loading globalRestExceptionAdvice");
        return new GlobalExceptionAdvice();
    }

    @Bean
    @ConditionalOnMissingBean
    public GlobalRequestAdvice globalRequestAdvice() {
        log.debug("Loading globalRequestAdvice");
        return new GlobalRequestAdvice();
    }

    @Bean
    @ConditionalOnMissingBean
    public GlobalResponseRAdvice globalResponseRAdvice() {
        log.debug("Loading globalResponseRAdvice");
        return new GlobalResponseRAdvice();
    }

    @Bean
    @ConditionalOnMissingBean
    public GlobalFeignErrorAdvice globalFeignErrorAdvice() {
        log.debug("Loading globalFeignErrorAdvice");
        return new GlobalFeignErrorAdvice();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (baseWebInterceptors != null && !baseWebInterceptors.isEmpty()) {
            baseWebInterceptors.forEach(baseInterceptor -> {
                log.debug("Loading {}", baseInterceptor.getClass().getSimpleName());
                registry.addInterceptor(baseInterceptor).addPathPatterns(baseInterceptor.pathPatterns()).excludePathPatterns(baseInterceptor.excludePathPatterns());
            });
        } else {
            log.warn("baseWebInterceptors is empty!");
        }
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //配置拦截器访问静态资源
        registry.addResourceHandler("doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");
    }

    @Bean
    public WebSocketClient webSocketClient() {
        return new StandardWebSocketClient();
    }

    @PostConstruct
    public void init() {
        // 启动WebSocket服务端
        if (!baseWebSocketServers.isEmpty()) {
            for (BaseWebSocketServer server : baseWebSocketServers) {
//                WebSocketKit.startServer(server.getClass());
            }
        }
    }

    /**
     * 跨域请求
     *
     * @return CorsFilter 跨域配置
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Collections.singletonList("*"));
        configuration.setAllowedMethods(Collections.singletonList("*"));
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return new CorsFilter(source);
    }

}