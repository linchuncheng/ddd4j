package com.ddd4j.cloud.contract;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 分页结果
 *
 * @author Jensen
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Page<T> implements Serializable {

    private List<T> records;
    private long total;
    private long pageSize;
    private long currentPage;
    private long totalPages;

    public static <T> Page<T> of(List<T> records, long total, long pageSize, long currentPage) {
        return new Page<>(records, total, pageSize, currentPage, totalPages(total, pageSize));
    }

    public static <T> Page<T> empty() {
        return new Page<>(Collections.emptyList(), 0, 0, 0, 0);
    }

    public boolean isEmpty() {
        return records == null || records.isEmpty();
    }

    private static long totalPages(long total, long pageSize) {
        if (total == 0 || pageSize <= 0) {
            return 0;
        }
        return (total + pageSize - 1) / pageSize;
    }
}
