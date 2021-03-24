package com.piggybank.util;

import java.util.function.Function;

public class Action<T> {
    private final T object;
    private final boolean doNothing;

    public static <O> Action<O> doNothing() {
        return new Action<>(true);
    }

    public static <O> Action<O> of(O object) {
        return new Action<>(object);
    }

    private Action(T object) {
        this.object = object;
        this.doNothing = false;
    }

    private Action(boolean doNothing) {
        this.object = null;
        this.doNothing = doNothing;
    }

    public T get() {
        return object;
    }

    public <R> void then(Function<T, R> function) {
        if (!doNothing) {
            function.apply(object);
        }
    }

    public <E extends Throwable> void thenThrow(E throwable) throws E {
        if (!doNothing) {
            throw throwable;
        }
    }
}
