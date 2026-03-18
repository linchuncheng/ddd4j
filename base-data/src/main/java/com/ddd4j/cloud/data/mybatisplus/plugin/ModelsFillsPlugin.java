package com.ddd4j.cloud.data.mybatisplus.plugin;

import com.ddd4j.cloud.core.contract.Model;
import com.ddd4j.cloud.core.contract.Page;
import com.ddd4j.cloud.core.contract.Query;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * ModelsFillsPlugin
 * <p>
 * 模型聚合插件<br>
 *
 * @author Jensen
 * @version 1.0
 * @公众号 架构师修行录
 * @date 2024/07/31
 */
@Intercepts({@Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})})
public class ModelsFillsPlugin implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object result = invocation.proceed();
        Object parameter = invocation.getArgs()[1];
        // 获取 Mapper 方法的参数
        if (parameter instanceof MapperMethod.ParamMap) {
            for (Object value : ((Map) parameter).values()) {
                if (value instanceof Query) {
                    fills((Query) value, result);
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public Object plugin(Object target) {
        if (target instanceof Executor) {
            return Plugin.wrap(target, this);
        }
        return target;
    }

    private void fills(Query query, Object result) {
        if (result == null) return;
        if (result instanceof Model) {
            query.fill(Collections.singletonList(result));
        } else if (result instanceof List) {
            if (!((List) result).isEmpty() && ((List) result).get(0) instanceof Model) {
                query.fill((List) result);
            }
        } else if (result instanceof Page) {
            if (!((Page) result).isEmpty() && ((Page) result).getRecords().get(0) instanceof Model) {
                query.fill(((Page) result).getRecords());
            }
        }
    }

}