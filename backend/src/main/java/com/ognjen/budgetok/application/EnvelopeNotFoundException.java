package com.ognjen.budgetok.application;

public class EnvelopeNotFoundException extends RuntimeException {
    public EnvelopeNotFoundException(Long id) {
        super("Envelope not found with id: " + id);
    }
}
