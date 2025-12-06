package com.ognjen.template.systemtest.e2etests;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BankOkEnvelopeIntegrationE2eTest {

  private final HttpClient client = HttpClient.newHttpClient();

  @Test
  void givenElectronicsEnvelope_whenAddingExpenseToBankOkAndSyncing_thenExpenseAppearsInEnvelope()
      throws Exception {

    String envelopeName = "electronics";

    // Arrange - Sync Bank OK first to get clean state
    HttpRequest syncBankOkBeforeRequest = HttpRequest.newBuilder()
        .uri(new URI("http://localhost:8080/api/bankok/sync-bank-ok"))
        .POST(HttpRequest.BodyPublishers.noBody())
        .build();

    HttpResponse<String> syncBankOkBeforeResponse = client.send(syncBankOkBeforeRequest,
        HttpResponse.BodyHandlers.ofString());

    assertEquals(204, syncBankOkBeforeResponse.statusCode(),
        "Should sync Bank OK before test");

    // Arrange - Get electronics envelope ID first
    HttpRequest getElectronicsEnvelopeRequest = HttpRequest.newBuilder()
        .uri(new URI("http://localhost:8080/api/envelopes?name=" + envelopeName))
        .GET()
        .build();

    HttpResponse<String> getElectronicsEnvelopeResponse = client.send(getElectronicsEnvelopeRequest,
        HttpResponse.BodyHandlers.ofString());

    assertEquals(200, getElectronicsEnvelopeResponse.statusCode(),
        "Should fetch electronics envelope");
    String electronicsEnvelopeBody = getElectronicsEnvelopeResponse.body();

    // Extract electronics envelope ID
    long electronicsEnvelopeId = extractIdFromResponse(electronicsEnvelopeBody);
    assertTrue(electronicsEnvelopeId > 0,
        "Should extract valid electronics envelope ID");

    // Act - Create expense in Bank OK external API (localhost:8081)
    String expenseTitle = "Samsung 25";
    int expensePrice = 250;
    String bankOkExpensePayload = "{\"title\":\"" + expenseTitle + "\",\"price\":"
        + expensePrice + ",\"envelopeName\":\"" + envelopeName + "\",\"transactionType\":\"WITHDRAW\"}";
    HttpRequest createBankOkExpenseRequest = HttpRequest.newBuilder()
        .uri(new URI("http://localhost:8081/api/expenses/create-expense"))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(bankOkExpensePayload))
        .build();

    HttpResponse<String> createBankOkExpenseResponse = client.send(createBankOkExpenseRequest,
        HttpResponse.BodyHandlers.ofString());

    assertEquals(201, createBankOkExpenseResponse.statusCode(),
        "Should create expense in Bank OK");
    String bankOkExpenseResponseBody = createBankOkExpenseResponse.body();
    long bankOkExpenseId = extractIdFromResponse(bankOkExpenseResponseBody);
    assertTrue(bankOkExpenseId > 0,
        "Should extract valid Bank OK expense ID");

    // Arrange - Verify the expense ID is not in the envelope initially
    assertFalse(electronicsEnvelopeBody.contains("\"id\":" + bankOkExpenseId),
        "Electronics envelope should not have Bank OK expense (ID: " + bankOkExpenseId + ") initially");

    // Act - Call sync-bank-ok endpoint to sync expenses to envelopes
    HttpRequest syncBankOkRequest = HttpRequest.newBuilder()
        .uri(new URI("http://localhost:8080/api/bankok/sync-bank-ok"))
        .POST(HttpRequest.BodyPublishers.noBody())
        .build();

    HttpResponse<String> syncBankOkResponse = client.send(syncBankOkRequest,
        HttpResponse.BodyHandlers.ofString());

    assertEquals(204, syncBankOkResponse.statusCode(),
        "Should sync Bank OK expenses to envelopes");

    // Act - Get electronics envelope again to verify Samsung 25 expense is now there
    HttpRequest getUpdatedElectronicsEnvelopeRequest = HttpRequest.newBuilder()
        .uri(new URI("http://localhost:8080/api/envelopes/" + electronicsEnvelopeId))
        .GET()
        .build();

    HttpResponse<String> getUpdatedElectronicsEnvelopeResponse = client.send(
        getUpdatedElectronicsEnvelopeRequest,
        HttpResponse.BodyHandlers.ofString());

    assertEquals(200, getUpdatedElectronicsEnvelopeResponse.statusCode(),
        "Should fetch updated electronics envelope");
    String updatedElectronicsEnvelopeBody = getUpdatedElectronicsEnvelopeResponse.body();

    // Assert - Verify Samsung 25 expense is now in the envelope
    assertTrue(updatedElectronicsEnvelopeBody.contains("\"expenses\""),
        "Electronics envelope should have expenses array");
    assertTrue(updatedElectronicsEnvelopeBody.contains("\"bankExpenseId\":" + bankOkExpenseId),
        "Electronics envelope should contain Bank OK expense (bankExpenseId: " + bankOkExpenseId + ") after sync");
    assertTrue(updatedElectronicsEnvelopeBody.contains("\"" + expenseTitle + "\""),
        "Electronics envelope should contain Samsung 25 expense after sync");
    assertTrue(updatedElectronicsEnvelopeBody.contains("\"amount\":" + expensePrice),
        "Samsung 25 expense should have correct price");
    assertTrue(updatedElectronicsEnvelopeBody.contains("\"transactionType\":\"WITHDRAW\""),
        "Samsung 25 expense should be marked as WITHDRAW");
  }

  private long extractIdFromResponse(String responseBody) {
    int idIndex = responseBody.indexOf("\"id\":");
    if (idIndex == -1) {
      return -1;
    }
    int idEndIndex = responseBody.indexOf(",", idIndex);
    if (idEndIndex == -1) {
      idEndIndex = responseBody.indexOf("}", idIndex);
    }
    String idStr = responseBody.substring(idIndex + 5, idEndIndex).trim();
    return Long.parseLong(idStr);
  }

  private int extractExpensePrice(String cartBody, String expenseTitle) {
    // Find the expense by title
    int titleIndex = cartBody.indexOf("\"title\":\"" + expenseTitle + "\"");
    if (titleIndex == -1) {
      return -1;
    }

    // Find the price field after this title (within the same expense object)
    int priceIndex = cartBody.indexOf("\"price\":", titleIndex);
    if (priceIndex == -1) {
      return -1;
    }

    // Make sure we don't go past the end of this expense object
    int nextExpenseIndex = cartBody.indexOf("\"title\":", titleIndex + 1);
    if (nextExpenseIndex != -1 && priceIndex > nextExpenseIndex) {
      return -1; // Price is in next expense, not this one
    }

    // Extract the price value
    int priceStart = priceIndex + 8; // length of "price":
    int priceEnd = priceStart;

    // Find the end of the number
    while (priceEnd < cartBody.length()) {
      char c = cartBody.charAt(priceEnd);
      if (!Character.isDigit(c) && c != '.') {
        break;
      }
      priceEnd++;
    }

    String priceStr = cartBody.substring(priceStart, priceEnd).trim();
    return Double.valueOf(priceStr).intValue();
  }
}
