package com.piggybank.repository;

import com.piggybank.model.Transaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static com.piggybank.util.FirebaseEmulatorServices.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * todo
 */
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

    /**
     * The test() method succeeds given no message (message is null).
     */
    @Test
    public void testWithoutMessageSucceeds() {
        String result = txnRepository.test(null);
        assertEquals("Success! No message supplied", result);
    }

    /**
     * The test() method succeeds given a message.
     */
    @Test
    public void testWithMessageSucceeds() {
        String result = txnRepository.test("test");
        assertEquals("Success! Here is your message: test", result);
    }

    /**
     * The processesBankTxn() method succeeds if a $10 transaction to the user's bank is processed.
     */
    @Test
    public void processBankTxnSucceeds() {
        // User1 sends $10 to their bank.
        Transaction txn = new Transaction(Transaction.TransactionType.BANK);
        txn.setTransactorEmail("user1@email.com");
        txn.setAmount(1000L);

        try {
            long initialBalance = accRepository.get("user1@email.com").getBalance();
            txnRepository.processBankTxn(txn);
            long finalBalance = accRepository.get("user1@email.com").getBalance();
            assertEquals(txn.getAmount(), initialBalance - finalBalance);
            assertNotNull(getFromFirestore("Transactions", txn.getId(), Transaction.class));
        } catch (Exception e) {
            fail(e); 
        }
    }

    /**
     * The processesBankTxn() method fails since the transaction type is not of type BANK.
     */
    @Test
    public void processBankTxnFailsInvalidType() {
        // User1 sends $10 to their bank.
        Transaction txn = new Transaction(Transaction.TransactionType.PEER_TO_PEER);
        txn.setTransactorEmail("user1@email.com");
        txn.setAmount(1000L);

        try {
            txnRepository.processBankTxn(txn);
            fail("IllegalArgumentException not thrown when it should have.");
        } catch (IllegalArgumentException e) {
            assertEquals("Transaction type does not match (must be type BANK)", e.getMessage());
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * The processesBankTxn() method fails since the amount was not specified.
     */
    @Test
    public void processBankTxnFailsNoAmount() {
        // User1 sends $10 to their bank.
        Transaction txn = new Transaction(Transaction.TransactionType.BANK);
        txn.setTransactorEmail("user1@email.com");

        try {
            txnRepository.processBankTxn(txn);
            fail("IllegalArgumentException not thrown when it should have.");
        } catch (IllegalArgumentException e) {
            assertEquals("Amount not specified", e.getMessage());
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * The processesBankTxn() method fails since the transactor's account doesn't exist.
     */
    @Test
    public void processBankTxnFailsNoTransactorExists() {
        // User1 sends $10 to their bank.
        Transaction txn = new Transaction(Transaction.TransactionType.BANK);
        txn.setTransactorEmail("invalid-email");
        txn.setAmount(1000L);

        try {
            txnRepository.processBankTxn(txn);
            fail("IllegalArgumentException not thrown when it should have");
        } catch (IllegalArgumentException e) {
            assertEquals("Account associated with transactor doesn't exist", e.getMessage());
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * The processesBankTxn() method fails since they don't have enough funds in their account to transfer.
     */
    @Test
    public void processBankTxnFailsLowBalance() {
        // User1 sends $10 to their bank.
        Transaction txn = new Transaction(Transaction.TransactionType.BANK);
        txn.setTransactorEmail("jbiewer@wisc.edu");
        txn.setAmount(1000L);

        try {
            txnRepository.processBankTxn(txn);
            fail("IllegalArgumentException not thrown when it should have");
        } catch (IllegalArgumentException e) {
            assertEquals("Transaction amount exceeds account balance", e.getMessage());
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     *
     */
    @Test
    public void processPeerTxnSucceeds() {
        // User2 sends $10 to User1
        Transaction txn = new Transaction(Transaction.TransactionType.PEER_TO_PEER);
        txn.setTransactorEmail("user2@email.com");
        txn.setRecipientEmail("user1@email.com");
        txn.setAmount(1000L);

        try {
            long user1InitialBalance = accRepository.get("user1@email.com").getBalance();
            long user2InitialBalance = accRepository.get("user2@email.com").getBalance();
            txnRepository.processPeerTxn(txn);

            long user2FinalBalance = accRepository.get("user2@email.com").getBalance();
            long user1FinalBalance = accRepository.get("user1@email.com").getBalance();

            assertEquals(txn.getAmount(), user1FinalBalance - user1InitialBalance);
            assertEquals(txn.getAmount(), user2InitialBalance - user2FinalBalance);
            assertNotNull(getFromFirestore("Transactions", txn.getId(), Transaction.class));
        } catch (Exception e) {
            fail(e); 
        }
    }

    /**
     * The processesPeerTxn() method fails since the transaction type is not of type PEER_TO_PEER.
     */
    @Test
    public void processPeerTxnFailsInvalidType() {
        // User2 sends $10 to their User1.
        Transaction txn = new Transaction(Transaction.TransactionType.BANK);
        txn.setTransactorEmail("user2@email.com");
        txn.setRecipientEmail("user1@email.com");
        txn.setAmount(1000L);

        try {
            txnRepository.processPeerTxn(txn);
            fail("IllegalArgumentException not thrown when it should have.");
        } catch (IllegalArgumentException e) {
            assertEquals("Transaction type does not match (must be type PEER_TO_PEER)", e.getMessage());
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * The processesPeerTxn() method fails since the amount was not specified.
     */
    @Test
    public void processPeerTxnFailsNoAmount() {
        // User1 sends $10 to their bank.
        Transaction txn = new Transaction(Transaction.TransactionType.PEER_TO_PEER);
        txn.setTransactorEmail("user1@email.com");

        try {
            txnRepository.processPeerTxn(txn);
            fail("IllegalArgumentException not thrown when it should have.");
        } catch (IllegalArgumentException e) {
            assertEquals("Amount not specified", e.getMessage());
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * The processesPeerTxn() method fails since either the transactor or recipient accounts don't exist.
     */
    @Test
    public void processPeerTxnFailsNoTransactorOrRecipientExists() {
        // User2 sends $10 to their User1.
        Transaction nullTransactorTxn = new Transaction(Transaction.TransactionType.PEER_TO_PEER);
        nullTransactorTxn.setTransactorEmail("invalid-email");
        nullTransactorTxn.setRecipientEmail("user1@email.com");
        nullTransactorTxn.setAmount(1000L);

        Transaction nullRecipientTxn = new Transaction(Transaction.TransactionType.PEER_TO_PEER);
        nullRecipientTxn.setTransactorEmail("user2@email.com");
        nullRecipientTxn.setRecipientEmail("invalid-email");
        nullRecipientTxn.setAmount(1000L);

        try {
            txnRepository.processPeerTxn(nullTransactorTxn);
            fail("IllegalArgumentException not thrown when it should have.");
        } catch (IllegalArgumentException e) {
            assertEquals("Account associated with transactor doesn't exist", e.getMessage());
        } catch (Exception e) {
            fail(e);
        }

        try {
            txnRepository.processPeerTxn(nullRecipientTxn);
            fail("IllegalArgumentException not thrown when it should have.");
        } catch (IllegalArgumentException e) {
            assertEquals("Account associated with recipient doesn't exist", e.getMessage());
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * The processesPeerTxn() method fails since the transactor doesn't have enough funds in their account to transfer.
     */
    @Test
    public void processPeerTxnFailsLowBalance() {
        // User2 sends $10 to their User1.
        Transaction txn = new Transaction(Transaction.TransactionType.PEER_TO_PEER);
        txn.setTransactorEmail("jbiewer@wisc.edu");
        txn.setRecipientEmail("user1@email.com");
        txn.setAmount(1000L);

        try {
            txnRepository.processPeerTxn(txn);
            fail("IllegalArgumentException not thrown when it should have.");
        } catch (IllegalArgumentException e) {
            assertEquals("Transaction amount exceeds transactor's account balance", e.getMessage());
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * todo
     */
    @Test
    public void getTxnSucceeds() {
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

    /**
     * todo
     */
    @Test
    public void getTxnFailsNotFound() {
        try {
            txnRepository.getTxn("invalid-txn-id");
            fail("IllegalArgumentException not thrown when it should have.");
        } catch (IllegalArgumentException e) {
            assertEquals("Transaction with that ID doesn't exist", e.getMessage());
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * todo
     */
    @Test
    public void getAllTxnFromUserSucceeds() {
        List<Transaction> userTxns = null;
        try {
            userTxns = txnRepository.getAllTxnFromUser("user1@email.com");
        } catch (Exception e) {
            fail(e);
        }
        assertEquals(userTxns.size(), 3);
        assertEquals(userTxns.get(0).getId(), "tx-id0");
        assertEquals(userTxns.get(1).getId(), "tx-id1");
        assertEquals(userTxns.get(2).getId(), "tx-id2");
    }

    /**
     * todo
     */
    @Test
    public void getAllTxnFromUserFailsEmailNotFound() {
        try {
            txnRepository.getAllTxnFromUser("invalid-email");
            fail("IllegalArgumentException not thrown when it should have.");
        } catch (IllegalArgumentException e) {
            assertEquals("Account with that email not found", e.getMessage());
        } catch (Exception e) {
            fail(e);
        }
    }
}
