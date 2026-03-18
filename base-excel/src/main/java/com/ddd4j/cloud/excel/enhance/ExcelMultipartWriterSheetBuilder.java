package com.ddd4j.cloud.excel.enhance;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.idev.excel.ExcelWriter;
import cn.idev.excel.exception.ExcelGenerateException;
import cn.idev.excel.write.builder.AbstractExcelWriterParameterBuilder;
import cn.idev.excel.write.metadata.WriteSheet;
import com.ddd4j.cloud.excel.utils.ExcelKit;

import java.util.List;

/**
 * 可分段写入Excel的ExcelWriterSheetBuilder
 *
 * @author zmj
 */
public class ExcelMultipartWriterSheetBuilder
        extends AbstractExcelWriterParameterBuilder<ExcelMultipartWriterSheetBuilder, WriteSheet> {

    private static final int MAX_ROW_COUNT = 65535;

    private ExcelWriter excelWriter;

    /**
     * Sheet
     */
    private WriteSheet writeSheet;

    /**
     * 行数统计
     */
    private int totalRowCount = 0;

    /**
     * 当前Sheet序号
     */
    private int currentSheetNo = 0;

    /**
     * 最大行数
     */
    private int maxRowCount = MAX_ROW_COUNT;

    public ExcelMultipartWriterSheetBuilder() {

        this.writeSheet = new WriteSheet();
    }

    public ExcelMultipartWriterSheetBuilder(ExcelWriter excelWriter) {

        this.writeSheet = new WriteSheet();
        this.excelWriter = excelWriter;
    }

    /**
     * Starting from 0
     *
     * @param sheetNo
     * @return
     */
    public ExcelMultipartWriterSheetBuilder sheetNo(Integer sheetNo) {

        writeSheet.setSheetNo(sheetNo);
        this.currentSheetNo = sheetNo;
        return this;
    }

    /**
     * sheet name
     *
     * @param sheetName
     * @return
     */
    public ExcelMultipartWriterSheetBuilder sheetName(String sheetName) {

        writeSheet.setSheetName(sheetName);
        return this;
    }

    public WriteSheet build() {

        return writeSheet;
    }

    public int getMaxRowCount() {

        return maxRowCount;
    }

    public void setMaxRowCount(int maxRowCount) {

        this.maxRowCount = maxRowCount;
    }

    public void doWrite(List data) {

        if (excelWriter == null) {
            throw new ExcelGenerateException(
                    "Must use 'EasyExcelFactory.write().sheet()' to call this method");
        }
        if (!CollectionUtil.isEmpty(data)) {
            int remainRow = this.maxRowCount - this.totalRowCount;
            if (remainRow < data.size()) {

                if (remainRow > 0) {
                    this.doWrite(data.subList(0, remainRow));
                    this.nextSheet();
                    this.doWrite(data.subList(remainRow, data.size()));
                } else {
                    this.nextSheet();
                    this.doWrite(data);
                }
            } else {
                //统计行数
                this.totalRowCount += data.size();
                excelWriter.write(data, build());
            }
        } else {
            excelWriter.write(data, build());
        }
    }

    /**
     * 新建Sheet
     */
    public void nextSheet() {
        // 如果超过最大行数，新建Sheet
        this.currentSheetNo++;
        WriteSheet writeSheet = new WriteSheet();
        BeanUtil.copyProperties(this.writeSheet, writeSheet);
        writeSheet.setSheetNo(this.currentSheetNo);
        writeSheet.setSheetName(this.writeSheet.getSheetName() + this.currentSheetNo);
        // 清空CustomWriteHandler
        writeSheet.setCustomWriteHandlerList(ExcelKit.getWriteHandlers());
        this.writeSheet = writeSheet;
        this.totalRowCount = 0;
    }

    public void finish() {

        if (this.excelWriter != null) {
            this.excelWriter.finish();
        }
    }

    @Override
    protected WriteSheet parameter() {

        return writeSheet;
    }
}
