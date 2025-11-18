package com.ognjen.budgetok.application;

public record TransferRequest(
    long sourceEnvelopeId,
    long targetEnvelopeId,
    int amount,
    String memo
) {}
