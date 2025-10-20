package com.ognjen.budgetok.acceptance;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ExpenseAcceptanceIT {

  @Autowired
  private BudgetDsl budget;
  UiDriver driver;

  @BeforeAll
  void launchBrowser() {
    driver = new UiDriver();
  }

  @BeforeEach
  void createContextAndPage() {

    driver.initContext();
    budget = new BudgetDsl(driver);
  }

  @AfterEach
  void closeContext() {
    driver.closeContext();
  }

  @AfterAll
  void closeBrowser() {
    driver.close();
  }

  @Test
  void shouldCreateExpenseForJana() {
    budget.createEnvelope("envelope: Jana", "budget: 100");

    budget.addExpenseToEnvelope("memo: toy", "amount: 50.00", "envelope: Jana");

    budget.assertExpenseAdded("memo: toy", "amount: 50.00", "envelope: Jana");
  }

}
