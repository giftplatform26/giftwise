package com.giftplatform.payment;

import com.giftplatform.payment.provider.RazorpayClient;
import com.giftplatform.payment.service.GuestCheckoutService;
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
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {
        "gift.otp.secret-base64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
        "gift.payment.razorpay.key-id=rzp_test_checkout_fixture",
        "spring.jpa.hibernate.ddl-auto=validate"
})
@Testcontainers
class GuestCheckoutServiceIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired GuestCheckoutService service;
    @Autowired JdbcTemplate jdbc;
    @MockitoBean RazorpayClient razorpay;

    UUID event() {
        UUID owner = UUID.randomUUID(), event = UUID.randomUUID();
        jdbc.update("INSERT INTO users(id,email,password_hash,role) VALUES (?,?,?,?)",
                owner, owner + "@example.test", "hash", "CUSTOMER");
        jdbc.update("INSERT INTO events(id,title,occasion_type,status,owner_id) VALUES (?,?,?,?,?)",
                event, "Checkout", "BIRTHDAY", "PUBLISHED", owner);
        return event;
    }

    GuestCheckoutService.Request request(UUID event, String key, String name) {
        return new GuestCheckoutService.Request(event, null, "EVENT_WALLET",
                25000L, key, name, null, null);
    }

    void provider() {
        when(razorpay.createOrder(anyLong(), anyString())).thenAnswer(inv ->
                new RazorpayClient.ProviderOrder(
                        "order_" + UUID.randomUUID().toString().replace("-", ""),
                        inv.getArgument(0), "INR", "created"));
    }

    @Test
    void createsCheckoutWithGuestAndPublicKey() {
        provider();
        var result = service.create(request(event(), UUID.randomUUID().toString(), "  Guest  "));
        assertEquals("rzp_test_checkout_fixture", result.razorpayKeyId());
        assertEquals("PENDING", result.status());
        assertEquals("Guest", jdbc.queryForObject(
                "SELECT guest_name FROM contributions WHERE payment_order_id=?",
                String.class, result.localOrderId()));
    }

    @Test
    void retryReturnsSameOrderAndOneContribution() {
        provider();
        var req = request(event(), UUID.randomUUID().toString(), "Guest");
        var first = service.create(req);
        var second = service.create(req);
        assertEquals(first.localOrderId(), second.localOrderId());
        assertEquals(1, jdbc.queryForObject(
                "SELECT COUNT(*) FROM contributions WHERE payment_order_id=?",
                Integer.class, first.localOrderId()));
        verify(razorpay, times(1)).createOrder(anyLong(), anyString());
    }

    @Test
    void retryWithChangedGuestIsRejected() {
        provider();
        UUID event = event();
        String key = UUID.randomUUID().toString();
        service.create(request(event, key, "Guest A"));
        assertThrows(ResponseStatusException.class,
                () -> service.create(request(event, key, "Guest B")));
    }

    @Test
    void blankGuestRejectedBeforeProviderCall() {
        assertThrows(ResponseStatusException.class,
                () -> service.create(request(event(), UUID.randomUUID().toString(), "  ")));
        verifyNoInteractions(razorpay);
    }

    @Test
    void providerFailureRollsBackOrderAndContribution() {
        UUID event = event();
        String key = UUID.randomUUID().toString();
        when(razorpay.createOrder(anyLong(), anyString()))
                .thenThrow(new IllegalStateException("provider failure"));
        assertThrows(IllegalStateException.class,
                () -> service.create(request(event, key, "Guest")));
        assertEquals(0, jdbc.queryForObject(
                "SELECT COUNT(*) FROM payment_orders WHERE idempotency_key=?",
                Integer.class, key));
    }
}
