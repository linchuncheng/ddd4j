package com.ddd4j.cloud.data.mybatisplus.typehandlers;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

/**
 * 类型转换：json <-> List<String>
 */
public class ListStringTypeHandler extends ListTypeHandler<String> {
    @Override
    protected TypeReference<List<String>> elementType() {
        return new TypeReference<List<String>>() {
        };
    }
}