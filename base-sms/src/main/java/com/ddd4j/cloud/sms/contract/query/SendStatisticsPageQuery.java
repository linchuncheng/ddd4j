package com.ddd4j.cloud.sms.contract.query;

import com.ddd4j.cloud.sms.contract.enums.IsGlobeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 查询短信发送量详情
 *
 * @author zhouhengzhe
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class SendStatisticsPageQuery extends BasePageQuery {

    /**
     * 短信发送范围。取值：
     * <p>
     * 1：国内短信发送记录。
     * <p>
     * 2：国际/港澳台短信发送记录。
     * <p>
     * 示例值:
     * 1
     * 取值 <= 2
     *
     * @see IsGlobeEnum
     */
    private Integer globe;

    /**
     * 开始日期，格式为 yyyyMMdd，例如 20181225。
     * <p>
     * 示例值:
     * 20201002
     */
    private String startDate;

    /**
     * 结束日期，格式为 yyyyMMdd，例如 20181225。
     * <p>
     * 示例值:
     * 20201003
     */
    private String endDate;
    // owerId
    private Long ownerId;
    // ResourceOwnerAccount
    private String resourceOwnerAccount;
    // ResourceOwnerId
    private Long resourceOwnerId;

    /**
     * 签名名称。
     * <p>
     * 示例值:
     * 阿里云平台
     */
    private String signName;


    /**
     * 模板类型。取值：
     * <p>
     * 0：验证码。
     * <p>
     * 1：通知短信。
     * <p>
     * 2：推广短信。（仅支持企业客户）
     * <p>
     * 3：国际/港澳台消息。（仅支持企业客户）
     * <p>
     * 7：数字短信。
     * <p>
     * 示例值:
     * 0
     *
     * @see com.zshy.base.sms.contract.enums.OuterTemplateTypeEnum
     */
    private Integer templateType;
}
