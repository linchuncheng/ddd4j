package com.ddd4j.cloud.sms.contract.vo;

import com.ddd4j.cloud.sms.contract.enums.AuditStatusEnum;
import com.ddd4j.cloud.sms.contract.enums.BusinessTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 短信签名分页VO
 *
 * @author zhouhengzhe
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SmsSignPageVO implements Serializable {
    /**
     * 签名审批状态。取值：
     * <p>
     * AUDIT_STATE_INIT：审核中。
     * <p>
     * AUDIT_STATE_PASS：审核通过。
     * <p>
     * AUDIT_STATE_NOT_PASS：审核未通过，请在返回参数 Reason 中查看审核未通过原因。
     * <p>
     * AUDIT_STATE_CANCEL：取消审核。
     * <p>
     * 示例值:
     * AUDIT_STATE_NOT_PASS
     *
     * @see AuditStatusEnum
     */
    private String auditStatus;

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
     * @see BusinessTypeEnum
     */
    private String businessType;

    /**
     * 短信签名的创建日期和时间，格式为 yyyy-MM-dd HH:mm:ss。
     * <p>
     * 示例值:
     * 2020-01-08 16:44:13
     */
    private LocalDateTime createDate;

    /**
     * 工单 ID。
     * <p>
     * 示例值:
     * 236****5
     */
    private String orderId;

    /**
     * 审核备注。
     * <p>
     * 如果审核状态为审核通过或审核中，参数 Reason 显示为“无审核备注”。
     * <p>
     * 如果审核状态为审核未通过，参数 Reason 显示审核的具体原因。
     */
    private SmsSignPageInfo reason;

    /**
     * 签名名称。
     * <p>
     * 示例值:
     * 阿里云
     */
    private String signName;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class SmsSignPageInfo implements Serializable {
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
