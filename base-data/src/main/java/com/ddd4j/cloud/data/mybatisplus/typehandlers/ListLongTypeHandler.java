package com.ddd4j.cloud.data.mybatisplus.typehandlers;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

/**
 * 类型转换：json <-> List<Long>
 */
public class ListLongTypeHandler extends ListTypeHandler<Long> {
    @Override
    protected TypeReference<List<Long>> elementType() {
        return new TypeReference<List<Long>>() {
        };
    }
}