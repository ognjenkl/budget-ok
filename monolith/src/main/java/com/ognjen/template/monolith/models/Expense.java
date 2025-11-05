package com.ognjen.template.monolith.models;

import java.time.LocalDateTime;

public class Expense {
    private long id;
    private long envelopeId;
    private int amount;
    private String memo;
    private String transactionType; // "WITHDRAW" or "DEPOSIT"
    private String date;

    public Expense() {
    }

    public Expense(long envelopeId, int amount, String memo, String transactionType) {
        this.envelopeId = envelopeId;
        this.amount = amount;
        this.memo = memo;
        this.transactionType = transactionType;
        this.date = LocalDateTime.now().toString();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getEnvelopeId() {
        return envelopeId;
    }

    public void setEnvelopeId(long envelopeId) {
        this.envelopeId = envelopeId;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
