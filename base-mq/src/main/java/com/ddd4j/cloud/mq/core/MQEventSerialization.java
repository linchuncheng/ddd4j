package com.ddd4j.cloud.mq.core;

public interface MQEventSerialization {

    <S, T> T deserialize(S src, Class<T> dist) throws RuntimeException;

    <T> T serialize(Object src) throws RuntimeException;

}