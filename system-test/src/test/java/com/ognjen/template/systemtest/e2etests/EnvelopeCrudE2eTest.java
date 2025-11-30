package com.ognjen.template.systemtest.e2etests;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnvelopeCrudE2eTest {

  private final HttpClient client = HttpClient.newHttpClient();
  private final String baseUrl = "http://localhost:8080/api/envelopes";

  @Test
  void givenNoEnvelope_whenCreateEnvelope_thenEnvelopeIsCreated() throws Exception {

    String payload = "{\"name\":\"Groceries\",\"budget\":1000}";

    HttpRequest request = HttpRequest.newBuilder()
        .uri(new URI(baseUrl))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(payload))
        .build();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    assertEquals(201, response.statusCode(), "Should return 201 OK");
    assertTrue(response.body().contains("\"name\":\"Groceries\""),
        "Response should contain envelope name");
    assertTrue(response.body().contains("\"budget\":1000"), "Response should contain budget");
    assertTrue(response.body().contains("\"id\":"), "Response should contain envelope ID");
  }

  @Test
  void givenMultipleEnvelopesExist_whenGetAllEnvelopes_thenReturnAllEnvelopes() throws Exception {

    createEnvelope("Groceries", 1000);
    createEnvelope("Entertainment", 500);
    createEnvelope("Utilities", 300);
    HttpRequest request = HttpRequest.newBuilder()
        .uri(new URI(baseUrl))
        .GET()
        .build();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    assertEquals(200, response.statusCode(), "Should return 200 OK");
    String body = response.body();
    assertTrue(body.contains("\"Groceries\""), "Response should contain Groceries envelope");
    assertTrue(body.contains("\"Entertainment\""),
        "Response should contain Entertainment envelope");
    assertTrue(body.contains("\"Utilities\""), "Response should contain Utilities envelope");
  }

  @Test
  void givenExistingEnvelope_whenGetEnvelopeById_thenReturnEnvelope() throws Exception {

    long envelopeId = createEnvelope("Shopping", 750);
    HttpRequest request = HttpRequest.newBuilder()
        .uri(new URI(baseUrl + "/" + envelopeId))
        .GET()
        .build();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    assertEquals(200, response.statusCode(), "Should return 200 OK");
    String body = response.body();
    assertTrue(body.contains("\"name\":\"Shopping\""), "Response should contain envelope name");
    assertTrue(body.contains("\"budget\":750"), "Response should contain budget");
    assertTrue(body.contains("\"id\":" + envelopeId),
        "Response should contain correct envelope ID");
  }

  @Test
  void givenExistingEnvelope_whenUpdateEnvelope_thenEnvelopeIsUpdated() throws Exception {

    long envelopeId = createEnvelope("Old Name", 1000);
    String payload = "{\"name\":\"Updated Name\",\"budget\":2000}";
    HttpRequest request = HttpRequest.newBuilder()
        .uri(new URI(baseUrl + "/" + envelopeId))
        .header("Content-Type", "application/json")
        .method("PUT", HttpRequest.BodyPublishers.ofString(payload))
        .build();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    assertEquals(200, response.statusCode(), "Should return 200 OK");
    String body = response.body();
    assertTrue(body.contains("\"name\":\"Updated Name\""), "Response should contain updated name");
    assertTrue(body.contains("\"budget\":2000"), "Response should contain updated budget");
  }

  @Test
  void givenExistingEnvelope_whenDeleteEnvelope_thenEnvelopeIsDeleted() throws Exception {

    long envelopeId = createEnvelope("To Delete", 500);
    HttpRequest deleteRequest = HttpRequest.newBuilder()
        .uri(new URI(baseUrl + "/" + envelopeId))
        .DELETE()
        .build();

    HttpResponse<String> response = client.send(deleteRequest,
        HttpResponse.BodyHandlers.ofString());

    assertEquals(204, response.statusCode(), "Should return 204 No Content");
  }

  @Test
  void givenEmptyName_whenCreateEnvelope_thenReturnBadRequest() throws Exception {

    String payload = "{\"name\":\"\",\"budget\":1000}";

    HttpRequest request = HttpRequest.newBuilder()
        .uri(new URI(baseUrl))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(payload))
        .build();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    assertEquals(400, response.statusCode(), "Should return 400 Bad Request for empty name");
  }

  @ParameterizedTest(name = "{1}")
  @MethodSource("provideInvalidEnvelopePayloads")
  void givenInvalidInput_whenCreateEnvelope_thenReturnBadRequest(String payload, String description)
      throws Exception {

    HttpRequest request = HttpRequest.newBuilder()
        .uri(new URI(baseUrl))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(payload))
        .build();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    assertEquals(400, response.statusCode(), "Should return 400 Bad Request for: " + description);
  }

  private static Stream<Arguments> provideInvalidEnvelopePayloads() {
    return Stream.of(
        Arguments.of("{\"name\":\"\",\"budget\":1000}",
            "empty name"
        ),
        Arguments.of("{\"budget\":1000}",
            "missing name"
        )
    );
  }

  @Test
  void givenNegativeBudget_whenCreateEnvelope_thenReturnBadRequest() throws Exception {

    String payload = "{\"name\":\"Invalid\",\"budget\":-100}";

    HttpRequest request = HttpRequest.newBuilder()
        .uri(new URI(baseUrl))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(payload))
        .build();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    assertEquals(400, response.statusCode(), "Should return 400 Bad Request for negative budget");
  }

  @Test
  void givenNonIntegerBudget_whenCreateEnvelope_thenReturnBadRequest() throws Exception {

    String payload = "{\"name\":\"Invalid\",\"budget\":\"not-a-number\"}";

    HttpRequest request = HttpRequest.newBuilder()
        .uri(new URI(baseUrl))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(payload))
        .build();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    assertEquals(400, response.statusCode(), "Should return 400 Bad Request for non-integer budget type");
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

    return extractIdFrom(response);
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
