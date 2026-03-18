package com.ddd4j.cloud.data.mybatisplus.plugin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ddd4j.cloud.core.context.ThreadContext;
import com.ddd4j.cloud.core.contract.constant.ContextConstants;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.*;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * SqlMonitorPlugin
 * <p>
 * SQL监控插件<br>
 *
 * @author Jensen
 * @version 1.0
 * @公众号 架构师修行录
 * @date 2024/05/16
 */
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class SqlMonitorPlugin implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        BoundSql boundSql = statementHandler.getBoundSql();
        ThreadContext.set(ContextConstants.PREPARING_SQL, boundSql.getSql().replaceAll("\\n", " ").replaceAll("\\s\\s", " "));
        if (statementHandler.getParameterHandler().getParameterObject() instanceof MapperMethod.ParamMap) {
            MapperMethod.ParamMap parameterObject = (MapperMethod.ParamMap) statementHandler.getParameterHandler().getParameterObject();
            if (parameterObject.containsKey("param1") && parameterObject.get("param1") instanceof QueryWrapper) {
                Map<String, Object> params = ((QueryWrapper) parameterObject.get("param1")).getParamNameValuePairs();
                if (params != null && params.size() > 0) {
                    // 将 HashMap 的键放入一个列表中
                    List<String> keys = new ArrayList<>(params.keySet());
                    // 对键的列表进行排序
                    Collections.sort(keys);
                    // 创建一个新列表来存放排序后的值
                    List<String> sortedSqlParams = new ArrayList<>();
                    // 遍历排序后的键，从 HashMap 中收集对应的值
                    for (String key : keys) {
                        Object o = params.get(key);
                        if (o != null) {
                            sortedSqlParams.add(String.valueOf(o));
                        }
                    }
                    ThreadContext.set(ContextConstants.SQL_PARAMS, sortedSqlParams);
                }
            }
        }
        long startTime = System.currentTimeMillis();
        try {
            return invocation.proceed();
        } finally {
            ThreadContext.set(ContextConstants.LAST_SQL_SPENDS, (System.currentTimeMillis() - startTime));
        }
    }

    @Override
    public Object plugin(Object target) {
        if (target instanceof StatementHandler) {
            return Plugin.wrap(target, this);
        }
        return target;
    }

}
