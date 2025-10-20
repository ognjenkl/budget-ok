package com.ognjen.budgetok;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class BudgetOkApplicationTests {

    @Test
    void contextLoads() {
        // This test will verify that the Spring application context loads successfully
    }
}
