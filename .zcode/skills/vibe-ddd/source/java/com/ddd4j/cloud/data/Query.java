package com.ddd4j.cloud.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;

/**
 * 查询基类：分页 / 排序 / 租户开关。
 * <p>
 * 子类添加带条件后缀的字段即可声明查询条件，字段名去掉后缀后
 * 按驼峰转下划线映射为数据库列，翻译规则见 {@link QueryTranslator}。
 * <p>
 * 示例：
 * <pre>
 * public class UserQuery extends Query {
 *     private String username;          // username = ?
 *     private String nicknameLike;      // nickname LIKE '%?%'
 *     private Integer ageGe;            // age >= ?
 * }
 * </pre>
 *
 * @author Jensen
 */
@Data
public class Query implements Serializable {

    // 页码，从 1 开始
    private Integer current = 1;
    // 每页条数；小于 0 表示不分页
    private Integer size = 20;
    // 排序：字段名_方向，多个用英文逗号分隔，如 createTime_DESC,id_ASC
    private String orderBys;
    // 忽略租户隔离，仅对本次查询生效；不参与 JSON 反序列化，只能在代码中显式设置
    @JsonIgnore
    private boolean ignoreTenant = false;

    public boolean isPaged() {
        return size != null && size >= 0;
    }

    @SuppressWarnings("unchecked")
    public <Q extends Query> Q ignoreTenant() {
        this.ignoreTenant = true;
        return (Q) this;
    }

    @SuppressWarnings("unchecked")
    public <Q extends Query> Q orderBy(String... orderBys) {
        if (orderBys != null) {
            this.orderBys = String.join(",", orderBys);
        }
        return (Q) this;
    }
}
