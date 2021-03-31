package com.piggybank.util;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public abstract class Util {
    /**
     * Given any object, if the object is null, an action that does nothing is returned. Otherwise, an action
     * containing the object is returned.
     *
     * @param object Nullable object to check.
     * @param <T> Type of the object.
     * @return Action that does nothing if the object is null, otherwise an action containing the object.
     */
    @NonNull
    public static <T> Action<T> ifNonNull(@Nullable T object) {
        if (object != null) {
            return Action.of(object);
        } else {
            return Action.doNothing();
        }
    }

    /**
     * Given any object, if the object is not null, an action that does nothing is returned. Otherwise, an action
     * containing the object is returned.
     *
     * @param object Nullable object to check.
     * @param <T> Type of the object.
     * @return Action that does nothing if the object is not null, otherwise an action containing the object.
     */
    @NonNull
    public static <T> Action<T> ifNull(@Nullable T object) {
        return object == null ?
                Action.of(null) :
                Action.doNothing();
    }
}
