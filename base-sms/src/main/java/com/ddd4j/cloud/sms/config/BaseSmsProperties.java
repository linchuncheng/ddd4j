package com.ddd4j.cloud.sms.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * sms属性配置
 *
 * @author zhouhengzhe
 * @version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties(prefix = "ddd4j.sms")
public class BaseSmsProperties {
    private AliyunSmsProperties aliyun;
    private BoshitongSmsProperties boshitong;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AliyunSmsProperties {
        /**
         * 账号
         */
        private String accessKeyId;
        /**
         * 密码
         */
        private String accessKeySecret;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BoshitongSmsProperties {

        /**
         * 账号
         */
        private String uid;

        /**
         * 密码
         */
        private String pwd;

        /**
         *
         */
        private String srcphone;
    }
}
