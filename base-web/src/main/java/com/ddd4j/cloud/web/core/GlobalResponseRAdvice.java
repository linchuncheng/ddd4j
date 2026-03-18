package com.ddd4j.cloud.web.core;

import com.ddd4j.cloud.core.contract.IR;
import com.ddd4j.cloud.core.contract.Model;
import com.ddd4j.cloud.core.contract.R;
import com.ddd4j.cloud.core.contract.annotation.RawResponse;
import com.ddd4j.cloud.core.kit.JsonKit;
import com.ddd4j.cloud.web.config.BaseWebProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 解决因为全局异常返回导致的Knife4j文档不显示的问题
 *
 * @author zhz
 */
@RestControllerAdvice(basePackages = {"com.ddd4j.cloud"}, annotations = {RestController.class})
public class GlobalResponseRAdvice implements ResponseBodyAdvice<Object> {
    @Autowired
    BaseWebProperties baseWebProperties;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> aClass) {
        return baseWebProperties.getMvc().getEnableRResponse() && !returnType.hasMethodAnnotation(RawResponse.class);
    }

    @Override
    public Object beforeBodyWrite(Object data, MethodParameter returnType, MediaType mediaType, Class<? extends HttpMessageConverter<?>> aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        if (data == null || returnType.getParameterType().isAssignableFrom(void.class)) {
            return R.ok();
        }
        if (IR.class.isAssignableFrom(returnType.getParameterType())) {
            return data;
        }
        Object r = R.ok(data);
        if (!Model.class.isAssignableFrom(returnType.getParameterType()) && returnType.getGenericParameterType().equals(String.class)) {
            //将数据包装在R对象里后转换为json串进行返回
            r = JsonKit.toJson(r);
        }
        //否则直接包装成R对象返回
        return r;
    }
}
