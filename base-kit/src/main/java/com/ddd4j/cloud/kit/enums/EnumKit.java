package com.ddd4j.cloud.kit.enums;

import com.ddd4j.cloud.core.context.SpringContext;
import com.ddd4j.cloud.core.contract.enums.IEnum;
import com.ddd4j.cloud.core.contract.exception.ServiceException;
import com.ddd4j.cloud.core.kit.BizAssert;
import com.ddd4j.cloud.kit.cache.CacheUtils;
import com.ddd4j.cloud.kit.lang.CollKit;
import com.ddd4j.cloud.kit.lang.FunctionKit;
import com.google.common.collect.Maps;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.lang.Nullable;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * EnumUtils
 * <p>
 * 枚举工具类
 *
 * @author zhouhengzhe
 * @see EnumValueResolver
 */
@Slf4j
@UtilityClass
public class EnumKit {
    private static final Map<Class<? extends IEnum<? extends Serializable>>, List<? extends IEnum<? extends Serializable>>> ENUM_POOL;

    static {
        ENUM_POOL = new ConcurrentHashMap<>();
    }

    public static <C extends IEnum<? extends Serializable>> String getDesc(Class<C> clazz,
                                                                           Serializable code) {

        List<C> enumList = getEnumList(clazz);

        for (C c : enumList) {
            if (c.getCode().equals(code)) {
                return c.getDesc();
            }
        }

        return null;
    }

    public static <C extends IEnum<? extends Serializable>> C getByCode(Class<C> clazz,
                                                                        Serializable code) {

        if (Objects.isNull(code)) {
            return null;
        }

        List<C> enumList = getEnumList(clazz);
        for (C c : enumList) {
            if (c.getCode() == null) {
                continue;
            }
            if (String.valueOf(c.getCode()).equals(String.valueOf(code))) {
                return c;
            }
        }

        return null;
    }

    public static <C extends IEnum<? extends Serializable>> C getByDesc(Class<C> clazz,
                                                                        String desc) {

        if (desc == null || "null".equals(desc.trim()) || "undefined".equals(desc.trim())) {
            return null;
        }

        List<C> enumList = getEnumList(clazz);
        for (C c : enumList) {
            if (c.getDesc().equals(desc)) {
                return c;
            }
        }

        return null;
    }


    public static <C extends IEnum<? extends Serializable>> List<String> getDescs(Class<C> clazz) {
        List<C> enumList = getEnumList(clazz);
        return enumList.stream().map(t -> t.getDesc()).collect(Collectors.toList());
    }

    private static <C extends IEnum<? extends Serializable>> List<C> getEnumList(Class<C> clazz) {

        if (!ENUM_POOL.containsKey(clazz)) {
            try {
                List<C> enumList = new ArrayList<>();
                if (Enum.class.isAssignableFrom(clazz)) {
                    // 如果是Enum类型
                    Method method = clazz.getMethod("values");

                    C[] enums = (C[]) method.invoke(clazz);

                    if (ArrayUtil.isNotEmpty(enums)) {
                        enumList.addAll(Arrays.asList(enums));
                    }
                } else {
                    // 如果不是Enum类型，那么必须是Bean
                    Collection<C> enums = SpringContext.getBeans(clazz);
                    if (CollectionUtil.isNotEmpty(enums)) {
                        enumList.addAll(enums);
                    }
                }
                ENUM_POOL.put(clazz, enumList);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                log.error(e.getMessage(), e);
                throw new ServiceException(e.getMessage());
            }
        }

        return (List<C>) ENUM_POOL.get(clazz);
    }

    // 根据 value 查询枚举（将 value 转为字符串再进行匹配）
    @Nullable
    public <E extends Enum<E> & EnumValueResolver<E, ?>> E getByStringCode(Class<E> enumType, Object code) {
        if (isNullCode(code)) {
            return null;
        }

        String codeStr = code.toString();
        E[] values = enumType.getEnumConstants();
        for (E e : values) {
            if (e.getStringCode().equals(codeStr)) {
                return e;
            }
        }
        return null;
    }


    // 根据 code 查询枚举
    @Nullable
    public <E extends Enum<E>> E getByCode(Class<E> enumType, Object code, Function<E, ?> function) {
        return getByCode(enumType, code, function, (E) null);
    }


    /**
     * 根据 code 查询枚举
     *
     * @param enumType    枚举类型
     * @param code        解析值
     * @param function    解析函数
     * @param defaultCode 默认值
     */
    @Nullable
    public <E extends Enum<E>> E getByCode(Class<E> enumType, Object code, Function<E, ?> function, @Nullable E defaultCode) {
        if (isNullCode(code)) {
            return defaultCode;
        }

        E[] codes = enumType.getEnumConstants();
        for (E e : codes) {
            if (function.apply(e).equals(code)) {
                return e;
            }
        }
        return defaultCode;
    }


    // 根据 value 查询枚举
    @Nullable
    public <E extends Enum<E> & EnumValueResolver<E, ?>> E getByObjCode(Class<E> enumType, Object code) {
        return getByCode(enumType, code, (E) null);
    }

    // 根据 value 查询枚举
    public <E extends Enum<E> & EnumValueResolver<E, ?>> E getByCode(Class<E> enumType, Object code, E defaultCode) {
        if (isNullCode(code)) {
            return defaultCode;
        }

        E[] values = enumType.getEnumConstants();
        for (E e : values) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return defaultCode;
    }


