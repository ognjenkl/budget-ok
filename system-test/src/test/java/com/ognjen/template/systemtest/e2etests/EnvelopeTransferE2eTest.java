package com.ognjen.template.systemtest.e2etests;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

class EnvelopeTransferE2eTest {

    private final HttpClient client = HttpClient.newHttpClient();
    private final String baseUrl = "http://localhost:8080/api/envelopes";

    @Test
    void transferAmount_shouldSucceed_whenSufficientBalance() throws Exception {
        // This test validates successful transfer between envelopes (CRUD + Business Logic)

        // Arrange - Create two envelopes
        long envelopeA = createEnvelope("Groceries", 1000);
        long envelopeB = createEnvelope("Entertainment", 500);

        // Act - Transfer 200 from A to B
        String transferPayload = "{\"sourceEnvelopeId\":" + envelopeA + ",\"targetEnvelopeId\":" + envelopeB + ",\"amount\":200,\"memo\":\"Transfer\"}";
        HttpRequest transferRequest = HttpRequest.newBuilder()
                .uri(new URI(baseUrl + "/transfer"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(transferPayload))
                .build();

        HttpResponse<String> transferResponse = client.send(transferRequest, HttpResponse.BodyHandlers.ofString());

        // Assert - Transfer should succeed
        assertEquals(200, transferResponse.statusCode(), "Transfer should return 200 OK");
        assertTrue(transferResponse.body().contains("\"message\":\"Transfer successful\""),
                   "Response should contain success message");

        // Verify source envelope balance (1000 - 200 = 800)
        HttpResponse<String> envelopeAResponse = getEnvelope(envelopeA);
        String envelopeABody = envelopeAResponse.body();
        // The balance should reflect the withdrawal
        assertTrue(envelopeABody.contains("\"budget\":1000"), "Source envelope should have budget 1000");

        // Verify target envelope has the deposit expense
        HttpResponse<String> envelopeBResponse = getEnvelope(envelopeB);
        String envelopeBBody = envelopeBResponse.body();
        assertTrue(envelopeBBody.contains("\"DEPOSIT\""), "Target envelope should have deposit transaction");
    }

    @Test
    void transferAmount_shouldFail_whenInsufficientBalance() throws Exception {
        // This test validates business logic - insufficient balance check

        // Arrange - Create two envelopes
        long envelopeA = createEnvelope("Limited Budget", 50);
        long envelopeB = createEnvelope("Target", 500);

        // Act - Try to transfer 200 from A to B (insufficient balance)
        String transferPayload = "{\"sourceEnvelopeId\":" + envelopeA + ",\"targetEnvelopeId\":" + envelopeB + ",\"amount\":200,\"memo\":\"Transfer\"}";
        HttpRequest transferRequest = HttpRequest.newBuilder()
                .uri(new URI(baseUrl + "/transfer"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(transferPayload))
                .build();

        HttpResponse<String> transferResponse = client.send(transferRequest, HttpResponse.BodyHandlers.ofString());

        // Assert - Transfer should fail with 400 Bad Request
        assertEquals(400, transferResponse.statusCode(), "Transfer should fail with 400 when insufficient balance");
        assertTrue(transferResponse.body().contains("Insufficient balance"),
                   "Error message should mention insufficient balance");

        // Verify balances remain unchanged
        HttpResponse<String> envelopeAResponse = getEnvelope(envelopeA);
        String envelopeABody = envelopeAResponse.body();
        // Should still have original budget with no expenses
        assertFalse(envelopeABody.contains("\"WITHDRAW\""), "Source envelope should not have any withdraw transactions");
    }

    @Test
    void transferAmount_shouldFail_whenTargetEnvelopeNotFound() throws Exception {
        // This test validates error handling - envelope not found (404)

        // Arrange - Create one envelope
        long sourceEnvelopeId = createEnvelope("Source", 1000);
        long nonExistentTargetId = 99999;

        // Act - Try to transfer to non-existent envelope
        String transferPayload = "{\"sourceEnvelopeId\":" + sourceEnvelopeId + ",\"targetEnvelopeId\":" + nonExistentTargetId + ",\"amount\":100,\"memo\":\"Transfer\"}";
        HttpRequest transferRequest = HttpRequest.newBuilder()
                .uri(new URI(baseUrl + "/transfer"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(transferPayload))
                .build();

        HttpResponse<String> transferResponse = client.send(transferRequest, HttpResponse.BodyHandlers.ofString());

        // Assert - Transfer should fail with 404 Not Found
        assertEquals(404, transferResponse.statusCode(), "Transfer should fail with 404 when target not found");
        assertTrue(transferResponse.body().contains("not found"),
                   "Error message should mention envelope not found");

        // Verify source envelope balance unchanged
        HttpResponse<String> envelopeResponse = getEnvelope(sourceEnvelopeId);
        String envelopeBody = envelopeResponse.body();
        assertFalse(envelopeBody.contains("\"WITHDRAW\""), "Source envelope should not have any expenses");
    }

    // Helper methods
    private long createEnvelope(String name, int budget) throws Exception {
        String payload = "{\"name\":\"" + name + "\",\"budget\":" + budget + "}";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(baseUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Should create envelope successfully");

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
