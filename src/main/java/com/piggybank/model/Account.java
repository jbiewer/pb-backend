package com.piggybank.model;

import java.util.List;

/**
 * todo
 */
public class Account {
    /**
     * todo
     * @param account
     * @return
     */
    public static Account filterSensitiveData(Account account) {
        account.setPassword(null);
        account.setTransactionIds(null);
        return account;
    }

    public enum AccountType {
        CUSTOMER,
        MERCHANT
    }

    private String username;
    private String password;
    private String email;
    private String profilePictureUrl;
    private Float balance;
    private BankAccount bankAccount;
    private final AccountType type;
    private List<String> transactionIds;

    //default constructor needed for http requests
    public Account(){ 
        this.type = null;
    }

    public Account(AccountType type) {
        this.type = type;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public Float getBalance() {
        return balance;
    }

    public void setBalance(Float balance) {
        this.balance = balance;
    }

    public BankAccount getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(BankAccount bankAccount) {
        this.bankAccount = bankAccount;
    }

    public AccountType getType() {
        return type;
    }

    public List<String> getTransactionIds() {
        return transactionIds;
    }

    public void setTransactionIds(List<String> transactionIds) {
        this.transactionIds = transactionIds;
    }
}
