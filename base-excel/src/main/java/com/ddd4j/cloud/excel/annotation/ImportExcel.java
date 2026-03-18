package com.ddd4j.cloud.excel.annotation;


import com.ddd4j.cloud.excel.handler.listener.DefaultAnalysisEventListener;
import com.ddd4j.cloud.excel.handler.listener.ListAnalysisEventListener;

import java.lang.annotation.*;

/**
 * 导入excel
 *
 * @author zhouhengzhe
 * @version 1.0
 * @date 2023/6/25 16:23
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface ImportExcel {
    // 前端上传字段名称 file
    String fileName() default "file";

    /**
     * 读取的监听器类
     *
     * @return readListener
     */
    Class<? extends ListAnalysisEventListener<?>> readListener() default DefaultAnalysisEventListener.class;

    /**
     * 是否跳过空行
     *
     * @return 默认跳过
     */
    boolean ignoreEmptyRow() default false;

    /**
     * 指定读取的标题行
     *
     * @return 标题行
     */
    int headRowNumber() default 1;
}
