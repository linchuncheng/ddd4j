package com.ddd4j.cloud.core.contract;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 基础仓库接口，不同ORM框架需要实现当前接口
 *
 * @author Jensen
 * @公众号 架构师修行录
 */
public interface BaseRepository<M extends Model, Q extends Query<M>> {

    /**
     * 保存
     */
    boolean save(M model);

    /**
     * 批量保存
     */
    boolean save(List<M> models);

    /**
     * 更新
     */
    boolean update(M model);

    /**
     * 自定义更新
     */
    boolean update(M model, Q query);

    /**
     * 批量保存
     */
    boolean update(List<M> models);

    /**
     * 保存或更新
     */
    boolean upsert(M model);

    // 根据模型删除
    boolean delete(M model);

    /**
     * 根据条件删除
     */
    boolean delete(Q query);

    /**
     * 根据ID删除
     */
    boolean delete(Class<M> modelClass, Serializable id);

    /**
     * 批量删除
     */
    boolean delete(Class<M> modelClass, List<? extends Serializable> ids);

    /**
     * 按ID列表查询列表
     */
    List<M> list(Class<M> modelClass, List<? extends Serializable> ids);

    /**
     * 按查询参数查询列表
     */
    List<M> list(Q query);

    /**
     * 按查询参数查询首行
     */
    M first(Q query);

    /**
     * 按查询参数查询一行
     */
    M one(Q query);

    /**
     * 按ID查询一行
     */
    M get(Class<Model> modelClass, Serializable id);

    /**
     * 按查询参数查询总行数
     */
    Long count(Q query);

    /**
     * 按查询参数、返回结果列、分组字段查询
     */
    List<Map<String, Object>> maps(Q query);

    /**
     * 按查询参数查询是否存在
     */
    boolean exist(Q query);

    /**
     * 按查询参数分页查询
     */
    Page<M> page(Q query);

    // 通过Query直接调用DAO层方法分页查询
    <R, Q extends Query<?>> Page<R> page(Q query, Supplier<List<R>> method);

}