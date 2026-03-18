package com.ddd4j.cloud.sms.contract.enums;

import com.ddd4j.cloud.core.contract.enums.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 发送短信类型
 *
 * @author zhouhengzhe
 * @version 1.0
 * x
 */
@Getter
@AllArgsConstructor
public enum SmsTypeEnum implements IEnum<String> {

    ALIYUN("aliyun", "阿里云SMS短信"),
    BOSHITONG("boshitong", "博士通SMS短信");

    private final String code;
    private final String desc;
}
