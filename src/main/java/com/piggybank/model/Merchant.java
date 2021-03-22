package com.piggybank.model;

public class Merchant extends Account{


    public Merchant(String username, String password) {
        super(AccountType.MERCHANT, username, password);
    }
    
}
