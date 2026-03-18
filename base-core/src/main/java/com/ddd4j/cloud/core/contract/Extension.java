package com.ddd4j.cloud.core.contract;

import com.ddd4j.cloud.core.context.SpringContext;

import java.util.Collection;
import java.util.function.Predicate;

/**
 * BEAN扩展
 */
public interface Extension {

    static <R extends Extension> R of(Predicate<R> predicate) {
        Collection<Extension> beans = SpringContext.getBeans(Extension.class);
        if (!beans.isEmpty()) {
            for (Extension extension : beans) {
                if (predicate.test((R) extension)) {
                    return (R) extension;
                }
            }
        }
        throw new RuntimeException("Extension not found!");
    }

}
