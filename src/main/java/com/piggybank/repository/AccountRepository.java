package com.piggybank.repository;

import com.google.cloud.firestore.CollectionReference;
import com.google.firebase.cloud.FirestoreClient;
import com.piggybank.util.Result;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Repository;

import java.util.Objects;

/**
 * Interface for database interactions for accounts.
 */
@Repository
public class AccountRepository {
    private final CollectionReference accountsRef;

    /**
     * Initializes the collection reference to the value at the specified property location
     * in application.yml.
     *
     * @param env Environment containing properties.
     */
    public AccountRepository(Environment env) {
        String label = Objects.requireNonNull(env.getProperty("firebase.database.labels.accounts")); // todo
        accountsRef = FirestoreClient.getFirestore().collection(label);
    }

    /**
     * Tests the AccountController interface.
     *
     * @return Result of query.
     */
    public Result<String> test() {
        return new Result<>("Success!");
    }
}
