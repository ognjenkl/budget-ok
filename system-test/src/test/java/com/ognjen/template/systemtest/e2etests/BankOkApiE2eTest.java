package com.ognjen.template.systemtest.e2etests;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

class BankOkApiE2eTest {

    @Test
    void getBankOkCart_shouldReturnCartWithProducts() throws Exception {
        // This test verifies integration with external Bank OK system (DummyJSON)

        // Arrange
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/bankok/carts/1"))
                .GET()
                .build();

        // Act
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Assert
        assertEquals(200, response.statusCode(), "Should return 200 OK when fetching cart");

        String responseBody = response.body();

        // Verify JSON structure contains expected fields
        assertTrue(responseBody.contains("\"id\""), "Response should contain id field");
        assertTrue(responseBody.contains("\"userId\""), "Response should contain userId field");
        assertTrue(responseBody.contains("\"products\""), "Response should contain products array");
        assertTrue(responseBody.contains("\"total\""), "Response should contain total field");

        // Verify the response has a cart structure
        assertTrue(responseBody.contains("{"), "Response should be valid JSON object");
        assertTrue(responseBody.contains("}"), "Response should contain closing brace");
    }

    @Test
    void getBankOkCartById_shouldReturnSpecificCart() throws Exception {
        // This test verifies we can fetch a specific cart by ID from Bank OK

        // Arrange
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/bankok/carts/id/2"))
                .GET()
                .build();

        // Act
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Assert
        assertEquals(200, response.statusCode(), "Should return 200 OK when fetching cart by ID");

        String responseBody = response.body();

        // Verify the response contains cart data
        assertTrue(responseBody.contains("\"id\":2") || responseBody.contains("\"id\": 2"),
                   "Response should contain id with value 2");
        assertTrue(responseBody.contains("\"products\""), "Response should contain products array");
    }
}
