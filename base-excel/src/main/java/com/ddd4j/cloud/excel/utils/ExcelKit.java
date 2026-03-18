package com.ddd4j.cloud.excel.utils;

import com.ddd4j.cloud.excel.enhance.ExcelMultipartWriterBuilder;
import com.ddd4j.cloud.excel.enhance.ExcelMultipartWriterSheetBuilder;
import com.ddd4j.cloud.excel.exception.ExcelException;
import com.ddd4j.cloud.excel.vo.ExcelModel;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.springframework.web.multipart.MultipartFile;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.idev.excel.EasyExcel;
import cn.idev.excel.read.builder.ExcelReaderBuilder;
import cn.idev.excel.read.listener.ReadListener;
import cn.idev.excel.support.ExcelTypeEnum;
import cn.idev.excel.write.handler.WriteHandler;
import cn.idev.excel.write.metadata.style.WriteCellStyle;
import cn.idev.excel.write.metadata.style.WriteFont;
import cn.idev.excel.write.style.column.AbstractColumnWidthStyleStrategy;
import cn.idev.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Excel工具类
 *
 * @author zhouhengzhe
 */
@Slf4j
public class ExcelKit {
    // 默认列宽策略
    private static final WriteHandler DEFAULT_COLUMN_WIDTH_STYLE_STRATEGY = new LongestMatchColumnWidthStyleStrategy();

    /**
     * 读取Excel
     *
     * @param file
     * @param listener
     */
    public static <T> ExcelReaderBuilder read(MultipartFile file, Class<T> clazz,
                                              ReadListener<T> listener) {
        try {
            return EasyExcel.read(file.getInputStream(), clazz, listener);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new ExcelException(e.getMessage());
        }
    }

    /**
     * 导出Xls至Response
     *
     * @param sheetName
     * @param clazz
     * @param <T>
     */
    public static <T extends ExcelModel> void exportXls(String sheetName, Class<T> clazz) {

        exportXls(sheetName, sheetName, clazz, Collections.emptyList(), null);
    }

    /**
     * 导出Xls至Response
     *
     * @param sheetName
     * @param clazz
     * @param datas
     * @param <T>
     */
    public static <T extends ExcelModel> void exportXls(String sheetName, Class<T> clazz,
                                                        List<T> datas) {

        exportXls(sheetName, sheetName, clazz, datas, null);
    }

    /**
     * 导出Xls至Response
     *
     * @param fileName
     * @param sheetName
     * @param clazz
     * @param <T>
     */
    public static <T extends ExcelModel> void exportXls(String fileName, String sheetName,
                                                        Class<T> clazz) {

        exportXls(fileName, sheetName, clazz, Collections.emptyList(), null);
    }

    /**
     * 导出Xls至Response
     *
     * @param fileName
     * @param sheetName
     * @param clazz
     * @param datas
     * @param <T>
     */
    public static <T extends ExcelModel> void exportXls(String fileName, String sheetName,
                                                        Class<T> clazz,
                                                        List<T> datas) {

        exportXls(fileName, sheetName, clazz, datas, null);
    }

