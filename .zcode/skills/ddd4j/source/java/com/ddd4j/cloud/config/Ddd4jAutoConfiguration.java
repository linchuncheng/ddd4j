package com.ddd4j.cloud.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.ddd4j.cloud.data.Ddd4jTenantLineHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * ddd4j 自动装配入口
 * <p>
 * 只装配框架默认行为，业务声明的 MybatisPlusInterceptor / MetaObjectHandler 优先。
 *
 * @author Jensen
 */
@AutoConfiguration
@EnableConfigurationProperties(Ddd4jProperties.class)
public class Ddd4jAutoConfiguration {

    @Bean
    @ConditionalOnClass(MybatisPlusInterceptor.class)
    @ConditionalOnMissingBean(MybatisPlusInterceptor.class)
    public MybatisPlusInterceptor mybatisPlusInterceptor(Ddd4jProperties properties) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 多租户拦截器必须在分页拦截器之前
        if (properties.getTenant().isEnabled()) {
            interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(new Ddd4jTenantLineHandler(properties.getTenant())));
        }
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(properties.getDataConfig().getDbType()));
        return interceptor;
    }
}
