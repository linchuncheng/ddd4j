package com.ddd4j.cloud.data.config;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.ddd4j.cloud.core.contract.BaseRepository;
import com.ddd4j.cloud.data.mybatisplus.MybatisPlusRepositoryImpl;
import com.ddd4j.cloud.data.mybatisplus.plugin.InsertIgnorePlugin;
import com.ddd4j.cloud.data.mybatisplus.plugin.ModelsFillsPlugin;
import com.ddd4j.cloud.data.mybatisplus.plugin.SqlMonitorPlugin;
import com.ddd4j.cloud.data.mybatisplus.typehandlers.BaseTypeHandler;
import com.ddd4j.cloud.data.mybatisplus.typehandlers.BigDecimalTypeHandler;

import java.math.BigDecimal;
import java.util.List;

import javax.sql.DataSource;

import org.apache.ibatis.logging.slf4j.Slf4jImpl;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(DataSource.class)
@AutoConfigureAfter({ DataSourceAutoConfiguration.class, MybatisPlusAutoConfiguration.class })
public class BaseDataConfig {

    @Bean
    @ConditionalOnClass(MybatisPlusAutoConfiguration.class)
    public BaseRepository mybatisPlusRepositoryImpl(MybatisPlusProperties mybatisPlusProperties,
            List<BaseTypeHandler> jsonStringTypeHandlers,
            SqlSessionFactory sqlSessionFactory) {
        // 从 SqlSessionFactory 获取 MybatisConfiguration
        MybatisConfiguration configuration = (MybatisConfiguration) sqlSessionFactory.getConfiguration();

        if (configuration.getLogImpl() == null) {
            configuration.setLogImpl(Slf4jImpl.class);
        }
        TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
        typeHandlerRegistry.register(BigDecimal.class, new BigDecimalTypeHandler());
        typeHandlerRegistry.register(JdbcType.DECIMAL, new BigDecimalTypeHandler());

        if (jsonStringTypeHandlers != null && !jsonStringTypeHandlers.isEmpty()) {
            for (BaseTypeHandler baseTypeHandler : jsonStringTypeHandlers) {
                typeHandlerRegistry.register(baseTypeHandler.type(), baseTypeHandler);
            }
        }

        // 添加 MyBatis Plus 拦截器
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        configuration.addInterceptor(interceptor);
        configuration.addInterceptor(new InsertIgnorePlugin());
        configuration.addInterceptor(new SqlMonitorPlugin());
        configuration.addInterceptor(new ModelsFillsPlugin());

        return new MybatisPlusRepositoryImpl();
    }

}