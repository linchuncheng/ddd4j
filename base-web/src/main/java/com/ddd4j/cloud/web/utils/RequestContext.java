package com.ddd4j.cloud.web.utils;

import com.ddd4j.cloud.core.context.ThreadContext;
import com.ddd4j.cloud.core.contract.constant.ContextConstants;
import com.ddd4j.cloud.core.kit.JsonKit;
import com.ddd4j.cloud.kit.lang.StrKit;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingRequestWrapper;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.experimental.UtilityClass;

@UtilityClass
public class RequestContext {
    // 令牌前缀
    public static final String TOKEN_PREFIX = "Bearer ";
    // 令牌秘钥
    public final static String TOKEN_SECRET = "abcdefghijklmnopqrstuvwxyz";

    public HttpServletRequest get() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) return null;
        return attributes.getRequest();
    }

    public String getUrl() {
        HttpServletRequest request = get();
        if (request == null) return null;
        return request.getRequestURL().toString();
    }

    public String getUri() {
        HttpServletRequest request = get();
        if (request == null) return null;
        return request.getRequestURI();
    }

    public String getParams() {
        if (ThreadContext.contains(ContextConstants.REQUEST_PARAMS)) {
            return ThreadContext.get(ContextConstants.REQUEST_PARAMS);
        }
        HttpServletRequest request = get();
        if (request == null) return null;
        if (Objects.equals(request.getMethod(), "GET")) {
            if (request.getParameterMap() != null && !request.getParameterMap().isEmpty()) {
                return JsonKit.toJson(request.getParameterMap().entrySet());
            }
        } else {
            try {
                // 使用ContentCachingRequestWrapper包装原始请求
                ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
                return new String(requestWrapper.getContentAsByteArray());
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    // 获取请求BearerToken
    public String getBearerToken() {
        // 从header获取token标识
        String bearerToken = RequestContext.getHeader(ContextConstants.AUTHORIZATION);
        // 如果前端设置了令牌前缀，则裁剪掉前缀
        if (StrKit.isNotEmpty(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
            bearerToken = bearerToken.replaceFirst(TOKEN_PREFIX, "");
        }
        return bearerToken;
    }

    // 获取请求端类型
    public String getClientType() {
        // 从header获取token标识
        return RequestContext.getHeader(ContextConstants.CLIENT_TYPE);
    }

    public Map<String, String> getHeaders() {
        HttpServletRequest request = get();
        if (request == null) return Collections.emptyMap();
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            headers.put(headerName, headerValue);
        }
        return headers;
    }

    public String getHeader(String header) {
        HttpServletRequest request = get();
        if (request == null) return null;
        return request.getHeader(header);
    }

    public String getOrDefault(String header, String defaultValue) {
        String o = getHeader(header);
        if (o == null) return defaultValue;
        return o;
    }

    public Integer getOrDefault(String header, Integer defaultValue) {
        try {
            String headerValue = getHeader(header);
            if (headerValue == null) {
                return defaultValue;
            }
            return Integer.valueOf(headerValue);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) return null;
        return attributes.getRequest();
    }


    public HttpServletResponse getResponse() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) return null;
        return attributes.getResponse();
    }

    // 获取String参数
    public static String getParameter(String name) {
        return getRequest().getParameter(name);
    }

    /**
     * 获得所有请求参数
     *
     * @param request 请求对象{@link ServletRequest}
     * @return Map
     */
    public static Map<String, String[]> getParams(ServletRequest request) {
        final Map<String, String[]> map = request.getParameterMap();
        return Collections.unmodifiableMap(map);
    }


    /**
     * 获得所有请求参数
     *
     * @return Map
     */
    public static Map<String, String> getParamMap() {
        Map<String, String> params = new HashMap<>();
        for (Map.Entry<String, String[]> entry : getParams(getRequest()).entrySet()) {
            params.put(entry.getKey(), String.join(",", entry.getValue()));
        }
        return params;
    }


    /**
     * 内容编码
     *
     * @param str 内容
     * @return 编码后的内容
     */
    public static String urlEncode(String str) {
        try {
            return URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    /**
     * 内容解码
     *
     * @param str 内容
     * @return 解码后的内容
     */
    public static String urlDecode(String str) {
        try {
            return URLDecoder.decode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    // 获取Boolean参数
    public static Boolean getParameterToBool(String name) {
        return toBool(getRequest().getParameter(name), null);
    }

    /**
     * 转换为字符串<br>
     * 如果给定的值为null，或者转换失败，返回默认值<br>
     * 转换失败不会报错
     *
     * @param value        被转换的值
     * @param defaultValue 转换错误时的默认值
     * @return 结果
     */
    public static String toStr(Object value, String defaultValue) {
        if (null == value) {
            return defaultValue;
        }
        if (value instanceof String) {
            return (String) value;
        }
        return value.toString();
    }

    /**
     * 转换为boolean<br>
     * String支持的值为：true、false、yes、ok、no、1、0、是、否, 如果给定的值为空，或者转换失败，返回默认值<br>
     * 转换失败不会报错
     *
     * @param value        被转换的值
     * @param defaultValue 转换错误时的默认值
     * @return 结果
     */
    public static Boolean toBool(Object value, Boolean defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        String valueStr = toStr(value, null);
        if (valueStr == null || valueStr.isEmpty()) {
            return defaultValue;
        }
        valueStr = valueStr.trim().toLowerCase();
        switch (valueStr) {
            case "true":
            case "yes":
            case "ok":
            case "1":
            case "是":
                return true;
            case "false":
            case "no":
            case "0":
            case "否":
                return false;
            default:
                return defaultValue;
        }
    }

}
