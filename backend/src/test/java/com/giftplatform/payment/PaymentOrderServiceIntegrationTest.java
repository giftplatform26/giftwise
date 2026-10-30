package com.giftplatform.payment;

import com.giftplatform.payment.entity.PaymentOrder;
import com.giftplatform.payment.repository.PaymentOrderRepository;
import com.giftplatform.payment.service.PaymentOrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.server.ResponseStatusException;
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
class PaymentOrderServiceIntegrationTest {

    @Container
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17");

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired PaymentOrderService service;
    @Autowired PaymentOrderRepository orders;
    @Autowired JdbcTemplate jdbc;

    private UUID event(String status) {
        UUID userId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        jdbc.update(
            "INSERT INTO users(id,email,password_hash,role) VALUES (?,?,?,?)",
            userId, userId + "@example.test", "test-hash", "CUSTOMER"
        );

        jdbc.update(
            """
            INSERT INTO events(
                id,title,occasion_type,status,owner_id
            ) VALUES (?,?,?,?,?)
            """,
            eventId, "Payment Test Event",
            "BIRTHDAY", status, userId
        );

        return eventId;
    }

    private UUID gift(UUID eventId, String status, String visibility) {
        UUID giftId = UUID.randomUUID();

        jdbc.update(
            """
            INSERT INTO gifts(
                id,event_id,name,target_amount,status,visibility
            ) VALUES (?,?,?,?,?,?)
            """,
            giftId, eventId, "Test Gift",
            new BigDecimal("10000.00"), status, visibility
        );

        return giftId;
    }

    private String key() {
        return UUID.randomUUID().toString();
    }

    private void expectStatus(
            HttpStatus status,
            org.junit.jupiter.api.function.Executable operation) {
        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class, operation);
        assertEquals(status.value(), ex.getStatusCode().value());
    }

    @Test
    void publishedEventAcceptsWalletContribution() {
        UUID eventId = event("PUBLISHED");

        PaymentOrder order = service.create(
                eventId, null, "EVENT_WALLET", 200000, key());

        assertNotNull(order.getId());
        assertEquals("CREATED", order.getStatus());
        assertEquals("EVENT_WALLET", order.getContributionType());
        assertTrue(orders.findById(order.getId()).isPresent());
    }

    @Test
    void draftEventRejectsContribution() {
        UUID eventId = event("DRAFT");

        expectStatus(HttpStatus.CONFLICT, () ->
                service.create(eventId, null,
                        "EVENT_WALLET", 10000, key()));
    }

    @Test
    void completedEventRejectsContribution() {
        UUID eventId = event("COMPLETED");

        expectStatus(HttpStatus.CONFLICT, () ->
                service.create(eventId, null,
                        "EVENT_WALLET", 10000, key()));
    }

    @Test
    void publicActiveGiftAcceptsContribution() {
        UUID eventId = event("PUBLISHED");
        UUID giftId = gift(eventId, "ACTIVE", "PUBLIC");

        PaymentOrder order = service.create(
                eventId, giftId, "GIFT_PARTIAL", 50000, key());

        assertEquals(giftId, order.getGiftId());
        assertEquals(50000L, order.getAmountPaise());
    }

    @Test
    void draftGiftRejectsContribution() {
        UUID eventId = event("PUBLISHED");
        UUID giftId = gift(eventId, "ACTIVE", "DRAFT");

        expectStatus(HttpStatus.CONFLICT, () ->
                service.create(eventId, giftId,
                        "GIFT_PARTIAL", 10000, key()));
    }

    @Test
    void cancelledGiftRejectsContribution() {
        UUID eventId = event("PUBLISHED");
        UUID giftId = gift(eventId, "CANCELLED", "PUBLIC");

        expectStatus(HttpStatus.CONFLICT, () ->
                service.create(eventId, giftId,
                        "GIFT_PARTIAL", 10000, key()));
    }

    @Test
    void crossEventGiftIsRejected() {
        UUID eventA = event("PUBLISHED");
        UUID eventB = event("PUBLISHED");
        UUID giftB = gift(eventB, "ACTIVE", "PUBLIC");

        expectStatus(HttpStatus.NOT_FOUND, () ->
                service.create(eventA, giftB,
                        "GIFT_PARTIAL", 10000, key()));
    }

    @Test
    void amountAboveGiftTargetIsAcceptedForFutureWalletSplit() {
        UUID eventId = event("PUBLISHED");
        UUID giftId = gift(eventId, "ACTIVE", "PUBLIC");
        String requestKey = key();

        PaymentOrder order = service.create(
                eventId, giftId,
                "GIFT_PARTIAL", 1000001, requestKey);

        assertNotNull(order.getId());
        assertEquals(eventId, order.getEventId());
        assertEquals(giftId, order.getGiftId());
        assertEquals(1000001L, order.getAmountPaise());
        assertEquals("GIFT_PARTIAL", order.getContributionType());
        assertEquals("CREATED", order.getStatus());
        assertEquals(requestKey, order.getIdempotencyKey());
    }

    @Test
    void identicalIdempotencyRequestReturnsSameOrder() {
        UUID eventId = event("PUBLISHED");
        String requestKey = key();

        PaymentOrder first = service.create(
                eventId, null, "EVENT_WALLET", 20000, requestKey);

        PaymentOrder second = service.create(
                eventId, null, "EVENT_WALLET", 20000, requestKey);

        assertEquals(first.getId(), second.getId());
        assertEquals(1, orders.findAll().stream()
                .filter(o -> requestKey.equals(o.getIdempotencyKey()))
                .count());
    }

    @Test
    void conflictingIdempotencyRequestIsRejected() {
        UUID eventId = event("PUBLISHED");
        String requestKey = key();

        service.create(
                eventId, null, "EVENT_WALLET", 20000, requestKey);

        expectStatus(HttpStatus.CONFLICT, () ->
                service.create(eventId, null,
                        "EVENT_WALLET", 30000, requestKey));
    }
}
