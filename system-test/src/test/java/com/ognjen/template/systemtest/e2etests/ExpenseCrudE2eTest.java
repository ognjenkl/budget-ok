package com.ognjen.template.systemtest.e2etests;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExpenseCrudE2eTest {

  private final HttpClient client = HttpClient.newHttpClient();
  private final String baseUrl = "http://localhost:8080/api/envelopes";

  @Test
  void givenExistingEnvelope_whenCreateWithdrawExpense_thenExpenseIsAdded()
      throws Exception {

    long envelopeId = createEnvelope("Shopping", 1000);
    String payload = "{\"amount\":150,\"memo\":\"Groceries\",\"transactionType\":\"WITHDRAW\"}";
    HttpRequest request = HttpRequest.newBuilder()
        .uri(new URI(baseUrl + "/" + envelopeId + "/expenses"))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(payload))
        .build();

    HttpResponse<String> response = client.send(request,
        HttpResponse.BodyHandlers.ofString());

    assertEquals(201, response.statusCode(), "Should return 201 CREATED");
    String body = response.body();
    assertTrue(body.contains("\"WITHDRAW\""), "Response should contain WITHDRAW transaction");
    assertTrue(body.contains("\"amount\":150"), "Response should contain expense amount");
    assertTrue(body.contains("\"memo\":\"Groceries\""), "Response should contain expense memo");
  }

  @Test
  void givenExistingEnvelope_whenCreateDepositExpense_thenExpenseIsAdded()
      throws Exception {

    long envelopeId = createEnvelope("Refunds", 500);
    String payload = "{\"amount\":50,\"memo\":\"Refund for return\",\"transactionType\":\"DEPOSIT\"}";
    HttpRequest request = HttpRequest.newBuilder()
        .uri(new URI(baseUrl + "/" + envelopeId + "/expenses"))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(payload))
        .build();

    HttpResponse<String> response = client.send(request,
        HttpResponse.BodyHandlers.ofString());

    assertEquals(201, response.statusCode(), "Should return 201 CREATED");
    String body = response.body();
    assertTrue(body.contains("\"DEPOSIT\""), "Response should contain DEPOSIT transaction");
    assertTrue(body.contains("\"amount\":50"), "Response should contain expense amount");
    assertTrue(body.contains("\"memo\":\"Refund for return\""),
        "Response should contain expense memo");
  }

  @Test
  void givenExistingEnvelope_whenCreateMultipleExpenses_thenAllExpensesAreAdded() throws Exception {

    long envelopeId = createEnvelope("Mixed Transactions", 2000);

    String withdrawExpense1Payload = "{\"amount\":100,\"memo\":\"Purchase 1\",\"transactionType\":\"WITHDRAW\"}";
    HttpRequest request1 = HttpRequest.newBuilder()
        .uri(new URI(baseUrl + "/" + envelopeId + "/expenses"))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(withdrawExpense1Payload))
        .build();
    client.send(request1, HttpResponse.BodyHandlers.ofString());

    String depositExpensePayload = "{\"amount\":30,\"memo\":\"Refund\",\"transactionType\":\"DEPOSIT\"}";
    HttpRequest request2 = HttpRequest.newBuilder()
        .uri(new URI(baseUrl + "/" + envelopeId + "/expenses"))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(depositExpensePayload))
        .build();
    client.send(request2, HttpResponse.BodyHandlers.ofString());

    String withdrawExpense2Payload = "{\"amount\":75,\"memo\":\"Purchase 2\",\"transactionType\":\"WITHDRAW\"}";
    HttpRequest request3 = HttpRequest.newBuilder()
        .uri(new URI(baseUrl + "/" + envelopeId + "/expenses"))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(withdrawExpense2Payload))
        .build();
    HttpResponse<String> response3 = client.send(request3, HttpResponse.BodyHandlers.ofString());

    assertEquals(201, response3.statusCode(), "Should add all expenses");
    String body = response3.body();
    assertTrue(body.contains("\"WITHDRAW\""), "Response should contain WITHDRAW transactions");
    assertTrue(body.contains("\"DEPOSIT\""), "Response should contain DEPOSIT transaction");
  }

  @Test
  void givenEnvelopeWithExpenses_whenGetEnvelope_thenAllExpensesAreReturned() throws Exception {

    long envelopeId = createEnvelope("Vacation Fund", 3000);
    addExpense(envelopeId, 500, "Flight", "WITHDRAW");
    addExpense(envelopeId, 200, "Hotel", "WITHDRAW");
    addExpense(envelopeId, 100, "Meals", "WITHDRAW");

    HttpRequest request = HttpRequest.newBuilder()
        .uri(new URI(baseUrl + "/" + envelopeId))
        .GET()
        .build();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    assertEquals(200, response.statusCode(), "Should return 200 OK");
    String body = response.body();
    assertTrue(body.contains("\"Flight\""), "Response should contain Flight expense");
    assertTrue(body.contains("\"Hotel\""), "Response should contain Hotel expense");
    assertTrue(body.contains("\"Meals\""), "Response should contain Meals expense");
    assertTrue(body.contains("\"amount\":500"), "Response should contain flight amount");
    assertTrue(body.contains("\"amount\":200"), "Response should contain hotel amount");
    assertTrue(body.contains("\"amount\":100"), "Response should contain meals amount");
  }

  private long createEnvelope(String name, int budget) throws Exception {
    String payload = "{\"name\":\"" + name + "\",\"budget\":" + budget + "}";
    HttpRequest request = HttpRequest.newBuilder()
        .uri(new URI(baseUrl))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(payload))
        .build();

    HttpResponse<String> response = client.send(request,
        HttpResponse.BodyHandlers.ofString());
    assertEquals(201, response.statusCode(), "Should create envelope successfully");

    return extractIdFrom(response);
  }

  private void addExpense(long envelopeId, int amount, String memo, String transactionType)
      throws Exception {
    String payload =
        "{\"amount\":" + amount + ",\"memo\":\"" + memo + "\",\"transactionType\":\"" +
            transactionType + "\"}";
    HttpRequest request = HttpRequest.newBuilder()
        .uri(new URI(baseUrl + "/" + envelopeId + "/expenses"))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(payload))
        .build();

    HttpResponse<String> response = client.send(request,
        HttpResponse.BodyHandlers.ofString());
    assertEquals(201, response.statusCode(), "Should add expense successfully");
  }

  private long extractIdFrom(HttpResponse<String> response) {

    String responseBody = response.body();
    int idIndex = responseBody.indexOf("\"id\":");
    int idEndIndex = responseBody.indexOf(",", idIndex);
    if (idEndIndex == -1) {
      idEndIndex = responseBody.indexOf("}", idIndex);
    }
    String idStr = responseBody.substring(idIndex + 5, idEndIndex).trim();
    return Long.parseLong(idStr);
  }
}
