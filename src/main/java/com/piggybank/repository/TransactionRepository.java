package com.piggybank.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldMask;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.internal.NonNull;
import com.piggybank.model.Account;
import com.piggybank.model.Transaction;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.piggybank.model.Account.AccountType;

/**
 * Interface for database interactions for transactions.
 */
@Repository
public class TransactionRepository extends PBRepository {
    private final CollectionReference accountCollection;

    /**
     * Initializes the collection reference to the value at the specified property location
     * in application.yml.
     *
     * @param env Environment containing properties.
     */
    public TransactionRepository(Environment env) {
        super(Objects.requireNonNull(env.getProperty("firebase.database.labels.transactions")));
        String accountsLabel = Objects.requireNonNull(env.getProperty("firebase.database.labels.accounts"));
        accountCollection = FirestoreClient.getFirestore().collection(accountsLabel);
    }

    /**
     * Tests the TransactionController interface.
     *
     * @return Success message.
     */
    @NonNull
    public String test(String message) {
        return message == null ?
                "Success! No message supplied" :
                "Success! Here is your message: " + message;
    }

    /**
     * Processes a bank transaction by taking the amount specified by the transaction object and negating it from
     * the account (represented by the transactor email) and then transferring it to the account's bank account.
     *
     * @param bankTxn Bank transaction information.
     * @return Message indicating success.
     * @throws IllegalArgumentException When the transaction type is not of type BANK, the amount is not specified,
     *                                  the account w/ email 'transactorEmail' does not exist, or the amount in the
     *                                  transaction exceeds the account's balance.
     * @throws Exception For any internal error.
     */
    @NonNull
    public String processBankTxn(@NonNull Transaction bankTxn) throws Exception {
        if (bankTxn.getType() != Transaction.TransactionType.BANK) {
            throw new IllegalArgumentException("Transaction type does not match (must be type BANK)");
        }
        if (bankTxn.getAmount() == null) {
            throw new IllegalArgumentException("Amount not specified");
        }

        ApiFuture<String> futureTx = FirestoreClient.getFirestore().runTransaction(tx -> {
            DocumentReference document = accountCollection.document(bankTxn.getTransactorEmail());
            DocumentSnapshot snapshot = document.get(FieldMask.of("balance", "transactionIds")).get();
            Account transactor = snapshot.toObject(Account.class);

            if (!snapshot.exists() || transactor == null) {
                throw new IllegalArgumentException("Account associated with transactor doesn't exist");
            } else {
                // Transaction amount can't be more than current balance
                if (transactor.getBalance() < bankTxn.getAmount()) {
                    throw new IllegalArgumentException("Transaction amount exceeds account balance");
                }

                // Create a new transaction document and associate it with the transactor.
                bankTxn.setId(UUID.randomUUID().toString());

                transactor.getTransactionIds().add(bankTxn.getId());
                tx.update(document, new HashMap<>() {{
                    put("transactionIds", transactor.getTransactionIds());
                    put("balance", transactor.getBalance() - bankTxn.getAmount());
                }});

                // This is where we would add the balance to the bank account, but we can't do that :(

                tx.create(collection.document(bankTxn.getId()), bankTxn);
                return "Transaction successful!";
            }
        });

        return getApiFuture(futureTx);
    }

