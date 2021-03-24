package com.piggybank.model;

import java.util.Objects;

/**
 * todo
 */
public class BankAccount {
    private long accountNumber;
    private long routingNumber;
    private String nameOnAccount;

    public BankAccount() {}

    public long getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(long accountNumber) {
        this.accountNumber = accountNumber;
    }

    public long getRoutingNumber() {
        return routingNumber;
    }

    public void setRoutingNumber(long routingNumber) {
        this.routingNumber = routingNumber;
    }

    public String getNameOnAccount() {
        return nameOnAccount;
    }

    public void setNameOnAccount(String nameOnAccount) {
        this.nameOnAccount = nameOnAccount;
    }

    @Override
    public String toString() {
        return "BankAccount{" +
                "accountNumber=" + accountNumber +
                ", routingNumber=" + routingNumber +
                ", nameOnAccount='" + nameOnAccount + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BankAccount account = (BankAccount) o;
        return accountNumber == account.accountNumber &&
                routingNumber == account.routingNumber &&
                Objects.equals(nameOnAccount, account.nameOnAccount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountNumber, routingNumber, nameOnAccount);
    }
}