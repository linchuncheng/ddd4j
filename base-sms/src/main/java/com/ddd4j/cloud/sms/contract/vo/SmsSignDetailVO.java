package com.ddd4j.cloud.sms.contract.vo;

import com.ddd4j.cloud.sms.contract.enums.TemplateStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * 查询短信签名申请状态
 *
 * @author zhouhengzhe
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SmsSignDetailVO implements Serializable {

    /**
     * 短信签名的创建日期和时间。
     * <p>
     * 示例值:
     * 2019-01-08 16:44:13
     */
    private LocalDateTime createDate;

    /**
     * 审核备注。
     * <p>
     * 如果审核状态为审核通过或审核中，参数 Reason 显示为“无审核备注”。
     * 如果审核状态为审核未通过，参数 Reason 显示审核的具体原因。
     * 示例值:
     * 文件不能证明信息真实性，请重新上传
     */
    private String reason;

    /**
     * 短信签名。
     * <p>
     * 示例值:
     * 阿里云
     */
    private String signName;

    /**
     * 签名审核状态。取值：
     * <p>
     * 0：审核中。
     * 1：审核通过。
     * 2：审核失败，请在返回参数 Reason 中查看审核失败原因。
     * 10：取消审核。
     * 示例值:
     * 1
     *
     * @see TemplateStatusEnum
     */
    private Integer signStatus;
}
