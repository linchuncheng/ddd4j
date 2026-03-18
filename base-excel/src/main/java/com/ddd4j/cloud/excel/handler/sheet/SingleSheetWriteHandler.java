package com.ddd4j.cloud.excel.handler.sheet;

import com.ddd4j.cloud.excel.annotation.ExportExcel;
import com.ddd4j.cloud.excel.annotation.Sheet;
import com.ddd4j.cloud.excel.enhance.WriterBuilderEnhancer;
import com.ddd4j.cloud.excel.exception.ExcelException;
import com.ddd4j.cloud.excel.properties.ExcelConfigProperties;
import com.ddd4j.cloud.excel.properties.SheetBuildProperties;

import java.util.List;

import org.springframework.beans.factory.ObjectProvider;

import cn.idev.excel.ExcelWriter;
import cn.idev.excel.converters.Converter;
import cn.idev.excel.write.metadata.WriteSheet;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 处理单sheet 页面
 *
 * @author zhouhengzhe
 * @version 1.0
 * @date 2023/6/25 17:04
 */
public class SingleSheetWriteHandler extends AbstractSheetWriteHandler {

    public SingleSheetWriteHandler(ExcelConfigProperties configProperties,
                                   ObjectProvider<List<Converter<?>>> converterProvider, WriterBuilderEnhancer excelWriterBuilderEnhance) {
        super(configProperties, converterProvider, excelWriterBuilderEnhance);
    }

    /**
     * obj 是List 且list不为空同时list中的元素不是是List 才返回true
     *
     * @param obj 返回对象
     * @return boolean
     */
    @Override
    public boolean support(Object obj) {
        if (obj instanceof List) {
            List<?> objList = (List<?>) obj;
            return !objList.isEmpty() && !(objList.get(0) instanceof List);
        } else {
            throw new ExcelException("@ExportExcel 返回值必须为List类型");
        }
    }

    @Override
    public void write(Object obj, HttpServletResponse response, ExportExcel exportExcel) {
        List<?> eleList = (List<?>) obj;
        ExcelWriter excelWriter = getExcelWriter(response, exportExcel);

        // 获取 Sheet 配置
        SheetBuildProperties sheetBuildProperties;
        Sheet[] sheets = exportExcel.sheets();
        if (sheets != null && sheets.length > 0) {
            sheetBuildProperties = new SheetBuildProperties(sheets[0]);
        } else {
            sheetBuildProperties = new SheetBuildProperties(0);
        }

        // 模板信息
        String template = exportExcel.template();

        // 创建sheet
        WriteSheet sheet;
        if (eleList.isEmpty()) {
            sheet = this.emptySheet(sheetBuildProperties, template);
        } else {
            Class<?> dataClass = eleList.get(0).getClass();
            sheet = this.emptySheet(sheetBuildProperties, dataClass, template, exportExcel.headGenerator());
        }

        if (exportExcel.fill()) {
            // 填充 sheet
            excelWriter.fill(eleList, sheet);
        } else {
            // 写入 sheet
            excelWriter.write(eleList, sheet);
        }

        excelWriter.finish();
    }
}
