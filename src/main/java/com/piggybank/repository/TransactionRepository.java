package com.piggybank.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.internal.NonNull;
import com.piggybank.model.Account;
import com.piggybank.model.Transaction;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Repository
public class TransactionRepository extends PBRepository {
    private final CollectionReference accountCollection;

    public TransactionRepository(Environment env) {
        super(Objects.requireNonNull(env.getProperty("firebase.database.labels.transactions")));
        String accountsLabel = Objects.requireNonNull(env.getProperty("firebase.database.labels.accounts"));
        accountCollection = FirestoreClient.getFirestore().collection(accountsLabel);
    }

    /**
     * todo
     * @param message
     * @return
     */
    @NonNull
    public String test(String message) {
        return message == null ?
                "Success! No message supplied" :
                "Success! Here is your message: " + message;
    }

    /**
     * todo
     * @param bankTxn
     * @return
     * @throws Exception
     */
    @NonNull
    public String bankTxn(@NonNull Transaction bankTxn) throws Exception {
        if (bankTxn.getType() != Transaction.TransactionType.BANK) {
            throw new IllegalArgumentException("Transaction type does not match (must be type BANK)");
        }

        ApiFuture<String> futureTx = FirestoreClient.getFirestore().runTransaction(tx -> {
            DocumentReference document = accountCollection.document(bankTxn.getTransactorEmail());
            DocumentSnapshot snapshot = document.get(FieldMask.of("balance", "transactionIds")).get();
            Account transactor = snapshot.toObject(Account.class);

            if (!snapshot.exists() || transactor == null) {
                throw new IllegalArgumentException("Account associated with txn transactor doesn't exist.");
            } else {
                // Transaction amount can't be more than current balance
                if (transactor.getBalance() < bankTxn.getAmount()) {
                    throw new IllegalArgumentException("Transaction amount exceeds account balance!");
                }

                String id = UUID.randomUUID().toString();
                ApiFuture<WriteResult> createFuture = collection.document(id).create(bankTxn);

                // Add transaction ID to account's list and remove transaction amount from account's balance.
                // transactor.getTransactionIds().add(id);
                if(transactor.getTransactionIds() == null) {
                    transactor.setTransactionIds(new ArrayList<String>());
                }
                transactor.addTransaction(id);

                tx.update(document, "transactionIds", transactor.getTransactionIds());
                tx.update(document, "balance", transactor.getBalance() - bankTxn.getAmount());
                
                createFuture.get();
                System.out.println("HELLO WORLD");
                return "Transaction successful!";
            }
        });

        return getApiFuture(futureTx);
    }

    /**
     * todo
     * @param peerTxn
     * @return
     * @throws Exception
     */
    @NonNull
    public Object peerTxn(Transaction peerTxn) throws Exception {
        if (peerTxn.getType() != Transaction.TransactionType.PEER_TO_PEER) {
            throw new IllegalArgumentException("Transaction type does not match (must be type PEER_TO_PEER)");
        }

        ApiFuture<String> futureTx = FirestoreClient.getFirestore().runTransaction(tx -> {
            DocumentReference transactorDoc = accountCollection.document(peerTxn.getTransactorEmail());
            DocumentReference recipientDoc = accountCollection.document(peerTxn.getRecipientEmail());

            DocumentSnapshot transactorSnap = transactorDoc.get(FieldMask.of("balance", "transactionIds")).get();
            DocumentSnapshot recipientSnap = recipientDoc.get(FieldMask.of("balance", "transactionIds")).get();

            Account transactor = transactorSnap.toObject(Account.class);
            Account recipient = recipientSnap.toObject(Account.class);

            if (!transactorSnap.exists() || transactor == null) {
                throw new IllegalArgumentException("Account does not exist specified by the transactor email");
            } else if (!recipientSnap.exists() || recipient == null) {
                throw new IllegalArgumentException("Account does not exist specified by the recipient email");
            } else {
                // Transaction amount can't be more than current balance
                if (transactor.getBalance() < peerTxn.getAmount()) {
                    throw new IllegalArgumentException("Transaction amount exceeds transactor's account balance!");
                }

                String id = UUID.randomUUID().toString();
                ApiFuture<WriteResult> createFuture = collection.document(id).create(peerTxn);


                transactor.addTransaction(peerTxn.getId());
                recipient.addTransaction(peerTxn.getId());
                tx.update(transactorDoc, "transactionIds", transactor.getTransactionIds());
                tx.update(recipientDoc, "transactionIds", recipient.getTransactionIds());
                // Update transaction ID lists and balances in both the transactor and recipient documents.
                transactor.getTransactionIds().add(id);
                recipient.getTransactionIds().add(id);
                tx.update(transactorDoc, "transactionIds", transactor.getTransactionIds());
                tx.update(recipientDoc, "transactionIds", recipient.getTransactionIds());
                tx.update(transactorDoc, "balance", transactor.getBalance() - peerTxn.getAmount());
                tx.update(recipientDoc, "balance", recipient.getBalance() + peerTxn.getAmount());

                createFuture.get();
                return "Transaction successful!";
            }
        });

        return getApiFuture(futureTx);
    }

    /**
     * todo
     * @param txnId
     * @return
     * @throws Exception
     */
    @NonNull
    public Transaction getTxn(String txnId) throws Exception {
        for (DocumentReference document : collection.listDocuments()) {
            if (document.getId().equals(txnId)) {
                Transaction txn = getApiFuture(document.get()).toObject(Transaction.class);
                if (txn == null) {
                    break;
                } else {
                    return txn;
                }
            }
        }

        throw new IllegalArgumentException("Transaction with that ID doesn't exist");
    }

    /**
     * todo
     * @param email
     * @return
     * @throws Exception
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