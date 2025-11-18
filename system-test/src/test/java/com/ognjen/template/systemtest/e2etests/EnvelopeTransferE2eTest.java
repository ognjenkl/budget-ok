package com.ognjen.template.systemtest.e2etests;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnvelopeTransferE2eTest {

  private final HttpClient client = HttpClient.newHttpClient();
  private final String baseUrl = "http://localhost:8080/api/envelopes";

  @Test
  void givenSufficientBalance_whenTransferringAmount_thenTransferSucceeds() throws Exception {

    long sourceEnvelopeId = createEnvelope("Groceries", 1000);
    long targetEnvelopeId = createEnvelope("Entertainment", 500);
    int transferAmount = 200;

    String transferPayload = "{\"sourceEnvelopeId\":" + sourceEnvelopeId
        + ",\"targetEnvelopeId\":" + targetEnvelopeId
        + ",\"amount\":" + transferAmount
        + ",\"memo\":\"Transfer\"}";
    HttpRequest transferRequest = HttpRequest.newBuilder()
        .uri(new URI(baseUrl + "/transfer"))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(transferPayload))
        .build();

    HttpResponse<String> transferResponse = client.send(transferRequest,
        HttpResponse.BodyHandlers.ofString());

    assertEquals(200, transferResponse.statusCode(), "Transfer should return 200 OK");
    assertTrue(transferResponse.body().contains("\"message\":\"Transfer successful\""),
        "Response should contain success message");

    HttpResponse<String> sourceEnvelopeResponse = getEnvelope(sourceEnvelopeId);
    String sourceEnvelopeBody = sourceEnvelopeResponse.body();
    assertTrue(sourceEnvelopeBody.contains("\"balance\":800"),
        "Source envelope balance should be 800 after transfer");
    assertTrue(sourceEnvelopeBody.contains("\"transactionType\":\"WITHDRAW\""),
        "Source envelope should have WITHDRAW transaction");
    assertTrue(sourceEnvelopeBody.contains("\"amount\":" + transferAmount),
        "Source envelope should have a withdrawal of " + transferAmount);
    assertTrue(sourceEnvelopeBody.contains("\"memo\":\"Transfer\""),
        "Source envelope transaction should have correct memo");

    HttpResponse<String> targetEnvelopeResponse = getEnvelope(targetEnvelopeId);
    String targetEnvelopeBody = targetEnvelopeResponse.body();
    assertTrue(targetEnvelopeBody.contains("\"balance\":700"),
        "Target envelope balance should be 700 after transfer");
    assertTrue(targetEnvelopeBody.contains("\"transactionType\":\"DEPOSIT\""),
        "Target envelope should have DEPOSIT transaction");
    assertTrue(targetEnvelopeBody.contains("\"amount\":" + transferAmount),
        "Target envelope should have a deposit of " + transferAmount);
    assertTrue(targetEnvelopeBody.contains("\"memo\":\"Transfer\""),
        "Target envelope transaction should have correct memo");
  }

  @Test
  void givenInsufficientBalance_whenTransferringAmount_thenTransferFails() throws Exception {

    long sourceEnvelopeId = createEnvelope("Limited Budget", 50);
    long targetEnvelopeId = createEnvelope("Target", 500);
    int transferAmount = 200;

    String transferPayload = "{\"sourceEnvelopeId\":" + sourceEnvelopeId
        + ",\"targetEnvelopeId\":" + targetEnvelopeId
        + ",\"amount\":" + transferAmount
        + ",\"memo\":\"Transfer\"}";
    HttpRequest transferRequest = HttpRequest.newBuilder()
        .uri(new URI(baseUrl + "/transfer"))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(transferPayload))
        .build();

    HttpResponse<String> transferResponse = client.send(transferRequest,
        HttpResponse.BodyHandlers.ofString());

    assertEquals(400, transferResponse.statusCode(),
        "Transfer should fail with 400 when insufficient balance");
    assertTrue(transferResponse.body().contains("Insufficient balance"),
        "Error message should mention insufficient balance");

    HttpResponse<String> sourceEnvelopeResponse = getEnvelope(sourceEnvelopeId);
    String sourceEnvelopeBody = sourceEnvelopeResponse.body();
    assertTrue(sourceEnvelopeBody.contains("\"balance\":50"),
        "Source envelope balance should remain 50 after failed transfer");
    assertFalse(sourceEnvelopeBody.contains("\"WITHDRAW\""),
        "Source envelope should not have any withdraw transactions when transfer fails");

    HttpResponse<String> targetEnvelopeResponse = getEnvelope(targetEnvelopeId);
    String targetEnvelopeBody = targetEnvelopeResponse.body();
    assertTrue(targetEnvelopeBody.contains("\"balance\":500"),
        "Target envelope balance should remain 500 after failed transfer");
    assertFalse(targetEnvelopeBody.contains("\"DEPOSIT\""),
        "Target envelope should not have any deposit transactions when transfer fails");
  }

  @Test
  void givenNonExistentTargetEnvelope_whenTransferringAmount_thenTransferFails() throws Exception {

    long sourceEnvelopeId = createEnvelope("Source", 1000);
    long nonExistentTargetId = 99999;

    String transferPayload =
        "{\"sourceEnvelopeId\":" + sourceEnvelopeId + ",\"targetEnvelopeId\":" + nonExistentTargetId
            + ",\"amount\":100,\"memo\":\"Transfer\"}";
    HttpRequest transferRequest = HttpRequest.newBuilder()
        .uri(new URI(baseUrl + "/transfer"))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(transferPayload))
        .build();

    HttpResponse<String> transferResponse = client.send(transferRequest,
        HttpResponse.BodyHandlers.ofString());

    assertEquals(404, transferResponse.statusCode(),
        "Transfer should fail with 404 when target not found");
    assertTrue(transferResponse.body().contains("not found"),
        "Error message should mention envelope not found");
    HttpResponse<String> envelopeResponse = getEnvelope(sourceEnvelopeId);
    String envelopeBody = envelopeResponse.body();
    assertFalse(envelopeBody.contains("\"WITHDRAW\""),
        "Source envelope should not have any expenses");
  }

  private long createEnvelope(String name, int budget) throws Exception {
    String payload = "{\"name\":\"" + name + "\",\"budget\":" + budget + "}";
    HttpRequest request = HttpRequest.newBuilder()
        .uri(new URI(baseUrl))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(payload))
        .build();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    assertEquals(201, response.statusCode(), "Should create envelope successfully");

    // Extract ID from response
    String responseBody = response.body();
    int idIndex = responseBody.indexOf("\"id\":");
    int idEndIndex = responseBody.indexOf(",", idIndex);
    if (idEndIndex == -1) {
      idEndIndex = responseBody.indexOf("}", idIndex);
    }
    String idStr = responseBody.substring(idIndex + 5, idEndIndex).trim();
    return Long.parseLong(idStr);
  }

  private HttpResponse<String> getEnvelope(long id) throws Exception {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(new URI(baseUrl + "/" + id))
        .GET()
        .build();

    return client.send(request, HttpResponse.BodyHandlers.ofString());
  }
}
