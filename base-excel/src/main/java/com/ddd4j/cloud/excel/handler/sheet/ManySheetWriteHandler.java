package com.ddd4j.cloud.excel.handler.sheet;

import com.ddd4j.cloud.excel.annotation.ExportExcel;
import com.ddd4j.cloud.excel.annotation.Sheet;
import com.ddd4j.cloud.excel.enhance.WriterBuilderEnhancer;
import com.ddd4j.cloud.excel.exception.ExcelException;
import com.ddd4j.cloud.excel.properties.ExcelConfigProperties;
import com.ddd4j.cloud.excel.properties.SheetBuildProperties;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.ObjectProvider;

import cn.idev.excel.ExcelWriter;
import cn.idev.excel.converters.Converter;
import cn.idev.excel.write.metadata.WriteSheet;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 处理多个sheet页面
 *
 * @author zhouhengzhe
 * @version 1.0
 * @date 2023/6/25 17:04
 */
public class ManySheetWriteHandler extends AbstractSheetWriteHandler {

    public ManySheetWriteHandler(ExcelConfigProperties configProperties,
                                 ObjectProvider<List<Converter<?>>> converterProvider, WriterBuilderEnhancer excelWriterBuilderEnhance) {
        super(configProperties, converterProvider, excelWriterBuilderEnhance);
    }

    /**
     * 当且仅当List不为空且List中的元素也是List 才返回true
     *
     * @param obj 返回对象
     * @return boolean
     */
    @Override
    public boolean support(Object obj) {
        if (obj instanceof List) {
            List<?> objList = (List<?>) obj;
            return !objList.isEmpty() && objList.get(0) instanceof List;
        } else {
            throw new ExcelException("@ExportExcel 返回值必须为List类型");
        }
    }

    @Override
    public void write(Object obj, HttpServletResponse response, ExportExcel exportExcel) {
        List<?> objList = (List<?>) obj;
        int objListSize = objList.size();

        String template = exportExcel.template();

        ExcelWriter excelWriter = getExcelWriter(response, exportExcel);
        List<SheetBuildProperties> sheetBuildPropertiesList = getSheetBuildProperties(exportExcel, objListSize);

        for (int i = 0; i < sheetBuildPropertiesList.size(); i++) {
            SheetBuildProperties sheetBuildProperties = sheetBuildPropertiesList.get(i);
            // 创建sheet
            WriteSheet sheet;
            List<?> eleList;
            if (objListSize <= i) {
                eleList = new ArrayList<>();
                sheet = this.emptySheet(sheetBuildProperties, template);
            } else {
                eleList = (List<?>) objList.get(i);
                if (eleList.isEmpty()) {
                    sheet = this.emptySheet(sheetBuildProperties, template);
                } else {
                    Class<?> dataClass = eleList.get(0).getClass();
                    sheet = this.emptySheet(sheetBuildProperties, dataClass, template, exportExcel.headGenerator());
                }
            }

            if (exportExcel.fill()) {
                // 填充 sheet
                excelWriter.fill(eleList, sheet);
            } else {
                // 写入 sheet
                excelWriter.write(eleList, sheet);
            }
        }

        excelWriter.finish();
    }

    private static List<SheetBuildProperties> getSheetBuildProperties(ExportExcel exportExcel, int objListSize) {
        List<SheetBuildProperties> sheetBuildPropertiesList = new ArrayList<>();
        Sheet[] sheets = exportExcel.sheets();
        if (sheets != null && sheets.length > 0) {
            for (Sheet sheet : sheets) {
                sheetBuildPropertiesList.add(new SheetBuildProperties(sheet));
            }
        } else {
            for (int i = 0; i < objListSize; i++) {
                sheetBuildPropertiesList.add(new SheetBuildProperties(i));
            }
        }
        return sheetBuildPropertiesList;
    }

}
