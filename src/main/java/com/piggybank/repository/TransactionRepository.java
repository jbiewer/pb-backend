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

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * todo
 */
@Repository
public class TransactionRepository extends PBRepository {
    private final CollectionReference accountCollection;

    /**
     * todo
     * @param env
     */
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
                System.out.println("tx amount: "+bankTxn.getAmount());

                bankTxn.setId(UUID.randomUUID().toString());

                // Add transaction ID to account's list and remove transaction amount from account's balance.
                transactor.getTransactionIds().add(bankTxn.getId());
                tx.update(document, new HashMap<>() {{
                    put("transactionIds", transactor.getTransactionIds());
                    put("balance", transactor.getBalance() - bankTxn.getAmount());
                }});

                tx.create(collection.document(bankTxn.getId()), bankTxn);
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
    public Object processPeerTxn(Transaction peerTxn) throws Exception {
        if (peerTxn.getType() != Transaction.TransactionType.PEER_TO_PEER) {
            throw new IllegalArgumentException("Transaction type does not match (must be type PEER_TO_PEER)");
        }
        if (peerTxn.getAmount() == null) {
            throw new IllegalArgumentException("Amount not specified");
        }

        ApiFuture<String> futureTx = FirestoreClient.getFirestore().runTransaction(tx -> {
            DocumentReference transactorDoc = accountCollection.document(peerTxn.getTransactorEmail());
            DocumentReference recipientDoc = accountCollection.document(peerTxn.getRecipientEmail());

            DocumentSnapshot transactorSnap = transactorDoc.get(FieldMask.of("balance", "transactionIds")).get();
            DocumentSnapshot recipientSnap = recipientDoc.get(FieldMask.of("balance", "transactionIds")).get();

            Account transactor = transactorSnap.toObject(Account.class);
            Account recipient = recipientSnap.toObject(Account.class);

            if (!transactorSnap.exists() || transactor == null) {
                throw new IllegalArgumentException("Account associated with transactor doesn't exist");
            } else if (!recipientSnap.exists() || recipient == null) {
                throw new IllegalArgumentException("Account associated with recipient doesn't exist");
            } else {
                // Transaction amount can't be more than current balance
                if (transactor.getBalance() < peerTxn.getAmount()) {
                    throw new IllegalArgumentException("Transaction amount exceeds transactor's account balance");
                }

                peerTxn.setId(UUID.randomUUID().toString());

                // Update transaction ID lists and balances in both the transactor and recipient documents.
                transactor.getTransactionIds().add(peerTxn.getId());
                recipient.getTransactionIds().add(peerTxn.getId());
                tx.update(transactorDoc, new HashMap<>() {{
                    put("transactionIds", transactor.getTransactionIds());
                    put("balance", transactor.getBalance() - peerTxn.getAmount());
                }});
                tx.update(recipientDoc, new HashMap<>() {{
                    put("transactionIds", recipient.getTransactionIds());
                    put("balance", recipient.getBalance() + peerTxn.getAmount());
                }});

                tx.create(collection.document(peerTxn.getId()), peerTxn);
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