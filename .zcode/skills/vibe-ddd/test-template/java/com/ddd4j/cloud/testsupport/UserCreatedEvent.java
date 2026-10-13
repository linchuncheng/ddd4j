package com.ddd4j.cloud.testsupport;

import com.ddd4j.cloud.contract.DomainEvent;

/**
 * 领域事件样例：用户已创建
 */
public class UserCreatedEvent extends DomainEvent<User> {

    public UserCreatedEvent(User payload) {
        super(payload);
    }
}
