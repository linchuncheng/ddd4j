package com.ddd4j.cloud.data;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.ddd4j.cloud.contract.Page;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 基于 MyBatis-Plus BaseMapper 的仓储实现，模型即实体（模型类直接标注 @TableName）。
 * <p>
 * 业务仓储继承并注入对应 Mapper 即可：
 * <pre>
 * \@Repository
 * public class UserRepository extends MybatisRepository&lt;User, UserQuery&gt; {
 *     public UserRepository(UserMapper mapper) {
 *         super(mapper);
 *     }
 * }
 * </pre>
 *
 * @author Jensen
 */
public abstract class MybatisRepository<M, Q extends Query> implements Repository<M, Q> {

    protected final BaseMapper<M> mapper;

    protected MybatisRepository(BaseMapper<M> mapper) {
        this.mapper = mapper;
    }

    @Override
    public boolean insert(M model) {
        AuditFiller.onInsert(model);
        return mapper.insert(model) > 0;
    }

    @Override
    public boolean insertBatch(Collection<M> models) {
        if (models == null || models.isEmpty()) {
            return false;
        }
        models.forEach(AuditFiller::onInsert);
        return Db.saveBatch(models);
    }

    @Override
    public boolean updateById(M model) {
        AuditFiller.onUpdate(model);
        return mapper.updateById(model) > 0;
    }

    @Override
    public boolean update(M model, Q query) {
        AuditFiller.onUpdate(model);
        return execute(query, () -> mapper.update(model, translate(query))) > 0;
    }

    @Override
    public boolean deleteById(Serializable id) {
        return mapper.deleteById(id) > 0;
    }

    @Override
    public boolean deleteByIds(Collection<? extends Serializable> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        return mapper.deleteByIds(ids) > 0;
    }

    @Override
    public boolean delete(Q query) {
        return execute(query, () -> mapper.delete(translate(query))) > 0;
    }

    @Override
    public M get(Serializable id) {
        return mapper.selectById(id);
    }

    @Override
    public M one(Q query) {
        return execute(query, () -> mapper.selectOne(translate(query)));
    }

    @Override
    public List<M> list(Q query) {
        return execute(query, () -> mapper.selectList(translate(query)));
    }

    @Override
    public Page<M> page(Q query) {
        if (!query.isPaged()) {
            List<M> records = list(query);
            return Page.of(records, records.size(), records.size(), 1);
        }
        IPage<M> result = execute(query, () -> {
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<M> mpPage =
                    new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(query.getCurrent(), query.getSize());
            return mapper.selectPage(mpPage, translate(query));
        });
        return Page.of(result.getRecords(), result.getTotal(), result.getSize(), result.getCurrent());
    }

    @Override
    public long count(Q query) {
        Long count = execute(query, () -> mapper.selectCount(translate(query)));
        return count == null ? 0 : count;
    }

    @Override
    public boolean exists(Q query) {
        return execute(query, () -> mapper.exists(translate(query)));
    }

    @Override
    public List<M> search(Consumer<QueryWrapper<M>> condition) {
        QueryWrapper<M> wrapper = new QueryWrapper<>();
        condition.accept(wrapper);
        return mapper.selectList(wrapper);
    }

    private QueryWrapper<M> translate(Q query) {
        return QueryTranslator.translate(query);
    }

    // Query 声明了忽略租户时，仅绕过本次执行的租户拦截
    private <T> T execute(Q query, Supplier<T> task) {
        return query.isIgnoreTenant() ? TenantManager.ignoring(task) : task.get();
    }
}
