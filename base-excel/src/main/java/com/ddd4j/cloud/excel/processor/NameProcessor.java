package com.ddd4j.cloud.excel.processor;

import java.lang.reflect.Method;

/**
 * 命名处理器
 *
 * @author zhouhengzhe
 * @version 1.0
 * @date 2023/6/25 16:39
 */
public interface NameProcessor {

    /**
     * 解析名称
     *
     * @param args   拦截器对象
     * @param method 方法
     * @param key    表达式
     * @return 名称
     */
    String doDetermineName(Object[] args, Method method, String key);
}
