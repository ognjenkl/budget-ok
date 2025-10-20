package com.ognjen.budgetok.acceptance;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BudgetDsl {

  private final UiDriver uiDriver;

  public void createEnvelope(String... args) {
    Params params = new Params(args);
    String envelopeName = params.getValue("envelope");
    String envelopeBudget = params.getValue("budget");

    uiDriver.createEnvelope(new String[]{envelopeName, envelopeBudget});
  }

  public void addExpenseToEnvelope(String... args) {
    Params params = new Params(args);
    String envelopeName = params.getValue("envelope");
    String amount = params.getValue("amount");
    String memo = params.getValue("memo");

    // Find and click the view expenses button for the envelope
    uiDriver.clickViewExpensesButton(envelopeName);
    
    // Click the add expense button
    uiDriver.clickAddExpenseButton();
    
    // Fill in the expense details
    uiDriver.fillExpenseForm(amount, memo);
    
    // Submit the form
    uiDriver.submitExpenseForm();
  }

  public void assertExpenseAdded(String... args) {
  }
}
