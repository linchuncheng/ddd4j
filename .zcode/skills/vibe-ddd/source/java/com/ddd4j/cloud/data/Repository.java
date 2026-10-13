package com.ddd4j.cloud.data;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ddd4j.cloud.contract.Page;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * 显式仓储：CRUD 的唯一入口，实现类为 {@link MybatisRepository}。
 * <p>
 * 方法语义一目了然，不依赖任何隐式上下文（租户除外，它是数据隔离的显式约定）。
 *
 * @author Jensen
 */
public interface Repository<M, Q extends Query> {

    boolean insert(M model);

    boolean insertBatch(Collection<M> models);

    boolean updateById(M model);

    // 按条件批量更新：model 的非 null 字段作为 SET 值
    boolean update(M model, Q query);

    boolean deleteById(Serializable id);

    boolean deleteByIds(Collection<? extends Serializable> ids);

    boolean delete(Q query);

    M get(Serializable id);

    // 条件查询单条，命中多行会抛出异常，适合唯一键查询
    M one(Q query);

    List<M> list(Q query);

    Page<M> page(Q query);

    long count(Q query);

    boolean exists(Q query);

    // 逃生口：Query 后缀约定覆盖不了的复杂条件，直接操作 Wrapper
    List<M> search(Consumer<QueryWrapper<M>> condition);
}
