package com.ddd4j.cloud.sms.contract.vo;

import com.ddd4j.cloud.sms.contract.enums.AuditStatusEnum;
import com.ddd4j.cloud.sms.contract.enums.OuterTemplateTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 短信模板分页VO
 *
 * @author zhouhengzhe
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SmsTemplatePageVO implements Serializable {
    /**
     * AUDIT_STATE_INIT：审核中。
     * AUDIT_STATE_PASS：审核通过。
     * AUDIT_STATE_NOT_PASS：审核未通过，请在返回参数 Reason 中查看审核未通过原因。
     * AUDIT_STATE_CANCEL或AUDIT_SATE_CANCEL：取消审核。
     *
     * @see AuditStatusEnum
     */
    private String auditStatus;
    // 短信模板的创建时间，格式为 yyyy-MM-dd HH:mm:ss。
    private LocalDateTime createDate;
    // 工单 ID。
    private String orderId;

    /**
     * 模板类型（推荐对外使用）。
     * 0：验证码短信。
     * 1：通知短信。
     * 2：推广短信。
     * 3：国际/港澳台短信。
     * 7：数字短信。
     *
     * @see OuterTemplateTypeEnum
     */
    private Integer outerTemplateType;
    // 审核失败的原因
    private SmsTemplatePageInfo reason;
    // 短信模板 CODE
    private String templateCode;
    // 模板内容。
    private String templateContent;
    // 模板名称
    private String templateName;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class SmsTemplatePageInfo implements Serializable {
        /**
         * 审批未通过的时间，格式为 yyyy-MM-dd HH:mm:ss。
         */
        private LocalDateTime rejectDate;

        /**
         * 审批未通过的备注信息。
         */
        private String rejectInfo;

        /**
         * 审批未通过的原因。
         */
        private String rejectSubInfo;
    }
}
