package com.ddd4j.cloud.mq.config;

import com.ddd4j.cloud.mq.core.MQClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.annotation.Order;

import java.util.List;
import java.util.Objects;

/**
 * 基础MQ配置
 */
@Slf4j
@Order(PriorityOrdered.HIGHEST_PRECEDENCE + 10)
@Configuration
public class BaseMQConfig {
    @Autowired
    private List<MQClient> mqClients;

    @EventListener
    public void initMQClient(ApplicationStartedEvent event) {
        try {
            if (mqClients != null && !mqClients.isEmpty()) {
                // 获取所有实现了MQClient的MQ客户端实现类
                for (MQClient mqClient : mqClients) {
                    if (!Objects.equals(mqClient.config().getImpl(), "none")) {
                        // 先初始化
                        mqClient.init();
                        // 再启动
                        mqClient.start();
                    }
                }
            }
        } catch (Exception e) {
            log.error("初始化MQ失败", e);
        }
    }
}