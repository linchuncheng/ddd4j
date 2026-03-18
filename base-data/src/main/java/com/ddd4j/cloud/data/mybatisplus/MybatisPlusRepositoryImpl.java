package com.ddd4j.cloud.data.mybatisplus;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.ddd4j.cloud.core.config.BaseCoreProperties;
import com.ddd4j.cloud.core.context.RepositoryContext;
import com.ddd4j.cloud.core.context.SpringContext;
import com.ddd4j.cloud.core.context.ThreadContext;
import com.ddd4j.cloud.core.contract.BaseRepository;
import com.ddd4j.cloud.core.contract.Model;
import com.ddd4j.cloud.core.contract.Page;
import com.ddd4j.cloud.core.contract.Query;
import com.ddd4j.cloud.core.contract.constant.ContextConstants;
import com.ddd4j.cloud.core.kit.BeanKit;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.ibatis.binding.MapperMethod.ParamMap;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionHolder;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "### BASE-DATA : MybatisPlusRepositoryImpl ###")
public class MybatisPlusRepositoryImpl implements BaseRepository<Model, Query<Model>> {
    static Map<Class<? extends Model>, TableScheme> TABLE_SCHEME_MAP = new ConcurrentHashMap<>();

    public TableScheme tableScheme(Class<? extends Model> modelClass) {
        return TABLE_SCHEME_MAP.computeIfAbsent(modelClass,
                clazz -> TableScheme.build(RepositoryContext.entityClass(clazz)));
    }

    public BaseMapper dao(Class<?> mappingClass) {
        return RepositoryContext.dao(mappingClass);
    }

