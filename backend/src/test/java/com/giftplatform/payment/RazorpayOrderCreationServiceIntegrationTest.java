package com.giftplatform.payment;

import com.giftplatform.payment.provider.RazorpayClient;
import com.giftplatform.payment.service.RazorpayOrderCreationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {
    "gift.otp.secret-base64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
    "spring.jpa.hibernate.ddl-auto=validate"
})
@Testcontainers
class RazorpayOrderCreationServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17");

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired RazorpayOrderCreationService service;
    @Autowired JdbcTemplate jdbc;

    @MockitoBean RazorpayClient razorpay;

    private UUID event() {
        UUID user = UUID.randomUUID();
        UUID event = UUID.randomUUID();

        jdbc.update("""
            INSERT INTO users(id,email,password_hash,role)
            VALUES (?,?,?,?)
            """, user, user + "@example.test",
            "test-hash", "CUSTOMER");

        jdbc.update("""
            INSERT INTO events(id,title,occasion_type,status,owner_id)
            VALUES (?,?,?,?,?)
            """, event, "Razorpay Test",
            "BIRTHDAY", "PUBLISHED", user);

        return event;
    }

    private void providerSuccess() {
        when(razorpay.createOrder(anyLong(), anyString()))
                .thenAnswer(invocation ->
                        new RazorpayClient.ProviderOrder(
                                "order_" + java.util.UUID.randomUUID().toString().replace("-", ""),
                                invocation.getArgument(0),
                                "INR",
                                "created"));
    }

    @Test
    void createsAndPersistsProviderOrder() {
        UUID event = event();
        providerSuccess();

        var result = service.create(
                event, null, "EVENT_WALLET",
                50000, UUID.randomUUID().toString());

        assertTrue(result.razorpayOrderId().startsWith("order_"));
        assertEquals(50000L, result.amountPaise());
        assertEquals("INR", result.currency());
        assertEquals("PENDING", result.status());

        String stored = jdbc.queryForObject("""
            SELECT provider_order_id
            FROM payment_orders WHERE id = ?
            """, String.class, result.localOrderId());

        assertEquals(result.razorpayOrderId(), stored);
    }

    @Test
    void repeatedRequestDoesNotCreateSecondProviderOrder() {
        UUID event = event();
        String key = UUID.randomUUID().toString();
        providerSuccess();

        var first = service.create(
                event, null, "EVENT_WALLET", 50000, key);

        var second = service.create(
                event, null, "EVENT_WALLET", 50000, key);

        assertEquals(first.localOrderId(), second.localOrderId());
        assertEquals(first.razorpayOrderId(),
                second.razorpayOrderId());

        verify(razorpay, times(1))
                .createOrder(eq(50000L), anyString());
    }

    @Test
    void rejectsProviderAmountMismatchAndRollsBack() {
        UUID event = event();
        String key = UUID.randomUUID().toString();

        when(razorpay.createOrder(anyLong(), anyString()))
                .thenReturn(new RazorpayClient.ProviderOrder(
                        "order_TestBad", 1, "INR", "created"));

        assertThrows(IllegalStateException.class, () ->
                service.create(event, null,
                        "EVENT_WALLET", 50000, key));

        Integer count = jdbc.queryForObject("""
            SELECT COUNT(*) FROM payment_orders
            WHERE idempotency_key = ?
            """, Integer.class, key);

        assertEquals(0, count);
    }

    @Test
    void providerFailureDoesNotPersistIncompleteOrder() {
        UUID event = event();
        String key = UUID.randomUUID().toString();

        when(razorpay.createOrder(anyLong(), anyString()))
                .thenThrow(new IllegalStateException(
                        "Simulated provider failure"));

        assertThrows(IllegalStateException.class, () ->
                service.create(event, null,
                        "EVENT_WALLET", 50000, key));

        Integer count = jdbc.queryForObject("""
            SELECT COUNT(*) FROM payment_orders
            WHERE idempotency_key = ?
            """, Integer.class, key);

        assertEquals(0, count);
    }
}
