package com.giftplatform.payment;

import com.giftplatform.payment.entity.PaymentOrder;
import com.giftplatform.payment.repository.PaymentOrderRepository;
import com.giftplatform.payment.service.PaymentOrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
    "gift.otp.secret-base64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
    "spring.jpa.hibernate.ddl-auto=validate"
})
@Testcontainers
class PaymentOrderConcurrencyTest {

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

    private UUID publishedEvent() {
        UUID user = UUID.randomUUID();
        UUID event = UUID.randomUUID();

        jdbc.update(
            "INSERT INTO users(id,email,password_hash,role) VALUES (?,?,?,?)",
            user, user + "@example.test", "test-hash", "CUSTOMER");

        jdbc.update(
            """
            INSERT INTO events(
                id,title,occasion_type,status,owner_id,host_display_name
            ) VALUES (?,?,?,?,?,'Test Host')
            """,
            event, "Concurrent Payment Event",
            "BIRTHDAY", "PUBLISHED", user);

        return event;
    }

    @Test
    void simultaneousIdenticalRequestsReturnOneOrder() throws Exception {
        UUID eventId = publishedEvent();
        String key = UUID.randomUUID().toString();

        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);

        try {
            Callable<PaymentOrder> request = () -> {
                if (!start.await(10, TimeUnit.SECONDS)) {
                    throw new AssertionError("Start timeout");
                }
                return service.create(
                    eventId, null, "EVENT_WALLET", 25000, key);
            };

            Future<PaymentOrder> first = pool.submit(request);
            Future<PaymentOrder> second = pool.submit(request);

            start.countDown();

            PaymentOrder a = first.get(30, TimeUnit.SECONDS);
            PaymentOrder b = second.get(30, TimeUnit.SECONDS);

            assertEquals(a.getId(), b.getId());

            assertEquals(1L,
                jdbc.queryForObject(
                    "SELECT COUNT(*) FROM payment_orders WHERE idempotency_key = ?",
                    Long.class, key));
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void simultaneousConflictingRequestsRejectOne() throws Exception {
        UUID eventId = publishedEvent();
        String key = UUID.randomUUID().toString();

        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);

        try {
            Callable<Object> firstRequest = () -> {
                if (!start.await(10, TimeUnit.SECONDS))
                    throw new AssertionError("Start timeout");
                try {
                    return service.create(
                            eventId, null, "EVENT_WALLET", 10000, key);
                } catch (org.springframework.web.server.ResponseStatusException ex) {
                    return ex;
                }
            };

            Callable<Object> secondRequest = () -> {
                if (!start.await(10, TimeUnit.SECONDS))
                    throw new AssertionError("Start timeout");
                try {
                    return service.create(
                            eventId, null, "EVENT_WALLET", 20000, key);
                } catch (org.springframework.web.server.ResponseStatusException ex) {
                    return ex;
                }
            };

            Future<Object> first = pool.submit(firstRequest);
            Future<Object> second = pool.submit(secondRequest);

            start.countDown();

            Object a = first.get(30, TimeUnit.SECONDS);
            Object b = second.get(30, TimeUnit.SECONDS);

            long successes = java.util.stream.Stream.of(a, b)
                    .filter(PaymentOrder.class::isInstance)
                    .count();

            long conflicts = java.util.stream.Stream.of(a, b)
                    .filter(org.springframework.web.server.ResponseStatusException.class::isInstance)
                    .map(org.springframework.web.server.ResponseStatusException.class::cast)
                    .filter(ex -> ex.getStatusCode().value() == 409)
                    .count();

            assertEquals(1, successes);
            assertEquals(1, conflicts);

            assertEquals(1L, jdbc.queryForObject(
                    "SELECT COUNT(*) FROM payment_orders WHERE idempotency_key = ?",
                    Long.class, key));

        } finally {
            pool.shutdownNow();
        }
    }

}
