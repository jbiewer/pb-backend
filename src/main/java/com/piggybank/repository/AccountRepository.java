package com.piggybank.repository;

import com.piggybank.model.Account;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.firebase.cloud.FirestoreClient;
import com.piggybank.model.Customer;
import com.piggybank.model.Merchant; 
import com.piggybank.model.Account.AccountType;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

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
    public String test(String message) {
        return "Success! Here is your message: " + message;
    }

    /**
     * todo
     * @param username
     * @param password
     * @return
     */
    @NonNull
    public String login(String username, String password) {
        return "filler";
    }

    /**
     * Creates new account if one not found with same username. 
     * Fields set according to Account object parameter
     * 
     * @param newAccount - Account object representing the new account
     * @return
     */
    public String create(Account newAccount) throws Throwable {
        if (newAccount.getType() == null) {
            throw new IllegalArgumentException("Must specify account type.");
        }

        try {
            collection.document(newAccount.getUsername()).create(newAccount).get();
            return "Account created successfully!";
        } catch (ExecutionException | InterruptedException e) {
            throw e.getCause();
        }
    }

    /**
     * Updates the account info if one exists with given username. If fields of the Account
     * object parameter are not null, the fields of the account associated with the username parameter
     * are updated
     * 
     * @param username - username of account to update
     * @param content - object containing fields that need updating
     * @return 
     */
    public String update(String username, Account content) throws Throwable {
        ApiFuture<String> futureTx = FirestoreClient.getFirestore().runTransaction(transaction -> {
            // Keep track of the account's current document (may be changed w/ new username).
            DocumentReference currentReference = collection.document(username);

            // Copy document over to newly labelled document, if new username was specified.
            if (content.getUsername() != null && !content.getUsername().equals(username)) {
                DocumentReference oldReference = currentReference;
                DocumentReference newReference = collection.document(content.getUsername());

                //contains data from old account
                DocumentSnapshot snapshot = transaction.get(oldReference).get();
                if (snapshot.exists() && snapshot.getData() != null) {
                    //adds all old data to new document with updated username
                    transaction.create(newReference, snapshot.getData());
                    transaction.delete(oldReference);
                    currentReference = newReference;
                    //*** test to see if this is needed to update username field of new document
                    transaction.update(currentReference, "username", content.getUsername());
                } else {
                    throw new Exception("Account with that username not found.");
                }
            }

            // Change other fields if requested.
            // Lambdas only allow logically-final fields.
            final DocumentReference currentRef = currentReference;
            ifNonNull(content.getPassword()).then(password -> transaction.update(currentRef, "password", password));
            ifNonNull(content.getEmail()).then(email -> transaction.update(currentRef, "email", email));
            ifNonNull(content.getProfilePictureUrl()).then(url -> transaction.update(currentRef, "profilePictureUrl", url));
            ifNonNull(content.getBalance()).then(balance -> transaction.update(currentRef, "balance", balance));
            // requires testing
            ifNonNull(content.getBankAccount()).then(bank -> transaction.update(currentRef, "bankAccount", bank));
            return "Account successfully updated!";
        });

        try {
            return futureTx.get();
        } catch (ExecutionException | InterruptedException e) {
            throw e.getCause();
        }
    }

    /**
     * Get the account info associated with the given username. 
     * Should not send back sensitive information
     * 
     * @param username - username linked to the account of interest
     * @return
     */
    public Account get(String username) throws Throwable {
        ApiFuture<Account> futureTx = FirestoreClient.getFirestore().runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(collection.document(username)).get();
            if (snapshot.exists()) {
                Account account = Objects.requireNonNull(snapshot.toObject(Account.class));
                return Account.filterSensitiveData(account);
            } else {
                throw new IllegalArgumentException("Account with that username not found.");
            }
        });

        try {
            return futureTx.get();
        } catch (ExecutionException | InterruptedException e) {
            throw e.getCause();
        }
    }
}
