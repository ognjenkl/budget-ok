package com.ognjen.budgetok.application;

import java.util.List;

public interface EnvelopeService {

  Envelope create(Envelope envelope);

  List<Envelope> getAll();

  Envelope getById(long id);

  void delete(long id);

  Envelope update(long id, Envelope envelope);

  Envelope addExpense(long id, ExpenseDto expenseDto);

  TransferResponse transfer(TransferRequest request);
}
