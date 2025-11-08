package com.ognjen.template.systemtest.e2etests;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BankOkEnvelopeIntegrationE2eTest {

  private final HttpClient client = HttpClient.newHttpClient();

  @Test
  void givenBankOkCartAsExpenses_whenSyncingExpenses_thenEnvelopeContainsExpenses()
      throws Exception {
    // This test demonstrates the complete flow:
    // 1. Fetch expenses from Bank OK (external system)
    // 2. Extract product data from cart
    // 3. Create envelope
    // 4. Add expense from Bank OK product to the envelope
    // 5. Verify envelope balance reflects the expense

    // Arrange - Fetch cart from Bank OK
    HttpRequest bankOkRequest = HttpRequest.newBuilder()
        .uri(new URI("http://localhost:8080/api/bankok/carts/2"))
        .GET()
        .build();

    HttpResponse<String> bankOkResponse = client.send(bankOkRequest,
        HttpResponse.BodyHandlers.ofString());

    // Assert Bank OK call succeeded
    assertEquals(200, bankOkResponse.statusCode(), "Should fetch cart from Bank OK");
    String cartBody = bankOkResponse.body();
    assertTrue(cartBody.contains("\"products\""), "Cart should contain products");

    // Extract Golf Ball product from Bank OK cart
    String productTitle = "Golf Ball";
    int productPrice = extractProductPrice(cartBody, productTitle);
    assertTrue(productPrice > 0, "Should extract valid Golf Ball price");

    // Act - Create envelope for the expenses
    String envelopePayload = "{\"name\":\"Bank OK Sync\",\"budget\":5000}";
    HttpRequest createEnvelopeRequest = HttpRequest.newBuilder()
        .uri(new URI("http://localhost:8080/api/envelopes"))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(envelopePayload))
        .build();

    HttpResponse<String> envelopeResponse = client.send(createEnvelopeRequest,
        HttpResponse.BodyHandlers.ofString());

    // Assert envelope created
    assertEquals(200, envelopeResponse.statusCode(), "Should create envelope");
    String envelopeBody = envelopeResponse.body();
    assertTrue(envelopeBody.contains("\"name\":\"Bank OK Sync\""),
        "Envelope should have correct name");

    // Extract envelope ID
    long envelopeId = extractIdFromResponse(envelopeBody);
    assertTrue(envelopeId > 0, "Should extract valid envelope ID");

    // Act - Add expense to the envelope using Bank OK product data
    String expensePayload = "{\"amount\":" + productPrice + ",\"memo\":\"" + productTitle
        + "\",\"transactionType\":\"WITHDRAW\"}";
    HttpRequest addExpenseRequest = HttpRequest.newBuilder()
        .uri(new URI("http://localhost:8080/api/envelopes/" + envelopeId + "/expenses"))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(expensePayload))
        .build();

    HttpResponse<String> addExpenseResponse = client.send(addExpenseRequest,
        HttpResponse.BodyHandlers.ofString());

    // Assert expense added
    assertEquals(200, addExpenseResponse.statusCode(), "Should add expense");
    String expenseResultBody = addExpenseResponse.body();
    assertTrue(expenseResultBody.contains("\"WITHDRAW\""),
        "Response should show withdraw transaction");
    assertTrue(expenseResultBody.contains("\"amount\":" + productPrice),
        "Response should contain product price as amount");
    assertTrue(expenseResultBody.contains("\"memo\":\"" + productTitle + "\""),
        "Response should contain product title as memo");

    // Act - Fetch the updated envelope to verify balance
    HttpRequest getEnvelopeRequest = HttpRequest.newBuilder()
        .uri(new URI("http://localhost:8080/api/envelopes/" + envelopeId))
        .GET()
        .build();

    HttpResponse<String> getEnvelopeResponse = client.send(getEnvelopeRequest,
        HttpResponse.BodyHandlers.ofString());

    // Assert final verification
    assertEquals(200, getEnvelopeResponse.statusCode(), "Should fetch updated envelope");
    String finalEnvelopeBody = getEnvelopeResponse.body();

    // Verify the envelope has the expense
    assertTrue(finalEnvelopeBody.contains("\"expenses\""), "Envelope should have expenses array");
    assertTrue(finalEnvelopeBody.contains("\"" + productTitle + "\""),
        "Envelope should contain synced product title");

    // Verify expense data is correctly stored
    assertTrue(finalEnvelopeBody.contains("\"amount\":" + productPrice),
        "Expense should have product price as amount");
    assertTrue(finalEnvelopeBody.contains("\"transactionType\":\"WITHDRAW\""),
        "Expense should be marked as WITHDRAW");

    // Verify balance calculation
    int expectedBalance = 5000 - productPrice;
    assertTrue(finalEnvelopeBody.contains("\"balance\":" + expectedBalance),
        "Envelope balance should be " + expectedBalance + " after withdrawal");
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

  private int extractProductPrice(String cartBody, String productTitle) {
    // Find the product by title
    int titleIndex = cartBody.indexOf("\"title\":\"" + productTitle + "\"");
    if (titleIndex == -1) {
      return -1;
    }

    // Find the price field after this title (within the same product object)
    int priceIndex = cartBody.indexOf("\"price\":", titleIndex);
    if (priceIndex == -1) {
      return -1;
    }

    // Make sure we don't go past the end of this product object
    int nextProductIndex = cartBody.indexOf("\"title\":", titleIndex + 1);
    if (nextProductIndex != -1 && priceIndex > nextProductIndex) {
      return -1; // Price is in next product, not this one
    }

    // Extract the price value
    int priceStart = priceIndex + 8; // length of "price":
    int priceEnd = cartBody.indexOf(",", priceStart);
    if (priceEnd == -1) {
      priceEnd = cartBody.indexOf("}", priceStart);
    }

    String priceStr = cartBody.substring(priceStart, priceEnd).trim();
    return Double.valueOf(priceStr).intValue();
  }
}
