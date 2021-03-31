package com.piggybank.model;

/**
 * Account model but with the type set to CUSTOMER.
 */
public class Customer extends Account {
    public Customer() {
        super(AccountType.CUSTOMER);
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
