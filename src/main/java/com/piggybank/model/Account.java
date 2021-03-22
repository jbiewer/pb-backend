package com.piggybank.model;

import java.util.List;

public class Account {
    
    public enum AccountType {
        MERCHANT, CUSTOMER
    }

    public String username; 
    public String password; 
    public String email; 
    public String profilePictureURL; 
    public float balance; 
    public BankAccount bankAccount; 
    public final AccountType type; 
    public List<String> transactions; 

    public Account(AccountType type, String username, String password) { 
        this.type = type; 
        this.username = username; 
        this.password = password; 
    }

    public void setUsername(String username) { this.username = username;  }

    public String getUsername() { return username; }

    public void setPassword(String password) {  this.password = password; }

    public String getPassword() {  return password;  }

    public void setEmail(String email) { this.email = email; }

    public String getEmail() { return email; }

    public void setProfilePictureUrl(String profilePictureURL) { this.profilePictureURL = profilePictureURL;   }

    public String getProfilePictureURL() { return profilePictureURL;  }

    public void setBalance(float balance) { this.balance = balance; }

    public float getBalance() { return balance; }

    public void setBankAccount(BankAccount bankAccount) { this.bankAccount = bankAccount; }

    public BankAccount getBankAccount() { return bankAccount; }

    public AccountType getAccountType() { return type; }

    public void setTransactionList(List<String> transactions) { this.transactions = transactions; }

    public List<String> getTransactionList() { return transactions; }

    public void addTransaction(String txn) {
        transactions.add(txn);
    }
}
