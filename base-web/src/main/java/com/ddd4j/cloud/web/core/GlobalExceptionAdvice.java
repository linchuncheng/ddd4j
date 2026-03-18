package com.ddd4j.cloud.web.core;

import com.ddd4j.cloud.core.contract.R;
import com.ddd4j.cloud.core.contract.enums.ResultCode;
import com.ddd4j.cloud.core.contract.exception.ServiceException;
import com.ddd4j.cloud.core.contract.exception.ValidateException;
import com.ddd4j.cloud.core.kit.ExceptionKit;
import com.ddd4j.cloud.kit.lang.StrKit;
import com.ddd4j.cloud.web.utils.EscapeKit;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionAdvice {
    private static final Logger log = LoggerFactory.getLogger("### BASE-WEB : GlobalExceptionAdvice ###");

    // 请求方式不支持
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public R handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',不支持'{}'请求", requestURI, e.getMethod());
        return R.fail(e.getMessage());
    }


    // 请求路径中缺少必需的路径变量
    @ExceptionHandler(MissingPathVariableException.class)
    public R handleMissingPathVariableException(MissingPathVariableException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求路径中缺少必需的路径变量'{}',发生系统异常.", requestURI, e);
        return R.fail(String.format("请求路径中缺少必需的路径变量[%s]", e.getVariableName()));
    }

    // 请求参数类型不匹配
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public R handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String value = e.getValue() == null ? null : e.getValue().toString();
        if (StrKit.isNotEmpty(value)) {
            value = EscapeKit.clean(value);
        }
        log.error("请求参数类型不匹配'{}',发生系统异常.", requestURI, e);
        return R.fail(String.format("请求参数类型不匹配，参数[%s]要求类型为：'%s'，但输入值为：'%s'", e.getName(), e.getRequiredType().getName(), value));
    }

    @ExceptionHandler({BindException.class})
    public R<String> bindException(HttpServletRequest request, Model model, BindException e) {
        List<String> errList = e.getFieldErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.toList());
        log.error("请求参数校验失败：{} {}", errList, model);
        return R.fail(ResultCode.PARAMETER_VALIDATION_FAILED.getCode(), String.join(",", errList));
    }

    @ExceptionHandler({ValidateException.class})
    public R<String> validatorException(HttpServletRequest request, ValidateException e) {
        String projectStackTrace = ExceptionKit.getProjectStackTraces(e);
        log.error("请求参数校验失败：{}\n**StackTraces:** {}", e.getMessage(), projectStackTrace);
        return R.fail(ResultCode.PARAMETER_VALIDATION_FAILED.getCode(), e.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public R<String> methodArgumentNotValidExceptionHandler(HttpServletRequest request, MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String projectStackTrace = ExceptionKit.getProjectStackTraces(e);
        log.error("请求参数校验失败：{}\n**StackTraces:** {}", fieldError.getDefaultMessage(), projectStackTrace);
        e.printStackTrace();
        return R.fail(ResultCode.PARAMETER_VALIDATION_FAILED.getCode(), fieldError.getDefaultMessage());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public R<String> handle(HttpServletRequest request, NoHandlerFoundException e) {
        log.error("", e);
        return R.fail(404, "地址错误！！！" + request.getRequestURI() + "非法访问!");
    }

    @ExceptionHandler({ServiceException.class})
    public R<String> serviceException(HttpServletRequest request, ServiceException e) {
        String projectStackTrace = ExceptionKit.getProjectStackTraces(e);
        log.warn("服务异常：{}\n@See {}", e.getMessage(), projectStackTrace);
        return R.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler({NullPointerException.class})
    public R<String> nullPointerException(HttpServletRequest request, NullPointerException e) {
        String projectStackTrace = ExceptionKit.getProjectStackTraces(e);
        log.error("空指针异常\n**StackTraces:** {}", projectStackTrace);
        e.printStackTrace();
        return R.fail(ResultCode.FAIL.getCode(), e.getMessage());
    }

    @ExceptionHandler({RuntimeException.class})
    public R<String> runTimeException(HttpServletRequest request, RuntimeException e) {
        String projectStackTrace = ExceptionKit.getProjectStackTraces(e);
        log.error("运行时异常", e);
        return R.fail(ResultCode.FAIL.getCode(), e.getMessage());
    }

}
