package com.ddd4j.cloud.core.contract;

import com.ddd4j.cloud.core.context.RepositoryContext;
import com.ddd4j.cloud.core.context.SpringContext;
import com.ddd4j.cloud.core.kit.BizAssert;
import com.ddd4j.cloud.core.kit.JsonKit;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 模型基类，支持增删改的充血模型
 *
 * @author Jensen
 * @公众号 架构师修行录
 */
public abstract class Model implements Serializable {

    public boolean save() {
        return SpringContext.getBean(BaseRepository.class).save(this);
    }

    public void save(String ifFailed, Object... params) {
        BizAssert.isTrue(save(), ifFailed, params);
    }

    public boolean update() {
        return SpringContext.getBean(BaseRepository.class).update(this);
    }

    public void update(String ifFailed) {
        BizAssert.isTrue(update(), ifFailed);
    }

    public <Q extends Query> boolean update(Q query) {
        query.before();
        return SpringContext.getBean(BaseRepository.class).update(this, query);
    }

    public boolean upsert() {
        return SpringContext.getBean(BaseRepository.class).upsert(this);
    }

    public boolean delete() {
        return SpringContext.getBean(BaseRepository.class).delete(this);
    }

    public static <M extends Model> boolean save(List<M> models) {
        if (models == null || models.isEmpty()) {
            return false;
        }
        return SpringContext.getBean(BaseRepository.class).save(models);
    }

    public static <M extends Model> boolean update(List<M> models) {
        if (models == null || models.isEmpty()) {
            return false;
        }
        return SpringContext.getBean(BaseRepository.class).update(models);
    }

    // 通过Map参数转换为模型
    public static Model convert(String name, Map<String, Object> modelMap) {
        String json = JsonKit.toJson(modelMap);
        if (json == null || json.isEmpty()) return null;
        return JsonKit.toObject(json, RepositoryContext.modelClass(name));
    }

    // 批量通过Map参数转换为模型
    public static List<Model> convert(String name, List<Map<String, Object>> modelMaps) {
        if (modelMaps == null || modelMaps.isEmpty()) {
            return null;
        }
        return modelMaps.stream().map(modelMap -> convert(name, modelMap)).filter(Objects::nonNull).collect(Collectors.toList());
    }
}