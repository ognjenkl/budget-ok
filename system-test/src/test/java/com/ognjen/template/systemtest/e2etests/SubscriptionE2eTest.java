package com.ognjen.template.systemtest.e2etests;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SubscriptionE2eTest {

  private final HttpClient client = HttpClient.newHttpClient();

  @Test
  void givenCalculatePriceEndpoint_whenCallingWithPrice_thenCannotVerifyExactTaxAmountFromExternalBankOk()
      throws Exception {

    int basePrice = 100;

    // Act - Call calculate-price endpoint which internally calls Bank OK for tax
    String pricePayload = "{\"price\":" + basePrice + "}";
    HttpRequest priceRequest = HttpRequest.newBuilder()
        .uri(new URI("http://localhost:8080/api/subscription/calculate-tax"))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(pricePayload))
        .build();

    HttpResponse<String> priceResponse = client.send(priceRequest,
        HttpResponse.BodyHandlers.ofString());

    // Assert - Verify the price calculation was successful
    assertEquals(200, priceResponse.statusCode(),
        "Should calculate price with Bank OK tax");

    String priceResponseBody = priceResponse.body();
    assertTrue(priceResponseBody.contains("\"finalPrice\""),
        "Response should contain finalPrice field");

    // Extract the finalPrice from response
    int finalPrice = extractPriceFromResponse(priceResponseBody, "finalPrice");
    assertTrue(finalPrice > 0,
        "finalPrice should be a valid positive price");

    // Assert - Verify tax was applied (finalPrice > basePrice)
    // This proves Bank OK's tax was applied, but we DON'T verify the EXACT amount
    assertTrue(finalPrice > basePrice,
        "finalPrice ("
            + finalPrice
            + ") should be greater than basePrice ("
            + basePrice
            + ") due to Bank OK tax");

  }

  @Test
  void givenSubscriptionEndpoint_whenCallingWithPrice_thenCannotVerifyExactDiscountedPriceBeforeAndAfter4Pm()
      throws Exception {

    int originalPrice = 100;

    // Act - Call subscription endpoint which internally uses Bank OK's discount
    String subscriptionPayload = "{\"price\":" + originalPrice + "}";
    HttpRequest subscriptionRequest = HttpRequest.newBuilder()
        .uri(new URI("http://localhost:8080/api/subscription/calculate-discount"))
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
