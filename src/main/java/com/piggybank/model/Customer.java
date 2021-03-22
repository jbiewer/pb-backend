package com.piggybank.model;

public class Customer extends Account{

    public Customer(String username, String password) {
        super(AccountType.CUSTOMER, username, password);
    }

}
