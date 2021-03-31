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
 * Utility class containing a set of static methods to construct models using mock data.
 */
public abstract class MockModels {
    private static final Random rand = new Random();

    /**
     * Creates an account of class type Customer with mock data.
     *
     * @return Account with mock data but the type is set to CUSTOMER.
     */
    public static Customer mockCustomer() {
        return mockAccount(new Customer());
    }

    /**
     * Creates an account of class type Merchant with mock data.
     *
     * @return Account with mock data but the type is set to MERCHANT.
     */
    public static Merchant mockMerchant() {
        return mockAccount(new Merchant());
    }

    /**
     * Creates an account of class type Account with mock data and type field not set.
     *
     * @return Account with mock data.
     */
    public static Account mockAccount() {
        return mockAccount(new Account());
    }

    /**
     * Creates an account of class type Account with mock data and type field specified.
     *
     * @param type Type of account.
     * @return Account with mock data.
     */
    public static Account mockAccount(AccountType type) {
        Account account = new Account();
        account.setType(type);
        return mockAccount(account);
    }

    /**
     * Creates an account of class type T with mock data.
     *
     * @param account Account model instance to use.
     * @param <T> Class type of the account model.
     * @return Account with mock data.
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
     * Creates a bank account with mock data.
     *
     * @return Bank account with mock data.
     */
    public static BankAccount mockBankAccount() {
        BankAccount account = new BankAccount();
        account.setNameOnAccount(UUID.randomUUID().toString());
        account.setAccountNumber(rand.nextLong());
        account.setRoutingNumber(rand.nextLong());
        return account;
    }
}
