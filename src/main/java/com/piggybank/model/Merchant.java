package com.piggybank.model;

/**
 * Account model but with the type set to MERCHANT.
 */
public class Merchant extends Account {
    public Merchant() {
        super(AccountType.MERCHANT);
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
