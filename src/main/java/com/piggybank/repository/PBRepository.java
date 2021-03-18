package com.piggybank.repository;

import com.google.cloud.firestore.CollectionReference;
import com.google.firebase.cloud.FirestoreClient;

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
}
