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
import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties =
        "gift.otp.secret-base64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
@Testcontainers
class PaymentFoundationIntegrityTest {

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
            """, event, "Payment Test", "BIRTHDAY", "PUBLISHED", user);

        return event;
    }

    UUID gift(UUID eventId) {
        UUID id = UUID.randomUUID();
        jdbc.update("""
            INSERT INTO gifts(id,event_id,name,target_amount)
            VALUES (?,?,?,?)
            """, id, eventId, "Test Gift", new BigDecimal("10000.00"));
        return id;
    }

    void order(UUID eventId, UUID giftId, long amount, String type,
               String idempotencyKey, String providerOrderId) {
        jdbc.update("""
            INSERT INTO payment_orders
            (id,event_id,gift_id,amount_paise,contribution_type,
             idempotency_key,provider_order_id)
            VALUES (?,?,?,?,?,?,?)
            """,
            UUID.randomUUID(), eventId, giftId, amount,
            type, idempotencyKey, providerOrderId);
    }

    void assertSqlState(String expected, Runnable operation) {
        DataIntegrityViolationException error =
                assertThrows(DataIntegrityViolationException.class, operation::run);

        Throwable cause = error;
        while (cause != null && !(cause instanceof SQLException)) {
            cause = cause.getCause();
        }

        assertNotNull(cause, "Expected a PostgreSQL SQLException");
        assertEquals(expected, ((SQLException) cause).getSQLState());
    }

    @Test
    void acceptsValidWalletOrder() {
        UUID eventId = event();
        order(eventId, null, 200000, "EVENT_WALLET",
                UUID.randomUUID().toString(), null);

        assertEquals(1, jdbc.queryForObject("""
            SELECT COUNT(*) FROM payment_orders WHERE event_id=?
            """, Integer.class, eventId));
    }

    @Test
    void rejectsZeroAmount() {
        UUID eventId = event();
        assertSqlState("23514", () ->
                order(eventId, null, 0, "EVENT_WALLET",
                        UUID.randomUUID().toString(), null));
    }

    @Test
    void rejectsNegativeAmount() {
        UUID eventId = event();
        assertSqlState("23514", () ->
                order(eventId, null, -100, "EVENT_WALLET",
                        UUID.randomUUID().toString(), null));
    }

    @Test
    void rejectsInvalidContributionType() {
        UUID eventId = event();
        assertSqlState("23514", () ->
                order(eventId, null, 1000, "INVALID_TYPE",
                        UUID.randomUUID().toString(), null));
    }

    @Test
    void rejectsGiftPaymentWithoutGift() {
        UUID eventId = event();
        assertSqlState("23514", () ->
                order(eventId, null, 1000, "GIFT_PARTIAL",
                        UUID.randomUUID().toString(), null));
    }

    @Test
    void rejectsWalletPaymentWithGift() {
        UUID eventId = event();
        UUID giftId = gift(eventId);

        assertSqlState("23514", () ->
                order(eventId, giftId, 1000, "EVENT_WALLET",
                        UUID.randomUUID().toString(), null));
    }

    @Test
    void rejectsDuplicateIdempotencyKey() {
        UUID eventId = event();
        String key = UUID.randomUUID().toString();

        order(eventId, null, 1000, "EVENT_WALLET", key, null);

        assertSqlState("23505", () ->
                order(eventId, null, 2000, "EVENT_WALLET", key, null));
    }

    @Test
    void rejectsDuplicateProviderOrderId() {
        UUID eventId = event();
        String providerId = "order_" + UUID.randomUUID();

        order(eventId, null, 1000, "EVENT_WALLET",
                UUID.randomUUID().toString(), providerId);

        assertSqlState("23505", () ->
                order(eventId, null, 2000, "EVENT_WALLET",
                        UUID.randomUUID().toString(), providerId));
    }

    @Test
    void rejectsDuplicateWebhookEvent() {
        String providerId = "evt_" + UUID.randomUUID();

        jdbc.update("""
            INSERT INTO payment_webhook_events
            (id,provider,provider_event_id,event_type)
            VALUES (?,'RAZORPAY',?,'payment.captured')
            """, UUID.randomUUID(), providerId);

        assertSqlState("23505", () -> jdbc.update("""
            INSERT INTO payment_webhook_events
            (id,provider,provider_event_id,event_type)
            VALUES (?,'RAZORPAY',?,'payment.captured')
            """, UUID.randomUUID(), providerId));
    }

    @Test
    void rejectsZeroLedgerEntry() {
        UUID eventId = event();

        assertSqlState("23514", () -> jdbc.update("""
            INSERT INTO wallet_ledger_entries
            (id,event_id,entry_group_id,account_code,amount_paise,entry_type)
            VALUES (?, ?, ?, 'PAYMENT_CLEARING', 0, 'TEST')
            """, UUID.randomUUID(), eventId, UUID.randomUUID()));
    }


    @Test
    void rejectsPaymentForGiftBelongingToAnotherEvent() {
        UUID eventA = event();
        UUID eventB = event();
        UUID giftB = gift(eventB);

        assertSqlState("23503", () ->
                order(eventA, giftB, 1000, "GIFT_PARTIAL",
                        UUID.randomUUID().toString(), null));
    }

    @Test
    void acceptsPaymentForGiftInSameEvent() {
        UUID eventId = event();
        UUID giftId = gift(eventId);

        order(eventId, giftId, 1000, "GIFT_PARTIAL",
                UUID.randomUUID().toString(), null);

        assertEquals(1, jdbc.queryForObject("""
            SELECT COUNT(*)
            FROM payment_orders
            WHERE event_id = ? AND gift_id = ?
            """, Integer.class, eventId, giftId));
    }

    @Test
    void rejectsContributionForGiftBelongingToAnotherEvent() {
        UUID eventA = event();
        UUID eventB = event();
        UUID giftB = gift(eventB);

        UUID orderId = UUID.randomUUID();

        jdbc.update("""
            INSERT INTO payment_orders
            (id,event_id,contribution_type,amount_paise,idempotency_key)
            VALUES (?,?,'EVENT_WALLET',1000,?)
            """, orderId, eventA, UUID.randomUUID().toString());

        assertSqlState("23503", () -> jdbc.update("""
            INSERT INTO contributions
            (id,payment_order_id,event_id,gift_id,guest_name,amount_paise)
            VALUES (?,?,?,?,?,?)
            """,
            UUID.randomUUID(), orderId, eventA, giftB,
            "Test Guest", 1000));
    }

    @Test
    void rejectsMissingEventReference() {
        assertSqlState("23503", () ->
                order(UUID.randomUUID(), null, 1000, "EVENT_WALLET",
                        UUID.randomUUID().toString(), null));
    }
}
