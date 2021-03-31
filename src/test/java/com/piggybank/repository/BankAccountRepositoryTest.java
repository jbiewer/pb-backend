package com.piggybank.repository;

import com.piggybank.model.Account;
import com.piggybank.model.BankAccount;
import com.piggybank.model.Customer;
import com.piggybank.model.Merchant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static com.piggybank.mocks.MockModels.*;
import static com.piggybank.util.FirebaseEmulatorServices.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-testing suite for BankAccountRepository.
 * After starting a Spring application, injects the BankAccountRepository bean as a dependency to be used
 * for running unit tests on.
 */
@SpringBootTest
public class BankAccountRepositoryTest {

    @Autowired
    private BankAccountRepository repository;

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
        String result = repository.test(null);
        assertEquals("Success! No message supplied", result);
    }

    /**
     * The test() method succeeds given a message.
     */
    @Test
    public void testWithMessageSucceeds() {
        String result = repository.test("test");
        assertEquals("Success! Here is your message: test", result);
    }

    /**
     * The update() method succeeds with fake bank information.
     */
    @Test
    public void updateSucceedsExistingBankAccount() {
        String email = "user1@email.com";
        BankAccount bank = mockBankAccount();
        BankAccount storedBank;

        try {
            String result = repository.update(email, bank);
            assertEquals(result, "Bank account successfully updated!");

            storedBank = getFromFirestore("Accounts", email, Account.class).getBankAccount();
            assertEquals(bank, storedBank);
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * The update() method succeeds with fake bank information and the account in Firestore
     * currently doesn't have a bank account defined.
     */
    @Test
    public void updateSucceedsNoBankAccount() {
        String email = "jbiewer@wisc.edu";
        BankAccount bank = mockBankAccount();
        BankAccount storedBank;

        try {
            String result = repository.update(email, bank);
            assertEquals(result, "Bank account successfully updated!");

            storedBank = getFromFirestore("Accounts", email, Account.class).getBankAccount();
            assertEquals(bank, storedBank);
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * The update() method fails given an invalid email (email not found).
     */
    @Test
    public void updateFailsInvalidEmail() {
        BankAccount bank = mockBankAccount();
        try {
            repository.update("invalid-email", bank);
            fail("Did not throw exception with invalid email");
        } catch (IllegalArgumentException e) {
            assertEquals("Account with that email not found", e.getMessage());
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * The remove() method succeeds in removing an existing bank account.
     */
    @Test
    public void removeSucceedsExistingBankAccount() {
        String email = "user1@email.com";
        BankAccount storedBank;

        try {
            String result = repository.remove(email);
            assertEquals(result, "Bank account successfully removed!");

            storedBank = getFromFirestore("Accounts", email, Account.class).getBankAccount();
            assertNull(storedBank);
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * The remove() method succeeds even with no existing bank account.
     */
    @Test
    public void removeSucceedsNoBankAccount() {
        String email = "jbiewer@wisc.edu";
        BankAccount storedBank;

        try {
            String result = repository.remove(email);
            assertEquals(result, "Bank account successfully removed!");

            storedBank = getFromFirestore("Accounts", email, Account.class).getBankAccount();
            assertNull(storedBank);
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * The remove() method fails given an invalid email (email not found).
     */
    @Test
    public void removeFailsInvalidEmail() {
        try {
            repository.remove("invalid-email");
            fail("Did not throw exception with invalid email");
        } catch (IllegalArgumentException e) {
            assertEquals("Account with that email not found", e.getMessage());
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * The get() method succeeds in retrieving an existing bank account.
     */
    @Test
    public void getSucceedsExistingBankAccount() {
        String email = "user1@email.com";
        BankAccount bank, storedBank;

        try {
            bank = repository.get(email);
            assertNotNull(bank);
            storedBank = getFromFirestore("Accounts", email, Account.class).getBankAccount();
            assertEquals(bank, storedBank);
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * The get() method succeeds in returning null on no bank account existing.
     */
    @Test
    public void getSucceedsNoBankAccount() {
        String email = "jbiewer@wisc.edu";
        BankAccount bank, storedBank;

        try {
            bank = repository.get(email);
            assertNull(bank);
            storedBank = getFromFirestore("Accounts", email, Account.class).getBankAccount();
            assertEquals(bank, storedBank);
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * The get() method fails given an invalid email (email not found).
     */
    @Test
    public void getFailsInvalidEmail() {
        try {
            repository.get("invalid-email");
            fail("Did not throw exception with invalid email");
        } catch (IllegalArgumentException e) {
            assertEquals("Account with that email not found", e.getMessage());
        } catch (Exception e) {
            fail(e);
        }
    }
}