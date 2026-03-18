package com.ddd4j.cloud.mq.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 基础MQ属性
 */
@Data
@ConfigurationProperties(prefix = "ddd4j.mq")
public class BaseMQProperties {
    // MQ实现：none|kafka|rocket|rabbit|redis|redisStream，一个应用只用一套MQ发布和订阅
    private String impl = "none";
    // 服务地址
    private String server = "";
    // 命名空间，或作为所有主题的前缀，用于环境隔离等场景，如UAT/生产/租户环境共用一个MQ
    private String namespace = "";
    // 是否持久化到本地，需要定义实现了MQEventStorer的Bean
    private boolean persist = false;
    // 序列化器，定义实现了MQEventSerialization的Bean
    private String serialization = "JsonMQEventSerialization";
    // 是否自动提交
    private boolean autoAck = false;
    // 发送失败重试次数
    private int retries = 0;
    // 用户
    private String username = "";
    // 密码
    private String password = "";
    // 默认主题
    private String defaultTopic = "DEFAULT";
    // 交换机
    private String exchange = "";
    // 三段式拼接符
    private String concat = "_";
}