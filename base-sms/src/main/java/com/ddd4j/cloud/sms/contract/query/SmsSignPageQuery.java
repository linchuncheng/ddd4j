package com.ddd4j.cloud.sms.contract.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 短信签名分页查询请求类
 *
 * @author zhouhengzhe
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class SmsSignPageQuery extends BasePageQuery {
    // OwnerId
    private Long ownerId;
    // ResourceOwnerAccount
    private String resourceOwnerAccount;
    // ResourceOwnerId
    private Long resourceOwnerId;
}
