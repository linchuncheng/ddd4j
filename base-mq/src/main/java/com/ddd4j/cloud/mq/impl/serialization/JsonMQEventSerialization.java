package com.ddd4j.cloud.mq.impl.serialization;

import com.ddd4j.cloud.core.kit.JsonKit;
import com.ddd4j.cloud.mq.core.MQEventSerialization;
import org.springframework.stereotype.Component;

@Component("JsonMQEventSerialization")
public class JsonMQEventSerialization implements MQEventSerialization {

    @Override
    public <S, T> T deserialize(S src, Class<T> dist) throws RuntimeException {
        if (src == null || ((String) src).isEmpty()) {
            return null;
        }
        return JsonKit.toObject(src, dist);
    }

    @Override
    public String serialize(Object src) throws RuntimeException {
        return JsonKit.toJson(src);
    }

}