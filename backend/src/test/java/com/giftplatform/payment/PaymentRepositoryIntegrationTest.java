package com.giftplatform.payment;

import com.giftplatform.payment.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
    "gift.otp.secret-base64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
    "spring.jpa.hibernate.ddl-auto=validate"
})
@Testcontainers
class PaymentRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17");

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired PaymentOrderRepository orders;
    @Autowired PaymentTransactionRepository transactions;
    @Autowired ContributionRepository contributions;
    @Autowired WalletLedgerEntryRepository ledger;
    @Autowired PaymentWebhookEventRepository webhooks;
    @Autowired DataSource dataSource;

    @Test
    void allPaymentRepositoriesAreAvailable() {
        assertNotNull(orders);
        assertNotNull(transactions);
        assertNotNull(contributions);
        assertNotNull(ledger);
        assertNotNull(webhooks);
    }

    @Test
    void repositoriesCanQueryEmptyPaymentTables() {
        assertEquals(0, orders.count());
        assertEquals(0, transactions.count());
        assertEquals(0, contributions.count());
        assertEquals(0, ledger.count());
        assertEquals(0, webhooks.count());
    }

    @Test
    void allPaymentTablesExist() throws Exception {
        String[] tables = {
            "payment_orders",
            "payment_transactions",
            "contributions",
            "wallet_ledger_entries",
            "payment_webhook_events"
        };

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            for (String table : tables) {
                try (ResultSet rs = statement.executeQuery(
                        "SELECT COUNT(*) FROM " + table)) {
                    assertTrue(rs.next(), table + " is unavailable");
                }
            }
        }
    }
}
