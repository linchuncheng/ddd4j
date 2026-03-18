package com.ddd4j.cloud.mq.core;

import com.ddd4j.cloud.core.contract.MQEvent;

public interface MQEventStorer<T extends MQEvent> {
    // 消息持久化
    void store(T mqEvent);
}