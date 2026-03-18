package com.ddd4j.cloud.excel;

import com.ddd4j.cloud.excel.aop.DynamicNameAspect;
import com.ddd4j.cloud.excel.aop.ExportExcelReturnValueHandler;
import com.ddd4j.cloud.excel.aop.ImportExcelArgumentResolver;
import com.ddd4j.cloud.excel.processor.NameProcessor;
import com.ddd4j.cloud.excel.processor.NameSpelExpressionProcessor;
import com.ddd4j.cloud.excel.properties.ExcelConfigProperties;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

/**
 * 导出excel，通过mica-auto 自动生成springboot的META-INF目录下的spring.factories
 *
 * @author zhouhengzhe
 * @version 1.0
 * @date 2023/6/25 16:30
 */
@Configuration
@RequiredArgsConstructor
@Import(ExcelHandlerConfiguration.class)
@EnableConfigurationProperties(ExcelConfigProperties.class)
public class ExportExcelAutoConfiguration {

    private final RequestMappingHandlerAdapter requestMappingHandlerAdapter;

    private final ExportExcelReturnValueHandler exportExcelReturnValueHandler;


    /**
     * SPEL 解析处理器
     *
     * @return NameProcessor excel名称解析器
     */
    @Bean
    @ConditionalOnMissingBean
    public NameProcessor nameProcessor() {
        return new NameSpelExpressionProcessor();
    }

    /**
     * Excel名称解析处理切面
     *
     * @param nameProcessor SPEL 解析处理器
     * @return DynamicNameAspect
     */
    @Bean
    @ConditionalOnMissingBean
    public DynamicNameAspect dynamicNameAspect(NameProcessor nameProcessor) {
        return new DynamicNameAspect(nameProcessor);
    }

    // 追加 Excel返回值处理器 到 springmvc 中
    @PostConstruct
    public void setReturnValueHandlers() {
        List<HandlerMethodReturnValueHandler> returnValueHandlers = requestMappingHandlerAdapter
                .getReturnValueHandlers();

        List<HandlerMethodReturnValueHandler> newHandlers = new ArrayList<>();
        newHandlers.add(exportExcelReturnValueHandler);
        assert returnValueHandlers != null;
        newHandlers.addAll(returnValueHandlers);
        requestMappingHandlerAdapter.setReturnValueHandlers(newHandlers);
    }

    // 追加 Excel 请求处理器 到 springmvc 中
    @PostConstruct
    public void setRequestExcelArgumentResolver() {
        List<HandlerMethodArgumentResolver> argumentResolvers = requestMappingHandlerAdapter.getArgumentResolvers();
        List<HandlerMethodArgumentResolver> resolverList = new ArrayList<>();
        resolverList.add(new ImportExcelArgumentResolver());
        resolverList.addAll(argumentResolvers);
        requestMappingHandlerAdapter.setArgumentResolvers(resolverList);
    }

}
