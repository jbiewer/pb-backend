package com.piggybank.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
import com.piggybank.model.Account;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

import static com.piggybank.model.Account.AccountType;
import static com.piggybank.util.Util.ifNonNull;
import static com.piggybank.util.Util.ifNull;

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
    public String create(@NonNull Account newAccount) throws Exception {
        ifNull(newAccount.getType()).thenThrow(new IllegalArgumentException("Must specify account type"));
        if (newAccount.getType() == AccountType.MERCHANT) {
            ifNull(newAccount.getBankAccount()).thenThrow(new IllegalArgumentException("Merchant account must have a bank account"));
        }
        ifNull(newAccount.getEmail()).thenThrow(new IllegalArgumentException("Must specify account email"));
        ifNull(newAccount.getPassword()).thenThrow(new IllegalArgumentException("Must specify account password"));

        //create document where id = email
        getApiFuture(collection.document(newAccount.getEmail()).create(newAccount));
        return "Account created successfully!";
    }

    /**
     * todo
     * @param email
     * @param password
     * @return
     * @throws Exception
     */
    public String login(@NonNull String email, @NonNull String password) throws Exception {
        ApiFuture<String> futureTx = FirestoreClient.getFirestore().runTransaction(tx -> {
            // Confirm account exists.
            DocumentSnapshot snapshot = tx.get(collection.document(email)).get();
            if (!snapshot.exists()) {
                throw new IllegalArgumentException("Account with that email not found");
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
     * Updates the account info if one exists with given email. If fields of the Account
     * object parameter are not null, the fields of the account associated with the email parameter
     * are updated
     * 
     * @param email - username of account to update
     * @param content - object containing fields that need updating
     * @return todo
     */
    public String update(@NonNull String email, @NonNull Account content) throws Exception {
        ApiFuture<String> futureTx = FirestoreClient.getFirestore().runTransaction(tx -> {
            String currentEmail = email;

            // Copy document over to newly labelled document, if new username was specified.
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

            // Change other fields if requested, except for transaction IDs.
            DocumentReference document = collection.document(currentEmail);
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
     * Get the account info associated with the given email. 
     * Should not send back sensitive information
     * 
     * @param email - email linked to the account of interest
     * @return todo
     */
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
     * 
     * @param username
     * @return
     * @throws Exception
     */
    public boolean usernameExists(String username) throws Exception {
        ApiFuture<Boolean> futureTx = FirestoreClient.getFirestore().runTransaction(transaction -> {
//            Optional<DocumentSnapshot> snapshot = StreamSupport.stream(collection.listDocuments().spliterator(), true)
//                    .map(document -> {
//                        try { return document.get().get(); }
//                        catch (InterruptedException | ExecutionException e) { return null; }
//                    })
//                    .filter(Objects::nonNull)
//                    .filter(snap -> username.equals(snap.get("username")))
//                    .findFirst();
//            return snapshot.isPresent();

            //get all account documents in db
            ApiFuture<QuerySnapshot> orderFuture = collection.get();
            List<QueryDocumentSnapshot> orderDocuments = orderFuture.get().getDocuments();
            //return true if one of the docs' matches input username
            for(QueryDocumentSnapshot doc: orderDocuments) {
                if(doc.toObject(Account.class).getUsername().equals(username)) {
                    return true;
                }
            }
            return false;
        });

        return getApiFuture(futureTx);
    }
}