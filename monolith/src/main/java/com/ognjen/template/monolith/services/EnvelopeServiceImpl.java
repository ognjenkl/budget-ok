package com.ognjen.template.monolith.services;

import com.ognjen.template.monolith.models.Envelope;
import com.ognjen.template.monolith.models.Expense;
import com.ognjen.template.monolith.repositories.EnvelopeRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class EnvelopeServiceImpl implements EnvelopeService {
    private final EnvelopeRepository repository;
    private final AtomicLong expenseIdGenerator = new AtomicLong(1);

    public EnvelopeServiceImpl(EnvelopeRepository repository) {
        this.repository = repository;
    }

    @Override
    public Envelope createEnvelope(String name, int budget) {
        Envelope envelope = new Envelope(name, budget);
        return repository.save(envelope);
    }

    @Override
    public Optional<Envelope> getEnvelopeById(long id) {
        return repository.findById(id);
    }

    @Override
    public List<Envelope> getAllEnvelopes() {
        return repository.findAll();
    }

    @Override
    public void deleteEnvelope(long id) {
        repository.delete(id);
    }

    @Override
    public Envelope updateEnvelope(long id, String name, int budget) {
        Optional<Envelope> optional = repository.findById(id);
        if (optional.isPresent()) {
            Envelope envelope = optional.get();
            envelope.setName(name);
            envelope.setBudget(budget);
            repository.update(envelope);
            return envelope;
        }
        throw new IllegalArgumentException("Envelope not found with id: " + id);
    }

    @Override
    public Envelope addExpense(long envelopeId, int amount, String memo, String transactionType) {
        Optional<Envelope> optional = repository.findById(envelopeId);
        if (optional.isPresent()) {
            Envelope envelope = optional.get();
            Expense expense = new Expense(envelopeId, amount, memo, transactionType);
            expense.setId(expenseIdGenerator.getAndIncrement());
            envelope.addExpense(expense);
            repository.update(envelope);
            return envelope;
        }
        throw new IllegalArgumentException("Envelope not found with id: " + envelopeId);
    }

    @Override
    public Envelope transferAmount(long sourceEnvelopeId, long targetEnvelopeId, int amount, String memo) {
        // Validate source envelope exists
        Optional<Envelope> sourceOptional = repository.findById(sourceEnvelopeId);
        if (!sourceOptional.isPresent()) {
            throw new IllegalArgumentException("Source envelope not found with id: " + sourceEnvelopeId);
        }

        // Validate target envelope exists
        Optional<Envelope> targetOptional = repository.findById(targetEnvelopeId);
        if (!targetOptional.isPresent()) {
            throw new IllegalArgumentException("Target envelope not found with id: " + targetEnvelopeId);
        }

        Envelope sourceEnvelope = sourceOptional.get();
        Envelope targetEnvelope = targetOptional.get();

        // Check source has sufficient balance (business logic)
        if (sourceEnvelope.getBalance() < amount) {
            throw new IllegalArgumentException("Insufficient balance in source envelope. Available: " + sourceEnvelope.getBalance() + ", Requested: " + amount);
        }

        // Create WITHDRAW expense in source
        Expense withdrawExpense = new Expense(sourceEnvelopeId, amount, memo, "WITHDRAW");
        withdrawExpense.setId(expenseIdGenerator.getAndIncrement());
        sourceEnvelope.addExpense(withdrawExpense);

        // Create DEPOSIT expense in target
        Expense depositExpense = new Expense(targetEnvelopeId, amount, memo, "DEPOSIT");
        depositExpense.setId(expenseIdGenerator.getAndIncrement());
        targetEnvelope.addExpense(depositExpense);

        // Update both envelopes
        repository.update(sourceEnvelope);
        repository.update(targetEnvelope);

        return sourceEnvelope;
    }
}
