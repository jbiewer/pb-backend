package com.piggybank.util;

import java.util.Objects;
import java.util.function.Function;

public abstract class Util {
    /**
     * todo
     * @param object
     * @param <T>
     * @return
     */
    public static <T> Action<T> ifNonNull(T object) {
        if (Objects.nonNull(object)) {
            return Action.of(object);
        } else {
            return Action.doNothing();
        }
    }

    public static <R> Action<R> ifNonNull(Function<Void, R> function) {
        Void v = null;
        return ifNonNull(function.apply(v));
    }
}
