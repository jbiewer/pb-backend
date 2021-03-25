package com.piggybank.util;

import java.util.function.Function;

public class Action<O> {
    private final O object;
    private final boolean doNothing;

    public static <E> Action<E> doNothing() {
        return new Action<>(true);
    }

    public static <E> Action<E> of(E object) {
        return new Action<>(object);
    }

    private Action(O object) {
        this.object = object;
        this.doNothing = false;
    }

    private Action(boolean doNothing) {
        this.object = null;
        this.doNothing = doNothing;
    }

    public O get() {
        return object;
    }

    public <R> Action<R> then(Function<O, R> function) {
        if (!doNothing) {
            return Action.of(function.apply(object));
        }
        return Action.doNothing();
    }

    public <T extends Throwable> Action<O> thenThrow(T throwable) throws T {
        if (!doNothing) {
            throw throwable;
        }
        return this;
    }
}
