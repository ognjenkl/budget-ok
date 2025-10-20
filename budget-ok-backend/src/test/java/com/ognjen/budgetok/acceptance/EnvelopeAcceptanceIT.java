package com.ognjen.budgetok.acceptance;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EnvelopeAcceptanceIT {

  @Autowired
  private UiDriver driver;

  @BeforeAll
  void launchBrowser() {
    driver = new UiDriver();
  }

  @AfterAll
  void closeBrowser() {
    driver.close();
  }

  @BeforeEach
  void createContextAndPage() {
    System.out.println("=== Setting up test context ===");
    driver.initContext();
    
    // Navigate to the envelopes page and wait for it to load
    System.out.println("Navigating to envelopes page...");
    driver.navigateTo("/envelopes");
    
    // Wait for the page to be fully loaded
    System.out.println("Waiting for page to load...");
    driver.waitForElement("h1:has-text('Envelopes')");
    System.out.println("Page loaded successfully");
  }

  @AfterEach
  void closeContext() {
    driver.closeContext();
  }

  @Test
  void shouldCreateEnvelope() {
    System.out.println("=== Starting shouldCreateEnvelope test ===");
    try {
      // Given
      String uniqueName = "Test Envelope " + System.currentTimeMillis();
      String[] envelope = {uniqueName, "100.5"};
      System.out.println("Test envelope: " + uniqueName);

      // When
      System.out.println("Creating envelope...");
      driver.createEnvelope(envelope);
      System.out.println("Envelope creation request sent");

      // Then
      String successMessage = "Envelope created successfully!";
      System.out.println("Verifying success message...");
      assertTrue(
          driver.isTextVisible(successMessage, 5000), // Wait up to 5 seconds
          String.format("Success message '%s' should be visible on the page. Current page: %s", 
              successMessage, driver.getPageContent())
      );
      
      // Verify the envelope appears in the list
      System.out.println("Verifying envelope in the list...");
      assertTrue(
          driver.isTextVisible(uniqueName, 5000), // Wait up to 5 seconds
          String.format("Envelope with name '%s' should be visible in the list. Current page: %s", 
              uniqueName, driver.getPageContent())
      );
      
      System.out.println("Test completed successfully");
    } catch (Exception e) {
      System.err.println("Test failed: " + e.getMessage());
      e.printStackTrace();
      throw e;
    }
  }

  @Test
  void shouldCreateMultipleEnvelopes() {
    // Given
    String[][] testEnvelopes = createThreeEnvelopes();

    // When and Then
    for (String[] envelope : testEnvelopes) {

      driver.createEnvelope(envelope);

      driver.waitForTimeout(500);
      String successMessage = "Envelope created successfully!";
      assertTrue(
          driver.isTextVisible(successMessage),
          String.format("Success message '%s' should be visible on the page", successMessage)
      );

      driver.waitForTimeout(3000);
      assertFalse(
          driver.isTextVisible(successMessage),
          String.format("Success message '%s' should not be visible on the page", successMessage)
      );
    }
  }

  @NotNull
  private String[][] createThreeEnvelopes() {
    return new String[][]{
        {"Rent", "1200.0"},
        {"Groceries", "400"},
        {"Utilities", "200"}
    };
  }
}
