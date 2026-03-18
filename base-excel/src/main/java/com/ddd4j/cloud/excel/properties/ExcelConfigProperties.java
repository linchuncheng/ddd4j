package com.ddd4j.cloud.excel.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * excel配置
 *
 * @author zhouhengzhe
 * @version 1.0
 * @date 2023/6/25 16:35
 */
@Data
@ConfigurationProperties(prefix = "ddd4j.excel")
public class ExcelConfigProperties {
    // 模板路径
    private String templatePath = "excel";
}
