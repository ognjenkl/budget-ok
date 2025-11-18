package com.ognjen.budgetok;

import com.ognjen.budgetok.application.Envelope;
import com.ognjen.budgetok.application.EnvelopeService;
import com.ognjen.budgetok.application.EnvelopeServiceImpl;
import com.ognjen.budgetok.application.ExpenseDto;
import com.ognjen.budgetok.application.InMemoryEnvelopeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class EnvelopeServiceTest {

  EnvelopeService service;

  @BeforeEach
  void setUp() {
    service = new EnvelopeServiceImpl(new InMemoryEnvelopeRepository());
  }

  @Test
  void shouldCreateEnvelope() {
    var envelopes = service.getAll();
    assertThat(envelopes).isEmpty();

    Envelope envelope = new Envelope();
    envelope.setName("Envelope 1");
    envelope.setBudget(100);

    Envelope createdEnvelope = service.create(envelope);

    assertThat(createdEnvelope.getId()).isNotNull();
    assertThat(createdEnvelope.getName()).isEqualTo("Envelope 1");
    assertThat(createdEnvelope.getBudget()).isEqualTo(100);

    envelopes = service.getAll();
    assertThat(envelopes).hasSize(1);
  }

  @Test
  void shouldEnvelopeHasNoExpenses() {
    Envelope envelope = new Envelope();
    envelope.setName("Envelope 1");
    envelope.setBudget(100);
    envelope = service.create(envelope);
    assertThat(envelope.getId()).isNotNull();
    assertThat(envelope.hasExpenses()).isFalse();
  }

  @Test
  void shouldEnvelopeHasExpenses() {
    Envelope envelope = new Envelope();
    envelope.setName("Envelope 1");
    envelope.setBudget(100);
    envelope = service.create(envelope);
    assertThat(envelope.getId()).isNotNull();
    ExpenseDto expenseDto = new ExpenseDto(100, "expense", "WITHDRAW");

    envelope = service.addExpense(envelope.getId(), expenseDto);

    assertThat(envelope.hasExpenses()).isTrue();
  }

  @Test
  void shouldAddExpenseToEnvelope() {
    Envelope envelope = new Envelope();
    envelope.setName("Envelope 1");
    envelope.setBudget(100);
    envelope = service.create(envelope);
    assertThat(envelope.getId()).isNotNull();
    ExpenseDto expenseDto = new ExpenseDto(100, "expense", "WITHDRAW");

    envelope = service.addExpense(envelope.getId(), expenseDto);

    assertThat(envelope.hasExpenses()).isTrue();
    assertThat(envelope.getExpenseAmount("expense")).isEqualTo(100);
  }


  @Test
  void shouldThrowExceptionWhenExpenseNotFound() {
    Envelope envelope = new Envelope();
    envelope.setName("Empty Envelope");
    envelope.setBudget(100);
    final Envelope envelope2 = service.create(envelope);

    assertThatThrownBy(() -> envelope2.getExpenseAmount("non-existent"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Expense not found");
  }

  @Test
  void shouldAddTwoExpensesToEnvelope() {
    Envelope envelope = new Envelope();
    envelope.setName("Envelope 1");
    envelope.setBudget(1000);
    envelope = service.create(envelope);
    assertThat(envelope.getId()).isNotNull();
    ExpenseDto expenseDto1 = new ExpenseDto(100, "expense1", "WITHDRAW");
    ExpenseDto expenseDto2 = new ExpenseDto(200, "expense2", "WITHDRAW");
    service.addExpense(envelope.getId(), expenseDto1);

    envelope = service.addExpense(envelope.getId(), expenseDto2);

    assertThat(envelope.hasExpenses()).isTrue();
    assertThat(envelope.getExpenseAmount("expense1")).isEqualTo(100);
    assertThat(envelope.getExpenseAmount("expense2")).isEqualTo(200);
  }
}
