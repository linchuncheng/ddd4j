package com.ddd4j.cloud.web.api;

import com.ddd4j.cloud.core.context.RepositoryContext;
import com.ddd4j.cloud.core.context.SpringContext;
import com.ddd4j.cloud.core.contract.BaseRepository;
import com.ddd4j.cloud.core.contract.Model;
import com.ddd4j.cloud.core.contract.Page;
import com.ddd4j.cloud.core.contract.Query;
import com.ddd4j.cloud.core.contract.exception.ServiceException;
import com.ddd4j.cloud.core.kit.BizAssert;
import com.ddd4j.cloud.core.kit.JsonKit;
import com.ddd4j.cloud.kit.lang.CollKit;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import lombok.SneakyThrows;

/**
 * 聚合控制器，实现该控制器的Controller，自带CRUD方法
 *
 * @author Jensen
 * @公众号 架构师修行录
 */
public interface AggregateController {
    // 公共详情
    @Operation(summary = "根据ID查询详情")
    @RequestMapping(value = "/{model}/detail/{id}", method = {RequestMethod.GET, RequestMethod.POST})
    default Model detail(@PathVariable("model") String model, @PathVariable("id") String id) {
        return SpringContext.getBean(BaseRepository.class).get(RepositoryContext.modelClass(model), id);
    }

    // 公共详情，按条件查第一条
    @Operation(summary = "根据条件查询详情")
    @RequestMapping(value = "/{model}/detail", method = {RequestMethod.GET, RequestMethod.POST})
    default Model detail(@PathVariable("model") String model, @RequestParam(required = false) Map<String, Object> params, @RequestBody(required = false) Map<String, Object> body) {
        return convert(model, body != null ? body : params).first();
    }

    // 公共分页，按条件查分页
    @Operation(summary = "查询分页")
    @RequestMapping(value = "/{model}/page", method = {RequestMethod.GET, RequestMethod.POST})
    default Page<?> page(@PathVariable("model") String model, @RequestParam(required = false) Map<String, Object> params, @RequestBody(required = false) Map<String, Object> body) {
        return convert(model, body != null ? body : params).page();
    }

    // 公共列表，按条件查列表
    @Operation(summary = "查询列表")
    @RequestMapping(value = "/{model}/list", method = {RequestMethod.GET, RequestMethod.POST})
    default List<?> list(@PathVariable("model") String model, @RequestParam(required = false) Map<String, Object> params, @RequestBody(required = false) Map<String, Object> body) {
        return convert(model, body != null ? body : params).list();
    }

    // 公共是否存在
    @Operation(summary = "查询是否存在")
    @RequestMapping(value = "/{model}/exist", method = {RequestMethod.GET, RequestMethod.POST})
    default Boolean exist(@PathVariable("model") String model, @RequestParam(required = false) Map<String, Object> params, @RequestBody(required = false) Map<String, Object> body) {
        return convert(model, body != null ? body : params).exist();
    }

    // 公共计数
    @Operation(summary = "查询计数")
    @RequestMapping(value = "/{model}/count", method = {RequestMethod.GET, RequestMethod.POST})
    default Long count(@PathVariable("model") String model, @RequestParam(required = false) Map<String, Object> params, @RequestBody(required = false) Map<String, Object> body) {
        return convert(model, body != null ? body : params).count();
    }

    // 公共条件删除
    @Operation(summary = "根据条件删除")
    @PostMapping("/{model}/remove")
    default void remove(@PathVariable("model") String model, @RequestBody Map<String, Object> body) {
        convert(model, body).delete();
    }

    // 公共条件导出
    @Operation(summary = "导出")
    @RequestMapping(value = "/{model}/export", method = {RequestMethod.GET, RequestMethod.POST})
    default void export(@PathVariable("model") String model, @RequestParam(required = false) Map<String, Object> params, @RequestBody(required = false) Map<String, Object> body) {
        convert(model, body != null ? body : params).export();
    }

    // 公共创建
    @Operation(summary = "创建")
    @PostMapping("/{model}/create")
    default Model create(@PathVariable("model") String modelName, @RequestBody Map<String, Object> model) {
        Model m = Model.convert(modelName, model);
        if (m == null) return null;
        m.save();
        return m;
    }

    // 公共批量创建
    @Operation(summary = "批量创建")
    @PostMapping("/{model}/createBatch")
    default void createBatch(@PathVariable("model") String model, @RequestBody List<Map<String, Object>> models) {
        Model.save(Model.convert(model, models));
    }

    // 公共修改
    @Operation(summary = "修改")
    @PostMapping("/{model}/modify")
    default void modify(@PathVariable("model") String modelName, @RequestBody Map<String, Object> model) {
        Model m = Model.convert(modelName, model);
        if (m != null) {
            m.update();
        }
    }

    // 公共批量修改
    @Operation(summary = "批量修改")
    @PostMapping("/{model}/modifyBatch")
    default void modifyBatch(@PathVariable("model") String model, @RequestBody List<Map<String, Object>> models) {
        List<Model> convertedModels = Model.convert(model, models);
        for (Model m : convertedModels) {
            m.update();
        }
    }

    // 公共保存（创建或修改）
    @Operation(summary = "保存")
    @PostMapping("/{model}/save")
    default Model save(@PathVariable("model") String modelNeme, @RequestBody Map<String, Object> model) {
        Model m = Model.convert(modelNeme, model);
        if (m == null) return null;
        m.upsert();
        return m;
    }

    // 公共批量删除
    @Operation(summary = "根据ID批量删除")
    @PostMapping("/{model}/remove/{ids}")
    default void remove(@PathVariable("model") String model, @PathVariable("ids") String ids) {
        BizAssert.hasValue(ids, "ID集不能为空");
        String[] idList = ids.split(",");
        List<String> list = CollKit.toList(idList);
        SpringContext.getBean(BaseRepository.class).delete(RepositoryContext.modelClass(model), list);
    }

    // 通过模型类找到查询类，并把Map参数转换为查询参数
    @SneakyThrows
    static Query<?> convert(String model, Map<String, Object> queryMap) {
        Class<? extends Query> queryClass = RepositoryContext.queryClass(RepositoryContext.modelClass(model));
        if (queryClass == null) {
            throw new ServiceException("Query not found");
        }
        Query<?> query = JsonKit.toObject(queryMap, queryClass);
        if (query == null) {
            query = queryClass.newInstance();
        }
        return query;
    }


}