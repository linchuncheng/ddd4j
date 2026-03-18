package com.ddd4j.cloud.sms.contract.command;

import java.io.Serializable;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新短信模版
 *
 * @author zhouhengzhe
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SmsTemplateUpdateCommand implements Serializable {
    // 短信模板申请说明，是模板审核的参考信息之一。长度不超过 100 个字符。
    @NotBlank(message = "remark不能为空")
    @Length(max = 100, message = "remark长度不能超过100")
    private String remark;

    /**
     * 模板内容，长度不超过 500 个字符。更多规范，请参见模板内容规范。
     * <a href="https://help.aliyun.com/zh/sms/user-guide/message-template-specifications/">...</a>
     */
    @NotBlank(message = "templateContent不能为空")
    @Length(max = 500, message = "templateContent长度不能超过500")
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
    @NotBlank(message = "templateType不能为空")
    @Range(min = 0, max = 3, message = "templateType取值范围为0-3")
    private Integer templateType;
    // 模板CODE。
    @NotBlank(message = "templateCode不能为空")
    private String templateCode;
}
