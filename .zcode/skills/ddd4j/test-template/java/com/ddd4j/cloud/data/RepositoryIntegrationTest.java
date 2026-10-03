package com.ddd4j.cloud.data;

import com.ddd4j.cloud.context.AppContext;
import com.ddd4j.cloud.contract.Page;
import com.ddd4j.cloud.testsupport.User;
import com.ddd4j.cloud.testsupport.UserQuery;
import com.ddd4j.cloud.testsupport.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 仓储 + 租户隔离 + 审计填充的 H2 集成测试（字段风格对齐 fengqun-scm：字符串ID、操作人审计）
 */
@SpringBootTest(classes = com.ddd4j.cloud.testsupport.TestApplication.class)
class RepositoryIntegrationTest {

    @Autowired
    private UserRepository repository;

    @BeforeEach
    void setUp() {
        AppContext.clear();
        AppContext.current().setTenantId("t-1");
        AppContext.current().setUserId("u-1");
        repository.delete(new UserQuery().ignoreTenant());
    }

    @AfterEach
    void tearDown() {
        AppContext.clear();
    }

    private User user(String username, int age) {
        User user = new User();
        user.setUsername(username);
        user.setAge(age);
        return user;
    }

    @Test
    void insertAndGetFillsAuditAndTenant() {
        User user = user("tom", 20);
        assertThat(repository.insert(user)).isTrue();
        assertThat(user.getId()).isNotNull();

        User loaded = repository.get(user.getId());
        assertThat(loaded.getUsername()).isEqualTo("tom");
        // 审计填充：时间 + 操作人
        assertThat(loaded.getCreateTime()).isNotNull();
        assertThat(loaded.getUpdateTime()).isNotNull();
        assertThat(loaded.getCreatedBy()).isEqualTo("u-1");
        assertThat(loaded.getUpdatedBy()).isEqualTo("u-1");
        // 租户随上下文自动写入
        assertThat(loaded.getTenantId()).isEqualTo("t-1");
    }

    @Test
    void updateOverwritesUpdateAuditButKeepsCreateAudit() {
        User tom = user("tom", 18);
        repository.insert(tom);
        assertThat(tom.getCreatedBy()).isEqualTo("u-1");
        LocalDateTime firstUpdateTime = tom.getUpdateTime();

        AppContext.current().setUserId("u-2");
        tom.setAge(19);
        assertThat(repository.updateById(tom)).isTrue();

        User loaded = repository.get(tom.getId());
        // update_by / update_time 无条件覆盖；create_* 保持首次写入
        assertThat(loaded.getUpdatedBy()).isEqualTo("u-2");
        assertThat(loaded.getUpdateTime()).isAfter(firstUpdateTime);
        assertThat(loaded.getCreatedBy()).isEqualTo("u-1");
    }

    @Test
    void systemContextSkipsUserFillButStillFillsTime() {
        AppContext.current().setUserId(null);
        User user = user("job", 1);
        repository.insert(user);
        assertThat(user.getCreatedBy()).isNull();
        assertThat(user.getCreateTime()).isNotNull();
    }

    @Test
    void tenantIsolation() {
        repository.insert(user("a", 1));
        AppContext.current().setTenantId("t-2");
        repository.insert(user("b", 2));

        AppContext.current().setTenantId("t-1");
        assertThat(repository.list(new UserQuery())).extracting(User::getUsername).containsExactly("a");

        AppContext.current().setTenantId("t-2");
        assertThat(repository.list(new UserQuery())).extracting(User::getUsername).containsExactly("b");

        // 显式忽略租户
        assertThat(repository.list(new UserQuery().ignoreTenant())).hasSize(2);
    }

    @Test
    void missingTenantContextIsRejected() {
        repository.insert(user("a", 1));
        AppContext.clear();
        // fail-closed：无租户上下文的查询直接失败，而不是静默读到全量数据
        // （MP 拦截器层抛出的异常会被 MyBatis 包装一层，因此断言根因）
        assertThatThrownBy(() -> repository.list(new UserQuery()))
                .hasRootCauseInstanceOf(IllegalStateException.class)
                .hasStackTraceContaining("租户ID");
        // 平台任务的正确姿势是显式豁免
        assertThat(repository.list(new UserQuery().ignoreTenant())).hasSize(1);
    }

    @Test
    void queryFilters() {
        repository.insert(user("tom", 18));
        repository.insert(user("tommy", 30));
        repository.insert(user("jerry", 45));

        UserQuery like = new UserQuery();
        like.setUsernameLike("tom");
        assertThat(repository.list(like)).hasSize(2);

        UserQuery range = new UserQuery();
        range.setAgeGe(18);
        range.setAgeLe(30);
        assertThat(repository.list(range)).hasSize(2);

        UserQuery exact = new UserQuery();
        exact.setUsername("jerry");
        assertThat(repository.one(exact).getAge()).isEqualTo(45);

        assertThat(repository.count(new UserQuery())).isEqualTo(3);
        assertThat(repository.exists(new UserQuery())).isTrue();
    }

    @Test
    void pageReturnsFengqunContractShape() {
        for (int i = 0; i < 25; i++) {
            repository.insert(user("u" + i, i));
        }
        UserQuery query = new UserQuery();
        query.setCurrent(2);
        query.setSize(20);
        query.setOrderBys("age_ASC");

        Page<User> page = repository.page(query);
        assertThat(page.getTotal()).isEqualTo(25);
        assertThat(page.getPageSize()).isEqualTo(20);
        assertThat(page.getCurrentPage()).isEqualTo(2);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getRecords()).hasSize(5);
        assertThat(page.getRecords().get(0).getAge()).isEqualTo(20);
    }

    @Test
    void updateByIdAndBulkUpdate() {
        User tom = user("tom", 18);
        repository.insert(tom);

        tom.setAge(19);
        assertThat(repository.updateById(tom)).isTrue();

        UserQuery like = new UserQuery();
        like.setUsernameLike("tom");
        User patch = new User();
        patch.setAge(20);
        assertThat(repository.update(patch, like)).isTrue();
        assertThat(repository.one(like).getAge()).isEqualTo(20);
    }

    @Test
    void deleteVariants() {
        User u1 = user("a", 1);
        User u2 = user("b", 2);
        repository.insert(u1);
        repository.insert(u2);

        assertThat(repository.deleteById(u1.getId())).isTrue();

        UserQuery byName = new UserQuery();
        byName.setUsername("b");
        assertThat(repository.delete(byName)).isTrue();

        assertThat(repository.exists(new UserQuery())).isFalse();
    }

    @Test
    void insertBatchUsesBatchExecutor() {
        List<User> users = List.of(user("b1", 1), user("b2", 2), user("b3", 3));
        assertThat(repository.insertBatch(users)).isTrue();
        assertThat(repository.count(new UserQuery())).isEqualTo(3);
    }

    @Test
    void escapeHatchWithWrapperConsumer() {
        repository.insert(user("old", 70));
        repository.insert(user("young", 17));

        List<User> seniors = repository.search(wrapper -> wrapper.ge("age", 18));
        assertThat(seniors).extracting(User::getUsername).containsExactly("old");
    }
}
