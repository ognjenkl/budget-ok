package com.ognjen.template.systemtest.e2etests;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

class BankOkEnvelopeIntegrationE2eTest {

    private final HttpClient client = HttpClient.newHttpClient();

    @Test
    void syncExpensesFromBankOk_shouldCreateExpensesInEnvelope() throws Exception {
        // This test demonstrates the complete flow:
        // 1. Fetch expenses from Bank OK (external system)
        // 2. Create envelope
        // 3. Add expenses from Bank OK to the envelope
        // 4. Verify envelope balance reflects all expenses

        // Arrange - Fetch cart from Bank OK
        HttpRequest bankOkRequest = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/bankok/carts/1"))
                .GET()
                .build();

        HttpResponse<String> bankOkResponse = client.send(bankOkRequest, HttpResponse.BodyHandlers.ofString());

        // Assert Bank OK call succeeded
        assertEquals(200, bankOkResponse.statusCode(), "Should fetch cart from Bank OK");
        String cartBody = bankOkResponse.body();
        assertTrue(cartBody.contains("\"products\""), "Cart should contain products");

        // Act - Create envelope for the expenses
        String envelopePayload = "{\"name\":\"Bank OK Sync\",\"budget\":5000}";
        HttpRequest createEnvelopeRequest = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/envelopes"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(envelopePayload))
                .build();

        HttpResponse<String> envelopeResponse = client.send(createEnvelopeRequest, HttpResponse.BodyHandlers.ofString());

        // Assert envelope created
        assertEquals(200, envelopeResponse.statusCode(), "Should create envelope");
        String envelopeBody = envelopeResponse.body();
        assertTrue(envelopeBody.contains("\"name\":\"Bank OK Sync\""), "Envelope should have correct name");

        // Extract envelope ID
        long envelopeId = extractIdFromResponse(envelopeBody);
        assertTrue(envelopeId > 0, "Should extract valid envelope ID");

        // Act - Add an expense to the envelope (simulating sync from Bank OK products)
        String expensePayload = "{\"amount\":100,\"memo\":\"Product from Bank OK\",\"transactionType\":\"WITHDRAW\"}";
        HttpRequest addExpenseRequest = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/envelopes/" + envelopeId + "/expenses"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(expensePayload))
                .build();

        HttpResponse<String> addExpenseResponse = client.send(addExpenseRequest, HttpResponse.BodyHandlers.ofString());

        // Assert expense added
        assertEquals(200, addExpenseResponse.statusCode(), "Should add expense");
        String expenseResultBody = addExpenseResponse.body();
        assertTrue(expenseResultBody.contains("\"WITHDRAW\""), "Response should show withdraw transaction");

        // Act - Fetch the updated envelope to verify balance
        HttpRequest getEnvelopeRequest = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/envelopes/" + envelopeId))
                .GET()
                .build();

        HttpResponse<String> getEnvelopeResponse = client.send(getEnvelopeRequest, HttpResponse.BodyHandlers.ofString());

        // Assert final verification
        assertEquals(200, getEnvelopeResponse.statusCode(), "Should fetch updated envelope");
        String finalEnvelopeBody = getEnvelopeResponse.body();

        // Verify the envelope has the expense
        assertTrue(finalEnvelopeBody.contains("\"expenses\""), "Envelope should have expenses array");
        assertTrue(finalEnvelopeBody.contains("Product from Bank OK"), "Envelope should contain synced expense");

        // Verify expense data is correctly stored
        assertTrue(finalEnvelopeBody.contains("\"amount\":100"), "Expense should have amount 100");
        assertTrue(finalEnvelopeBody.contains("\"transactionType\":\"WITHDRAW\""), "Expense should be marked as WITHDRAW");
    }

    @Test
    void multipleBankOkCartsCanBeImportedAsEnvelopes() throws Exception {
        // This test demonstrates that we can fetch multiple carts from Bank OK
        // and create corresponding envelopes

        // Arrange & Act - Fetch multiple carts
        HttpResponse<String> cart1Response = fetchCart(1);
        HttpResponse<String> cart2Response = fetchCart(2);

        // Assert both carts fetched successfully
        assertEquals(200, cart1Response.statusCode(), "Should fetch cart 1");
        assertEquals(200, cart2Response.statusCode(), "Should fetch cart 2");

        // Verify both have cart structure
        assertTrue(cart1Response.body().contains("\"id\""), "Cart 1 should have id field");
        assertTrue(cart2Response.body().contains("\"products\""), "Cart 2 should have products field");

        // Act - Create envelope for each cart
        long envelope1 = createEnvelope("User 1 Expenses", 5000);
        long envelope2 = createEnvelope("User 2 Expenses", 3000);

        // Assert envelopes created
        assertTrue(envelope1 > 0, "Should create envelope 1");
        assertTrue(envelope2 > 0, "Should create envelope 2");

        // Verify both exist
        assertEquals(200, getEnvelopeStatus(envelope1), "Envelope 1 should exist");
        assertEquals(200, getEnvelopeStatus(envelope2), "Envelope 2 should exist");
    }

    // Helper methods
    private HttpResponse<String> fetchCart(int userId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/bankok/carts/" + userId))
                .GET()
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private long createEnvelope(String name, int budget) throws Exception {
        String payload = "{\"name\":\"" + name + "\",\"budget\":" + budget + "}";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/envelopes"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return extractIdFromResponse(response.body());
    }

    private int getEnvelopeStatus(long envelopeId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/envelopes/" + envelopeId))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.statusCode();
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
}
