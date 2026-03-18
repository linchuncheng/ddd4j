package com.ddd4j.cloud.sms.contract.enums;

import com.ddd4j.cloud.core.contract.enums.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 短信发送成功状态
 *
 * @author zhouhengzhe
 */
@Getter
@AllArgsConstructor
public enum SmsSendSuccessEnum implements IEnum<String> {

    DELIVERED("DELIVERED", "短信发送成功"),
    /**
     * 具体错误请看
     * <a href="https://help.aliyun.com/zh/sms/developer-reference/delivery-receipt-error-codes#section-91r-wtb-7z9">...</a>
     */
    OTHER("OTHER", "短信发送失败");

    private final String code;

    private final String desc;
}
