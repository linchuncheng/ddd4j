package com.ddd4j.cloud.data.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 审计字段：插入时自动填充当前时间
 * <p>
 * 字段类型支持 LocalDateTime / LocalDate / Date / Long（毫秒时间戳）
 *
 * @author Jensen
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OnCreate {
}
