package com.ddd4j.cloud.excel.handler.sheet;

import com.ddd4j.cloud.excel.annotation.ExportExcel;

import jakarta.servlet.http.HttpServletResponse;

/**
 * sheet 写出处理器
 *
 * @author zhouhengzhe
 * @version 1.0
 * @date 2023/6/25 17:04
 */
public interface SheetWriteHandler {

    /**
     * 是否支持
     *
     * @param obj
     * @return
     */
    boolean support(Object obj);

    /**
     * 校验
     *
     * @param exportExcel 注解
     */
    void check(ExportExcel exportExcel);

    /**
     * 返回的对象
     *
     * @param o           obj
     * @param response    输出对象
     * @param exportExcel 注解
     */
    void export(Object o, HttpServletResponse response, ExportExcel exportExcel);

    /**
     * 写成对象
     *
     * @param o           obj
     * @param response    输出对象
     * @param exportExcel 注解
     */
    void write(Object o, HttpServletResponse response, ExportExcel exportExcel);

}
