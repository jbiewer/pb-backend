package com.piggybank.util;

public abstract class Util {
    /**
     * todo
     * @param object
     * @param <T>
     * @return
     */
    public static <T> Action<T> ifNonNull(T object) {
        if (object != null) {
            return Action.of(object);
        } else {
            return Action.doNothing();
        }
    }

    /**
     * todo
     * @param object
     * @param <T>
     * @return
     */
    public static <T> Action<T> ifNull(T object) {
        return object == null ?
                Action.of(null) :
                Action.doNothing();
    }
}
