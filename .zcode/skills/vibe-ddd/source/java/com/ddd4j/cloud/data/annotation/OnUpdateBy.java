package com.ddd4j.cloud.data.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 审计字段：插入和更新时自动填充当前用户（{@code AppContext.userId()}）
 * <p>
 * 字段类型为 String；上下文无用户（系统任务）时保持原值不填充
 *
 * @author Jensen
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OnUpdateBy {
}
