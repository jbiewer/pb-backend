package com.piggybank.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.firebase.cloud.FirestoreClient;

import java.util.concurrent.ExecutionException;

/**
 * Parent class of all repositories for PiggyBank.
 */
public abstract class PBRepository {

    /** Firestore collection corresponding to the repository that interacts with it. */
    protected final CollectionReference collection;

    /**
     * Initializes the reference to the document collection labelled with 'collectionLabel'
     *
     * @param collectionLabel Label of the collection in Firestore.
     */
    public PBRepository(String collectionLabel) {
        this.collection = FirestoreClient.getFirestore().collection(collectionLabel);
    }

    /**
     * Given an future (async object), attempts to retrieve it by blocking until response.
     * If retrieving it is successful, the object retrieved is returned, otherwise the error/exception that
     * occurred is handled properly.
     *
     * @param future ApiFuture to retrieve and block on.
     * @param <T> Type of object being retrieved.
     * @return The object being retrieved.
     * @throws Exception When something goes wrong retrieving the future.
     */
    protected static <T> T getApiFuture(ApiFuture<T> future) throws Exception {
        try {
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            // todo: log internally
            if (e.getCause() instanceof Exception) {
                throw (Exception) e.getCause();
            } else {
                throw new Exception("Internal server error.");
            }
        }
    }
}
