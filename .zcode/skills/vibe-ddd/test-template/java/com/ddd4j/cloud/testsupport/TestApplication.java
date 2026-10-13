package com.ddd4j.cloud.testsupport;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 测试启动类：验证自动装配在真实 Spring 上下文中生效
 */
@SpringBootApplication
@EnableAsync
public class TestApplication {
}
