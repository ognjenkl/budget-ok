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
}
