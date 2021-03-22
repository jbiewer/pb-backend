package com.piggybank.repository;

import com.google.cloud.firestore.CollectionReference;
import com.google.firebase.cloud.FirestoreClient;
import com.piggybank.util.Action;

import java.util.Objects;

/**
 * todo
 */
public abstract class PBRepository {
    protected final CollectionReference collection;

    /**
     * todo
     * @param collectionLabel
     */
    public PBRepository(String collectionLabel) {
        this.collection = FirestoreClient.getFirestore().collection(collectionLabel);
    }

    /**
     * todo
     * @param object
     * @param <T>
     * @return
     */
    protected <T> Action<T> ifNonNull(T object) {
        if (Objects.nonNull(object)) {
            return Action.of(object);
        } else {
            return Action.doNothing();
        }
    }

}
