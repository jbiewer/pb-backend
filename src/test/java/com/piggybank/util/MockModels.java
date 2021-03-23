package com.piggybank.util;

import com.piggybank.model.Account;
import com.piggybank.model.BankAccount;
import com.piggybank.model.Customer;
import com.piggybank.model.Merchant;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

public abstract class MockModels {
    private static final Random rand = new Random();

    public static Customer mockCustomer() {
        return mockCustomer(MockModelOptions.none());
    }

    public static Customer mockCustomer(MockModelOptions options) {
        return mockAccount(new Customer());
    }

    public static Merchant mockMerchant() {
        return mockMerchant(MockModelOptions.none());
    }

    public static Merchant mockMerchant(MockModelOptions options) {
        return mockAccount(new Merchant());
    }

    public static Account mockAccount() {
        return mockAccount(MockModelOptions.none());
    }

    public static Account mockAccount(MockModelOptions options) {
        return mockAccount(new Account());
    }

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

    public static BankAccount mockBankAccount() {
        BankAccount account = new BankAccount();
        account.setNameOnAccount(UUID.randomUUID().toString());
        account.setAccountNumber(rand.nextLong());
        account.setRoutingNumber(rand.nextLong());
        return account;
    }
}
