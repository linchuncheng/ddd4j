package com.ddd4j.cloud.excel.annotation;


import com.ddd4j.cloud.excel.head.HeadGenerator;

import java.lang.annotation.*;

/**
 * sheet
 *
 * @author zhouhengzhe
 * @version 1.0
 * @date 2023/6/25 16:26
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Sheet {

    int sheetNo() default -1;

    // sheet name
    String sheetName();

    // 包含字段
    String[] includes() default {};

    // 排除字段
    String[] excludes() default {};

    // 头生成器
    Class<? extends HeadGenerator> headGenerateClass() default HeadGenerator.class;
}
