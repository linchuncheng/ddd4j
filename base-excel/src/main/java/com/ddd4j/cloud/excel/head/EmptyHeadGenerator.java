package com.ddd4j.cloud.excel.head;

/**
 * 空的 excel 头生成器，用来忽略 excel 头生成
 *
 * @author zhouhengzhe
 * @version 1.0
 * @date 2023/6/25 16:52
 */
public class EmptyHeadGenerator implements HeadGenerator {

    @Override
    public HeadMeta head(Class<?> clazz) {
        return new HeadMeta();
    }

}
