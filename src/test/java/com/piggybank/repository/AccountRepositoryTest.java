package com.piggybank.repository;

import com.piggybank.model.Account;
import com.piggybank.model.Customer;
import com.piggybank.model.Merchant;
import com.piggybank.util.FirebaseEmulatorServices;
import com.piggybank.util.mock.MockModels;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
public class AccountRepositoryTest {
    @Autowired
    private AccountRepository repository;

    /**
     * todo
     */
    @BeforeEach
    public void beforeEach() throws IOException, URISyntaxException {
        URI uri = Objects.requireNonNull(ClassLoader.getSystemResource("collections")).toURI();
        FirebaseEmulatorServices.generateFirestoreData(new File(uri));
    }

    /**
     * todo
     */
    @AfterEach
    public void afterEach() throws IOException, InterruptedException {
        FirebaseEmulatorServices.clearFirestoreDocuments();
    }

    /**
     * todo
     */
    @Test
    public void testWithoutMessage() {
        String result = repository.test(null);
        assertEquals("Success! No message supplied", result);
    }

    /**
     * todo
     */
    @Test
    public void testWithMessage() {
        String result = repository.test("test");
        assertEquals("Success! Here is your message: test", result);
    }

    /**
     * todo
     */
    @Test
    public void createAccountWithoutType() {
        Account account = MockModels.mockAccount();
        account.setType(null);
        try {
            repository.create(account);
            fail("Failed to throw exception for no account type");
        } catch (IllegalArgumentException e) {
            assertEquals("Must specify account type", e.getMessage());
        } catch (Throwable t) {
            fail(t);
        }
    }

    /**
     * todo
     */
    @Test
    public void createAccountWithoutUsername() {
        Account account = MockModels.mockCustomer();
        account.setUsername(null);
        try {
            repository.create(account);
            fail("Failed to throw exception for no account username");
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), "Must specify account username");
        } catch (Throwable t) {
            fail(t);
        }
    }

    /**
     * todo
     */
    @Test
    public void createCustomer() throws Throwable {
        Customer customer = MockModels.mockCustomer();
        assertEquals(repository.create(customer), "Account created successfully!");
    }

    /**
     * todo
     */
    @Test
    public void createMerchant() throws Throwable {
        Merchant merchant = MockModels.mockMerchant();
        assertEquals(repository.create(merchant), "Account created successfully!");
    }

    /**
     * todo
     */
    @Test
    public void createMerchantWithoutBankAccount() {
        Merchant merchant = MockModels.mockMerchant();
        merchant.setBankAccount(null);
        try {
            repository.create(merchant);
            fail("Failed to throw exception for no bank account");
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), "Merchant account must have a bank account");
        } catch (Throwable t) {
            fail(t);
        }
    }

    /**
     * todo
     */
    @Test
    public void updateAccount() throws Throwable {
        Account account = MockModels.mockAccount();
        account.setType(Account.AccountType.CUSTOMER);
        account.setUsername("user1");
        assertEquals("Account successfully updated!", repository.update(account.getUsername(), account));

        Account databaseAccount = FirebaseEmulatorServices.get("Accounts", account.getUsername(), Account.class);
        databaseAccount.setTransactionIds(null);
        assertEquals(account, databaseAccount);
    }

    /**
     * todo
     */
    @Test
    public void updateAccountNewUsername() throws Throwable {
        Account account = MockModels.mockAccount();
        account.setType(Account.AccountType.CUSTOMER);
        String username = account.getUsername();
        assertEquals("Account successfully updated!", repository.update("user1", account));

        Account databaseAccount = FirebaseEmulatorServices.get("Accounts", username, Account.class);
        databaseAccount.setTransactionIds(null);
        assertEquals(account, databaseAccount);
    }

    @Test
    public void updateAccountUsernameNotFound() {
        Account account = MockModels.mockCustomer();
        try {
            repository.update("user-that-does-not-exist", account);
        } catch (IllegalArgumentException e) {
            assertEquals("Account with that username not found", e.getMessage());
        } catch (Throwable t) {
            fail(t);
        }
    }
}

