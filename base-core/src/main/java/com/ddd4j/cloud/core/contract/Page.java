package com.ddd4j.cloud.core.contract;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.*;

/**
 * 分页
 *
 * @param <T>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Page<T> implements Serializable {
    // 列表数据
    private List<T> records;
    // 总记录数
    private long total;
    // 回写当前页
    private long current;
    // 回写每页大小
    private long size;
    // 扩展字段
    private Map<String, Object> extras;

    public Page(long current, long size) {
        this.current = current;
        this.size = size;
        this.total = 0L;
        this.records = new ArrayList<>();
    }

    public static <T> Page<T> of(List<T> records, long total, long current, long size) {
        return new Page<>(records, total, current, size, new HashMap<>());
    }

    public static <T> Page<T> empty() {
        return new Page<>(new ArrayList<>(), 0L, 0L, 0L, Collections.emptyMap());
    }

    @JsonIgnore
    public boolean isEmpty() {
        return this.records == null || this.records.isEmpty();
    }


}