    /**
     * Processes a peer-to-peer transaction by taking the amount specified by the transaction object and negating it
     * from the account represented by the transactor email and adding to the account represented by the recipient email.
     * The only two types of peer-to-peer transactions is customer to merchant and customer to customer.
     *
     * @param peerTxn Peer-to-peer transaction information.
     * @return Message indicating success.
     * @throws IllegalArgumentException When the transaction type is not of type PEER_TO_PEER, the amount is not
     *                                  specified, the account w/ email 'transactorEmail' does not exist, the account
     *                                  w/ email 'recipientEmail' does not exist, or the amount in the transaction
     *                                  exceeds the transacting account's balance.
     * @throws Exception For any internal error.
     */
    @NonNull
    public Object processPeerTxn(Transaction peerTxn) throws Exception {
        if (peerTxn.getType() != Transaction.TransactionType.PEER_TO_PEER) {
            throw new IllegalArgumentException("Transaction type does not match (must be type PEER_TO_PEER)");
        }
        if (peerTxn.getAmount() == null) {
            throw new IllegalArgumentException("Amount not specified");
        }
        if (peerTxn.getTransactorEmail().equals(peerTxn.getRecipientEmail())) {
            throw new IllegalArgumentException("Transactor and recipient emails must be different");
        }

        ApiFuture<String> futureTx = FirestoreClient.getFirestore().runTransaction(tx -> {
            DocumentReference transactorDoc = accountCollection.document(peerTxn.getTransactorEmail());
            DocumentReference recipientDoc = accountCollection.document(peerTxn.getRecipientEmail());

            DocumentSnapshot transactorSnap = transactorDoc.get(FieldMask.of("type", "balance", "transactionIds")).get();
            DocumentSnapshot recipientSnap = recipientDoc.get(FieldMask.of("type", "balance", "transactionIds")).get();

            Account transactor = transactorSnap.toObject(Account.class);
            Account recipient = recipientSnap.toObject(Account.class);

            // Ensure documents exists.
            if (!transactorSnap.exists() || transactor == null) {
                throw new IllegalArgumentException("Account associated with transactor doesn't exist");
            } else if (!recipientSnap.exists() || recipient == null) {
                throw new IllegalArgumentException("Account associated with recipient doesn't exist");
            }

            // Ensure transactor is a customer
            if (recipient.getType() != AccountType.CUSTOMER) {
                throw new IllegalArgumentException("Recipient can only be a customer");
            }

            peerTxn.setId(UUID.randomUUID().toString());

            // Update transaction ID lists and balances in both the transactor and recipient documents.
            transactor.getTransactionIds().add(peerTxn.getId());
            recipient.getTransactionIds().add(peerTxn.getId());
            Map<String, Object> transactorFields = new HashMap<>() {{
                put("transactionIds", transactor.getTransactionIds());
            }};
            if (transactor.getType() == AccountType.CUSTOMER) {
                // Transaction amount can't be more than current balance
                if (transactor.getBalance() < peerTxn.getAmount()) {
                    throw new IllegalArgumentException("Transaction amount exceeds transactor's account balance");
                }
                transactorFields.put("balance", transactor.getBalance() - peerTxn.getAmount());
            } else {
                // This is where we would transfer from merchant's bank account to the customer's balance.
                // Can't legally do this yet. :(
            }
            tx.update(transactorDoc, transactorFields);
            tx.update(recipientDoc, new HashMap<>() {{
                put("transactionIds", recipient.getTransactionIds());
                put("balance", recipient.getBalance() + peerTxn.getAmount());
            }});

            tx.create(collection.document(peerTxn.getId()), peerTxn);
            return "Transaction successful!";
        });

        return getApiFuture(futureTx);
    }

    /**
     * Retrieves a transaction given the ID of the transaction.
     *
     * @param txnId ID of the transaction to retrieve.
     * @return Message indicating success.
     * @throws IllegalArgumentException When the transaction w/ the specified ID doesn't exist.
     * @throws Exception For any internal error.
     */
    @NonNull
    public Transaction getTxn(String txnId) throws Exception {
        for (DocumentReference document : collection.listDocuments()) {
            if (document.getId().equals(txnId)) {
                DocumentSnapshot snap = document.get().get();
                Transaction txn = snap.toObject(Transaction.class);
                if (!snap.exists() || txn == null) {
                    break;
                } else {
                    return txn;
                }
            }
        }
        throw new IllegalArgumentException("Transaction with that ID doesn't exist");
    }

    /**
     * Retrieves a list of transactions where the IDs of each are contained in the transactionIds field of
     * the account represented by the email specified.
     *
     * @param email Email of the account to get the list of transaction IDs from.
     * @return Message indicating success.
     * @throws IllegalArgumentException When the account with the specified email doesn't exist.
     * @throws Exception For any internal error.
     */
    @NonNull
    public List<Transaction> getAllTxnFromUser(String email) throws Exception {
        ApiFuture<List<Transaction>> futureTx = FirestoreClient.getFirestore().runTransaction(tx -> {
            // Account to get transactions from.
            DocumentSnapshot snapshot = accountCollection.document(email).get(FieldMask.of("transactionIds")).get();
            Account account = snapshot.toObject(Account.class);
            if (!snapshot.exists() || account == null) {
                throw new IllegalArgumentException("Account with that email not found");
            }
            
            // Select all the transactions whose ID is in the account's transaction ID list.
            return account.getTransactionIds().parallelStream()
                    .map(id -> {
                        try {
                            return collection.document(id).get().get();
                        } catch (InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .filter(DocumentSnapshot::exists)
					.map(snap -> snap.toObject(Transaction.class))
                    .filter(Objects::nonNull)
					.collect(Collectors.toList());
        });

        return getApiFuture(futureTx);
    }
}