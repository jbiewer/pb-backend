package com.piggybank.repository;

import com.piggybank.model.Account;
import com.piggybank.model.Customer;
import com.piggybank.model.Merchant;
import com.piggybank.util.FirebaseEmulatorServices;
import com.piggybank.mocks.MockModels;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

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
    public void testWithoutMessageSucceeds() {
        String result = repository.test(null);
        assertEquals("Success! No message supplied", result);
    }

    /**
     * todo
     */
    @Test
    public void testWithMessageSucceeds() {
        String result = repository.test("test");
        assertEquals("Success! Here is your message: test", result);
    }

    /**
     * todo
     */
    @Test
    public void createAccountWithoutTypeFails() {
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
    public void createAccountWithoutEmailFails() {
        Account account = MockModels.mockCustomer();
        account.setEmail(null);
        try {
            repository.create(account);
            fail("Failed to throw exception for no account email");
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), "Must specify account email");
        } catch (Throwable t) {
            fail(t);
        }
    }

    /**
     * todo
     */
    @Test
    public void createCustomerSucceeds() {
        try {
            Customer customer = MockModels.mockCustomer();
            assertEquals(repository.create(customer), "Account created successfully!");
        } catch (Throwable t) {
            fail(t);
        }
    }

    /**
     * todo
     */
    @Test
    public void createMerchantSucceeds() {
        try {
            Merchant merchant = MockModels.mockMerchant();
            assertEquals(repository.create(merchant), "Account created successfully!");
        } catch (Throwable t) {
            fail(t);
        }
    }

    /**
     * todo
     */
    @Test
    public void createMerchantWithoutBankAccountFails() {
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
    public void loginSucceeds() {
        String email = "user1@email.com";
        String password = "user1-pw";
        try {
            assertEquals("Login successful!", repository.login(email, password));
        } catch (Throwable t) {
            fail(t);
        }
    }

    /**
     * todo
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
     * todo
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
     * todo
     */
    @Test
    public void updateAccountSucceeds() {
        Account account = MockModels.mockAccount();
        Account databaseAccount;

        try {
            account.setType(Account.AccountType.CUSTOMER);
            account.setEmail("user1@email.com");
            assertEquals("Account successfully updated!", repository.update(account.getEmail(), account));

            databaseAccount = FirebaseEmulatorServices.get("Accounts", account.getEmail(), Account.class);
            databaseAccount.setTransactionIds(null);
            assertEquals(account, databaseAccount);
        } catch (Throwable t) {
            fail(t);
        }
    }

    /**
     * todo
     */
    @Test
    public void updateAccountSucceedsNewUsername() {
        Account account = MockModels.mockAccount();
        Account databaseAccount;

        try {
            account.setType(Account.AccountType.CUSTOMER);
            String email = account.getEmail();
            assertEquals("Account successfully updated!", repository.update("user1@email.com", account));

            databaseAccount = FirebaseEmulatorServices.get("Accounts", email, Account.class);
            databaseAccount.setTransactionIds(null);
            assertEquals(account, databaseAccount);
        } catch (Throwable t) {
            fail(t);
        }
    }

    /**
     * todo
     */
    @Test
    public void updateAccountFailsEmailNotFound() {
        Account account = MockModels.mockCustomer();
        try {
            repository.update("user-that-does-not-exist@email.com", account);
        } catch (IllegalArgumentException e) {
            assertEquals("Account with that email not found", e.getMessage());
        } catch (Throwable t) {
            fail(t);
        }
    }

    @Test
    public void getReturnsAccountObject() {
        try {
            // System.out.println("*****RESPONSE ENTITY OK: "+ResponseEntity.ok(repository.get("user1@email.com")));
            assertEquals(repository.get("user1@email.com").getClass(), Account.class); 
        } catch(Throwable t) {
            fail(t);
        }
    }

    @Test
    public void getDoesNotReturnSensitiveInfo() {
        try {
            assertEquals(repository.get("user1@email.com").getPassword(), null);
        } catch(Throwable t) {
            fail(t);
        }
    }

    @Test
    public void getFailsEmailNotFound() {
        try {
            repository.get("email-does-not-exist@email.com");
            fail("Failed to throw exception for email not found");
        } catch (IllegalArgumentException e) {
            assertEquals("Account with that email not found", e.getMessage());
        } catch(Throwable t) {
            fail(t);
        }
    }
}

