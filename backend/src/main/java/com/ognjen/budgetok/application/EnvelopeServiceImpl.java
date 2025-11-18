package com.ognjen.budgetok.application;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnvelopeServiceImpl implements EnvelopeService {

  private final EnvelopeRepository envelopeRepository;

  @Override
  @Transactional
  public Envelope create(Envelope envelope) {
    return envelopeRepository.save(envelope);
  }

  @Override
  public List<Envelope> getAll() {
    return envelopeRepository.findAll();
  }

  @Override
  @Transactional(readOnly = true)
  public Envelope getById(long id) {
    return envelopeRepository.findById(id);
  }

  @Override
  @Transactional
  public void delete(long id) {
    envelopeRepository.deleteById(id);
  }

  @Override
  @Transactional
  public Envelope update(long id, Envelope envelope) {
    if (envelope.getId() != null && envelope.getId() != id) {
      throw new IllegalArgumentException("ID in path does not match ID in request body");
    }
    return envelopeRepository.save(envelope);
//    return envelopeRepository.update(id, envelope);
  }

  @Override
  @Transactional
  public Envelope addExpense(long id, ExpenseDto expenseDto) {
    Envelope envelope = envelopeRepository.findById(id);
    Expense expense = new Expense();
    expense.setAmount(expenseDto.amount());
    expense.setMemo(expenseDto.memo());
    expense.setTransactionType(expenseDto.transactionType());
    envelope.add(expense);
    return envelopeRepository.save(envelope);
  }

  @Override
  @Transactional
  public TransferResponse transfer(TransferRequest request) {
    // Check if source envelope exists
    Envelope sourceEnvelope = envelopeRepository.findById(request.sourceEnvelopeId());
    if (sourceEnvelope == null) {
      throw new IllegalArgumentException("Source envelope not found");
    }

    // Check if target envelope exists
    Envelope targetEnvelope = envelopeRepository.findById(request.targetEnvelopeId());
    if (targetEnvelope == null) {
      throw new IllegalArgumentException("Target envelope not found");
    }

    // Check if source has sufficient balance
    int sourceBalance = sourceEnvelope.getBalance();
    if (sourceBalance < request.amount()) {
      throw new IllegalArgumentException("Insufficient balance");
    }

    // Create withdrawal expense in source envelope
    Expense withdrawalExpense = new Expense();
    withdrawalExpense.setAmount(request.amount());
    withdrawalExpense.setMemo(request.memo());
    withdrawalExpense.setTransactionType("WITHDRAW");
    sourceEnvelope.add(withdrawalExpense);

    // Create deposit expense in target envelope
    Expense depositExpense = new Expense();
    depositExpense.setAmount(request.amount());
    depositExpense.setMemo(request.memo());
    depositExpense.setTransactionType("DEPOSIT");
    targetEnvelope.add(depositExpense);

    // Save both envelopes
    envelopeRepository.save(sourceEnvelope);
    envelopeRepository.save(targetEnvelope);

    return new TransferResponse("Transfer successful");
  }
}
