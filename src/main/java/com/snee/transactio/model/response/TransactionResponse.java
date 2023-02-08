package com.snee.transactio.model.response;

import com.google.gson.annotations.Expose;
import com.snee.transactio.model.Session;

public class TransactionResponse {
    public enum Status {
        FAILED,
        PENDING,
        COMPLETED
    }

    @Expose
    private Status status;

    @Expose
    private String transactionId;

    @Expose
    private int amount;

    @Expose
    private Session sessionData;

    @Expose
    private String recipientAccountNumber;

    public Status getStatus() {
        return status;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public TransactionResponse setStatus(Status status) {
        this.status = status;
        return this;
    }

    public TransactionResponse setTransactionId(String transactionId) {
        this.transactionId = transactionId;
        return this;
    }

    public TransactionResponse setAmount(int amount) {
        this.amount = amount;
        return this;
    }

    public TransactionResponse setRecipientAccountNumber(String recipientAccountNumber) {
        this.recipientAccountNumber = recipientAccountNumber;
        return this;
    }

    public Session getSessionData() {
        return sessionData;
    }

    public TransactionResponse setSessionData(Session sessionData) {
        this.sessionData = sessionData;
        return this;
    }

    public int getAmount() {
        return amount;
    }

    public String getRecipientAccountNumber() {
        return recipientAccountNumber;
    }
}