    @Override
    public boolean save(Model model) {
        Object entity = BeanKit.copy(model, RepositoryContext.entityClass(model.getClass()));
        insertFill((Class<Model>) model.getClass(), entity);
        boolean result = SqlHelper.retBool(dao(model.getClass()).insert(entity));
        if (result) {
            BeanKit.copy(entity, model);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public boolean save(List<Model> models) {
        if (models == null || models.isEmpty()) {
            return false;
        }
        List<?> entities = BeanKit.copy(models, RepositoryContext.entityClass(models.get(0).getClass()));
        boolean result = insertBatch((Class<Model>) models.get(0).getClass(), entities);
        if (result) {
            BeanKit.copy(entities, models);
        }
        return result;
    }

    @Override
    public boolean update(Model model) {
        Object entity = BeanKit.copy(model, RepositoryContext.entityClass(model.getClass()));
        updateFill((Class<Model>) model.getClass(), entity);
        boolean result = SqlHelper.retBool(dao(model.getClass()).updateById(entity));
        if (result) {
            Object updated = dao(model.getClass()).selectById(TableScheme.findFieldValue(entity, tableScheme((Class<Model>) model.getClass()).getId()));
            BeanKit.copy(updated, model);
        }
        return result;
    }

    @Override
    public boolean update(Model model, Query query) {
        query.setOrderBys(null);
        Object entity = BeanKit.copy(model, RepositoryContext.entityClass(model.getClass()));
        updateFill((Class<Model>) model.getClass(), entity);
        boolean result = SqlHelper.retBool(dao(model.getClass()).update(entity, this.getBaseWrapper(query)));
        if (result) {
            TableScheme tableScheme = tableScheme((Class<Model>) model.getClass());
            if (tableScheme.getId() != null) {
                Serializable id = TableScheme.findFieldValue(entity, tableScheme.getId());
                if (id != null) {
                    Object updated = dao(model.getClass()).selectById(id);
                    BeanKit.copy(updated, model);
                }
            }
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public boolean update(List<Model> models) {
        if (models == null || models.isEmpty()) {
            return false;
        }
        Class<?> entityClass = RepositoryContext.entityClass(models.get(0).getClass());
        List<?> entities = BeanKit.copy(models, entityClass);
        boolean result = updateBatch((Class<Model>) models.get(0).getClass(), entities, 100);
        if (result) {
            BeanKit.copy(entities, models);
        }
        return result;
    }

    @Override
    public boolean upsert(Model model) {
        QueryWrapper<Query<Model>> defaultWrapper = getDefaultWrapper(null, false);
        TableScheme tableScheme = tableScheme((Class<Model>) model.getClass());
        defaultWrapper.eq(TableScheme.getColumn(tableScheme.getId()), TableScheme.findFieldValue(model, tableScheme.getId()));
        Long exist = dao(model.getClass()).selectCount(defaultWrapper);
        if (Objects.equals(exist, 0L)) {
            return save(model);
        } else {
            return update(model);
        }
    }

    @Override
    public boolean delete(Class<Model> modelClass, Serializable id) {
        return id != null && SqlHelper.retBool(dao(modelClass.getClass()).deleteById(id));
    }

    @Override
    public boolean delete(Model model) {
        Class<?> entityClass = RepositoryContext.entityClass(model.getClass());
        Object entity = BeanKit.copy(model, entityClass);
        TableScheme tableScheme = tableScheme((Class<Model>) model.getClass());
        if (tableScheme.getId() != null) {
            Serializable id = TableScheme.findFieldValue(entity, tableScheme.getId());
            if (id != null) {
                return delete((Class<Model>) model.getClass(), id);
            }
        }
        return false;
    }

    @Override
    public boolean delete(Query query) {
        return SqlHelper.retBool(dao(query.getClass()).delete(this.getBaseWrapper(query)));
    }

    @Override
    public boolean delete(Class<Model> modelClass, List<? extends Serializable> ids) {
        if (ids == null || ids.isEmpty()) {
            log.warn("batch function query is empty or null");
            return false;
        }
        if (ids.size() >= 100) {
            throw new IllegalArgumentException("当前批量删除的ID不能大于100");
        }
        return SqlHelper.retBool(dao(modelClass).deleteBatchIds(ids));
    }

    @Override
    public List<Map<String, Object>> maps(Query query) {
        return (List<Map<String, Object>>) dao(query.getClass()).selectMaps(this.getBaseWrapper(query));
    }

    @Override
    public List<Model> list(Class<Model> modelClass, List<? extends Serializable> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("Error: ids must not be empty");
        }
        if (ids.size() >= 100) {
            throw new IllegalArgumentException("当前批量查询的ID不能大于100");
        }
        List<?> entities = dao(modelClass).selectBatchIds(ids);
        return BeanKit.copy(entities, modelClass);
    }

    @Override
    public List<Model> list(Query query) {
        List<?> entities = dao(query.getClass()).selectList(getBaseWrapper(query));
        return BeanKit.copy(entities, RepositoryContext.modelClass(query.getClass()));
    }

    @Override
    public Model first(Query query) {
        Object entity = dao(query.getClass()).selectOne(getBaseWrapper(query).last("limit 1"));
        return BeanKit.copy(entity, RepositoryContext.modelClass(query.getClass()));
    }

    @Override
    public Model one(Query query) {
        Object entity = dao(query.getClass()).selectOne(this.getBaseWrapper(query));
        return BeanKit.copy(entity, RepositoryContext.modelClass(query.getClass()));
    }

    @Override
    public Model get(Class<Model> modelClass, Serializable id) {
        Object entity = dao(modelClass).selectById(id);
        return BeanKit.copy(entity, modelClass);
    }

    @Override
    public Long count(Query query) {
        return dao(query.getClass()).selectCount(this.getBaseWrapper(query));
    }

    @Override
    public boolean exist(Query query) {
        return SqlHelper.retBool(count(query));
    }

    @Override
    public Page<Model> page(Query query) {
        try {
            PageHelper.startPage(query.getCurrent(), query.getSize());
            List<?> entities = dao(query.getClass()).selectList(this.getBaseWrapper(query));
            PageInfo<?> entityPage = PageInfo.of(entities);
            Class<Model> modelClass = RepositoryContext.modelClass(query.getClass());
            List<Model> models = BeanKit.copy(entities, modelClass);
            return Page.of(models, entityPage.getTotal(), query.getCurrent(), query.getSize());
        } finally {
            PageHelper.clearPage();
        }
    }

    public <R, Q extends Query<?>> Page<R> page(Q query, Supplier<List<R>> method) {
        try {
            PageHelper.startPage(query.getCurrent(), query.getSize());
            List<R> results = method.get();
            PageInfo<R> resultPage = PageInfo.of(results);
            return Page.of(results, resultPage.getTotal(), query.getCurrent(), query.getSize());
        } finally {
            PageHelper.clearPage();
        }
    }

    protected QueryWrapper<Query<Model>> getDefaultWrapper(Query<Model> query, boolean ignoreTenantId) {
        QueryWrapper<Query<Model>> wrapper = new QueryWrapper<>();
        if (!ignoreTenantId) {
            String tenantId = ThreadContext.get(ContextConstants.TENANT_ID);
            TableScheme tableScheme = tableScheme(RepositoryContext.modelClass(query.getClass()));
            if (tableScheme.getTenantId() != null && tenantId != null) {
                wrapper.eq(TableScheme.getColumn(tableScheme.getTenantId()), tenantId);
            }
        }
        return wrapper;
    }

    protected QueryWrapper<Query> getBaseWrapper(Query query) {
        QueryWrapper<Query> wrapper = getDefaultWrapper(query, query.isIgnoreTenantId());
        this.select(query, wrapper).where(query, wrapper).groupBy(query, wrapper).having(query, wrapper).orderBy(query, wrapper);
        return wrapper;
    }

    protected static SqlSessionFactory sqlSessionFactory(Class<?> entityClass) {
        return GlobalConfigUtils.currentSessionFactory(entityClass);
    }

    protected SqlSession sqlSession(Class<?> entityClass) {
        return SqlSessionUtils.getSqlSession(sqlSessionFactory(entityClass));
    }

    protected static SqlSession sqlSessionBatch(Class<?> entityClass) {
        return SqlHelper.sqlSessionBatch(entityClass);
    }

    protected static void closeSqlSession(Class<?> entityClass, SqlSession sqlSession) {
        SqlSessionUtils.closeSqlSession(sqlSession, GlobalConfigUtils.currentSessionFactory(entityClass));
    }

    protected static void clearSqlSessionCache(Class<?> entityClass) {
        SqlSessionHolder sqlSessionHolder = (SqlSessionHolder) TransactionSynchronizationManager.getResource(sqlSessionFactory(entityClass));
        boolean transaction = TransactionSynchronizationManager.isSynchronizationActive();
        if (sqlSessionHolder != null) {
            SqlSession sqlSession = sqlSessionHolder.getSqlSession();
            sqlSession.commit(!transaction);
        }
    }

    protected static String sqlStatement(Class<?> entityClass, SqlMethod sqlMethod) {
        return SqlHelper.table(entityClass).getSqlStatement(sqlMethod.getMethod());
    }

    protected boolean insertBatch(Class<Model> modelClass, List<?> entities) {
        Class<?> entityClass = entities.get(0).getClass();
        clearSqlSessionCache(entityClass);
        SqlSession batchSqlSession = sqlSessionBatch(entityClass);
        int i = 0;
        String sqlStatement = sqlStatement(entityClass, SqlMethod.INSERT_ONE);

        try {
            for (Iterator<?> iterator = entities.iterator(); iterator.hasNext(); ++i) {
                Object entity = iterator.next();
                insertFill(modelClass, entity);
                batchSqlSession.insert(sqlStatement, entity);
                if (i >= 1 && i % 100 == 0) {
                    batchSqlSession.flushStatements();
                }
            }

            batchSqlSession.flushStatements();
            return true;
        } finally {
            closeSqlSession(entityClass, batchSqlSession);
        }
    }

    public boolean updateBatch(Class<Model> modelClass, List<?> entities, int batchSize) {
        if (batchSize < 1) {
            return false;
        }
        if (entities == null || entities.isEmpty()) {
            log.warn("batch function query is empty or null");
            return false;
        } else {
            Class<?> entityClass = entities.get(0).getClass();
            clearSqlSessionCache(entityClass);
            SqlSession batchSqlSession = sqlSessionBatch(entityClass);
            int i = 0;
            String sqlStatement = sqlStatement(entityClass, SqlMethod.UPDATE_BY_ID);

            try {
                for (Iterator<?> iterator = entities.iterator(); iterator.hasNext(); ++i) {
                    Object entity = iterator.next();
                    ParamMap query = new ParamMap<>();
                    query.put("et", entity);
                    updateFill(modelClass, entity);
                    batchSqlSession.update(sqlStatement, query);
                    if (i >= 1 && i % batchSize == 0) {
                        batchSqlSession.flushStatements();
                    }
                }
                batchSqlSession.flushStatements();
                return true;
            } finally {
                closeSqlSession(entityClass, batchSqlSession);
            }
        }
    }

    protected boolean isCollectionType(Object o) {
        return o instanceof Collection;
    }

    protected boolean isDateType(Object o) {
        return o instanceof Date || o instanceof LocalDateTime || o instanceof LocalDate || o instanceof LocalTime;
    }

    private boolean isStringNotBlank(Object value) {
        return value != null && (!(value instanceof CharSequence) || ((CharSequence) value).length() != 0);
    }

    private String getFieldName(String fieldName, String queryAction) {
        String replaceLast = replaceLast(fieldName, queryAction, "");
        return replaceLast != null && !replaceLast.isEmpty() ? replaceLast.toLowerCase() : replaceLast;
    }

    protected void getColumnByField(TableScheme tableScheme, String fieldName, Consumer<String> action) {
        if (tableScheme.containsField(fieldName)) {
            action.accept(tableScheme.getField(fieldName));
        }
    }

    private MybatisPlusRepositoryImpl select(Query query, QueryWrapper<Query> baseWrapper) {
        if (query.getSelect() != null && !query.getSelect().isEmpty()) {
            baseWrapper.select(query.getSelect().split(query.getSplit()));
        }
        return this;
    }

    private MybatisPlusRepositoryImpl where(Query query, QueryWrapper<Query> baseWrapper) {
        // 关键字查询
        if (query.getKeyword() != null && query.getFields() != null && !query.getFields().isEmpty()) {
            for (String field : query.getFields().split(query.getSplit())) {
                if (!field.isEmpty()) {
                    if (query.getOrs() == null) {
                        query.setOrs(new LinkedHashMap<>());
                    }
                    query.getOrs().putIfAbsent(field, query.getKeyword());
                }
            }
        }
        Map<String, Object> mapParams = BeanKit.toMapClean(query);
        if (mapParams != null) {
            mapParams.forEach((k, v) -> this.setCondition(query, baseWrapper, k, v));
        }
        return this;
    }

    private QueryWrapper<Query> setCondition(Query query, QueryWrapper<Query> wrapper, String key, Object value) {
        if (value == null || Query.EXCLUDE_FIELDS.contains(key)) {
            return wrapper;
        }
        Class<Model> modelClass = RepositoryContext.modelClass(query.getClass());
        TableScheme tableScheme = tableScheme(modelClass);
        // 处理or查询
        if (Objects.equals(key, Query.ORS_QUERY) && value instanceof Map) {
            Map<String, Object> ors = (Map<String, Object>) value;
            if (!ors.isEmpty()) {
                wrapper.and(w -> ors.forEach((k, v) -> this.setCondition(query, w, k, v).or()));
            }
            return wrapper;
        }
        if (key.endsWith(Query.START_QUERY) && this.isDateType(value)) {
            this.getColumnByField(tableScheme, this.getFieldName(key, Query.START_QUERY), (p) -> wrapper.ge(p, value));
        } else if (key.endsWith(Query.END_QUERY) && this.isDateType(value)) {
            this.getColumnByField(tableScheme, this.getFieldName(key, Query.END_QUERY), (p) -> wrapper.le(p, value));
        } else if (key.endsWith(Query.GE_QUERY)) {
            this.getColumnByField(tableScheme, this.getFieldName(key, Query.GE_QUERY), (p) -> wrapper.ge(p, value));
        } else if (key.endsWith(Query.LE_QUERY)) {
            this.getColumnByField(tableScheme, this.getFieldName(key, Query.LE_QUERY), (p) -> wrapper.le(p, value));
        } else if (key.endsWith(Query.GT_QUERY)) {
            this.getColumnByField(tableScheme, this.getFieldName(key, Query.GT_QUERY), (p) -> wrapper.gt(p, value));
        } else if (key.endsWith(Query.LT_QUERY)) {
            this.getColumnByField(tableScheme, this.getFieldName(key, Query.LT_QUERY), (p) -> wrapper.lt(p, value));
        } else if (key.endsWith(Query.NOT_IN_QUERY) && this.isCollectionType(value) && !((Collection<?>) value).isEmpty()) {
            List<?> list = ((Collection<?>) value).stream().distinct().collect(Collectors.toList());
            this.getColumnByField(tableScheme, this.getFieldName(key, Query.NOT_IN_QUERY), (p) -> wrapper.notIn(p, list));
        } else if (key.endsWith(Query.NOT_IN_QUERY) && value instanceof String && !((String) value).isEmpty()) {
            String[] notIns = ((String) value).split(query.getSplit());
            List<String> list = Arrays.stream(notIns).distinct().filter(s -> !s.isEmpty()).collect(Collectors.toList());
            this.getColumnByField(tableScheme, this.getFieldName(key, Query.NOT_IN_QUERY), (p) -> wrapper.notIn(p, list));
        } else if (key.endsWith(Query.IN_QUERY) && this.isCollectionType(value) && !((Collection<?>) value).isEmpty()) {
            List<?> list = ((Collection<?>) value).stream().distinct().collect(Collectors.toList());
            this.getColumnByField(tableScheme, this.getFieldName(key, Query.IN_QUERY), (p) -> wrapper.in(p, list));
        } else if (key.endsWith(Query.IN_QUERY) && value instanceof String && !((String) value).isEmpty()) {
            String[] ins = ((String) value).split(query.getSplit());
            List<String> list = Arrays.stream(ins).distinct().filter(s -> !s.isEmpty()).collect(Collectors.toList());
            this.getColumnByField(tableScheme, this.getFieldName(key, Query.IN_QUERY), (p) -> wrapper.in(p, list));
        } else if (key.endsWith(Query.LIKE_QUERY) && value instanceof CharSequence) {
            if (this.isStringNotBlank(value)) {
                this.getColumnByField(tableScheme, this.getFieldName(key, Query.LIKE_QUERY), (p) -> wrapper.like(p, value));
            }
        } else if (key.endsWith(Query.LIKE_LEFT_QUERY) && value instanceof CharSequence) {
            if (this.isStringNotBlank(value)) {
                this.getColumnByField(tableScheme, this.getFieldName(key, Query.LIKE_LEFT_QUERY), (p) -> wrapper.likeLeft(p, value));
            }
        } else if (key.endsWith(Query.LIKE_RIGHT_QUERY) && value instanceof CharSequence) {
            if (this.isStringNotBlank(value)) {
                this.getColumnByField(tableScheme, this.getFieldName(key, Query.LIKE_RIGHT_QUERY), (p) -> wrapper.likeRight(p, value));
            }
        } else if (key.endsWith(Query.NOT_LIKE_QUERY) && value instanceof CharSequence) {
            if (this.isStringNotBlank(value)) {
                this.getColumnByField(tableScheme, this.getFieldName(key, Query.NOT_LIKE_QUERY), (p) -> wrapper.notLike(p, value));
            }
        } else if (key.endsWith(Query.NOT_QUERY)) {
            this.getColumnByField(tableScheme, this.getFieldName(key, Query.NOT_QUERY), (p) -> wrapper.ne(p, value));
        } else if (key.contains(Query.IN_JSON_QUERY)) {
            // 支持搜索JSON里某个字段值，如搜索Json字段extras里的key: value，查询入参keyInJsonExtras=value
            String[] valueJson = key.split(Query.IN_JSON_QUERY);
            String jsonKey = valueJson[0];
            String field = tableScheme.getField(valueJson[1].toLowerCase());
            String formattedValue = formatValue(value);
            if (field != null && formattedValue != null) {
                // 使用 JSON_CONTAINS 构建查询条件
                wrapper.apply(String.format("JSON_CONTAINS(%s, '%s', '$%s')", field, formattedValue, jsonKey));
            }
        } else if (key.endsWith(Query.CONTAINS_QUERY)) {
            // 支持搜索JsonArray里某个字段值，如搜索JsonArray字段labels里有没有label1，查询入参labelsContains=label1
            String field = tableScheme.getField(this.getFieldName(key, Query.CONTAINS_QUERY));
            String formattedValue = formatValue(value);
            if (field != null && formattedValue != null) {
                wrapper.apply(String.format("JSON_CONTAINS(%s, '%s') = 1", field, formattedValue));
            }
        } else if (key.endsWith(Query.NOT_CONTAINS_QUERY)) {
            // 支持搜索JsonArray里不包含某个字段值，如搜索JsonArray字段labels里有没有label1，查询入参labelsNotContains=label1
            String field = tableScheme.getField(this.getFieldName(key, Query.NOT_CONTAINS_QUERY));
            String formattedValue = formatValue(value);
            if (field != null && formattedValue != null) {
                wrapper.apply(String.format("JSON_CONTAINS(%s, '%s') = 0", field, formattedValue));
            }
        } else if (key.endsWith(Query.CONTAINS_ALL_QUERY) && this.isCollectionType(value) && !((Collection<?>) value).isEmpty()) {
            List<?> elements = ((Collection<?>) value).stream().distinct().collect(Collectors.toList());
            String field = tableScheme.getField(this.getFieldName(key, Query.CONTAINS_ALL_QUERY));
            if (field != null) {
                for (Object element : elements) {
                    String formattedValue = formatValue(element);
                    if (formattedValue != null) {
                        wrapper.apply(String.format("JSON_CONTAINS(%s, '%s') = 1", field, formattedValue));
                    }
                }
            }
        } else if (key.endsWith(Query.NOT_CONTAINS_ANY_QUERY) && this.isCollectionType(value) && !((Collection<?>) value).isEmpty()) {
            List<?> elements = ((Collection<?>) value).stream().distinct().collect(Collectors.toList());
            String field = tableScheme.getField(this.getFieldName(key, Query.NOT_CONTAINS_ANY_QUERY));
            if (field != null) {
                for (Object element : elements) {
                    String formattedValue = formatValue(element);
                    if (formattedValue != null) {
                        wrapper.apply(String.format("JSON_CONTAINS(%s, '%s') = 0", field, formattedValue));
                    }
                }
            }
        } else if (key.endsWith(Query.NULL_QUERY)) {
            if (Objects.equals(Boolean.TRUE, value)) {
                this.getColumnByField(tableScheme, this.getFieldName(key, Query.NULL_QUERY), wrapper::isNull);
            } else if (Objects.equals(Boolean.FALSE, value)) {
                this.getColumnByField(tableScheme, this.getFieldName(key, Query.NULL_QUERY), wrapper::isNotNull);
            }
        } else {
            this.getColumnByField(tableScheme, key, (p) -> wrapper.eq(p, value));
        }
        return wrapper;
    }

    /**
     * 根据值的类型格式化为 JSON 字符串
     *
     * @param value 查询值
     * @return 格式化后的 JSON 字符串
     */
    private String formatValue(Object value) {
        BaseCoreProperties baseCoreProperties = SpringContext.getBean(BaseCoreProperties.class);
        if (value instanceof String) {
            // 字符串需要加引号，并进行转义防止 SQL 注入
            return "\"" + escapeJsonString((String) value) + "\"";
        } else if (value instanceof Number || value instanceof Boolean) {
            return value.toString(); // 数字和布尔值直接转换为字符串
        } else if (value instanceof LocalDateTime) {
            return "\"" + escapeJsonString(
                    DateTimeFormatter.ofPattern(baseCoreProperties.getDateTimePattern()).format((LocalDateTime) value))
                    + "\"";
        } else if (value instanceof LocalDate) {
            return "\""
                    + escapeJsonString(
                            DateTimeFormatter.ofPattern(baseCoreProperties.getDatePattern()).format((LocalDate) value))
                    + "\"";
        } else if (value instanceof LocalTime) {
            return "\""
                    + escapeJsonString(
                            DateTimeFormatter.ofPattern(baseCoreProperties.getTimePattern()).format((LocalTime) value))
                    + "\"";
        } else {
            // 如果值类型不支持，返回 null 并跳过查询
            return null;
        }
    }

    /**
     * 转义 JSON 字符串中的特殊字符，防止 SQL 注入
     *
     * @param value 原始字符串
     * @return 转义后的字符串
     */
    private String escapeJsonString(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        StringBuilder sb = new StringBuilder(value.length() + 16);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                default:
                    // 过滤控制字符，防止注入
                    if (c < ' ') {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    private MybatisPlusRepositoryImpl groupBy(Query query, QueryWrapper<Query> wrapper) {
        if (query != null && query.getGroupBy() != null && !query.getGroupBy().isEmpty()) {
            wrapper.groupBy(query.getGroupBy());
        }
        return this;
    }

    private MybatisPlusRepositoryImpl having(Query query, QueryWrapper<Query> wrapper) {
        if (query != null && query.getHaving() != null && !query.getHaving().isEmpty()) {
            wrapper.having(query.getHaving());
        }
        return this;
    }

    private MybatisPlusRepositoryImpl orderBy(Query query, QueryWrapper<Query> wrapper) {
        TableScheme tableScheme = tableScheme(RepositoryContext.modelClass(query.getClass()));
        if (query.getOrderBys() == null || query.getOrderBys().isEmpty()) {
            if (tableScheme.getDefaultOrderBy() != null && tableScheme.getDefaultOrderBy().length > 0) {
                // 设置默认排序
                query.setOrderBys(String.join(query.getSplit(), tableScheme.getDefaultOrderBy()));
            }
        }
        if (query.getOrderBys() != null) {
            String[] orderBys = query.getOrderBys().split(query.getSplit());
            for (String orderBy : orderBys) {
                if (orderBy != null && !orderBy.isEmpty()) {
                    // 统一转成下划线形式，传参可以是驼峰式，也可以是下划线
                    String column = TableScheme.toUnderline(orderBy.replace("_asc", "").replace("_ASC", "").replace("_desc", "").replace("_DESC", ""));
                    wrapper.orderBy(tableScheme.containsColumn(column), !orderBy.toLowerCase().endsWith("_desc"), column);
                }
            }
        }
        return this;
    }

    private void insertFill(Class<Model> modelClass, Object entity) {
        TableScheme tableScheme = tableScheme(modelClass);
        try {
            if (tableScheme.getTenantId() != null && ThreadContext.contains(ContextConstants.TENANT_ID)) {
                String tenantId = ThreadContext.get(ContextConstants.TENANT_ID);
                if (tableScheme.getTenantId().getType() == Long.class) {
                    tableScheme.getTenantId().set(entity, Long.valueOf(tenantId));
                } else if (tableScheme.getTenantId().getType() == Integer.class) {
                    tableScheme.getTenantId().set(entity, Integer.valueOf(tenantId));
                } else if (tableScheme.getTenantId().getType() == String.class) {
                    tableScheme.getTenantId().set(entity, tenantId);
                }
            }
            if (tableScheme.getTableLogic() != null) {
                String defaultValue = tableScheme.getTableLogic().getAnnotation(TableLogic.class).value();
                if (defaultValue == null || defaultValue.isEmpty()) {
                    if (tableScheme.getTableLogic().getType().equals(Boolean.class) || tableScheme.getTableLogic().getType().equals(boolean.class)) {
                        tableScheme.getTableLogic().set(entity, false);
                    } else if (tableScheme.getTableLogic().getType().equals(Integer.class) || tableScheme.getTableLogic().getType().equals(int.class)) {
                        tableScheme.getTableLogic().set(entity, 0);
                    }
                } else {
                    if (tableScheme.getTableLogic().getType().equals(Boolean.class) || tableScheme.getTableLogic().getType().equals(boolean.class)) {
                        tableScheme.getTableLogic().set(entity, defaultValue.equals("0"));
                    } else if (tableScheme.getTableLogic().getType().equals(Integer.class) || tableScheme.getTableLogic().getType().equals(int.class)) {
                        tableScheme.getTableLogic().set(entity, Integer.valueOf(defaultValue));
                    }
                }
            }
            for (Field onCreateField : tableScheme.getOnCreateFields()) {
                if (onCreateField.getType().equals(LocalDateTime.class) && onCreateField.get(entity) == null) {
                    onCreateField.set(entity, LocalDateTime.now());
                } else if (onCreateField.getType().equals(LocalDate.class) && onCreateField.get(entity) == null) {
                    onCreateField.set(entity, LocalDate.now());
                }
            }
        } catch (IllegalAccessException ignore) {
        }
    }

    private void updateFill(Class<Model> modelClass, Object entity) {
        TableScheme tableScheme = tableScheme(modelClass);
        try {
            for (Field onUpdateField : tableScheme.getOnUpdateFields()) {
                if (onUpdateField.getType().equals(LocalDateTime.class) && onUpdateField.get(entity) == null) {
                    onUpdateField.set(entity, LocalDateTime.now());
                } else if (onUpdateField.getType().equals(LocalDate.class) && onUpdateField.get(entity) == null) {
                    onUpdateField.set(entity, LocalDate.now());
                }
            }
        } catch (IllegalAccessException ignore) {
        }
    }

    private static String replaceLast(String raw, String match, String replace) {
        if (raw == null || raw.isEmpty() || null == replace) {
            //参数不合法，原样返回
            return raw;
        }
        StringBuilder sBuilder = new StringBuilder(raw);
        int lastIndexOf = sBuilder.lastIndexOf(match);
        if (-1 == lastIndexOf) {
            return raw;
        }

        return sBuilder.replace(lastIndexOf, lastIndexOf + match.length(), replace).toString();
    }

}