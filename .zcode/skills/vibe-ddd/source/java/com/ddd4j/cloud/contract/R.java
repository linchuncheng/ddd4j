package com.ddd4j.cloud.contract;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一响应结构：{code, msg, data}。
 * <p>
 *
 * @author Jensen
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class R<T> implements Serializable {

    private String code;
    private String msg;
    private T data;

    public static <T> R<T> ok() {
        return new R<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), null);
    }

    public static <T> R<T> ok(T data) {
        return new R<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), data);
    }

    public static <T> R<T> ok(T data, String msg) {
        return new R<>(ResultCode.SUCCESS.getCode(), msg, data);
    }

    public static <T> R<T> fail() {
        return fail(ResultCode.FAIL.getCode(), ResultCode.FAIL.getMsg());
    }

    public static <T> R<T> fail(String msg) {
        return fail(ResultCode.FAIL.getCode(), msg);
    }

    public static <T> R<T> fail(ResultCode resultCode) {
        return fail(resultCode.getCode(), resultCode.getMsg());
    }

    public static <T> R<T> fail(IEnum iEnum) {
        return fail(iEnum.getCode(), iEnum.getMsg());
    }

    public static <T> R<T> fail(String code, String msg) {
        return new R<>(code, msg, null);
    }

    public static <T> R<T> fail(int code, String msg) {
        return fail(String.valueOf(code), msg);
    }

    @JsonIgnore
    public boolean isSuccess() {
        return ResultCode.SUCCESS.getCode().equals(this.code);
    }
}
