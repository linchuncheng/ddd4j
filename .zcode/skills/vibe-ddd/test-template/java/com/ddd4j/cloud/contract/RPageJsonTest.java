package com.ddd4j.cloud.contract;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * R / Page 的 JSON 契约测试：这是前后端约定，必须稳定
 */
class RPageJsonTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void successJsonShape() throws Exception {
        String json = objectMapper.writeValueAsString(R.ok("hi"));
        assertThat(json).isEqualTo("{\"code\":\"200\",\"msg\":\"操作成功\",\"data\":\"hi\"}");
    }

    @Test
    void successWithoutData() throws Exception {
        String json = objectMapper.writeValueAsString(R.ok());
        assertThat(json).isEqualTo("{\"code\":\"200\",\"msg\":\"操作成功\",\"data\":null}");
    }

    @Test
    void failJsonShape() throws Exception {
        String json = objectMapper.writeValueAsString(R.fail("参数错误"));
        assertThat(json).isEqualTo("{\"code\":\"500\",\"msg\":\"参数错误\",\"data\":null}");
        assertThat(objectMapper.readValue(json, R.class).isSuccess()).isFalse();
    }

    @Test
    void deserializeRoundTrip() throws Exception {
        R<String> r = R.ok("data");
        R<String> back = objectMapper.readValue(objectMapper.writeValueAsString(r), R.class);
        assertThat(back.isSuccess()).isTrue();
        assertThat(back.getMsg()).isEqualTo("操作成功");
        assertThat(back.getData()).isEqualTo("data");
    }

    @Test
    void pageJsonShape() throws Exception {
        Page<String> page = Page.of(List.of("a", "b"), 101, 20, 1);
        String json = objectMapper.writeValueAsString(page);
        assertThat(json).contains("\"records\":[\"a\",\"b\"]");
        assertThat(json).contains("\"total\":101");
        assertThat(json).contains("\"pageSize\":20");
        assertThat(json).contains("\"currentPage\":1");
        assertThat(json).contains("\"totalPages\":6");
    }

    @Test
    void bizExceptionCarriesCode() {
        BizException e = new BizException(ResultCode.FORBIDDEN);
        assertThat(e.getCode()).isEqualTo("403");
        assertThat(new BizException("自定义").getCode()).isEqualTo("500");
    }
}
