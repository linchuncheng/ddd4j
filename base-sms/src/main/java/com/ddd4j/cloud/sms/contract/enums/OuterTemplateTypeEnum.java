package com.ddd4j.cloud.sms.contract.enums;

import com.ddd4j.cloud.core.contract.enums.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 模板类型（推荐对外使用）。
 * 0：验证码短信。
 * 1：通知短信。
 * 2：推广短信。
 * 3：国际/港澳台短信。
 * 7：数字短信。
 *
 * @author zhouhengzhe
 */
@Getter
@AllArgsConstructor
public enum OuterTemplateTypeEnum implements IEnum<Integer> {

    VERIFICATION_CODE_SMS(0, "验证码短信"),
    NOTIFICATION_SMS(1, "通知短信"),
    PROMOTION_SMS(2, "推广短信"),
    INTERNATIONAL_SMS(3, "国际/港澳台短信"),
    NUMBER_SMS(7, "数字短信");

    private final Integer code;

    private final String desc;
}
