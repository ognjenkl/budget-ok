package com.ognjen.budgetok.component;

import com.ognjen.budgetok.TestcontainersConfiguration;
import com.ognjen.budgetok.application.Envelope;
import com.ognjen.budgetok.application.Expense;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@SpringJUnitConfig(TestcontainersConfiguration.class)
@TestPropertySource(properties = {
    "spring.flyway.enabled=true",
    "spring.flyway.locations=classpath:db/migration"
})
public class ExpenseComponentIT {

  @Autowired
  private TestRestTemplate restTemplate;

  private Envelope testEnvelope;
  private String baseUrl;

  @BeforeEach
  void setUp() {
    // Create a test envelope
    var envelope = Envelope.builder()
        .name("Groceries")
        .budget(1000)
        .build();

    testEnvelope = restTemplate.postForEntity("/api/envelopes", envelope, Envelope.class)
        .getBody();

    baseUrl = "/api/envelopes/" + testEnvelope.getId() + "/expenses";
  }

  @Test
  void shouldAddExpenseToEnvelope() {
    // Given
    var expense = Expense.builder()
        .amount(100)
        .memo("Weekly groceries")
        .build();

    // When
    var response = restTemplate.postForEntity(baseUrl, expense, Envelope.class);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    var createdEnvelope = response.getBody();
    assertThat(createdEnvelope).isNotNull();
    assertThat(createdEnvelope.hasExpenses()).isTrue();
    assertThat(createdEnvelope.getExpenseAmount("Weekly groceries")).isNotNull();
    assertThat(createdEnvelope.getExpenseAmount("Weekly groceries")).isEqualTo(100);
  }
}
