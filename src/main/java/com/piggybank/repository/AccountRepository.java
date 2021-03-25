package com.piggybank.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.firebase.cloud.FirestoreClient;
import com.piggybank.model.Account;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Field;
import java.util.Objects;

import static com.piggybank.model.Account.AccountType;
import static com.piggybank.util.Util.*;

/**
 * Interface for database interactions for accounts.
 */
@Repository
public class AccountRepository extends PBRepository {

    /**
     * Initializes the collection reference to the value at the specified property location
     * in application.yml.
     *
     * @param env Environment containing properties.
     */
    public AccountRepository(Environment env) {
        super(Objects.requireNonNull(env.getProperty("firebase.database.labels.accounts")));
    }

    /**
     * Tests the AccountController interface.
     *
     * @return Result of query.
     */
    @NonNull
    public String test(@Nullable String message) {
        return message == null ?
                "Success! No message supplied" :
                "Success! Here is your message: " + message;
    }

    /**
     * Creates new account if one not found with same username. 
     * Fields set according to Account object parameter
     * 
     * @param newAccount - Account object representing the new account
     * @return todo
     */
    public String create(@NonNull Account newAccount) throws Throwable {
        ifNull(newAccount.getType()).thenThrow(new IllegalArgumentException("Must specify account type"));
        if (newAccount.getType() == AccountType.MERCHANT) {
            ifNull(newAccount.getBankAccount()).thenThrow(new IllegalArgumentException("Merchant account must have a bank account"));
        }
        ifNull(newAccount.getUsername()).thenThrow(new IllegalArgumentException("Must specify account username"));

        getApiFuture(collection.document(newAccount.getUsername()).create(newAccount));
        return "Account created successfully!";
    }

    /**
     * todo
     * @param username
     * @param password
     * @return
     * @throws Throwable
     */
    public String login(@NonNull String username, @NonNull String password) throws Throwable {
        ApiFuture<String> futureTx = FirestoreClient.getFirestore().runTransaction(tx -> {
            // Confirm account exists.
            DocumentSnapshot snapshot = tx.get(collection.document(username)).get();
            if (!snapshot.exists()) {
                throw new IllegalArgumentException("Account with that username not found");
            }

            // Account exists--verify password.
            String storedPassword = snapshot.getString("password");
            if (password.equals(storedPassword)) {
                return "Login successful!";
            } else {
                throw new IllegalArgumentException("Password did not match");
            }
        });

        return getApiFuture(futureTx);
    }

    /**
     * Updates the account info if one exists with given username. If fields of the Account
     * object parameter are not null, the fields of the account associated with the username parameter
     * are updated
     * 
     * @param username - username of account to update
     * @param content - object containing fields that need updating
     * @return todo
     */
    public String update(@NonNull String username, @NonNull Account content) throws Throwable {
        ApiFuture<String> futureTx = FirestoreClient.getFirestore().runTransaction(tx -> {
            String currentUsername = username;

            // Copy document over to newly labelled document, if new username was specified.
            if (content.getUsername() != null && !content.getUsername().equals(username)) {
                DocumentSnapshot snapshot = tx.get(collection.document(username)).get();
                if (snapshot.exists() && snapshot.getData() != null) {
                    tx.set(collection.document(content.getUsername()), snapshot.getData());
                    tx.delete(collection.document(username));
                    currentUsername = content.getUsername();
                } else {
                    throw new IllegalArgumentException("Account with that username not found");
                }
            }

            // Change other fields if requested, except for transaction IDs.
            DocumentReference document = collection.document(currentUsername);
            content.setTransactionIds(null);
            for (Field declaredField : Account.class.getDeclaredFields()) {
                declaredField.setAccessible(true);
                ifNonNull(declaredField.get(content)).then(value ->
                    tx.update(document, declaredField.getName(), value)
                );
                declaredField.setAccessible(false);
            }

            return "Account successfully updated!";
        });

        return getApiFuture(futureTx);
    }

    /**
     * Get the account info associated with the given username. 
     * Should not send back sensitive information
     * 
     * @param username - username linked to the account of interest
     * @return todo
     */
    public Account get(String username) throws Throwable {
        ApiFuture<Account> futureTx = FirestoreClient.getFirestore().runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(collection.document(username)).get();
            if (snapshot.exists()) {
                Account account = Objects.requireNonNull(snapshot.toObject(Account.class));
                return Account.filterSensitiveData(account);
            } else {
                throw new IllegalArgumentException("Account with that username not found");
            }
        });

        return getApiFuture(futureTx);
    }
}