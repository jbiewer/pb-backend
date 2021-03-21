package com.piggybank.repository;

import com.piggybank.model.Account;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
public class AccountRepositoryTest {

    @Autowired
    private AccountRepository repository;

    /**
     * todo
     */
    @Test
    public void testTest() {
        String result = repository.test("test");
        Assertions.assertEquals(result, "Success! Here is your message: test");
    }

    @Test
    public void testCreateCustomer() {
        Account account = new Account(Account.AccountType.CUSTOMER);
        // account creation...
        String result = repository.create(account);
    }
}
