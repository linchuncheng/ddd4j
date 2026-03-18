package com.ddd4j.cloud.excel;

import cn.idev.excel.converters.Converter;
import com.ddd4j.cloud.excel.aop.ExportExcelReturnValueHandler;
import com.ddd4j.cloud.excel.enhance.DefaultWriterBuilderEnhancer;
import com.ddd4j.cloud.excel.enhance.WriterBuilderEnhancer;
import com.ddd4j.cloud.excel.handler.sheet.ManySheetWriteHandler;
import com.ddd4j.cloud.excel.handler.sheet.SheetWriteHandler;
import com.ddd4j.cloud.excel.handler.sheet.SingleSheetWriteHandler;
import com.ddd4j.cloud.excel.head.I18nHeaderCellWriteHandler;
import com.ddd4j.cloud.excel.properties.ExcelConfigProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * excel处理配置类
 *
 * @author zhouhengzhe
 * @version 1.0
 * @date 2023/6/25 16:29
 */
@RequiredArgsConstructor
public class ExcelHandlerConfiguration {

    private final ExcelConfigProperties configProperties;

    private final ObjectProvider<List<Converter<?>>> converterProvider;

    /**
     * ExcelBuild增强
     *
     * @return DefaultWriterBuilderEnhancer 默认什么也不做的增强器
     */
    @Bean
    @ConditionalOnMissingBean
    public WriterBuilderEnhancer writerBuilderEnhancer() {
        return new DefaultWriterBuilderEnhancer();
    }

    // 单sheet 写入处理器
    @Bean
    @ConditionalOnMissingBean
    public SingleSheetWriteHandler singleSheetWriteHandler(WriterBuilderEnhancer writerBuilderEnhancer) {
        return new SingleSheetWriteHandler(configProperties, converterProvider, writerBuilderEnhancer);
    }

    // 多sheet 写入处理器
    @Bean
    @ConditionalOnMissingBean
    public ManySheetWriteHandler manySheetWriteHandler(WriterBuilderEnhancer writerBuilderEnhancer) {
        return new ManySheetWriteHandler(configProperties, converterProvider, writerBuilderEnhancer);
    }

    /**
     * 返回Excel文件的 response 处理器
     *
     * @param sheetWriteHandlerList 页签写入处理器集合
     * @return ExportExcelReturnValueHandler
     */
    @Bean
    @ConditionalOnMissingBean
    public ExportExcelReturnValueHandler exportExcelReturnValueHandler(
            List<SheetWriteHandler> sheetWriteHandlerList) {
        return new ExportExcelReturnValueHandler(sheetWriteHandlerList);
    }

    /**
     * excel 头的国际化处理器
     *
     * @param messageSource 国际化源
     */
    @Bean
    @ConditionalOnBean(MessageSource.class)
    @ConditionalOnMissingBean
    public I18nHeaderCellWriteHandler i18nHeaderCellWriteHandler(MessageSource messageSource) {
        return new I18nHeaderCellWriteHandler(messageSource);
    }

}
