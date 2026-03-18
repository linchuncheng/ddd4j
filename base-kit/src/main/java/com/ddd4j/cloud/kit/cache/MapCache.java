package com.ddd4j.cloud.kit.cache;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * MapCache
 *
 * @author zhouhengzhe
 */
@SuppressWarnings("unchecked")
public class MapCache implements ICache {

    private final Map cache;

    MapCache(Map cache) {
        this.cache = Objects.requireNonNull(cache);
    }


    @Override
    public <T> T put(Object key, Object value) {
        return (T) cache.put(key, value);
    }


    @Override
    public <T> T get(Object key) {
        return (T) cache.get(key);
    }


    @Override
    public <T> T get(Object key, Supplier<T> provider) {
        T value = get(key);
        if (Objects.isNull(value)) {
            value = provider.get();
            if (Objects.nonNull(value)) {
                put(key, value);
            }
        }
        return value;
    }


    @Override
    public <T> T remove(Object key) {
        return (T) cache.remove(key);
    }


    @Override
    public void clear() {
        cache.clear();
    }

}
