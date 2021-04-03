package com.piggybank.repository;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import com.piggybank.model.Account;
import com.piggybank.model.Transaction;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static com.piggybank.mocks.MockModels.*;
import static com.piggybank.util.FirebaseEmulatorServices.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class TransactionRepositoryTest {
    
    @Autowired private TransactionRepository txnRepository;
    @Autowired private AccountRepository accRepository; 

    /**
     * Load the fake documents into Firestore before each test.
     */
    @BeforeEach
    public void beforeEach() throws IOException, URISyntaxException, ExecutionException, InterruptedException {
        URI uri = Objects.requireNonNull(ClassLoader.getSystemResource("collections")).toURI();
        loadFirestoreDocuments(new File(uri));
    }

    /**
     * Clear the fake documents from Firestore after each test.
     */
    @AfterEach
    public void afterEach() throws IOException, InterruptedException {
        clearFirestoreDocuments();
    }

    @Test
    public void testWithMessageSucceeds() {
        String result = txnRepository.test("test");
        assertEquals("Success! Here is your message: test", result);
    }

    @Test
    public void testGetAllTxnFromUserSucceeds() {
        try {
            List<Transaction> userTxns = txnRepository.getAllTxnFromUser("user1@email.com");
            assertEquals(userTxns.size(), 3);
            assertEquals(userTxns.get(0).getId(), "tx-id0");
            assertEquals(userTxns.get(1).getId(), "tx-id1");
            assertEquals(userTxns.get(2).getId(), "tx-id2");
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testGetTxnSucceeds() {
        try {
            Transaction txn1 = txnRepository.getTxn("tx-id1");
            Transaction txn4 = txnRepository.getTxn("tx-id4");
            Transaction txn7 = txnRepository.getTxn("tx-id7");
            assertEquals(txn1.getTransactorEmail(), "user1@email.com");
            assertEquals(txn4.getTransactorEmail(), "user2@email.com");
            assertEquals(txn7.getTransactorEmail(), "jbiewer@wisc.edu");
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testBankTxnUpdatesBalance() {
        try {
            //transaction: send $10 to bank
            Transaction txn = new Transaction(Transaction.TransactionType.BANK);
            txn.setId("tx-id9");
            txn.setTransactorEmail("user1@email.com");
            txn.setRecipientEmail(null);
            txn.setAmount(10);

            float initialBalance = accRepository.get("user1@email.com").getBalance(); 
            txnRepository.bankTxn(txn);
            float finalBalance = accRepository.get("user1@email.com").getBalance(); 
            assertEquals(txn.getAmount(), initialBalance - finalBalance);
        } catch (Exception e) {
            fail(e); 
        }
    }

    @Test
    public void testPeer2PeerTxnUpdatesBalances() {
        try {
            //transaction: user2 send $10 to user1
            Transaction txn = new Transaction(Transaction.TransactionType.PEER_TO_PEER);
            txn.setId("tx-id10");
            txn.setTransactorEmail("user2@email.com");
            txn.setRecipientEmail("user1@email.com");
            txn.setAmount(10);
            float user1InitialBalance = accRepository.get("user1@email.com").getBalance(); 
            float user2InitialBalance = accRepository.get("user2@email.com").getBalance();
            txnRepository.peerTxn(txn);
            float user2FinalBalance = accRepository.get("user2@email.com").getBalance();
            float user1FinalBalance = accRepository.get("user1@email.com").getBalance(); 

            float user1NetProfit = user1FinalBalance - user1InitialBalance; 
            float user2NetProfit = user2InitialBalance - user2FinalBalance; 
            //Maybe find a better way to do this (rounding to nearest hundredth?)
            assert(txn.getAmount() < user1NetProfit + 0.01 && txn.getAmount() > user1NetProfit - 0.01);
            assert(txn.getAmount() < user2NetProfit + 0.01 && txn.getAmount() > user2NetProfit - 0.01);
        } catch (Exception e) {
            fail(e); 
        }
    }

}
