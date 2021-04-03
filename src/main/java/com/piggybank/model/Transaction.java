package com.piggybank.model;

/**
 * todo
 */
public class Transaction {
    public enum TransactionType {
        BANK,
        PEER_TO_PEER
    }

    private String id;
    private String transactorEmail;
    private String recipientEmail; 
    private float amount;
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

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }
}