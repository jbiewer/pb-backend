package com.piggybank.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldMask;
import com.google.cloud.firestore.FieldPath;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;
import com.piggybank.model.Account;
import com.piggybank.model.BankAccount;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import static com.piggybank.util.Util.ifNonNull;

/**
 * Interface for database interactions for bank accounts.
 */
@Repository
public class BankAccountRepository extends PBRepository {

    /**
     * Initializes the collection reference to the value at the specified property location
     * in application.yml.
     *
     * @param env Environment containing properties.
     */
    public BankAccountRepository(Environment env) {
        super(Objects.requireNonNull(env.getProperty("firebase.database.labels.accounts")));
    }

    /**
     * Tests the BankAccountController interface.
     *
     * @return Success message.
     */
    @NonNull
    public String test(@Nullable String message) {
        return message == null ?
                "Success! No message supplied" :
                "Success! Here is your message: " + message;
    }

    /**
     * Given an email, finds the account associated with it and updates the account's bank account with the
     * values from 'content'. If a bank account does not exist, it will be created using the content object.
     * Only if the value in 'content' is non-null will it be updated in Firestore.
     *
     * @param email Email associated with the account to update.
     * @param content Data to update the account's bank account with.
     * @return Success string indicating the account's bank account was successfully updated.
     * @throws IllegalArgumentException When no account is found with the specified email.
     * @throws Exception When an internal error occurs.
     */
    @NonNull
    public String update(@NonNull String email, @NonNull BankAccount content) throws Exception {
        // Check if document w/ ID 'email' exists.
        DocumentSnapshot snapshot = collection.document(email).get(FieldMask.of("bankAccount")).get();
        if (!snapshot.exists()) {
            throw new IllegalArgumentException("Account with that email not found");
        }

        // Update the document's bankAccount field.
        BankAccount bank = snapshot.get("bankAccount", BankAccount.class);
        if (bank == null) {
            bank = content;
        } else {
            for (Field declaredField : BankAccount.class.getDeclaredFields()) {
                boolean accessible = declaredField.canAccess(content);
                declaredField.setAccessible(true);
                Object value = declaredField.get(content);
                if (value != null) {
                    declaredField.set(bank, value);
                }
                declaredField.setAccessible(accessible);
            }
        }
        collection.document(email).update("bankAccount", bank).get();

        return "Bank account successfully updated!";
    }

    /**
     * Given an email, finds the account associated with it and removes the account's bank account. If a bank
     * account does not exist, no changes will be made.
     *
     * @param email Email associated with the account to remove the bank account from.
     * @return Success string indicating the account's bank account was successfully removed.
     * @throws IllegalArgumentException When no account is found with the specified email.
     * @throws Exception When an internal error occurs.
     */
    @NonNull
    public String remove(@NonNull String email) throws Exception {
        // Check if document w/ ID 'email' exists.
        DocumentSnapshot snapshot = collection.document(email).get(FieldMask.of("bankAccount")).get();
        if (!snapshot.exists()) {
            throw new IllegalArgumentException("Account with that email not found");
        }

        // Remove the bank account.
        collection.document(email).update("bankAccount", null).get();
        return "Bank account successfully removed!";
    }

    /**
     * Given an email, finds the account associated with it and retrieves the account's bank account. If a bank
     * account does not exist, the object returned will be null.
     *
     * @param email Email associated with the account to get the bank account from.
     * @return The bank account associated with the account found.
     * @throws IllegalArgumentException When no account is found with the specified email.
     * @throws Exception When an internal error occurs.
     */
    @Nullable
    public BankAccount get(@NonNull String email) throws Exception {
        // Check if document w/ ID 'email' exists.
        DocumentSnapshot snapshot = collection.document(email).get(FieldMask.of("bankAccount")).get();
        if (!snapshot.exists()) {
            throw new IllegalArgumentException("Account with that email not found");
        }

        // Retrieve the bank account.
        return snapshot.get("bankAccount", BankAccount.class);
    }
}