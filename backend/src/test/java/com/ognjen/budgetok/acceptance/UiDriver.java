package com.ognjen.budgetok.acceptance;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.springframework.stereotype.Component;

@Component
public class UiDriver {

  private final String baseUrl;
  private Playwright playwright;
  private Browser browser;
  private BrowserContext context;
  private Page page;

  public UiDriver() {
    baseUrl = "http://localhost:5173";
    playwright = Playwright.create();
    browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
        .setHeadless(false) // Set to true in CI environment
        .setSlowMo(50));
  }

  public void initContext() {
    context = browser.newContext();
    page = context.newPage();
    navigateTo(baseUrl);
  }

  public void close() {
    if (playwright != null) {
      playwright.close();
    }
  }

  public void closeContext() {
    if (context != null) {
      context.close();
    }
  }

  public void navigateTo(String path) {
    String fullUrl = path.startsWith("http") ? path : baseUrl + path;
    System.out.println("Navigating to: " + fullUrl);
    try {
      page.navigate(fullUrl);
      page.waitForLoadState(LoadState.NETWORKIDLE);
    } catch (Exception e) {
      System.err.println("Error navigating to " + fullUrl + ": " + e.getMessage());
      throw e;
    }
  }

  private Response submitRequestToCreateEnvelope(String[] envelope, String path, String method) {
    try {
      System.out.println(
          "Creating envelope with name: " + envelope[0] + ", budget: " + envelope[1]);

      // Click the New Envelope button to open the modal
      clickNewEnvelopeButton();

      // Wait for the modal to be visible with a more specific selector
      Locator modal = page.locator("div[role='dialog']");
      modal.waitFor(new Locator.WaitForOptions()
          .setState(WaitForSelectorState.VISIBLE)
          .setTimeout(5000));

      // Wait for form fields to be ready
      Locator nameInput = modal.getByLabel("Name");
      Locator budgetInput = modal.getByLabel("Budget");

      // Clear and fill the form fields
      nameInput.clear();
      nameInput.fill(envelope[0]);
      budgetInput.clear();
      budgetInput.fill(envelope[1]);

      // Submit the form and wait for response
      Locator submitButton = modal.getByRole(AriaRole.BUTTON,
          new Locator.GetByRoleOptions().setName("Create"));

      return getResponse(path, method, submitButton::click);
    } catch (Exception e) {
      System.err.println("Error creating envelope: " + e.getMessage());
      throw e;
    }
  }

  private void clickNewEnvelopeButton() {
    try {
      System.out.println("Clicking New Envelope button");
      // Find the button with name 'New Envelope'
      Locator newButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("New Envelope"));

      // Wait for the button to be visible and clickable
      newButton.waitFor(new Locator.WaitForOptions()
          .setState(WaitForSelectorState.VISIBLE)
          .setTimeout(5000));

      newButton.click();

      // Wait for the modal to start appearing
      page.waitForTimeout(500);
    } catch (Exception e) {
      System.err.println("Error clicking New Envelope button: " + e.getMessage());
      throw e;
    }
  }

  private Response getResponse(String path, String method, Runnable callback) {

    return page.waitForResponse(
        response -> isPreconditionStisfied(path, method, response),
        callback
    );
  }

  private boolean isPreconditionStisfied(String path, String method, Response response) {
    return response.url().endsWith(path) && response.request().method().equals(method);
  }

  public void waitForTimeout(int timeout) {
    // Wait a moment before next submission to ensure UI updates
    page.waitForTimeout(timeout);
  }

  public void waitForElement(String selector) {
    page.waitForSelector(selector, new Page.WaitForSelectorOptions()
        .setState(WaitForSelectorState.VISIBLE)
        .setTimeout(5000));
  }

  public void createEnvelope(String[] envelope) {
    submitRequestToCreateEnvelope(envelope, "/api/envelopes", "POST");
  }

  public boolean isTextVisible(String text, int timeout) {
    try {
      page.waitForSelector(String.format("text='%s'" + ":visible", text),
          new Page.WaitForSelectorOptions()
              .setState(WaitForSelectorState.VISIBLE)
              .setTimeout(timeout));
      return true;
    } catch (Exception e) {
      System.err.println("Text not found: " + text);
      System.err.println("Current page content: " + getPageContent());
      return false;
    }
  }

  public String getPageContent() {
    try {
      return page.content();
    } catch (Exception e) {
      return "Error getting page content: " + e.getMessage();
    }
  }

  // Overloaded method with default timeout for backward compatibility
  public boolean isTextVisible(String text) {
    return isTextVisible(text, 2000);
  }

  public void clickViewExpensesButton(String envelopeName) {
    try {
      System.out.println("Clicking View Expenses button for envelope: " + envelopeName);
      
      // Find the first envelope row containing 'Jana' and get its View Expenses button
      Locator janaRow = page.locator("tr:has-text('Jana')").first();
      Locator viewButton = janaRow.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("View Expenses"));
      
      // Wait for the button to be visible and clickable
      viewButton.waitFor(new Locator.WaitForOptions()
          .setState(WaitForSelectorState.VISIBLE)
          .setTimeout(5000));
      
      viewButton.click();
      
      // Wait for the expenses view to load (using a more flexible selector that doesn't assume h2)
      page.waitForSelector(":text('Expenses for Jana')", 
          new Page.WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE));
    } catch (Exception e) {
      System.err.println("Error clicking View Expenses button: " + e.getMessage());
      throw e;
    }
  }

  public void clickAddExpenseButton() {
    try {
      System.out.println("Clicking Add Expense button");
      Locator addButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add Expense"));
      
      addButton.waitFor(new Locator.WaitForOptions()
          .setState(WaitForSelectorState.VISIBLE)
          .setTimeout(5000));
      
      addButton.click();
      
      // Wait for the expense form to appear
      page.waitForSelector("div[role='dialog']", 
          new Page.WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE));
    } catch (Exception e) {
      System.err.println("Error clicking Add Expense button: " + e.getMessage());
      throw e;
    }
  }

  public void fillExpenseForm(String amount, String memo) {
    try {
      System.out.println("Filling expense form with memo: " + memo + ", amount: " + amount);
      // Locate the modal using the antd modal class
      Locator modal = page.locator(".ant-modal");
      
      // Fill in the form fields
      Locator memoInput = modal.getByLabel("Memo");
      Locator amountInput = modal.getByLabel("Amount");
      
      // Clear and fill the memo field
      memoInput.clear();
      memoInput.fill(memo);
      
      // Clear and fill the amount field
      amountInput.clear();
      amountInput.fill(amount);
      
      // Small delay to ensure all fields are filled
      page.waitForTimeout(200);
    } catch (Exception e) {
      System.err.println("Error filling expense form: " + e.getMessage());
      throw e;
    }
  }

  public void submitExpenseForm() {
    try {
      System.out.println("Submitting expense form");
      // Locate the modal using the antd modal class
      Locator modal = page.locator(".ant-modal");
      Locator submitButton = modal.getByRole(AriaRole.BUTTON, 
          new Locator.GetByRoleOptions().setName("Add Expense"));
      
      submitButton.click();
      
      // Wait for the modal to close
      page.waitForSelector(".ant-modal", 
          new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));
    } catch (Exception e) {
      System.err.println("Error submitting expense form: " + e.getMessage());
      throw e;
    }
  }
}
