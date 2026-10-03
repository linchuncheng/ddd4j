package com.ddd4j.cloud.testsupport;

import com.ddd4j.cloud.data.MybatisRepository;
import org.springframework.stereotype.Repository;

/**
 * 测试仓储：业务仓储的全部样板就这么多
 */
@Repository
public class UserRepository extends MybatisRepository<User, UserQuery> {

    public UserRepository(UserMapper mapper) {
        super(mapper);
    }
}
