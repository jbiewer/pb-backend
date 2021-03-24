package com.piggybank.repository;

import com.piggybank.model.Account;
import com.piggybank.model.Customer;
import com.piggybank.model.Merchant;
import com.piggybank.util.FirebaseEmulatorService;
import com.piggybank.util.mock.MockModels;
import org.apache.http.util.Asserts;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
public class AccountRepositoryTest {

    @Autowired
    private AccountRepository repository;

    @AfterEach
    public void afterEach() throws IOException, InterruptedException {
        FirebaseEmulatorService.clearFirestoreDocuments();
    }

    /**
     * todo
     */
    @Test
    public void test() {
        String result = repository.test("test");
        Assertions.assertEquals(result, "Success! Here is your message: test");
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
            Assertions.fail("Failed to throw exception for account type");
        } catch (IllegalArgumentException e) {
            // Pass!
        } catch (Exception e) {
            Assertions.fail(e);
        }
    }

    /**
     * todo
     */
    @Test
    public void createCustomer() throws Exception {
        Customer customer = MockModels.mockCustomer();
        Assertions.assertNotNull(repository.create(customer));
    }

    /**
     * todo
     */
    @Test
    public void createMerchant() throws Exception {
        Merchant merchant = MockModels.mockMerchant();
        Assertions.assertNotNull(repository.create(merchant));
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
            Assertions.fail("Failed to throw exception for no bank account");
        } catch (IllegalArgumentException e) {
            // Pass!
        } catch (Exception e) {
            Assertions.fail(e);
        }
    }
}

