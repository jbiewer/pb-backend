package com.piggybank.model;

import java.util.List;
import java.util.Objects;

/**
 * Represents how an account is structured in Firestore.
 */
public class Account {
    /**
     * Given an account instance, removes any sensitive data such as encrypted passwords and
     * transactions ID's.
     *
     * @param account Account containing sensitive information to be removed.
     * @return The same account instance with sensitive information removed.
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
    private AccountType type;
    private String profilePictureUrl;
    private long balance;
    private BankAccount bankAccount;
    private List<String> transactionIds;

    // Default constructor needed for http requests.
    public Account() {}

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

    public AccountType getType() {
        return type;
    }

    public void setType(AccountType type) {
        this.type = type;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public BankAccount getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(BankAccount bankAccount) {
        this.bankAccount = bankAccount;
    }

    public List<String> getTransactionIds() {
        return transactionIds;
    }

    public void setTransactionIds(List<String> transactionIds) {
        this.transactionIds = transactionIds;
    }

    public void addTransaction(String txn) {
        this.transactionIds.add(txn);
    }

    @Override
    public String toString() {
        return "Account{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", type=" + type +
                ", profilePictureUrl='" + profilePictureUrl + '\'' +
                ", balance=" + balance +
                ", bankAccount=" + bankAccount +
                ", transactionIds=" + transactionIds +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return Objects.equals(username, account.username) &&
                Objects.equals(password, account.password) &&
                Objects.equals(email, account.email) &&
                type == account.type &&
                Objects.equals(profilePictureUrl, account.profilePictureUrl) &&
                Objects.equals(balance, account.balance) &&
                Objects.equals(bankAccount, account.bankAccount) &&
                Objects.equals(transactionIds, account.transactionIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                username,
                password,
                email,
                type,
                profilePictureUrl,
                balance,
                bankAccount,
                transactionIds
        );
    }
}
