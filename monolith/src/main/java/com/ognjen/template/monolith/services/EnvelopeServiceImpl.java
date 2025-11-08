package com.ognjen.template.monolith.services;

import com.ognjen.template.monolith.models.Envelope;
import com.ognjen.template.monolith.models.Expense;
import com.ognjen.template.monolith.repositories.EnvelopeRepository;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

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
  public Envelope transferAmount(long sourceEnvelopeId, long targetEnvelopeId, int amount,
      String memo) {

    Envelope sourceEnvelope = getEnvelope(sourceEnvelopeId);
    Envelope targetEnvelope = getEnvelope(targetEnvelopeId);

    // Check source has sufficient balance (business logic)
    if (sourceEnvelope.getBalance() < amount) {
      throw new IllegalArgumentException(
          "Insufficient balance in source envelope. Available: " + sourceEnvelope.getBalance()
              + ", Requested: " + amount);
    }

    Expense withdrawExpense = new Expense(sourceEnvelopeId, amount, memo, "WITHDRAW");
    withdrawExpense.setId(expenseIdGenerator.getAndIncrement());
    sourceEnvelope.addExpense(withdrawExpense);

    Expense depositExpense = new Expense(targetEnvelopeId, amount, memo, "DEPOSIT");
    depositExpense.setId(expenseIdGenerator.getAndIncrement());
    targetEnvelope.addExpense(depositExpense);

    repository.update(sourceEnvelope);
    repository.update(targetEnvelope);

    return sourceEnvelope;
  }

  private Envelope getEnvelope(long envelopeId) {

    Optional<Envelope> envelopeOptional = repository.findById(envelopeId);

    if (envelopeOptional.isEmpty()) {
      throw new IllegalArgumentException("Envelope not found with id: " + envelopeId);
    }

    return envelopeOptional.get();
  }

}
