package com.ognjen.budgetok.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
@EnableJdbcRepositories(basePackages = {
    "com.ognjen.budgetok.application",
    "com.ognjen.budgetok.infrastructure.persistence"
})
@ComponentScan(basePackages = {
    "com.ognjen.budgetok.application",
    "com.ognjen.budgetok.infrastructure.persistence"
})
public class TestConfig {

    @Bean
    @ServiceConnection
    public PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"))
                .withDatabaseName("testdb")
                .withUsername("test")
                .withPassword("test");
    }
}
