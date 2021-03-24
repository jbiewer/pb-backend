package com.piggybank.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.firebase.cloud.FirestoreClient;

import java.util.concurrent.ExecutionException;

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

    protected static <T> T getApiFuture(ApiFuture<T> future) throws Throwable {
        try {
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            throw e.getCause();
        }
    }
}
