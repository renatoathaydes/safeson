package com.athaydes.json.util;

import java.util.function.Function;

@FunctionalInterface
public interface CheckedFunction<T, R> extends Function<T, R> {
    @Override
    default R apply(T t) {
        try {
            return applyChecked(t);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    R applyChecked(T t) throws Exception;
}
