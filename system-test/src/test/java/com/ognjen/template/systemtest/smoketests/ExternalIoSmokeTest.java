package com.ognjen.template.systemtest.smoketests;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

class ExternalIoSmokeTest {

    private static final String BANKOK_BASE_URL = "http://localhost:8081/api/expenses";
    private final HttpClient client = HttpClient.newHttpClient();

    @Test
    void givenBankOkService_whenRequestingHealthCheck_thenShouldReturn200OK() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(BANKOK_BASE_URL))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Bank OK service should return 200 OK");
    }

    @Test
    void givenBankOkService_whenRequestingHealthCheck_thenShouldReturnJsonContent() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(BANKOK_BASE_URL))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Should return 200 OK");
        String contentType = response.headers().firstValue("content-type").orElse("");
        assertTrue(contentType.contains("application/json"),
                  "Content-Type should be application/json, but was: " + contentType);
    }
}
