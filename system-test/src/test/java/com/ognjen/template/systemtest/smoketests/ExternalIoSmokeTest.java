package com.ognjen.template.systemtest.smoketests;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

class ExternalIoSmokeTest {

    private static final String DUMMYJSON_BASE_URL = "https://dummyjson.com";
    private final HttpClient client = HttpClient.newHttpClient();

    @Test
    void givenDummyJsonApi_whenRequestingCart_thenShouldReturn200OK() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(DUMMYJSON_BASE_URL + "/carts/1"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "DummyJSON API should return 200 OK");
    }

    @Test
    void givenDummyJsonApi_whenRequestingCart_thenShouldReturnJsonContent() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(DUMMYJSON_BASE_URL + "/carts/1"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Should return 200 OK");
        String contentType = response.headers().firstValue("content-type").orElse("");
        assertTrue(contentType.contains("application/json"),
                  "Content-Type should be application/json, but was: " + contentType);
    }
}
