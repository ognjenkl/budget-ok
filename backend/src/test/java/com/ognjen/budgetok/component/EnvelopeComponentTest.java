package com.ognjen.budgetok.component;

import com.ognjen.budgetok.TestcontainersConfiguration;
import com.ognjen.budgetok.application.Envelope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@Testcontainers(disabledWithoutDocker = true)
class EnvelopeComponentTest {
    
    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        var envelopes = restTemplate.getForEntity("/api/envelopes", Envelope[].class).getBody();
      for (var envelope : envelopes) {
        restTemplate.delete("/api/envelopes/" + envelope.getId());
      }
    }

    @Test
    void shouldCreateEnvelope() {
        var envelope = new Envelope();
        envelope.setName("Groceries");
        envelope.setBudget(500);

        var response = restTemplate.postForEntity("/api/envelopes", envelope, Envelope.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        var createdEnvelope = response.getBody();
        assertThat(createdEnvelope).isNotNull();
        assertThat(createdEnvelope.getId()).isNotNull();
        assertThat(createdEnvelope.getName()).isEqualTo("Groceries");
        assertThat(createdEnvelope.getBudget()).isEqualTo(500);

        var getResponse = restTemplate.getForEntity("/api/envelopes/" + createdEnvelope.getId(), Envelope.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isEqualTo(createdEnvelope);
    }

    @Test
    void shouldRetrieveAllEnvelopes() {
        var envelope1 = new Envelope();
        envelope1.setName("Envelope 1");
        envelope1.setBudget(100);
        
        var envelope2 = new Envelope();
        envelope2.setName("Envelope 2");
        envelope2.setBudget(200);
        
        restTemplate.postForEntity("/api/envelopes", envelope1, Envelope.class);
        restTemplate.postForEntity("/api/envelopes", envelope2, Envelope.class);

        var response = restTemplate.getForEntity("/api/envelopes", Envelope[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        var envelopes = response.getBody();
        assertThat(envelopes).hasSize(2);
        assertThat(envelopes).extracting("name")
                .containsExactlyInAnyOrder("Envelope 1", "Envelope 2");
    }

    @Test
    void shouldUpdateEnvelope() {
        var envelope = new Envelope();
        envelope.setName("Groceries");
        envelope.setBudget(500);

        var createdEnvelope = restTemplate.postForEntity("/api/envelopes", envelope, Envelope.class).getBody();
        assertThat(createdEnvelope).isNotNull();

        createdEnvelope.setName("Updated Groceries");
        createdEnvelope.setBudget(600);

        restTemplate.put("/api/envelopes/" + createdEnvelope.getId(), createdEnvelope);

        var updatedEnvelope = restTemplate.getForEntity("/api/envelopes/" + createdEnvelope.getId(), Envelope.class).getBody();
        assertThat(updatedEnvelope).isNotNull();
        assertThat(updatedEnvelope.getName()).isEqualTo("Updated Groceries");
        assertThat(updatedEnvelope.getBudget()).isEqualTo(600);
    }

    @Test
    void shouldDeleteEnvelope() {
        var envelope = new Envelope();
        envelope.setName("Groceries");
        envelope.setBudget(500);

        var createdEnvelope = restTemplate.postForEntity("/api/envelopes", envelope, Envelope.class).getBody();
        assertThat(createdEnvelope).isNotNull();

        restTemplate.delete("/api/envelopes/" + createdEnvelope.getId());

        var response = restTemplate.getForEntity("/api/envelopes/" + createdEnvelope.getId(), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldRenameEnvelope() {
        var envelope = new Envelope();
        envelope.setName("Old Name");
        envelope.setBudget(300);

        var createdEnvelope = restTemplate.postForEntity("/api/envelopes", envelope, Envelope.class).getBody();
        assertThat(createdEnvelope).isNotNull();

        createdEnvelope.setName("New Name");
        restTemplate.exchange("/api/envelopes/" + createdEnvelope.getId(),
                HttpMethod.PATCH,
                new org.springframework.http.HttpEntity<>(createdEnvelope),
                Envelope.class);
    }
}
