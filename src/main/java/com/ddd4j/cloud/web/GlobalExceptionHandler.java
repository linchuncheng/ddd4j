package com.ddd4j.cloud.web;

import com.ddd4j.cloud.contract.BizException;
import com.ddd4j.cloud.contract.R;
import com.ddd4j.cloud.contract.ResultCode;
import com.ddd4j.cloud.context.AppContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

/**
 * 全局异常处理：所有异常统一转为 R 结构。
 * HTTP 状态保持 200，业务错误码在 body 的 code 中（与 3.x 行为一致，前端零改动）。
 *
 * @author Jensen
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public R<Void> handleBiz(BizException e) {
        log.warn("业务异常: {}", e.getMessage());
        return R.fail(e.getCode(), e.getMessage());
    }

    // MethodArgumentNotValidException 是 BindException 的子类，一个入口覆盖 @Valid / @Validated 表单绑定
    @ExceptionHandler(BindException.class)
    public R<Void> handleValidation(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return R.fail(ResultCode.BAD_REQUEST.getCode(), message);
    }

    // Spring 6.1+ 对 @RequestParam 等参数约束抛出的是 HandlerMethodValidationException
    @ExceptionHandler(HandlerMethodValidationException.class)
    public R<Void> handleMethodValidation(HandlerMethodValidationException e) {
        String message = e.getAllErrors().stream()
                .map(error -> String.valueOf(error.getDefaultMessage()))
                .collect(Collectors.joining("; "));
        return R.fail(ResultCode.BAD_REQUEST.getCode(), message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public R<Void> handleConstraint(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(GlobalExceptionHandler::describe)
                .collect(Collectors.joining("; "));
        return R.fail(ResultCode.BAD_REQUEST.getCode(), message);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class, IllegalArgumentException.class})
    public R<Void> handleBadRequest(Exception e) {
        return R.fail(ResultCode.BAD_REQUEST.getCode(), e.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public R<Void> handleNotFound(NoResourceFoundException e) {
        return R.fail(ResultCode.NOT_FOUND.getCode(), "资源不存在");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public R<Void> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return R.fail(ResultCode.BAD_REQUEST.getCode(), "请求方式不支持: " + e.getMethod());
    }

    @ExceptionHandler(Exception.class)
    public R<Void> handleUnknown(Exception e) {
        log.error("服务异常 traceId={}", AppContext.traceId(), e);
        return R.fail(ResultCode.FAIL);
    }

    private static String describe(ConstraintViolation<?> violation) {
        String path = violation.getPropertyPath().toString();
        int dot = path.lastIndexOf('.');
        return path.substring(dot + 1) + " " + violation.getMessage();
    }
}
