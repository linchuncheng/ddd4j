package com.ddd4j.cloud.excel.aop;

import com.ddd4j.cloud.excel.annotation.ExportExcel;
import com.ddd4j.cloud.excel.processor.NameProcessor;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 动态代理名称切面类
 *
 * @author zhouhengzhe
 * @version 1.0
 * @date 2023/6/25 16:37
 */
@Aspect
@RequiredArgsConstructor
public class DynamicNameAspect {

    public static final String EXCEL_NAME_KEY = "__EXCEL_NAME_KEY__";

    private final NameProcessor nameProcessor;

    @Before("@annotation(excel)")
    public void around(JoinPoint jp, ExportExcel excel) {
        //获取方法签名
        MethodSignature ms = (MethodSignature) jp.getSignature();

        String name = excel.name();
        // 当配置的 excel 名称为空时，取当前时间
        if (!StringUtils.hasText(name)) {
            name = LocalDateTime.now().toString();
        } else {
            name = nameProcessor.doDetermineName(jp.getArgs(), ms.getMethod(), excel.name());
        }

        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        Objects.requireNonNull(requestAttributes).setAttribute(EXCEL_NAME_KEY, name, RequestAttributes.SCOPE_REQUEST);
    }
}
