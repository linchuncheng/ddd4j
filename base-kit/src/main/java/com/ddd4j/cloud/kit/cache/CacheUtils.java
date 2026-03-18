package com.ddd4j.cloud.kit.cache;

import org.springframework.util.ConcurrentReferenceHashMap;

/**
 * CacheUtils
 * <p>
 * 缓存工具类
 *
 * @author zhouhengzhe
 */
public class CacheUtils {


    // 采用 weak hash map 实现
    private static final ICache WEAK_CACHE = new MapCache(new ConcurrentReferenceHashMap<>(16, ConcurrentReferenceHashMap.ReferenceType.WEAK));


    // weak cache
    public static ICache weak() {
        return WEAK_CACHE;
    }


}
