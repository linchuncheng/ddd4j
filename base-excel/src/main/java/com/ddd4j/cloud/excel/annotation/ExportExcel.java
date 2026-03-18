package com.ddd4j.cloud.excel.annotation;


import cn.idev.excel.converters.Converter;
import cn.idev.excel.support.ExcelTypeEnum;
import cn.idev.excel.write.handler.WriteHandler;
import cn.idev.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.ddd4j.cloud.excel.handler.style.DefaultHorizontalCellStyleStrategy;
import com.ddd4j.cloud.excel.head.HeadGenerator;

import java.lang.annotation.*;

/**
 * 导出excel
 *
 * @author zhouhengzhe
 * @version 1.0
 * @date 2023/6/25 16:25
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExportExcel {

    /**
     * 文件名称
     *
     * @return string
     */
    String name() default "";

    /**
     * 文件类型 （xlsx xls）
     *
     * @return string
     */
    ExcelTypeEnum suffix() default ExcelTypeEnum.XLSX;

    /**
     * 文件密码
     *
     * @return password
     */
    String password() default "";

    /**
     * sheet 名称，支持多个
     *
     * @return String[]
     */
    Sheet[] sheets() default {};

    /**
     * 内存操作
     *
     * @return 是/否
     */
    boolean inMemory() default false;

    /**
     * excel 模板
     *
     * @return String
     */
    String template() default "";

    /**
     * + 包含字段
     *
     * @return String[]
     */
    String[] include() default {};

    /**
     * 排除字段
     *
     * @return String[]
     */
    String[] exclude() default {};

    /**
     * 拦截器，自定义样式等处理器
     *
     * @return WriteHandler[]
     */
    Class<? extends WriteHandler>[] writeHandler() default {DefaultHorizontalCellStyleStrategy.class, LongestMatchColumnWidthStyleStrategy.class};

    /**
     * 转换器
     *
     * @return Converter[]
     */
    Class<? extends Converter>[] converter() default {};

    /**
     * 自定义Excel头生成器
     *
     * @return HeadGenerator
     */
    Class<? extends HeadGenerator> headGenerator() default HeadGenerator.class;

    /**
     * excel 头信息国际化
     *
     * @return boolean
     */
    boolean i18nHeader() default false;

    /**
     * 填充模式
     *
     * @return 是/否
     */
    boolean fill() default false;
}
