package com.ddd4j.cloud.excel.handler.style;


import cn.idev.excel.write.metadata.style.WriteCellStyle;
import cn.idev.excel.write.metadata.style.WriteFont;
import cn.idev.excel.write.style.HorizontalCellStyleStrategy;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;

/**
 * 默认的表头样式
 *
 * @author zhouhengzhe
 * @version 1.0
 * @date 2024/3/18 11:24
 */
public class DefaultHorizontalCellStyleStrategy extends HorizontalCellStyleStrategy {
    public DefaultHorizontalCellStyleStrategy() {
        super();
        // 头的策略  样式调整
        WriteCellStyle headWriteCellStyle = new WriteCellStyle();
        // 头背景 金黄色
        headWriteCellStyle.setFillForegroundColor(IndexedColors.GOLD.getIndex());
        WriteFont headWriteFont = new WriteFont();
        // 头字号 10
        headWriteFont.setFontHeightInPoints((short) 10);
        // 字体样式 微软雅黑
        headWriteFont.setFontName("微软雅黑");
        headWriteCellStyle.setWriteFont(headWriteFont);
        // 自动换行
        headWriteCellStyle.setWrapped(false);
        // 设置细边框
        headWriteCellStyle.setBorderBottom(BorderStyle.THIN);
        headWriteCellStyle.setBorderLeft(BorderStyle.THIN);
        headWriteCellStyle.setBorderRight(BorderStyle.THIN);
        headWriteCellStyle.setBorderTop(BorderStyle.THIN);
        // 设置边框颜色 25灰度
        headWriteCellStyle.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headWriteCellStyle.setTopBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headWriteCellStyle.setLeftBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headWriteCellStyle.setRightBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        // 水平对齐方式
        headWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        // 垂直对齐方式
        headWriteCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        // 内容的策略 微软雅黑 10号 粗体
        WriteCellStyle contentStyle = new WriteCellStyle();
        // 设置垂直居中
        contentStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        // 设置 水平居中
//        contentStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        WriteFont contentWriteFont = new WriteFont();
        // 内容字号
        contentWriteFont.setFontHeightInPoints((short) 10);
        // 字体样式
        contentWriteFont.setFontName("微软雅黑");
        contentStyle.setWriteFont(contentWriteFont);
        // 这个策略是 头是头的样式 内容是内容的样式 其他的策略可以自己实现
        setHeadWriteCellStyle(headWriteCellStyle);
    }
}
