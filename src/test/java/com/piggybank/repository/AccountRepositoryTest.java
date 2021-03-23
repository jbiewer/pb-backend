package com.piggybank.repository;

import com.piggybank.model.Account;
import com.piggybank.model.Customer;
import com.piggybank.model.Merchant;
import com.piggybank.util.FirebaseEmulatorService;
import com.piggybank.util.MockModels;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
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
    public void createCustomer() throws Throwable {
        Customer customer = MockModels.mockCustomer();
        Assertions.assertNotNull(repository.create(customer));
    }

    /**
     * todo
     */
    @Test
    public void createMerchant() throws Throwable {
        Merchant merchant = MockModels.mockMerchant();
        Assertions.assertNotNull(repository.create(merchant));
    }

    @Test
    public void createMerchantWithoutBankAccount() throws Throwable {
//        Merchant merchant = MockModels.mockMerchant();
    }

    @Test
    public void createAccountWithoutType() throws Throwable {
//        Account account = MockModels.mockAccount();
    }
}

