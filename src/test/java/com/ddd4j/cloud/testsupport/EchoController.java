package com.ddd4j.cloud.testsupport;

import com.ddd4j.cloud.context.AppContext;
import com.ddd4j.cloud.contract.BizException;
import com.ddd4j.cloud.web.RawResponse;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Web 层测试端点
 */
@Validated
@RestController
@RequestMapping("/test")
public class EchoController {

    @GetMapping("/echo")
    public Map<String, Object> echo(@RequestParam String name) {
        Map<String, Object> result = new HashMap<>();
        result.put("name", name);
        return result;
    }

    @GetMapping("/biz-error")
    public void bizError() {
        throw new BizException("自定义业务错误");
    }

    @GetMapping("/boom")
    public void boom() {
        throw new IllegalStateException("数据库连接失败");
    }

    @GetMapping("/raw")
    @RawResponse
    public Map<String, Object> raw() {
        return Map.of("raw", true);
    }

    @GetMapping("/context")
    public Map<String, Object> context() {
        Map<String, Object> result = new HashMap<>();
        result.put("userId", AppContext.userId());
        result.put("tenantId", AppContext.tenantId());
        result.put("traceId", AppContext.traceId());
        return result;
    }

    @GetMapping("/validate")
    public String validate(@RequestParam @NotBlank(message = "不能为空") String keyword) {
        return keyword;
    }
}
