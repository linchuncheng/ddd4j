package com.ddd4j.cloud.config;

import com.baomidou.mybatisplus.annotation.DbType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * ddd4j 配置项
 * <pre>
 * ddd4j:
 *   tenant:
 *     enabled: true          # 租户隔离开关
 *     column: tenant_id      # 租户字段名
 *     exclude-tables: []     # 不参与租户隔离的表
 *   web:
 *     enabled: true          # 上下文拦截器 / 响应包装 / 全局异常开关
 *     user-header: X-User-Id
 *     tenant-header: X-Tenant-Id
 *     trace-header: X-Trace-Id
 *   data-config:
 *     db-type: mysql         # 分页方言
 * </pre>
 *
 * @author Jensen
 */
@Data
@ConfigurationProperties(prefix = "ddd4j")
public class Ddd4jProperties {

    private Tenant tenant = new Tenant();
    private Web web = new Web();
    // 注意：不能叫 Data，会与 lombok.Data 注解冲突
    private DataConfig dataConfig = new DataConfig();
    // 异步任务是否自动传播 AppContext（装饰 Spring 默认任务执行器）
    private boolean contextPropagation = true;

    @Data
    public static class Tenant {
        private boolean enabled = true;
        private String column = "tenant_id";
        private List<String> excludeTables = new ArrayList<>();
    }

    @Data
    public static class Web {
        private boolean enabled = true;
        private String userIdHeader = "X-User-Id";
        private String tenantIdHeader = "X-Tenant-Id";
        private String traceIdHeader = "X-Trace-Id";
    }

    @Data
    public static class DataConfig {
        private DbType dbType = DbType.MYSQL;
    }
}
