package com.piggybank.model;

/**
 * todo
 */
public class PeerToPeerTransaction extends Transaction {
    private String receiverEmail;

    public PeerToPeerTransaction() {
        super(TransactionType.PEER_TO_PEER);
    }
}
