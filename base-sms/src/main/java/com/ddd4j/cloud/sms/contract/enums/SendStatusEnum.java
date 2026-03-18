package com.ddd4j.cloud.sms.contract.enums;

import com.ddd4j.cloud.core.contract.enums.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 短信发送状态，包括：
 * <p>
 * 1：等待回执。
 * 2：发送失败。
 * 3：发送成功。
 * 示例值:
 * 3
 *
 * @author zhouhengzhe
 */
@Getter
@AllArgsConstructor
public enum SendStatusEnum implements IEnum<Integer> {
    // 等待回执
    WAITING(1, "等待回执"),
    // 发送失败
    SEND_FAIL(2, "发送失败"),
    // 发送成功
    SEND_SUCCESS(3, "发送成功");

    private final Integer code;

    private final String desc;
}
