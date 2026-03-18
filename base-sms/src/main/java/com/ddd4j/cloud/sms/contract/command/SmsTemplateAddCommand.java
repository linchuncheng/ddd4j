package com.ddd4j.cloud.sms.contract.command;

import java.io.Serializable;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 添加短信模版
 *
 * @author zhouhengzhe
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SmsTemplateAddCommand implements Serializable {
    // 短信模板申请说明，是模板审核的参考信息之一。长度不超过 100 个字符。
    @NotBlank(message = "remark不能为空")
    private String remark;

    /**
     * 模板内容，长度不超过 500 个字符。更多规范，请参见模板内容规范。
     * <a href="https://help.aliyun.com/zh/sms/user-guide/message-template-specifications/">...</a>
     */
    @NotBlank(message = "templateContent不能为空")
    private String templateContent;
    // 模板名称，长度不超过 30 个字符。
    @NotBlank(message = "templateName不能为空")
    @Length(max = 30, message = "templateName长度不能超过30")
    private String templateName;

    /**
     * 短信类型。
     * 0：验证码。
     * 1：短信通知。
     * 2：推广短信。
     * 3：国际/港澳台消息。
     */
    @NotNull(message = "templateType不能为空")
    @Range(min = 0, max = 3, message = "templateType必须在0-3之间")
    private Integer templateType;
}
