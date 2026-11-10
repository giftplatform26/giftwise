package com.giftplatform.payment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
    "gift.otp.secret-base64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
    "spring.jpa.hibernate.ddl-auto=validate"
})
@Testcontainers
class ContributionAllocationIntegrityIntegrationTest {

    @Container
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17");

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired JdbcTemplate jdbc;

    UUID event() {
        UUID user = UUID.randomUUID();
        UUID event = UUID.randomUUID();

        jdbc.update("""
            INSERT INTO users(id,email,password_hash,role)
            VALUES (?,?,?,?)
            """, user, user + "@example.test", "test-hash", "CUSTOMER");

        jdbc.update("""
            INSERT INTO events(id,title,occasion_type,status,owner_id,host_display_name)
            VALUES (?,?,?,?,?,'Test Host')
            """, event, "Allocation Test", "BIRTHDAY", "PUBLISHED", user);

        return event;
    }

    UUID gift(UUID event) {
        UUID gift = UUID.randomUUID();

        jdbc.update("""
            INSERT INTO gifts(id,event_id,name,target_amount)
            VALUES (?,?,?,?)
            """, gift, event, "Test Gift", new BigDecimal("10000.00"));

        return gift;
    }

    UUID contribution(UUID event, UUID gift, long amount) {
        UUID order = UUID.randomUUID();
        UUID contribution = UUID.randomUUID();

        jdbc.update("""
            INSERT INTO payment_orders(
                id,event_id,gift_id,contribution_type,
                amount_paise,idempotency_key,status
            ) VALUES (?,?,?,?,?,?,?)
            """, order, event, gift,
            gift == null ? "EVENT_WALLET" : "GIFT_PARTIAL",
            amount, UUID.randomUUID().toString(), "CAPTURED");

        jdbc.update("""
            INSERT INTO contributions(
                id,payment_order_id,event_id,gift_id,
                guest_name,amount_paise
            ) VALUES (?,?,?,?,?,?)
            """, contribution, order, event, gift, "Test Guest", amount);

        return contribution;
    }

    void allocate(UUID contribution, UUID event, UUID gift,
                  String type, long amount) {
        jdbc.update("""
            INSERT INTO contribution_allocations(
                id,contribution_id,event_id,gift_id,
                allocation_type,amount_paise
            ) VALUES (?,?,?,?,?,?)
            """, UUID.randomUUID(), contribution, event, gift, type, amount);
    }

    void rejected(Runnable action) {
        org.springframework.dao.DataAccessException error =
                assertThrows(
                    org.springframework.dao.DataAccessException.class,
                    action::run
                );

        Throwable cause = error;
        while (cause != null &&
               !(cause instanceof java.sql.SQLException)) {
            cause = cause.getCause();
        }

        assertNotNull(cause, "Expected a PostgreSQL SQL exception");

        String sqlState =
                ((java.sql.SQLException) cause).getSQLState();

        assertTrue(
                java.util.Set.of(
                    "23502", // NOT NULL
                    "23503", // Foreign key
                    "23505", // Unique constraint
                    "23514", // Check constraint
                    "P0001"  // Explicit PL/pgSQL exception
                ).contains(sqlState),
                "Unexpected PostgreSQL SQLSTATE: " + sqlState
        );
    }

    @Test
    void acceptsValidGiftAndWalletSplit() {
        UUID event = event();
        UUID gift = gift(event);
        UUID contribution = contribution(event, gift, 400000);

        allocate(contribution, event, gift, "GIFT", 300000);
        allocate(contribution, event, null, "EVENT_WALLET", 100000);

        Long total = jdbc.queryForObject("""
            SELECT SUM(amount_paise)
            FROM contribution_allocations
            WHERE contribution_id = ?
            """, Long.class, contribution);

        assertEquals(400000L, total);
    }

    @Test
    void acceptsWalletOnlyAllocation() {
        UUID event = event();
        UUID contribution = contribution(event, null, 200000);

        allocate(contribution, event, null, "EVENT_WALLET", 200000);

        assertEquals(1, jdbc.queryForObject("""
            SELECT COUNT(*) FROM contribution_allocations
            WHERE contribution_id = ?
            """, Integer.class, contribution));
    }

    @Test
    void rejectsGiftAllocationWithoutGift() {
        UUID event = event();
        UUID contribution = contribution(event, null, 100000);

        rejected(() -> allocate(
            contribution, event, null, "GIFT", 100000));
    }

    @Test
    void rejectsWalletAllocationWithGift() {
        UUID event = event();
        UUID gift = gift(event);
        UUID contribution = contribution(event, gift, 100000);

        rejected(() -> allocate(
            contribution, event, gift, "EVENT_WALLET", 100000));
    }

    @Test
    void rejectsZeroAllocation() {
        UUID event = event();
        UUID contribution = contribution(event, null, 100000);

        rejected(() -> allocate(
            contribution, event, null, "EVENT_WALLET", 0));
    }

    @Test
    void rejectsNegativeAllocation() {
        UUID event = event();
        UUID contribution = contribution(event, null, 100000);

        rejected(() -> allocate(
            contribution, event, null, "EVENT_WALLET", -100));
    }

    @Test
    void rejectsDuplicateGiftAllocation() {
        UUID event = event();
        UUID gift = gift(event);
        UUID contribution = contribution(event, gift, 200000);

        allocate(contribution, event, gift, "GIFT", 100000);

        rejected(() -> allocate(
            contribution, event, gift, "GIFT", 100000));
    }

    @Test
    void rejectsDuplicateWalletAllocation() {
        UUID event = event();
        UUID contribution = contribution(event, null, 200000);

        allocate(contribution, event, null, "EVENT_WALLET", 100000);

        rejected(() -> allocate(
            contribution, event, null, "EVENT_WALLET", 100000));
    }

    @Test
    void rejectsCrossEventAllocation() {
        UUID eventA = event();
        UUID eventB = event();
        UUID contribution = contribution(eventA, null, 100000);

        rejected(() -> allocate(
            contribution, eventB, null, "EVENT_WALLET", 100000));
    }

    @Test
    void rejectsAllocationToDifferentGift() {
        UUID event = event();
        UUID originalGift = gift(event);
        UUID otherGift = gift(event);
        UUID contribution = contribution(event, originalGift, 100000);

        rejected(() -> allocate(
            contribution, event, otherGift, "GIFT", 100000));
    }

    @Test
    void rejectsGiftFromDifferentEvent() {
        UUID eventA = event();
        UUID eventB = event();
        UUID giftB = gift(eventB);
        UUID contribution = contribution(eventA, null, 100000);

        rejected(() -> allocate(
            contribution, eventA, giftB, "GIFT", 100000));
    }

    @Test
    void rejectsUnknownAllocationType() {
        UUID event = event();
        UUID contribution = contribution(event, null, 100000);

        rejected(() -> allocate(
            contribution, event, null, "OTHER", 100000));
    }
}
