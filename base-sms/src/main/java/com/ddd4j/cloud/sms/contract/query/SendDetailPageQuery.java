package com.ddd4j.cloud.sms.contract.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 模板详情查询参数
 *
 * @author zhouhengzhe
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class SendDetailPageQuery extends BasePageQuery {

    /**
     * 接收短信的手机号码。格式：
     * <p>
     * 国内短信：11 位手机号码，例如 1390000****。
     * 国际/港澳台消息：国际区号+号码，例如 8520000****。
     * 示例值:
     * 1390000****
     */
    private String phoneNumber;
    // ResourceOwnerAccount
    private String resourceOwnerAccount;
    // ResourceOwnerId
    private Long resourceOwnerId;

    /**
     * 发送回执 ID，即发送流水号。调用 SendSms 或 SendBatchSms 发送短信时，返回值中的 BizId 字段。
     * <p>
     * 说明 只能传入一个 Bizid 的值，无法传入多个 Bizid。
     * 示例值:
     * 134523^435****
     */
    private String bizId;
    // ownerId
    private Long ownerId;

    /**
     * 短信发送日期，支持查询最近 30 天的记录。展开详情
     * <p>
     * 示例值:
     * 20181228
     */
    private String sendDate;

}
