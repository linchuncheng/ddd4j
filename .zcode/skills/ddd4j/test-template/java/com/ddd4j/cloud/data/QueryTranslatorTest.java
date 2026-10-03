package com.ddd4j.cloud.data;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Query 后缀约定 → Wrapper 翻译规则的单测，这是框架最核心的约定
 */
class QueryTranslatorTest {

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class DemoQuery extends Query {
        private String username;
        private String nicknameLike;
        private String nicknameLikeLeft;
        private String nicknameLikeRight;
        private String statusNot;
        private List<Long> idIn;
        private List<Long> idNotIn;
        private Integer ageGt;
        private Integer ageLt;
        private Integer ageGe;
        private Integer ageLe;
        private LocalDateTime createTimeStart;
        private LocalDateTime createTimeEnd;
        private Boolean emailIsNull;
    }

    private QueryWrapper<Object> translate(DemoQuery query) {        return QueryTranslator.translate(query);
    }

    @Test
    void exactMatch() {
        DemoQuery query = new DemoQuery();
        query.setUsername("tom");
        QueryWrapper<Object> wrapper = translate(query);
        assertThat(wrapper.getSqlSegment()).contains("username =");
        assertThat(wrapper.getParamNameValuePairs().values()).containsExactly("tom");
    }

    @Test
    void likeContains() {
        DemoQuery query = new DemoQuery();
        query.setNicknameLike("张");
        QueryWrapper<Object> wrapper = translate(query);
        assertThat(wrapper.getSqlSegment()).contains("nickname LIKE");
        assertThat(wrapper.getParamNameValuePairs().values()).containsExactly("%张%");
    }

    @Test
    void likeLeftIsPrefixMatch() {
        DemoQuery query = new DemoQuery();
        query.setNicknameLikeLeft("张");
        QueryWrapper<Object> wrapper = translate(query);
        // 注意：MP 的条件参数是惰性生成的，必须先渲染 getSqlSegment() 再断言参数值
        assertThat(wrapper.getSqlSegment()).contains("nickname LIKE");
        assertThat(wrapper.getParamNameValuePairs().values()).containsExactly("张%");
    }

    @Test
    void likeRightIsSuffixMatch() {
        DemoQuery query = new DemoQuery();
        query.setNicknameLikeRight("张");
        QueryWrapper<Object> wrapper = translate(query);
        assertThat(wrapper.getSqlSegment()).contains("nickname LIKE");
        assertThat(wrapper.getParamNameValuePairs().values()).containsExactly("%张");
    }

    @Test
    void notEquals() {
        DemoQuery query = new DemoQuery();
        query.setStatusNot("DISABLED");
        QueryWrapper<Object> wrapper = translate(query);
        assertThat(wrapper.getSqlSegment()).contains("status <>");
    }

    @Test
    void inAndNotIn() {
        DemoQuery query = new DemoQuery();
        query.setIdIn(Arrays.asList(1L, 2L));
        query.setIdNotIn(List.of(3L));
        QueryWrapper<Object> wrapper = translate(query);
        assertThat(wrapper.getSqlSegment()).contains("id IN");
        assertThat(wrapper.getSqlSegment()).contains("id NOT IN");
        assertThat(wrapper.getParamNameValuePairs().values()).containsExactlyInAnyOrder(1L, 2L, 3L);
    }

    @Test
    void comparisonSuffixes() {
        DemoQuery query = new DemoQuery();
        query.setAgeGt(18);
        query.setAgeLt(60);
        query.setAgeGe(18);
        query.setAgeLe(60);
        QueryWrapper<Object> wrapper = translate(query);
        String sql = wrapper.getSqlSegment();
        assertThat(sql).contains("age >");
        assertThat(sql).contains("age <");
        assertThat(sql).contains("age >=");
        assertThat(sql).contains("age <=");
    }

    @Test
    void startEndAreRangeAliases() {
        DemoQuery query = new DemoQuery();
        query.setCreateTimeStart(LocalDateTime.now());
        query.setCreateTimeEnd(LocalDateTime.now());
        QueryWrapper<Object> wrapper = translate(query);
        String sql = wrapper.getSqlSegment();
        assertThat(sql).contains("create_time >=");
        assertThat(sql).contains("create_time <=");
    }

    @Test
    void isNullFlag() {
        DemoQuery query = new DemoQuery();
        query.setEmailIsNull(true);
        assertThat(translate(query).getSqlSegment()).contains("email IS NULL");

        query.setEmailIsNull(false);
        assertThat(translate(query).getSqlSegment()).contains("email IS NOT NULL");
    }

    @Test
    void orderByAscAndDesc() {
        DemoQuery query = new DemoQuery();
        query.setOrderBys("createTime_DESC,id_ASC");
        QueryWrapper<Object> wrapper = translate(query);
        assertThat(wrapper.getSqlSegment()).contains("create_time DESC");
        assertThat(wrapper.getSqlSegment()).contains("id ASC");
    }

    @Test
    void orderByRejectsInjection() {
        DemoQuery query = new DemoQuery();
        query.setOrderBys("id; DROP TABLE t_user");
        assertThatThrownBy(() -> translate(query))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("非法的查询字段");
    }

    @Test
    void nullAndBlankValuesAreSkipped() {
        DemoQuery query = new DemoQuery();
        query.setUsername(null);
        query.setNicknameLike("  ");
        QueryWrapper<Object> wrapper = translate(query);
        assertThat(wrapper.getSqlSegment()).isEmpty();
    }
}
