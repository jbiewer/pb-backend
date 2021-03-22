package com.piggybank.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.firebase.cloud.FirestoreClient;
import com.piggybank.model.Account;
import com.piggybank.model.Customer;
import com.piggybank.model.Merchant; 
import com.piggybank.model.Account.AccountType;
import com.piggybank.util.QueryException;
import com.piggybank.util.Result;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

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

    /**
     * Creates new account if one not found with same username. 
     * Fields set according to Account object parameter
     * 
     * @param account - Account object representing the new account
     * @return
     */
    public Result<String> create(Account account) {
        if (account.getAccountType() == null) {
            return new Result<>(new IllegalArgumentException("Must specify account type."));
        }
        try {
            accountsRef.document(account.getUsername()).create(account).get();
            return new Result<>("Account created successfully!");
        } catch (ExecutionException | InterruptedException e) {
            return new Result<>(e.getCause());
        }
    }

    /**
     * Updates the account info if one exists with given username. If fields of the Account
     * object parameter are not null, the fields of the account associated with the username parameter
     * are updated
     * 
     * @param username - username of account to update
     * @param account - object containing fields that need updating
     * @return 
     */
    public Result<String> update(String username, Account account) {
        ApiFuture<Result<String>> futureTx = FirestoreClient.getFirestore().runTransaction(transaction -> {
            // Keep track of the account's current document (may be changed w/ new username).
            DocumentReference currentReference = accountsRef.document(username);

            // Copy document over to newly labelled document, if new username was specified.
            if (account.getUsername() != null && !account.getUsername().equals(username)) {
                DocumentReference oldReference = currentReference;
                DocumentReference newReference = accountsRef.document(account.getUsername());

                //contains data from old account
                DocumentSnapshot snapshot = transaction.get(oldReference).get();
                if (snapshot.exists() && snapshot.getData() != null) {
                    //adds all old data to new document with updated username
                    transaction.create(newReference, snapshot.getData());
                    transaction.delete(oldReference);
                    currentReference = newReference;
                    //*** test to see if this is needed to update username field of new document
                    transaction.update(currentReference, "username", account.getUsername());
                } else {
                    return new Result<>(new QueryException("Account with that username not found."));
                }
            }

            // Change other fields if requested.

            if (account.getPassword() != null) {
                transaction.update(currentReference, "password", account.getPassword());
            }
            if (account.getEmail() != null) {
                transaction.update(currentReference, "address", account.getEmail());
            }
            if (account.getProfilePictureURL() != null) {
                transaction.update(currentReference, "profilePictureURL", account.getProfilePictureURL());
            }
            //balance set to -1 if no need to update?
            if (account.getBalance() > -1) {
                transaction.update(currentReference, "balance", account.getBalance());
            }

            //requires testing
            if (account.getBankAccount() != null) {
                transaction.update(currentReference, "bankAccount", account.getBankAccount());
            }

            if(account.getTransactionList() != null) {
                transaction.update(currentReference, "transactions", account.getTransactionList());
            }

            return new Result<>("Account successfully updated!");
        });

        try {
            return futureTx.get();
        } catch (ExecutionException | InterruptedException e) {
            return new Result<>(e.getCause());
        }
    }

    /**
     * Get the account info associated with the given username. 
     * Should not send back sensitive information
     * 
     * @param username - username linked to the account of interest
     * @return
     */
    public Result<Account> get(String username) {
        try {
            //TODO: Do not send back sensitive info to user (pwd)
            DocumentSnapshot snapshot = accountsRef.document(username).get().get();
            AccountType type = AccountType.valueOf(snapshot.getString("type"));
            if (Objects.equals(type, AccountType.CUSTOMER)) {
                return new Result<>(snapshot.toObject(Customer.class));
            } else {
                return new Result<>(snapshot.toObject(Merchant.class));
            }
        } catch (ExecutionException | InterruptedException e) {
            return new Result<>(e.getCause());
        }
    }
}
