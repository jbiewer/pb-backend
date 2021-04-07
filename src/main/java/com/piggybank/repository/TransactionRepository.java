package com.piggybank.repository;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.internal.NonNull;
import com.piggybank.model.Account;
import com.piggybank.model.Transaction;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;

@Repository
public class TransactionRepository extends PBRepository {

    private final CollectionReference accountCollection; 

    public TransactionRepository(Environment env) {
        super(Objects.requireNonNull(env.getProperty("firebase.database.labels.transactions")));
        String txnLabel = env.getProperty("firebase.database.labels.accounts");
        Firestore firestore = FirestoreClient.getFirestore();
        accountCollection = firestore.collection(Objects.requireNonNull(txnLabel));
    }

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
            //account of transactor
            DocumentSnapshot accountSnapshot = tx.get(accountCollection.document(bankTxn.getTransactorEmail())).get();
            Account accountObject = accountSnapshot.toObject(Account.class);

            if (!accountSnapshot.exists() || accountObject == null) {
                throw new IllegalArgumentException("Account associated with txn transactor doesn't exist.");
            } else {
                //reference to account document
                DocumentReference accountDoc = accountCollection.document(bankTxn.getTransactorEmail());

                //Transaction amount can't be more than current balance
                if (accountObject.getBalance() < bankTxn.getAmount()) {
                    throw new IllegalArgumentException("Transaction amount exceeds account balance!");
                }

                //update transaction id list in account document
                List<String> transactionIds = accountObject.getTransactionIds(); 
                transactionIds.add(bankTxn.getId());
                tx.update(accountDoc, "transactionIds", transactionIds);
                
                //remove transacted amount from account balance
                tx.update(accountDoc, "balance", accountObject.getBalance() - bankTxn.getAmount());
                
                //create transaction in transactions collection
                getApiFuture(collection.document(bankTxn.getId()).create(bankTxn));
                
                
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
            //account of transactor
            DocumentSnapshot transactorSnapshot = tx.get(accountCollection.document(peerTxn.getTransactorEmail())).get();
            Account transactorObject = transactorSnapshot.toObject(Account.class);

            DocumentSnapshot recipientSnapshot = tx.get(accountCollection.document(peerTxn.getRecipientEmail())).get();
            Account recipientObject = recipientSnapshot.toObject(Account.class);

            if (!transactorSnapshot.exists() || !recipientSnapshot.exists() || transactorObject == null || recipientObject == null) {
                throw new IllegalArgumentException("Account associated with txn transactor or recipient doesn't exist.");
            } else {
                //reference to account document
                DocumentReference transactorDoc = accountCollection.document(peerTxn.getTransactorEmail());
                DocumentReference recipientDoc = accountCollection.document(peerTxn.getRecipientEmail());

                //Transaction amount can't be more than current balance
                if (transactorObject.getBalance() < peerTxn.getAmount()) {
                    throw new IllegalArgumentException("Transaction amount exceeds transactor's account balance!");
                }

                //update transaction id lists in account documents for both transactor and recipient
                List<String> transactorIds = transactorObject.getTransactionIds(); 
                List<String> recipientIds = recipientObject.getTransactionIds();
                transactorIds.add(peerTxn.getId());
                recipientIds.add(peerTxn.getId());
                tx.update(transactorDoc, "transactionIds", transactorIds);
                tx.update(recipientDoc, "transactionIds", recipientIds);

                //update account balances for both transactor and recipient
                tx.update(transactorDoc, "balance", transactorObject.getBalance() - peerTxn.getAmount());
                tx.update(recipientDoc, "balance", recipientObject.getBalance() + peerTxn.getAmount());

                //create transaction in transactions collection
                getApiFuture(collection.document(peerTxn.getId()).create(peerTxn));
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
        ApiFuture<Transaction> futureTx = FirestoreClient.getFirestore().runTransaction(transaction -> {
            //find account with matching email
            // DocumentSnapshot snapshot = transaction.get(accountCollection.document(email)).get();
            // if (snapshot.exists()) {
                List<QueryDocumentSnapshot> documents = collection.get().get().getDocuments(); 
                //list of all Transaction objects in database
                List<Transaction> transactionList = documents.parallelStream()
					.map(documentSnapshot -> documentSnapshot.toObject(Transaction.class))
					.collect(Collectors.toList());

                //find transaction that matches input string

                return transactionList.stream()
                        .filter(currentTransaction -> currentTransaction.getId().equals(txnId))
                        .findAny().orElse(null);
            // } else {
            //     throw new IllegalArgumentException("Account with that email not found");
            // }
        });

        return getApiFuture(futureTx);
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
            //asynchronously retrieve all documents
            List<QueryDocumentSnapshot> documents = collection.get().get().getDocuments();

            //Account to get transactions from
            DocumentSnapshot snapshot = tx.get(accountCollection.document(email)).get();
            Account account = snapshot.toObject(Account.class);
            if (!snapshot.exists() || account == null) {
                throw new IllegalArgumentException("Account with that email not found");
            }
            
            //select all txns in transactions collection that also appear in user's txnId list
            List<Transaction> transactions = documents.parallelStream()
					.map(documentSnapshot -> documentSnapshot.toObject(Transaction.class))
                    .filter(txn -> account.getTransactionIds().contains(txn.getId()))
					.collect(Collectors.toList());

            if (transactions.isEmpty()) throw new Exception("Transaction list is empty");
            return transactions;
        });

        return getApiFuture(futureTx);
    }
}