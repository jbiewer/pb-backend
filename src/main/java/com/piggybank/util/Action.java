package com.piggybank.util;

import java.util.function.Function;

public class Action<T> {
    private final T object;

    public static <O> Action<O> doNothing() {
        return new Action<>(null);
    }

    public static <O> Action<O> of(O object) {
        return new Action<>(object);
    }

    private Action(T object) {
        this.object = object;
    }

    public T get() {
        return object;
    }

    public <R> void then(Function<T, R> function) {
        if (object != null) {
            function.apply(object);
        }
    }

    public T onException(Function<T, Void> function) {
        function.apply(object);
        return object;
    }
}
