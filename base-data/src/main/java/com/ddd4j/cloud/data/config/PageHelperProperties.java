package com.ddd4j.cloud.data.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * PageHelper属性
 */
@Data
@ConfigurationProperties(prefix = "pagehelper")
public class PageHelperProperties {
    private String helperDialect = "mysql";
    private Boolean reasonable = true;
    private Boolean supportMethodsArguments = true;
    private String params = "count=countSql";
    private Boolean pageSizeZero = true;
}