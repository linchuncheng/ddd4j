package com.ddd4j.cloud.kit.cache;

import java.util.function.Supplier;

/**
 * Cache
 * <p>
 * 缓存接口
 *
 * @author zhouhengzhe
 */
public interface ICache {

    <T> T put(Object key, Object value);

    <T> T get(Object key);

    <T> T get(Object key, Supplier<T> provider);

    <T> T remove(Object key);

    void clear();

}
