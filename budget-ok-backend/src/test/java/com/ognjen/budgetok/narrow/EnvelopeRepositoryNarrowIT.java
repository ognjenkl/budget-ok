package com.ognjen.budgetok.narrow;

import com.ognjen.budgetok.application.Envelope;
import com.ognjen.budgetok.application.EnvelopeRepository;
import com.ognjen.budgetok.application.Expense;
import com.ognjen.budgetok.config.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Import(TestConfig.class)
public class EnvelopeRepositoryNarrowIT {

  @Autowired
  private EnvelopeRepository envelopeRepository;

  @Test
  void shouldSaveEnvelope() {
    Envelope envelope = Envelope.builder()
        .name("test")
        .budget(1000)
        .build();

    Envelope savedEnvelope = envelopeRepository.save(envelope);

    assertThat(savedEnvelope.getId()).isNotNull();
    assertThat(savedEnvelope.getName()).isEqualTo("test");
    assertThat(savedEnvelope.getBudget()).isEqualTo(1000);
  }

  @Test
  void shouldAddEnvelopeWithExpense() {
    // Create and save an envelope
    Envelope envelope = Envelope.builder()
        .name("Groceries")
        .budget(500)
        .build();

    Envelope savedEnvelope = envelopeRepository.save(envelope);
    assertThat(savedEnvelope.getId()).isNotNull();

    // Add an expense to the envelope
    Expense expense = Expense.builder()
        .amount(50)
        .memo("Milk and eggs")
        .build();
    
    savedEnvelope.add(expense);
    
    // Save the updated envelope
    Envelope updatedEnvelope = envelopeRepository.save(savedEnvelope);

    // Verify the expense was added
    assertThat(updatedEnvelope.hasExpenses()).isTrue();
    assertThat(updatedEnvelope.getExpenseAmount("Milk and eggs")).isEqualTo(50);
  }
}
