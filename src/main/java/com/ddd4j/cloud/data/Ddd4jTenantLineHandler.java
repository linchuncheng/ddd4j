package com.ddd4j.cloud.data;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.ddd4j.cloud.config.Ddd4jProperties;
import com.ddd4j.cloud.context.AppContext;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 租户隔离处理器：为每条 SQL 自动追加 tenant_id 条件。
 * <p>
 * 缺省语义是 fail-closed：上下文没有租户时查询直接失败（避免平台任务静默读到全量数据），
 * 平台级任务必须通过 {@code Query.ignoreTenant()} 或 {@link TenantManager#ignoring} 显式豁免；
 * 表在排除名单（ddd4j.tenant.exclude-tables）中时不追加条件。
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
        String tenantId = AppContext.tenantId();
        if (tenantId == null) {
            throw new IllegalStateException("当前上下文没有租户ID，无法执行租户隔离查询；平台任务请用 Query.ignoreTenant() 显式豁免");
        }
        return new StringValue(tenantId);
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
        return excludeTables.contains(normalize(tableName));
    }

    private static String normalize(String tableName) {
        return tableName == null ? "" : tableName.toLowerCase(Locale.ROOT).replace("`", "");
    }
}
