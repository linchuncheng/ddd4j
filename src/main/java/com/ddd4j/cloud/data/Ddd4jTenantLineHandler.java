package com.ddd4j.cloud.data;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.ddd4j.cloud.config.Ddd4jProperties;
import com.ddd4j.cloud.context.AppContext;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 租户隔离处理器：为每条 SQL 自动追加 tenant_id 条件。
 * <p>
 * 以下情况不追加：本次查询显式忽略租户、当前请求无租户、表在排除名单中。
 *
 * @author Jensen
 */
public class Ddd4jTenantLineHandler implements TenantLineHandler {

    private final Ddd4jProperties.Tenant properties;
    private final Set<String> excludeTables;

    public Ddd4jTenantLineHandler(Ddd4jProperties.Tenant properties) {
        this.properties = properties;
        this.excludeTables = properties.getExcludeTables() == null ? Set.of() :
                properties.getExcludeTables().stream().map(Ddd4jTenantLineHandler::normalize).collect(Collectors.toSet());
    }

    @Override
    public Expression getTenantId() {
        Long tenantId = AppContext.tenantId();
        if (tenantId == null) {
            throw new IllegalStateException("当前上下文没有租户ID，无法执行租户隔离查询");
        }
        return new LongValue(tenantId);
    }

    @Override
    public String getTenantIdColumn() {
        return properties.getColumn();
    }

    @Override
    public boolean ignoreTable(String tableName) {
        if (TenantManager.isIgnored()) {
            return true;
        }
        if (AppContext.tenantId() == null) {
            return true;
        }
        return excludeTables.contains(normalize(tableName));
    }

    private static String normalize(String tableName) {
        return tableName == null ? "" : tableName.toLowerCase(Locale.ROOT).replace("`", "");
    }
}
