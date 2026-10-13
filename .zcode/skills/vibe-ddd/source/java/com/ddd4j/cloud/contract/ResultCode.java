package com.ddd4j.cloud.contract;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应码约定：字符串编码，成功 "200"、客户端错误 "4xx"、服务端错误 "5xx"
 *
 * @author Jensen
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS("200", "操作成功"),
    BAD_REQUEST("400", "请求参数错误"),
    UNAUTHORIZED("401", "未登录或凭证已失效"),
    FORBIDDEN("403", "无权限"),
    NOT_FOUND("404", "资源不存在"),
    FAIL("500", "操作失败");

    private final String code;
    private final String msg;
}
