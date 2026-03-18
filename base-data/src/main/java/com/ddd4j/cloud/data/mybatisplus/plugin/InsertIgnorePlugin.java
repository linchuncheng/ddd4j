package com.ddd4j.cloud.data.mybatisplus.plugin;

import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.sql.Connection;
import java.util.Objects;

/**
 * InsertIgnorePlugin
 * <p>
 * INSERT IGNORE 改写插件（将 INSERT 语句修改为 INSERT IGNORE 语句）<br>
 * 注：需和唯一索引搭配使用
 *
 * @author Jensen
 * @version 1.0
 * @公众号 架构师修行录
 * @date 2021/05/13 16:55
 */
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class InsertIgnorePlugin implements Interceptor {

    private static final ThreadLocal<Boolean> THREAD_LOCAL = new ThreadLocal<>();


    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        if (!Objects.equals(THREAD_LOCAL.get(), Boolean.TRUE)) {
            return invocation.proceed();
        }

        StatementHandler statementHandler = PluginUtils.realTarget(invocation.getTarget());
        MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
        // 只针对 insert 操作
        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
        SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();
        if (sqlCommandType != SqlCommandType.INSERT) {
            return invocation.proceed();
        }

        // 修改 sql
        BoundSql boundSql = (BoundSql) metaObject.getValue("delegate.boundSql");
        String buildSql = boundSql.getSql().replace("INSERT", "INSERT IGNORE").replace("insert", "INSERT IGNORE");
        metaObject.setValue("delegate.boundSql.sql", buildSql);
        return invocation.proceed();
    }


    @Override
    public Object plugin(Object target) {
        if (target instanceof StatementHandler) {
            return Plugin.wrap(target, this);
        }
        return target;
    }


    public static void enable() {
        THREAD_LOCAL.set(Boolean.TRUE);
    }

    public static void reset() {
        THREAD_LOCAL.remove();
    }

}