    // 根据 value 查询枚举
    public <E extends Enum<E>> E requiredByCode(Class<E> enumType, Object code, Function<E, ?> function) {
        E e = getByCode(enumType, code, function);
        if (Objects.isNull(e)) {
            throw new IllegalStateException(StrUtil.format("不支持的枚举[{}]值: {}", enumType.getSimpleName(), code));
        }
        return e;
    }


    public <E extends Enum<E> & EnumValueResolver<E, ?>> E requiredByCode(Class<E> enumType, Object code) {
        E e = getByObjCode(enumType, code);
        BizAssert.notNull(e, "不支持当前枚举值" + e);
        return e;
    }


    // 根据 value 查询枚举（并且必须是候选值之一）
    public <E extends Enum<E>> E requiredByCode(Class<E> enumType, Object code, Function<E, ?> function, E... candidates) {
        E e = requiredByCode(enumType, code, function);
        return candidate(enumType, e, code, candidates);
    }


    // 根据 value 查询枚举（并且必须是候选值之一）
    public <E extends Enum<E> & EnumValueResolver<E, ?>> E requiredByCode(Class<E> enumType, Object code, E... candidates) {
        E e = requiredByCode(enumType, code);
        return candidate(enumType, e, code, candidates);
    }


    // 判断是否为空值
    private boolean isNullCode(Object code) {
        if (Objects.isNull(code)) {
            return true;
        }

        if (code instanceof String && StrUtil.isBlank(code.toString())) {
            return true;
        }

        return false;
    }


    @SafeVarargs
    private <E extends Enum<E>> E candidate(Class<E> enumType, E e, Object code, E... candidates) {
        for (E candidate : candidates) {
            if (e == candidate) {
                return e;
            }
        }
        throw new IllegalStateException(StrUtil.format("不支持的枚举[{}]值: {}", enumType.getSimpleName(), code));
    }


    // 判断枚举值是否存在
    public <E extends Enum<E> & EnumValueResolver<E, V>, V> boolean exists(Class<E> enumType, V code) {
        if (Objects.isNull(code)) {
            return false;
        }
        return Objects.nonNull(getByObjCode(enumType, code));
    }


    /**
     * 获取枚举的 codeMap
     *
     * @param enumType 枚举类型
     * @return <字典值, 字典描述>
     */
    public <E extends Enum<E> & EnumValueResolver<E, V>, V> Map<V, String> getCodeMap(Class<E> enumType) {
        E[] codes = enumType.getEnumConstants();
        Map<V, String> codeMap = Maps.newHashMapWithExpectedSize(codes.length);
        for (E code : codes) {
            codeMap.put(code.getCode(), code.getMessage());
        }
        return Collections.unmodifiableMap(codeMap);
    }


    /**
     * 获取枚举的 codeMap（会进行弱缓存）
     *
     * @param enumType 枚举类型
     * @return <字典值, 字典描述>
     */
    public <E extends Enum<E> & EnumValueResolver<E, V>, V> Map<V, String> getCodeMapCache(Class<E> enumType) {
        return CacheUtils.weak().get(enumType.getName(), () -> getCodeMap(enumType));
    }


    /**
     * 获取枚举值描述
     *
     * @param enumType    枚举类型
     * @param code        枚举值
     * @param defaultCode 默认值
     * @return 枚举描述
     */
    @Nullable
    public <E extends Enum<E> & EnumValueResolver<E, V>, V> String getEnumMessage(Class<E> enumType, V code, String defaultCode) {
        if (Objects.isNull(code)) {
            return defaultCode;
        }
        return getCodeMapCache(enumType).getOrDefault(code, defaultCode);
    }


    /**
     * 获取枚举值描述
     *
     * @param enumType 枚举类型
     * @param code     枚举值
     * @return 枚举描述
     */
    @Nullable
    public <E extends Enum<E> & EnumValueResolver<E, V>, V> String getEnumMessage(Class<E> enumType, V code) {
        return getEnumMessage(enumType, code, null);
    }


    /**
     * 获取枚举值描述
     *
     * @param enumType 枚举类型
     * @param code     枚举值
     * @return 枚举描述（不存在则返回原 value）
     */
    public <E extends Enum<E> & EnumValueResolver<E, V>, V> String getEnumCodeMessage(Class<E> enumType, V code) {
        String desc = getEnumMessage(enumType, code);
        if (Objects.nonNull(desc)) {
            return desc;
        }
        return FunctionKit.map(code, String::valueOf);
    }


    // 判断目标值是否是枚举值
    @SafeVarargs
    public <V> boolean isIn(V code, EnumValueResolver<?, V>... enums) {
        if (CollKit.isEmpty(enums)) {
            throw new IllegalArgumentException("enums must not be empty");
        }
        return isIn(code, CollUtil.newHashSet(enums));
    }


    // 判断目标值是否是枚举值
    public <V> boolean isIn(V code, Set<? extends EnumValueResolver<?, V>> enumSet) {
        if (CollKit.isEmpty(enumSet)) {
            throw new IllegalArgumentException("enumSet must not be empty");
        }
        if (Objects.isNull(code)) {
            return false;
        }

        for (EnumValueResolver<?, V> anEnum : enumSet) {
            if (anEnum.isEquals(code)) {
                return true;
            }
        }
        return false;
    }

}
