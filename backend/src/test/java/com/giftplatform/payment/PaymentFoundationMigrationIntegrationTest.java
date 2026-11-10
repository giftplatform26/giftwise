package com.giftplatform.payment;

import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.containers.PostgreSQLContainer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class PaymentFoundationMigrationIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17");

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }


    @Autowired
    JdbcTemplate jdbc;

    @Test
    void paymentTablesExist() {
        for (String table : new String[] {
                "payment_orders",
                "payment_transactions",
                "contributions",
                "wallet_ledger_entries",
                "payment_webhook_events"
        }) {
            Integer count = jdbc.queryForObject(
                    """
                    SELECT COUNT(*)
                    FROM information_schema.tables
                    WHERE table_schema = 'public'
                      AND table_name = ?
                    """,
                    Integer.class,
                    table
            );

            assertEquals(1, count, "Missing table: " + table);
        }
    }

    @Test
    void paymentFoundationMigrationApplied() {
        Integer count = jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM flyway_schema_history
                WHERE version = '17'
                  AND success = true
                """,
                Integer.class
        );

        assertEquals(1, count);
    }

    @Test
    void paymentAmountsUseIntegerStorage() {
        String type = jdbc.queryForObject(
                """
                SELECT data_type
                FROM information_schema.columns
                WHERE table_name = 'payment_orders'
                  AND column_name = 'amount_paise'
                """,
                String.class
        );

        assertEquals("bigint", type);
    }

    @Test
    void webhookEventIdentityIsUnique() {
        Integer count = jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM pg_constraint
                WHERE conrelid = 'payment_webhook_events'::regclass
                  AND contype = 'u'
                """,
                Integer.class
        );

        assertTrue(count >= 1);
    }
}
