package com.ddd4j.cloud.sms.contract.enums;

import com.ddd4j.cloud.core.contract.enums.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 短信发送范围。取值：
 * <p>
 * 1：国内短信发送记录。
 * <p>
 * 2：国际/港澳台短信发送记录。
 *
 * @author zhouhengzhe
 */
@Getter
@AllArgsConstructor
public enum IsGlobeEnum implements IEnum<Integer> {
    // 国内短信发送记录。
    DOMESTIC(1, "国内短信发送记录"),

    // 国际/港澳台短信发送记录。
    INTERNATIONAL(2, "国际/港澳台短信发送记录");
    private final Integer code;
    private final String desc;
}
