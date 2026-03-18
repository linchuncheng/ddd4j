package com.ddd4j.cloud.data.mybatisplus.typehandlers;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

/**
 * 类型转换：json <-> List<Boolean>
 */
public class ListBooleanTypeHandler extends ListTypeHandler<Boolean> {
    @Override
    protected TypeReference<List<Boolean>> elementType() {
        return new TypeReference<List<Boolean>>() {
        };
    }
}