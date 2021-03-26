package com.piggybank.mocks;

import com.piggybank.model.Account;
import com.piggybank.model.BankAccount;
import com.piggybank.model.Customer;
import com.piggybank.model.Merchant;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

import static com.piggybank.model.Account.AccountType;

/**
 * todo
 */
public abstract class MockModels {
    private static final Random rand = new Random();

    /**
     * todo
     * @return
     */
    public static Customer mockCustomer() {
        return mockAccount(new Customer());
    }

    /**
     * todo
     * @return
     */
    public static Merchant mockMerchant() {
        return mockAccount(new Merchant());
    }

    /**
     * todo
     * @return
     */
    public static Account mockAccount() {
        return mockAccount(new Account());
    }

    public static Account mockAccount(AccountType type) {
        Account account = new Account();
        account.setType(type);
        return mockAccount(account);
    }

    /**
     * todo
     * @param account
     * @param <T>
     * @return
     */
    private static <T extends Account> T mockAccount(T account) {
        account.setUsername(UUID.randomUUID().toString());
        account.setPassword(UUID.randomUUID().toString());
        account.setEmail(UUID.randomUUID().toString());
        account.setProfilePictureUrl(UUID.randomUUID().toString());
        account.setBalance(rand.nextFloat());
        account.setBankAccount(mockBankAccount());
        account.setTransactionIds(new ArrayList<>(0));
        return account;
    }

    /**
     * todo
     * @return
     */
    public static BankAccount mockBankAccount() {
        BankAccount account = new BankAccount();
        account.setNameOnAccount(UUID.randomUUID().toString());
        account.setAccountNumber(rand.nextLong());
        account.setRoutingNumber(rand.nextLong());
        return account;
    }
}