    /**
     * 导出Xls至Response
     *
     * @param fileName
     * @param sheetName
     * @param clazz
     * @param datas
     * @param writeHandlers
     * @param <T>
     */
    public static <T extends ExcelModel> void exportXls(String fileName, String sheetName,
                                                        Class<T> clazz,
                                                        List<T> datas, List<WriteHandler> writeHandlers) {

        HttpServletResponse response = ResponseKit.getResponse();
        try (OutputStream os = response.getOutputStream()) {
            fileName = URLEncoder.encode(fileName + ExcelTypeEnum.XLS.getValue(),
                    StandardCharsets.UTF_8.name());
            response.setContentType("application/msexcel");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setHeader("FileName", fileName);
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);

            exportExcel(os, sheetName, ExcelTypeEnum.XLS, clazz, datas, writeHandlers);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new ExcelException("Xls导出异常");
        }
    }

    /**
     * 导出Excel
     *
     * @param os
     * @param sheetName
     * @param excelType
     * @param clazz
     * @param datas
     * @param writeHandlers
     * @param <T>
     */
    private static <T extends ExcelModel> void exportExcel(OutputStream os, String sheetName,
                                                           ExcelTypeEnum excelType,
                                                           Class<T> clazz, List<T> datas, List<WriteHandler> writeHandlers) {

        ExcelMultipartWriterSheetBuilder builder = new ExcelMultipartWriterBuilder().file(os)
                .excelType(excelType)
                .useDefaultStyle(false).head(clazz).sheet(sheetName);
        writeHandlers = getWriteHandlers(writeHandlers, clazz);

        writeHandlers.forEach(builder::registerWriteHandler);

        builder.doWrite(datas);
        builder.finish();
    }

    /**
     * 导出Xlsx至Response
     *
     * @param sheetName
     * @param clazz
     * @param <T>
     */
    public static <T extends ExcelModel> void exportXlsx(String sheetName, Class<T> clazz) {

        exportXlsx(sheetName, sheetName, clazz, Collections.emptyList(), null);
    }

    /**
     * 导出Xlsx至Response
     *
     * @param sheetName
     * @param clazz
     * @param datas
     * @param <T>
     */
    public static <T extends ExcelModel> void exportXlsx(String sheetName, Class<T> clazz,
                                                         List<T> datas) {

        exportXlsx(sheetName, sheetName, clazz, datas, null);
    }

    /**
     * 导出Xlsx至Response
     *
     * @param fileName
     * @param sheetName
     * @param clazz
     * @param <T>
     */
    public static <T extends ExcelModel> void exportXlsx(String fileName, String sheetName,
                                                         Class<T> clazz) {

        exportXlsx(fileName, sheetName, clazz, Collections.emptyList(), null);
    }

    /**
     * 导出Xlsx至Response
     *
     * @param fileName
     * @param sheetName
     * @param clazz
     * @param datas
     * @param <T>
     */
    public static <T extends ExcelModel> void exportXlsx(String fileName, String sheetName,
                                                         Class<T> clazz,
                                                         List<T> datas) {

        exportXlsx(fileName, sheetName, clazz, datas, null);
    }

    /**
     * 导出Xlsx至Response
     *
     * @param fileName
     * @param sheetName
     * @param clazz
     * @param datas
     * @param writeHandlers
     * @param <T>
     */
    public static <T extends ExcelModel> void exportXlsx(String fileName, String sheetName,
                                                         Class<T> clazz,
                                                         List<T> datas, List<WriteHandler> writeHandlers) {

        HttpServletResponse response = ResponseKit.getResponse();
        try (OutputStream os = response.getOutputStream()) {
            fileName = URLEncoder.encode(fileName + ExcelTypeEnum.XLSX.getValue(),
                    StandardCharsets.UTF_8.name());
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setHeader("FileName", fileName);
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);

            exportExcel(os, sheetName, ExcelTypeEnum.XLSX, clazz, datas, writeHandlers);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new ExcelException("Xls导出异常");
        }
    }

    /**
     * 导出Xls至文件
     *
     * @param sheetName
     * @param clazz
     * @param <T>
     */
    public static <T extends ExcelModel> void exportXls(File file, String sheetName, Class<T> clazz) {

        exportXls(file, sheetName, clazz, Collections.emptyList(), null);
    }

    /**
     * 导出Xls至文件
     *
     * @param sheetName
     * @param clazz
     * @param datas
     * @param <T>
     */
    public static <T extends ExcelModel> void exportXls(File file, String sheetName, Class<T> clazz,
                                                        List<T> datas) {

        exportXls(file, sheetName, clazz, datas, null);
    }

    /**
     * 导出Xls至文件
     *
     * @param sheetName
     * @param clazz
     * @param datas
     * @param writeHandlers
     * @param <T>
     */
    public static <T extends ExcelModel> void exportXls(File file, String sheetName, Class<T> clazz,
                                                        List<T> datas,
                                                        List<WriteHandler> writeHandlers) {

        exportExcel(FileUtil.getOutputStream(file), sheetName, ExcelTypeEnum.XLS, clazz, datas,
                writeHandlers);
    }

    /**
     * 导出Xlsx至文件
     *
     * @param sheetName
     * @param clazz
     * @param <T>
     */
    public static <T extends ExcelModel> void exportXlsx(File file, String sheetName,
                                                         Class<T> clazz) {

        exportXlsx(file, sheetName, clazz, Collections.emptyList(), null);
    }

    /**
     * 导出Xlsx至文件
     *
     * @param sheetName
     * @param clazz
     * @param datas
     * @param <T>
     */
    public static <T extends ExcelModel> void exportXlsx(File file, String sheetName, Class<T> clazz,
                                                         List<T> datas) {

        exportXlsx(file, sheetName, clazz, datas, null);
    }

    /**
     * 导出Xlsx至文件
     *
     * @param sheetName
     * @param clazz
     * @param datas
     * @param writeHandlers
     * @param <T>
     */
    public static <T extends ExcelModel> void exportXlsx(File file, String sheetName, Class<T> clazz,
                                                         List<T> datas,
                                                         List<WriteHandler> writeHandlers) {

        exportExcel(FileUtil.getOutputStream(file), sheetName, ExcelTypeEnum.XLSX, clazz, datas,
                writeHandlers);
    }

    /**
     * 获取WriteHandler
     *
     * @return
     */
    public static List<WriteHandler> getWriteHandlers() {

        return getWriteHandlers(null, null);
    }

    /**
     * 获取WriteHandler 如果不存在列宽策略则指定默认列宽策略
     *
     * @param writeHandlers
     * @return
     */
    public static List<WriteHandler> getWriteHandlers(List<WriteHandler> writeHandlers,
                                                      Class headClass) {

        List<WriteHandler> retList = new ArrayList<>();

        if (CollectionUtil.isEmpty(writeHandlers)) {
            retList.add(DEFAULT_COLUMN_WIDTH_STYLE_STRATEGY);

            return retList;
        }

        retList.addAll(writeHandlers);

        if (writeHandlers.stream().anyMatch(t -> t instanceof AbstractColumnWidthStyleStrategy)) {

            return retList;
        }

        retList.add(DEFAULT_COLUMN_WIDTH_STYLE_STRATEGY);

        return retList;
    }

    private static WriteCellStyle getHeadStyle(boolean isRequiredField) {
        WriteCellStyle headWriteCellStyle = new WriteCellStyle();
        headWriteCellStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        headWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.LEFT);
        headWriteCellStyle.setBorderTop(BorderStyle.THIN);
        headWriteCellStyle.setBorderBottom(BorderStyle.THIN);
        headWriteCellStyle.setBorderLeft(BorderStyle.THIN);
        headWriteCellStyle.setBorderRight(BorderStyle.THIN);
        headWriteCellStyle.setTopBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headWriteCellStyle.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headWriteCellStyle.setLeftBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headWriteCellStyle.setRightBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());

        WriteFont headWriteFont = new WriteFont();
        headWriteFont.setFontName("宋体");
        headWriteFont.setFontHeightInPoints((short) 11);
        headWriteFont.setBold(true);
        if (isRequiredField) {
            headWriteFont.setColor(Font.COLOR_RED);
        }
        headWriteCellStyle.setWriteFont(headWriteFont);

        return headWriteCellStyle;
    }
}
