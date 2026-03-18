package com.ddd4j.cloud.sms.contract.enums;

import com.ddd4j.cloud.core.contract.enums.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 签名场景类型。返回值以”类型“结尾。取值：
 * <p>
 * 验证码类型。
 * <p>
 * 通用类型。
 * <p>
 * 示例值:
 * 验证码类型
 *
 * @author zhouhengzhe
 */
@Getter
@AllArgsConstructor
public enum BusinessTypeEnum implements IEnum<String> {
    // 验证码类型
    SMS_CODE_TYPE("SMS_CODE_TYPE", "验证码类型"),
    // 通用类型
    SMS_COMMON_TYPE("SMS_COMMON_TYPE", "通用类型");


    private final String code;
    private final String desc;
}
