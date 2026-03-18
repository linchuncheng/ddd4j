package com.ddd4j.cloud.sms.contract.enums;

import com.ddd4j.cloud.core.contract.enums.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 模板审批类型
 *
 * @author ysm
 * @date 2024/01/21
 */
@Getter
@AllArgsConstructor
public enum AuditStatusEnum implements IEnum<String> {
    AUDIT_STATE_INIT("AUDIT_STATE_INIT", "审核中"),
    AUDIT_STATE_PASS("AUDIT_STATE_PASS", "审核通过"),
    AUDIT_STATE_NOT_PASS("AUDIT_STATE_NOT_PASS", "审核未通过"),
    AUDIT_SATE_CANCEL("AUDIT_SATE_CANCEL", "取消审核");

    private final String code;
    private final String desc;
}
