package com.ddd4j.cloud.sms.contract.vo;

import com.ddd4j.cloud.sms.contract.enums.OuterTemplateTypeEnum;
import com.ddd4j.cloud.sms.contract.enums.TemplateStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 查询短信模板审核状态返回体
 *
 * @author zhouhengzhe
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SmsTemplateQueryVO implements Serializable {

    /**
     * 短信模板的创建时间。
     * <p>
     * 示例值:
     * 2019-06-04 11:42:17
     */
    private LocalDateTime createDate;

    /**
     * 审核备注。非验证码类型短信，请选择短信通知类型为推广短信。
     * <p>
     * 如果审核状态为审核通过或审核中，参数 Reason 显示为“无审批备注”。
     * 如果审核状态为审核未通过，参数 Reason 显示审核的具体原因。
     * 示例值:
     * 无审批备注
     */
    private String reason;

    /**
     * 短信模板 CODE。
     * <p>
     * 示例值:
     * SMS_16703****
     */
    private String templateCode;

    /**
     * 模板内容。
     * <p>
     * 示例值:
     * 亲爱的会员！阿里云短信服务祝您新年快乐！
     */
    private String templateContent;

    /**
     * 模板名称。
     * <p>
     * 示例值:
     * 阿里云短信测试模板
     */
    private String templateName;

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
     * @see TemplateStatusEnum
     */
    private Integer templateStatus;

    /**
     * 短信类型。取值：
     * <p>
     * 0：验证码。
     * 1：短信通知。
     * 2：推广短信。
     * 3：国际/港澳台消息。
     * 示例值:
     * 1
     *
     * @see OuterTemplateTypeEnum
     */
    private Integer templateType;
}
