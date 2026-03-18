package com.ddd4j.cloud.kit.lang;

import cn.hutool.core.util.ObjectUtil;
import org.springframework.lang.Nullable;

import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * FunctionUtils
 * <p>
 * lambda function 工具类
 *
 * @author zhouhengzhe
 */
public abstract class FunctionKit {

    public static <T> BinaryOperator<T> first() {
        return (a, b) -> a;
    }


    public static <T> BinaryOperator<T> second() {
        return (a, b) -> b;
    }


    @Nullable
    public static <T, R> R map(@Nullable T obj, Function<T, R> func) {
        return obj == null ? null : func.apply(obj);
    }


    public static <T, R> R map(@Nullable T obj, Function<T, R> func, R defaultValue) {
        return ObjectUtil.defaultIfNull(map(obj, func), defaultValue);
    }


    public static <T, R> R map(@Nullable T obj, Function<T, R> func, Supplier<R> defaultSupplier) {
        R value = map(obj, func);
        return value != null ? value : defaultSupplier.get();
    }


}
