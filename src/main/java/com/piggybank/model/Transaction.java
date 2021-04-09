package com.piggybank.model;

import java.util.Objects;

/**
 * Represents how a transaction is structured in Firestore.
 */
public class Transaction {
    public enum TransactionType {
        BANK,
        PEER_TO_PEER
    }

    private String id;
    private String transactorEmail;
    private String recipientEmail; 
    private Long amount;
    private TransactionType type;

    public Transaction() {}

    public Transaction(TransactionType type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTransactorEmail() {
        return transactorEmail;
    }

    public void setTransactorEmail(String transactorEmail) {
        this.transactorEmail = transactorEmail;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(amount, that.amount) &&
               Objects.equals(id, that.id) &&
               Objects.equals(transactorEmail, that.transactorEmail) &&
               Objects.equals(recipientEmail, that.recipientEmail) &&
               type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, transactorEmail, recipientEmail, amount, type);
    }
}