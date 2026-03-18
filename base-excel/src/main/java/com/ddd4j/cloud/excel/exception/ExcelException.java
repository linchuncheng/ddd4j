package com.ddd4j.cloud.excel.exception;

/**
 * excel异常
 *
 * @author zhouhengzhe
 * @version 1.0
 * @date 2023/6/25 17:04
 */
public class ExcelException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ExcelException(String message) {
        super(message);
    }

}
