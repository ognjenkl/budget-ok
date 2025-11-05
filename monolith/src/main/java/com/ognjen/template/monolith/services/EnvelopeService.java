package com.ognjen.template.monolith.services;

import com.ognjen.template.monolith.models.Envelope;
import com.ognjen.template.monolith.models.Expense;
import java.util.List;
import java.util.Optional;

public interface EnvelopeService {
    Envelope createEnvelope(String name, int budget);
    Optional<Envelope> getEnvelopeById(long id);
    List<Envelope> getAllEnvelopes();
    void deleteEnvelope(long id);
    Envelope updateEnvelope(long id, String name, int budget);
    Envelope addExpense(long envelopeId, int amount, String memo, String transactionType);
}
