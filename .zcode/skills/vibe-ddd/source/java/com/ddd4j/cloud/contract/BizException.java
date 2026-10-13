package com.ddd4j.cloud.contract;

import lombok.Getter;

/**
 * 业务异常：抛出后由 GlobalExceptionHandler 转换为对应 code 的 R 响应
 *
 * @author Jensen
 */
@Getter
public class BizException extends RuntimeException {

    private final String code;

    public BizException(String msg) {
        this(ResultCode.FAIL.getCode(), msg);
    }

    public BizException(String code, String msg) {
        super(msg);
        this.code = code;
    }

    public BizException(ResultCode resultCode) {
        this(resultCode.getCode(), resultCode.getMsg());
    }

    public BizException(ResultCode resultCode, String msg) {
        this(resultCode.getCode(), msg);
    }

    public BizException(IEnum iEnum) {
        this(iEnum.getCode(), iEnum.getMsg());
    }
}
