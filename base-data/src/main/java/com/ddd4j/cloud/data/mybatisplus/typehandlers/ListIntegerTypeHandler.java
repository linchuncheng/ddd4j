package com.ddd4j.cloud.data.mybatisplus.typehandlers;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

/**
 * 类型转换：json <-> List<Integer>
 */
public class ListIntegerTypeHandler extends ListTypeHandler<Integer> {
    @Override
    protected TypeReference<List<Integer>> elementType() {
        return new TypeReference<List<Integer>>() {
        };
    }
}