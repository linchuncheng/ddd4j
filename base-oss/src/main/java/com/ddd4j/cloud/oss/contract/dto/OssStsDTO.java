package com.ddd4j.cloud.oss.contract.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * oss 临时token
 *
 * @author zhouhengzhe
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OssStsDTO {
    // 秘钥的key
    private String accessKeyId;
    // 秘钥的secret
    private String accessKeySecret;
    // 安全token
    private String securityToken;
    // 过期时间
    private String expiration;
}
