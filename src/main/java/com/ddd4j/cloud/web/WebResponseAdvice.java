package com.ddd4j.cloud.web;

import com.ddd4j.cloud.contract.R;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 响应包装：Controller 返回非 R 的 JSON 响应自动包装为 R.ok(data)。
 * <p>
 * 只处理 Jackson 序列化的响应；String / byte[] 等走其它转换器的端点原样返回，
 * 标注 {@link RawResponse} 的方法跳过包装。
 *
 * @author Jensen
 */
@RestControllerAdvice
public class WebResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        if (returnType.hasMethodAnnotation(RawResponse.class)) {
            return false;
        }
        return MappingJackson2HttpMessageConverter.class.isAssignableFrom(converterType);
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        return body instanceof R ? body : R.ok(body);
    }
}
