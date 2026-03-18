package com.ddd4j.cloud.excel.annotation;

import java.lang.annotation.*;

/**
 * 表格枚举类注解（配合converter使用）
 *
 * @author zhouhengzhe
 * @date 2025/1/23
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExcelEnum {
    /**
     * 枚举
     *
     * @return 枚举类
     */
    Class<? extends Enum> type();
}
