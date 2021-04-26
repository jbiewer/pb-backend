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
import java.util.concurrent.ExecutionException;
import java.util.stream.StreamSupport;

import static com.piggybank.model.Account.AccountType;

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
     * @return Success message.
     */
    @NonNull
    public String test(@Nullable String message) {
        return message == null ?
                "Success! No message supplied" :
                "Success! Here is your message: " + message;
    }

    /**
     * Creates a new account in Firestore if one with the specified email doesn't already exist.
     * Using the fields from the 'newAccount' parameter, a document is created in Firestore labelled with the
     * email in 'newAccount'.
     *
     * @param newAccount Account object representing the new account in Firestore.
     * @return Message indicating success.
     * @throws IllegalArgumentException When the account type, email, or password fields are not specified,
     *                                  or if the account type is MERCHANT but the bank account is not specified.
     * @throws Exception For any internal error.
     */
    @NonNull
    public String create(@NonNull Account newAccount) throws Exception {
        if (newAccount.getType() == null) { throw new IllegalArgumentException("Must specify account type"); }
        if (newAccount.getType() == AccountType.MERCHANT) {
            if (newAccount.getBankAccount() == null) { throw new IllegalArgumentException("Merchant account must have a bank account"); }
        }
        if (newAccount.getEmail() == null) { throw new IllegalArgumentException("Must specify account email"); }
        if (newAccount.getPassword() == null) { throw new IllegalArgumentException("Must specify account password"); }

        //create document where id = email
        getApiFuture(collection.document(newAccount.getEmail()).create(newAccount));
        return "Account created successfully!";
    }

    /**
     * Given an email and password, finds the account via the email and then verifies that the password passed in
     * matches the password that currently exists in Firestore. If an account is found and the passwords match,
     * the login is successful.
     *
     * @param email Email of an existing account.
     * @param password Password of the existing account to match against.
     * @return Success string if the password matches the account found in Firestore.
     * @throws IllegalArgumentException When either an account with the email doesn't exist or the password doesn't
     *                                  match the one found in Firestore.
     * @throws Exception When an unexpected exception occurs.
     */
    @NonNull
    public String login(@NonNull String email, @NonNull String password) throws Exception {
        ApiFuture<String> futureTx = FirestoreClient.getFirestore().runTransaction(tx -> {
            // Confirm account exists.
            DocumentSnapshot snapshot = tx.get(collection.document(email)).get();
            if (!snapshot.exists()) {
                throw new IllegalArgumentException("Account with that email not found");
            }

            // Update password if it's different.
            String storedPassword = snapshot.getString("password");
            if (!password.equals(storedPassword)) {
                tx.update(collection.document(email), "password", password);
            }

            return "Login successful!";
        });

        return getApiFuture(futureTx);
    }

    /**
     * Updates the account info if one exists with given email. If fields of the Account
     * object parameter are not null, the fields of the account associated with the email parameter
     * are updated
     * 
     * @param email Email of account to update.
     * @param content Account object containing fields that are used to update the account in Firestore.
     * @return A success message indicating the account was updated.
     * @throws IllegalArgumentException When an account with the email doesn't exist.
     * @throws Exception When an unexpected exception occurs.
     */
    @NonNull
    public String update(@NonNull String email, @NonNull Account content) throws Exception {
        ApiFuture<String> futureTx = FirestoreClient.getFirestore().runTransaction(tx -> {
            String currentEmail = email;

            // Copy document over to newly labelled document, if new email was specified.
            if (content.getEmail() != null && !content.getEmail().equals(email)) {
                DocumentSnapshot snapshot = tx.get(collection.document(email)).get();
                if (snapshot.exists() && snapshot.getData() != null) {
                    tx.set(collection.document(content.getEmail()), snapshot.getData());
                    tx.delete(collection.document(email));
                    currentEmail = content.getEmail();
                } else {
                    throw new IllegalArgumentException("Account with that email not found");
                }
            }

            // Change other fields if requested, except for transaction IDs and bank account.
            DocumentReference document = collection.document(currentEmail);
            content.setTransactionIds(null);
            content.setBankAccount(null);
            for (Field declaredField : Account.class.getDeclaredFields()) {
                boolean accessible = declaredField.canAccess(content);
                declaredField.setAccessible(true);
                //balance field's default value is 0 (not null), so set to -1 if don't want to update balance
                if (declaredField.getName().equals("balance")) {
                    if (!declaredField.get(content).toString().equals("-1")) {
                        tx.update(document, declaredField.getName(), declaredField.get(content));
                    }   
                } else {
                    if (declaredField.get(content) != null) {
                        tx.update(document, declaredField.getName(), declaredField.get(content));
                    }
                }
                declaredField.setAccessible(accessible);
            }
            return "Account successfully updated!";
        });

        return getApiFuture(futureTx);
    }

    /**
     * Get the account info associated with the given email. 
     * Doesn't send back sensitive information.
     * 
     * @param email Email linked to the account to retrieve.
     * @return The account object linked to the email.
     * @throws IllegalArgumentException When an account with the email doesn't exist.
     * @throws Exception When an unexpected exception occurs.
     */
    @NonNull
    public Account get(String email) throws Exception {
        ApiFuture<Account> futureTx = FirestoreClient.getFirestore().runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(collection.document(email)).get();
            if (snapshot.exists()) {
                Account account = Objects.requireNonNull(snapshot.toObject(Account.class));
                return Account.filterSensitiveData(account);
            } else {
                throw new IllegalArgumentException("Account with that email not found");
            }
        });

        return getApiFuture(futureTx);
    }

    
    /**
     * Given a username, determines if an account with that username exists.
     * Because the account documents in Firestore are labelled by the account's email, this method is useful
     * to determine if an account containing the username exists without having to specify the account's email.
     * 
     * @param username Username possibly linked to an account.
     * @return True if an account with that username exists, false otherwise.
     * @throws Exception When an unexpected exception occurs.
     */
    @NonNull
    public boolean usernameExists(String username) throws Exception {
        ApiFuture<Boolean> futureTx = FirestoreClient.getFirestore().runTransaction(transaction -> {
            // Using a parallel stream, check if any document has the username specified.
            return StreamSupport.stream(collection.listDocuments().spliterator(), true)
                    .map(document -> {
                        try {
                            return document.get().get();
                        } catch (InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .anyMatch(snap -> username.equals(snap.get("username")));
        });

        return getApiFuture(futureTx);
    }
}