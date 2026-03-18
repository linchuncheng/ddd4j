package com.ddd4j.cloud.core.contract;

import com.ddd4j.cloud.core.context.RepositoryContext;
import com.ddd4j.cloud.core.context.SpringContext;
import com.ddd4j.cloud.core.contract.exception.ServiceException;
import com.ddd4j.cloud.core.kit.JsonKit;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 查询基类，PO依赖此类用于条件查询数据库
 * <p>
 * 注意子类不要添加@Accessors(chain = true)注解，否则会导致后面BeanKit#toMap(Object, Map, Collection) 获取不到属性
 *
 * @author Jensen
 * @公众号 架构师修行录
 */
@Data
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public abstract class Query<M extends Model> {
    // 查询条件后缀：以该后缀结尾的参数，可以自动补充查询条件
    public static final String NOT_QUERY = "Not";// 对应数据库的!=语句
    public static final String NOT_IN_QUERY = "NotIn";// 对应数据库的not in语句，类型是集合或字符串（用英文逗号,分隔）
    public static final String IN_QUERY = "In";// 对应数据库的in语句，类型是集合或字符串（用英文逗号,分隔）
    public static final String LIKE_QUERY = "Like";// 对应数据库的like '%xxx%'语句
    public static final String LIKE_LEFT_QUERY = "LikeLeft";// 对应数据库的like 'xxx%'语句
    public static final String LIKE_RIGHT_QUERY = "LikeRight";// 对应数据库的like '%xxx'语句
    public static final String NOT_LIKE_QUERY = "NotLike";// 对应数据库的not like '%xxx%'语句
    public static final String GT_QUERY = "Gt";// 对应数据库的>语句
    public static final String LT_QUERY = "Lt";// 对应数据库的<语句
    public static final String GE_QUERY = "Ge";// 对应数据库的>=语句
    public static final String LE_QUERY = "Le";// 对应数据库的<=语句
    public static final String START_QUERY = "Start";// 对应数据库的>=语句，用于开始时间筛选，即：数据库字段 >= 入参
    public static final String END_QUERY = "End";// 对应数据库的<=语句，用于结束时间筛选，或定时任务的到期时间，即：数据库字段 <= 入参
    public static final String NULL_QUERY = "IsNull";// 对应数据库的is null或is not null语句。true: xxx is null, false: xxx is not null
    public static final String ORS_QUERY = "ors";// 对应数据库的 a = xx or b=yy
    public static final String IN_JSON_QUERY = "InJson";// keyInJsonField=value对应数据库JSON字段查询：JSON_CONTAINS(field, 'value', 'key')
    public static final String CONTAINS_QUERY = "Contains";// fieldContains=value对应数据库JSON字段查询：JSON_CONTAINS(field, 'value') = 1，JSON格式：[v1, v2]
    public static final String CONTAINS_ALL_QUERY = "ContainsAll";// fieldContainsAll=[v1, v2]对应数据库JSON字段查询：JSON_CONTAINS(field, 'v1') = 1 and JSON_CONTAINS(field, 'v2') = 1
    public static final String NOT_CONTAINS_QUERY = "NotContains";// fieldContains=value对应数据库JSON字段查询：JSON_CONTAINS(field, 'value') = 0，JSON格式：[v1, v2]
    public static final String NOT_CONTAINS_ANY_QUERY = "NotContainsAny";// fieldContainsAll=[v1, v2]对应数据库JSON字段查询：JSON_CONTAINS(field, 'v1') = 0 and JSON_CONTAINS(field, 'v2') = 0
    // 不参与查询的保留字段，注意不要在表中添加这些字段
    public static final List<String> EXCLUDE_FIELDS = Arrays.asList("select", "groupBy", "having", "orderBys", "fields",
            "keyword", "ignoreTenantId", "fills", "ignoreCount");
    // select字段列表，多个以split分隔
    protected String select;
    // 分组字段列表
    protected String groupBy;
    // having过滤条件
    protected String having;
    // 排序字段列表，多个以split分隔：aField_DESC,bField_ASC
    protected String orderBys;
    // or查询
    protected Map<String, Object> ors;
    // 关键字字段列表，多个以split分隔，配合keyword使用，最终转换成ors查询
    protected String fields;
    // 关键字查询(like '%s%')，配合fields使用
    protected Object keyword;
    // 忽略租户ID查询，默认不忽略
    @ToString.Exclude
    @JsonIgnore
    private boolean ignoreTenantId = false;
    // 聚合参数集，多个以split分隔
    protected String fills;
    // 查询分隔符：select/keyword/orderBys/fields/fills/xxIn/xxNotIn，默认英文逗号,
    @ToString.Exclude
    @JsonIgnore
    protected String split = ",";
    // 页码
    private Integer current = 1;
    // 页容
    private Integer size = 20;

    public <Q extends Query<M>> Q select(String... columns) {
        if (columns != null) {
            this.setSelect(String.join(split, columns));
        }
        return (Q) this;
    }

    public <Q extends Query<M>> Q groupBy(String groupBy) {
        this.setGroupBy(groupBy);
        return (Q) this;
    }

    public <Q extends Query<M>> Q having(String having) {
        this.setHaving(having);
        return (Q) this;
    }

    public <Q extends Query<M>> Q current(Integer current) {
        this.setCurrent(current);
        return (Q) this;
    }

    public <Q extends Query<M>> Q size(Integer size) {
        this.setSize(size);
        return (Q) this;
    }

    public <Q extends Query<M>> Q orderBy(String... orderBys) {
        if (orderBys != null) {
            this.orderBys = String.join(split, orderBys);
        }
        return (Q) this;
    }

    // or查询
    public <Q extends Query<M>> Q ors(Object... ors) {
        if (ors != null) {
            if (ors.length % 2 != 0) {
                throw new IllegalArgumentException("ors.length must not be singular");
            }
            Map<String, Object> orsMap = new HashMap<>();
            for (int i = 0; i < ors.length - 1; i += 2) {
                orsMap.put((String) ors[i], ors[i + 1]);
            }
            this.setOrs(orsMap);
        }
        return (Q) this;
    }

    public <Q extends Query<M>> Q keyword(String fields, Object keyword) {
        this.setFields(fields);
        this.setKeyword(keyword);
        return (Q) this;
    }

    // 忽略租户ID查询
    public <Q extends Query<M>> Q ignoreTenantId() {
        this.ignoreTenantId = true;
        return (Q) this;
    }

    // 忽略分页查询
    public <Q extends Query<M>> Q ignorePage() {
        this.setSize(-1);
        return (Q) this;
    }

    // 查询前置方法
    public void before() {
    }

    // 导出
    public void export() {
    }

    // 聚合哪些数据
    public <Q extends Query<M>> Q fills(String... fills) {
        if (fills != null) {
            if (this.fills != null) {
                this.setFills(this.fills + split + String.join(split, fills));
            } else {
                this.setFills(String.join(split, fills));
            }
        }
        return (Q) this;
    }

    // 是否聚合
    public Boolean isFill(String fill) {
        if (this.fills == null || this.fills.isEmpty()) {
            return false;
        }
        List<String> fills = Arrays.asList(this.fills.split(split));
        return fills.contains(fill);
    }

    // 当传入某聚合条件成立，执行聚合
    public <Q extends Query<M>> void when(String fill, Consumer<Q> consumer) {
        if (isFill(fill)) {
            consumer.accept((Q) this);
        }
    }

    public List<Map<String, Object>> maps() {
        this.before();
        return SpringContext.getBean(BaseRepository.class).maps(this);
    }

    public Map<String, Object> map() {
        List<Map<String, Object>> result = maps();
        if (result == null || result.isEmpty()) return Collections.emptyMap();
        return result.get(0);
    }

    public M one() {
        this.before();
        M model = (M) SpringContext.getBean(BaseRepository.class).one(this);
        if (model != null) {
            fill(model);
        }
        return model;
    }

    public M one(String ifNull, Object... params) {
        M one = one();
        if (one == null) {
            log.warn("{}: {}", ifNull, JsonKit.toJson(this));
            throw new ServiceException(ifNull, params);
        }
        return one;
    }

    public M first() {
        this.before();
        M model = (M) SpringContext.getBean(BaseRepository.class).first(this);
        if (model != null) {
            fill(model);
        }
        return model;
    }

    public M first(String ifNull, Object... params) {
        M first = first();
        if (first == null) {
            log.warn("{}: {}", ifNull, JsonKit.toJson(this));
            throw new ServiceException(ifNull, params);
        }
        return first;
    }

    public Long count() {
        this.before();
        return SpringContext.getBean(BaseRepository.class).count(this);
    }

    public boolean exist() {
        this.before();
        return SpringContext.getBean(BaseRepository.class).exist(this);
    }

    public void exist(String ifNotExist, Object... params) {
        if (!exist()) {
            log.warn("{}: {}", ifNotExist, JsonKit.toJson(this));
            throw new ServiceException(ifNotExist, params);
        }
    }

    public boolean notExist() {
        return !this.exist();
    }

    public void notExist(String ifExist, Object... params) {
        if (exist()) {
            log.warn("{}: {}", ifExist, JsonKit.toJson(this));
            throw new ServiceException(ifExist, params);
        }
    }

    public Page<M> page() {
        this.before();
        Page<M> page = SpringContext.getBean(BaseRepository.class).page(this);
        if (page.getRecords() != null && !page.getRecords().isEmpty()) {
            fill(page.getRecords());
        }
        return page;
    }

    public Page<M> page(String ifEmpty, Object... params) {
        Page<M> page = page();
        if (page == null || page.isEmpty()) {
            log.warn("{}: {}", ifEmpty, JsonKit.toJson(this));
            throw new ServiceException(ifEmpty, params);
        }
        return page;
    }

    public List<M> list() {
        this.before();
        List<M> models = SpringContext.getBean(BaseRepository.class).list(this);
        if (models != null && !models.isEmpty()) {
            fill(models);
        }
        return models;
    }

    public List<M> list(String ifEmpty, Object... params) {
        List<M> list = list();
        if (list == null || list.isEmpty()) {
            log.warn("{}: {}", ifEmpty, JsonKit.toJson(this));
            throw new ServiceException(ifEmpty, params);
        }
        return list;
    }

    public boolean delete() {
        return SpringContext.getBean(BaseRepository.class).delete(this);
    }

    public void fill(M model) {
        fill(Collections.singletonList(model));
    }

    public void fill(List<M> models) {

    }

    // 通过Query直接调用DAO层方法分页查询
    public <R, I, Q extends Query<?>> Page<R> page(BiFunction<I, Q, List<R>> daoMethod) {
        return page(() -> daoMethod.apply(RepositoryContext.dao(this), (Q) this));
    }

    // 通过Query自定义List转分页查询
    public <R, S> Page<R> page(Supplier<List<S>> method) {
        return SpringContext.getBean(BaseRepository.class).page(this, method);
    }

    public <R, I, Q> R invoke(BiFunction<I, Q, R> daoMethod) {
        return daoMethod.apply(RepositoryContext.dao(this), (Q) this);
    }

    public <R> R invoke(Supplier<R> method) {
        return method.get();
    }
}