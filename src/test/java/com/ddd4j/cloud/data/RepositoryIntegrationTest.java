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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 仓储 + 租户隔离 + 审计填充的 H2 集成测试
 */
@SpringBootTest(classes = com.ddd4j.cloud.testsupport.TestApplication.class)
class RepositoryIntegrationTest {

    @Autowired
    private UserRepository repository;

    @BeforeEach
    void setUp() {
        AppContext.clear();
        AppContext.current().setTenantId(1L);
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
        // 审计填充
        assertThat(loaded.getCreateTime()).isNotNull();
        assertThat(loaded.getUpdateTime()).isNotNull();
        // 租户随上下文自动写入
        assertThat(loaded.getTenantId()).isEqualTo(1L);
    }

    @Test
    void tenantIsolation() {
        repository.insert(user("a", 1));
        AppContext.current().setTenantId(2L);
        repository.insert(user("b", 2));

        AppContext.current().setTenantId(1L);
        assertThat(repository.list(new UserQuery())).extracting(User::getUsername).containsExactly("a");

        AppContext.current().setTenantId(2L);
        assertThat(repository.list(new UserQuery())).extracting(User::getUsername).containsExactly("b");

        // 显式忽略租户
        assertThat(repository.list(new UserQuery().ignoreTenant())).hasSize(2);
    }

    @Test
    void noTenantContextMeansNoFilter() {
        repository.insert(user("a", 1));
        AppContext.clear();
        assertThat(repository.list(new UserQuery())).hasSize(1);
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
