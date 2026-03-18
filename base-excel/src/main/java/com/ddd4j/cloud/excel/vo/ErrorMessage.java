package com.ddd4j.cloud.excel.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * 校验错误信息
 *
 * @author zhouhengzhe
 * @version 1.0
 * @date 2023/6/25 16:45
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorMessage {

    /**
     * 行号
     */
    private Long lineNum;

    /**
     * 错误信息
     */
    private Set<String> errors = new HashSet<>();

    public ErrorMessage(Set<String> errors) {
        this.errors = errors;
    }

    public ErrorMessage(String error) {
        HashSet<String> objects = new HashSet<>();
        objects.add(error);
        this.errors = objects;
    }

}
