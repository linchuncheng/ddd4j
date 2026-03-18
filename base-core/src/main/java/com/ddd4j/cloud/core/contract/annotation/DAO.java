package com.ddd4j.cloud.core.contract.annotation;

import com.ddd4j.cloud.core.contract.Model;
import com.ddd4j.cloud.core.contract.Query;

import java.lang.annotation.*;

/**
 * 数据访问注解
 * 作用：绑定实体、模型、查询类
 *
 * @author Jensen
 * @公众号 架构师修行录
 */
@Target(ElementType.TYPE)
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface DAO {
    Class<?> entity();

    Class<? extends Model> model();

    Class<? extends Query> query();
}
