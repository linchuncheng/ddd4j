package com.ddd4j.cloud.kit.enums;

import java.util.Objects;
import java.util.Optional;

/**
 * EnumValueResolver
 * <p>
 * 枚举值解析器
 *
 * @author zhouhengzhe
 */
public interface EnumValueResolver<E extends Enum<E>, V> {


    // 获取枚举值
    V getCode();


    // 获取 String 类型的枚举值
    default String getStringCode() {
        return getCode().toString();
    }


    // 获取 int 类型的枚举值
    default int getIntCode() {
        return Integer.parseInt(getStringCode());
    }


    // 获取枚举描述
    default String getMessage() {
        return Optional.ofNullable(getCode()).map(String::valueOf).orElse(null);
    }


    default boolean isEquals(V value) {
        return Objects.equals(getCode(), value);
    }


    default boolean isNotEquals(V value) {
        return !isEquals(value);
    }

}