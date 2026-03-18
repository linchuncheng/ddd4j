package com.ddd4j.cloud.sms.contract.enums;

import com.ddd4j.cloud.core.contract.enums.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 模板审核状态。取值：
 * <p>
 * 0：审核中。
 * 1：审核通过。
 * 2：审核未通过，请在返回参数 Reason 中查看审核失败原因。
 * 10：取消审核。
 * 示例值:
 * 1
 *
 * @author zhouhengzhe
 */
@Getter
@AllArgsConstructor
public enum TemplateStatusEnum implements IEnum<Integer> {
    // 审核中
    AUDITING(0, "审核中"),

    // 审核通过
    PASS(1, "审核通过"),

    // 审核未通过
    REJECT(2, "审核未通过"),

    // 取消审核
    CANCEL(10, "取消审核");

    private final Integer code;

    private final String desc;
}
