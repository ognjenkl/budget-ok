package com.ognjen.template.systemtest.e2etests;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SubscriptionDiscountE2eTest {

  private final HttpClient client = HttpClient.newHttpClient();

  @Test
  void givenSubscriptionEndpoint_whenCallingWithPrice_thenCannotVerifyExactDiscountedPriceFromExternalBankOk()
      throws Exception {
    // This test demonstrates an E2E scenario where we CANNOT verify an exact output value
    // because we cannot control the external I/O system (Bank OK's subscription discount).
    //
    // Scenario:
    // 1. E2E test calls /api/subscription endpoint with originalPrice: 100
    // 2. Backend internally calls Bank OK's subscription-discount endpoint
    // 3. Bank OK returns a discount (which we cannot control or predict)
    // 4. Backend calculates: finalPrice = originalPrice - bankOkDiscount
    // 5. Backend returns the finalPrice to us
    //
    // What we CAN verify:
    // - The subscription calculation was successful (200 status)
    // - A finalPrice was returned
    // - The finalPrice is less than the original price
    //
    // What we CANNOT verify:
    // - The exact finalPrice value (e.g., assertEquals(85, finalPrice))
    // - The exact discount amount from Bank OK
    // Because Bank OK's discount is determined by their external system,
    // we have read-only access, and we cannot control/modify the discount.

    int originalPrice = 100;

    // Act - Call subscription endpoint which internally uses Bank OK's discount
    String subscriptionPayload = "{\"price\":" + originalPrice + "}";
    HttpRequest subscriptionRequest = HttpRequest.newBuilder()
        .uri(new URI("http://localhost:8080/api/calculate-subscription"))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(subscriptionPayload))
        .build();

    HttpResponse<String> subscriptionResponse = client.send(subscriptionRequest,
        HttpResponse.BodyHandlers.ofString());

    // Assert - Verify the subscription calculation was successful
    assertEquals(200, subscriptionResponse.statusCode(),
        "Should calculate subscription price with Bank OK discount");

    String subscriptionResponseBody = subscriptionResponse.body();
    assertTrue(subscriptionResponseBody.contains("\"finalPrice\""),
        "Response should contain finalPrice field");

    // Extract the finalPrice from response
    int finalPrice = extractPriceFromResponse(subscriptionResponseBody, "finalPrice");
    assertTrue(finalPrice > 0,
        "finalPrice should be a valid positive price");

    // Assert - Verify the discount was applied (finalPrice < originalPrice)
    // This proves Bank OK's discount was applied, but we DON'T verify the EXACT amount
    assertTrue(finalPrice < originalPrice,
        "finalPrice ("
            + finalPrice
            + ") should be less than originalPrice ("
            + originalPrice
            + ") due to Bank OK discount");
  }

  private int extractPriceFromResponse(String responseBody, String priceFieldName) {
    // Find the price field by name (e.g., "finalPrice", "price", etc.)
    String searchPattern = "\"" + priceFieldName + "\":";
    int priceIndex = responseBody.indexOf(searchPattern);
    if (priceIndex == -1) {
      return -1;
    }

    // Extract the price value
    int priceStart = priceIndex + searchPattern.length();
    int priceEnd = priceStart;

    // Find the end of the number
    while (priceEnd < responseBody.length()) {
      char c = responseBody.charAt(priceEnd);
      if (!Character.isDigit(c) && c != '.') {
        break;
      }
      priceEnd++;
    }

    String priceStr = responseBody.substring(priceStart, priceEnd).trim();
    return Double.valueOf(priceStr).intValue();
  }
}
