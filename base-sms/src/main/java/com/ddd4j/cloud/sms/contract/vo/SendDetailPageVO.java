package com.ddd4j.cloud.sms.contract.vo;

import com.ddd4j.cloud.sms.contract.enums.SendStatusEnum;
import com.ddd4j.cloud.sms.contract.enums.SmsSendSuccessEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 分页查询短信发送记录和发送状态等信息返回值
 *
 * @author zhouhengzhe
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SendDetailPageVO implements Serializable {

    /**
     * 短信内容。
     * <p>
     * 示例值:
     * 【阿里云】验证码为：123，您正在登录，若非本人操作，请勿泄露
     */
    private String content;

    /**
     * 运营商短信状态码。
     * <p>
     * 短信发送成功：DELIVERED。
     * 短信发送失败：失败错误码请参见
     * <a href="https://help.aliyun.com/document_detail/101347.html?spm=api-workbench.api_explorer.0.0.716174aemP5W5b">...</a>
     *
     * @see SmsSendSuccessEnum
     */
    private String errCode;

    /**
     * 外部流水扩展字段。
     * <p>
     * 示例值:
     * 123
     */
    private String outId;

    /**
     * 接收短信的手机号码。
     * <p>
     * 示例值:
     * 1390000****
     */
    private String phoneNum;

    /**
     * 短信接收日期和时间。
     * <p>
     * 示例值:
     * 2019-01-08 16:44:13
     */
    private LocalDateTime receiveDate;

    /**
     * 短信发送日期和时间。
     * <p>
     * 示例值:
     * 2019-01-08 16:44:10
     */
    private LocalDateTime sendDate;

    /**
     * 短信发送状态，包括：
     * <p>
     * 1：等待回执。
     * 2：发送失败。
     * 3：发送成功。
     * 示例值:
     * 3
     *
     * @see SendStatusEnum
     */
    private Long sendStatus;

    /**
     * 短信模板 Code。
     * <p>
     * 说明 如果您选择[测试专用]阿里云通信和[测试专用]阿里云通信测试模板发送的测试短信，此接口不返回 TemplateCode 等字段信息。
     * 示例值:
     * SMS_12231****
     */
    private String templateCode;
}
