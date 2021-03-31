package com.piggybank.repository;

import com.piggybank.model.Account;
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
 * Unit-testing suite for the AccountRepository.
 * After starting a spring application, injects the AccountRepository bean as a dependency to be used
 * for running unit tests on.
 */
@SpringBootTest
public class AccountRepositoryTest {

    @Autowired private AccountRepository repository;

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
     * The create() method fails because no type is specified.
     */
    @Test
    public void createAccountWithoutTypeFails() {
        Account account = mockAccount();
        account.setType(null);
        try {
            repository.create(account);
            fail("Failed to throw exception for no account type");
        } catch (IllegalArgumentException e) {
            assertEquals("Must specify account type", e.getMessage());
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * The create() method fails because no email is specified.
     */
    @Test
    public void createAccountWithoutEmailFails() {
        Account account = mockCustomer();
        account.setEmail(null);
        try {
            repository.create(account);
            fail("Failed to throw exception for no account email");
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), "Must specify account email");
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * The create() method succeeds given mock customer data.
     */
    @Test
    public void createCustomerSucceeds() {
        try {
            Customer customer = mockCustomer();
            assertEquals(repository.create(customer), "Account created successfully!");
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * The create() method succeeds given mock merchant data.
     */
    @Test
    public void createMerchantSucceeds() {
        try {
            Merchant merchant = mockMerchant();
            assertEquals(repository.create(merchant), "Account created successfully!");
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * The create() method fails given mock merchant data w/out bank account specified.
     */
    @Test
    public void createMerchantWithoutBankAccountFails() {
        Merchant merchant = mockMerchant();
        merchant.setBankAccount(null);
        try {
            repository.create(merchant);
            fail("Failed to throw exception for no bank account");
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), "Merchant account must have a bank account");
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * The login() method succeeds given valid email/password credentials.
     */
    @Test
    public void loginSucceeds() {
        String email = "user1@email.com";
        String password = "user1-pw";
        try {
            assertEquals("Login successful!", repository.login(email, password));
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * The login() method fails given an invalid email (email not found).
     */
    @Test
    public void loginFailsEmailNotFound() {
        String email = "email-does-not-exist@email.com";
        String password = "user1-pw";
        try {
            repository.login(email, password);
            fail("Failed to throw exception for email not found");
        } catch (IllegalArgumentException e) {
            assertEquals("Account with that email not found", e.getMessage());
        } catch (Throwable e) {
            fail(e);
        }
    }

    /**
     * The login() method fails given an invalid password (password doesn't match one found in Firestore).
     */
    @Test
    public void loginFailsPasswordMismatch() {
        String email = "user1@email.com";
        String password = "not-user1-pw";
        try {
            repository.login(email, password);
            fail("Failed to throw exception for password mismatch");
        } catch (IllegalArgumentException e) {
            assertEquals("Password did not match", e.getMessage());
        } catch (Throwable e) {
            fail(e);
        }
    }

    /**
     * The update() method succeeds given a valid email w/ corresponding mock account data.
     */
    @Test
    public void updateAccountSucceeds() {
        Account account = mockAccount();
        Account databaseAccount;

        try {
            account.setType(Account.AccountType.CUSTOMER);
            account.setEmail("user1@email.com");
            assertEquals("Account successfully updated!", repository.update(account.getEmail(), account));

            databaseAccount = getFromFirestore("Accounts", account.getEmail(), Account.class);
            databaseAccount.setTransactionIds(null);
            assertEquals(account, databaseAccount);
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * The update() method succeeds given a valid email w/ corresponding mock account data and a new email.
     */
    @Test
    public void updateAccountSucceedsNewEmail() {
        Account account = mockAccount();
        Account databaseAccount;

        try {
            account.setType(Account.AccountType.CUSTOMER);
            String email = account.getEmail();
            assertEquals("Account successfully updated!", repository.update("user1@email.com", account));

            databaseAccount = getFromFirestore("Accounts", email, Account.class);
            databaseAccount.setTransactionIds(null);
            assertEquals(account, databaseAccount);
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * The update() method fails given an invalid email (email not found).
     */
    @Test
    public void updateAccountFailsEmailNotFound() {
        Account account = mockCustomer();
        try {
            repository.update("user-that-does-not-exist@email.com", account);
        } catch (IllegalArgumentException e) {
            assertEquals("Account with that email not found", e.getMessage());
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * The get() method succeeds given a valid email.
     */
    @Test
    public void getSucceeds() {
        Account account, databaseAccount;
        try {
            account = repository.get("user1@email.com");
            assertNotNull(account);

            databaseAccount = getFromFirestore("Accounts", "user1@email.com", Account.class);
            databaseAccount.setTransactionIds(null);
            databaseAccount.setPassword(null);
            assertEquals(account, databaseAccount);
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * The get() method succeeds and doesn't return sensitive info.
     */
    @Test
    public void getDoesNotReturnSensitiveInfo() {
        try {
            assertNull(repository.get("user1@email.com").getPassword());
        } catch(Exception e) {
            fail(e);
        }
    }

    /**
     * The get() method fails given an invalid email (email not found).
     */
    @Test
    public void getFailsEmailNotFound() {
        try {
            repository.get("email-does-not-exist@email.com");
            fail("Failed to throw exception for email not found");
        } catch (IllegalArgumentException e) {
            assertEquals("Account with that email not found", e.getMessage());
        } catch(Exception e) {
            fail(e);
        }
    }

    /**
     * The usernameExists() method returns true given a valid username and false given an invalid username.
     */
    @Test
    public void usernameExistsSucceeds() {
        try {
            assertTrue(repository.usernameExists("user1"));
            assertFalse(repository.usernameExists("nonexistent-user"));
        } catch(Exception e) {
            fail(e);
        }
    }
}