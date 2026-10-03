package com.ddd4j.cloud.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web 层集成测试：响应包装 / 全局异常 / 上下文拦截
 */
@SpringBootTest(classes = com.ddd4j.cloud.testsupport.TestApplication.class)
@AutoConfigureMockMvc
class WebLayerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void responseIsWrappedIntoR() throws Exception {
        mockMvc.perform(get("/test/echo").param("name", "tom"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.msg").value("操作成功"))
                .andExpect(jsonPath("$.data.name").value("tom"));
    }

    @Test
    void bizExceptionBecomesFailResponse() throws Exception {
        mockMvc.perform(get("/test/biz-error"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("500"))
                .andExpect(jsonPath("$.msg").value("自定义业务错误"));
    }

    @Test
    void unknownExceptionHidesInternalDetails() throws Exception {
        mockMvc.perform(get("/test/boom"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("500"))
                .andExpect(jsonPath("$.msg").value("操作失败"));
    }

    @Test
    void rawResponseAnnotationSkipsWrapping() throws Exception {
        mockMvc.perform(get("/test/raw"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.raw").value(true))
                .andExpect(jsonPath("$.code").doesNotExist());
    }

    @Test
    void validationErrorBecomesBadRequest() throws Exception {
        mockMvc.perform(get("/test/validate").param("keyword", " "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.msg").value("keyword 不能为空"));
    }

    @Test
    void headersPopulateAppContextAndTraceIdEchoes() throws Exception {
        MvcResult result = mockMvc.perform(get("/test/context")
                        .header("X-User-Id", "42")
                        .header("X-Tenant-Id", "7")
                        .header("X-Trace-Id", "trace-abc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(42))
                .andExpect(jsonPath("$.data.tenantId").value(7))
                .andExpect(jsonPath("$.data.traceId").value("trace-abc"))
                .andReturn();
        assertThat(result.getResponse().getHeader("X-Trace-Id")).isEqualTo("trace-abc");
    }

    @Test
    void missingTraceIdIsGenerated() throws Exception {
        mockMvc.perform(get("/test/context"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.traceId").isNotEmpty());
    }
}
