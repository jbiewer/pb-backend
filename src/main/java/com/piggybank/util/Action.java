package com.piggybank.util;

import java.util.function.Function;

public class Action<T> {
    private final T object;

    public static <T> Action<T> doNothing() {
        return new Action<>(null);
    }

    public static <T> Action<T> of(T object) {
        return new Action<>(object);
    }

    private Action(T object) {
        this.object = object;
    }

    public <R> void then(Function<T, R> function) {
        if (object != null) {
            function.apply(object);
        }
    }
}
