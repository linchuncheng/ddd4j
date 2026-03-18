package com.ddd4j.cloud.excel.enhance;

import com.ddd4j.cloud.excel.annotation.ExportExcel;
import com.ddd4j.cloud.excel.head.HeadGenerator;

import cn.idev.excel.write.builder.ExcelWriterBuilder;
import cn.idev.excel.write.builder.ExcelWriterSheetBuilder;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 默认的ExcelWriterBuilder增强类
 *
 * @author zhouhengzhe
 * @version 1.0
 * @date 2023/6/25 17:04
 */
public class DefaultWriterBuilderEnhancer implements WriterBuilderEnhancer {


    /**
     * ExcelWriterBuilder 增强
     *
     * @param writerBuilder ExcelWriterBuilder
     * @param response      HttpServletResponse
     * @param exportExcel   exportExcel
     * @param templatePath  模板地址
     * @return ExcelWriterBuilder
     */
    @Override
    public ExcelWriterBuilder enhanceExcel(ExcelWriterBuilder writerBuilder, HttpServletResponse response,
                                           ExportExcel exportExcel, String templatePath) {
        // doNothing
        return writerBuilder;
    }

    /**
     * ExcelWriterSheetBuilder 增强
     *
     * @param writerSheetBuilder ExcelWriterSheetBuilder
     * @param sheetNo            sheet角标
     * @param sheetName          sheet名，有模板时为空
     * @param dataClass          当前写入的数据所属类
     * @param template           模板文件
     * @param headEnhancerClass  当前指定的自定义头处理器
     * @return ExcelWriterSheetBuilder
     */
    @Override
    public ExcelWriterSheetBuilder enhanceSheet(ExcelWriterSheetBuilder writerSheetBuilder, Integer sheetNo,
                                                String sheetName, Class<?> dataClass, String template, Class<? extends HeadGenerator> headEnhancerClass) {
        // doNothing
        return writerSheetBuilder;
    }

}
