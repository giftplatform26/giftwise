package com.giftplatform.otp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class OtpRequestRateLimiterIntegrationTest {
    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired JdbcTemplate jdbc;
    @Autowired OtpRequestRateLimiter limiter;

    private static final String PHONE = "+919876543210";

    @BeforeEach
    void reset() {
        jdbc.update("DELETE FROM otp_request_rate_limits");
    }

    private static UUID user() { return UUID.randomUUID(); }

    private long count(String scope) {
        return jdbc.queryForObject("SELECT COALESCE(SUM(request_count), 0) FROM otp_request_rate_limits WHERE scope = ?", Long.class, scope);
    }

    private long countRows(String scope) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM otp_request_rate_limits WHERE scope = ?", Long.class, scope);
    }

    @Test void firstRequestAllowed() {
        assertTrue(limiter.acquire(user(), PHONE).allowed());
    }

    @Test void firstRequestHasZeroRetry() {
        assertEquals(0, limiter.acquire(user(), PHONE).retryAfterSeconds());
    }

    @Test void exactlyFiveRequestsAllowed() {
        UUID u = user();
        for (int i = 0; i < 5; i++) assertTrue(limiter.acquire(u, PHONE).allowed());
        assertEquals(5, count("USER"));
        assertEquals(5, count("PHONE"));
    }

    @Test void sixthRequestRejectedForUser() {
        UUID u = user();
        for (int i = 0; i < 5; i++) limiter.acquire(u, PHONE);
        assertFalse(limiter.acquire(u, "+919876543211").allowed());
    }

    @Test void sixthRequestRejectedForPhoneAcrossUsers() {
        for (int i = 0; i < 5; i++) assertTrue(limiter.acquire(user(), PHONE).allowed());
        assertFalse(limiter.acquire(user(), PHONE).allowed());
    }

    @Test void deniedUserRequestDoesNotConsumeOtherPhoneQuota() {
        UUID u = user();
        for (int i = 0; i < 5; i++) limiter.acquire(u, PHONE);
        assertFalse(limiter.acquire(u, "+919876543211").allowed());
        assertEquals(5, count("PHONE"));
    }

    @Test void deniedPhoneRequestRollsBackUserQuota() {
        for (int i = 0; i < 5; i++) limiter.acquire(user(), PHONE);
        assertFalse(limiter.acquire(user(), PHONE).allowed());
        assertEquals(5, count("USER"));
    }

    @Test void rejectedRequestsDoNotIncrementCounts() {
        UUID u = user();
        for (int i = 0; i < 5; i++) limiter.acquire(u, PHONE);
        for (int i = 0; i < 3; i++) assertFalse(limiter.acquire(u, PHONE).allowed());
        assertEquals(5, count("USER"));
        assertEquals(5, count("PHONE"));
    }

    @Test void deniedRequestProvidesPositiveRetry() {
        UUID u = user();
        for (int i = 0; i < 5; i++) limiter.acquire(u, PHONE);
        assertTrue(limiter.acquire(u, PHONE).retryAfterSeconds() > 0);
    }

    @Test void retryWithinHourIsAtMost3600Seconds() {
        UUID u = user();
        for (int i = 0; i < 5; i++) limiter.acquire(u, PHONE);
        assertTrue(limiter.acquire(u, PHONE).retryAfterSeconds() <= 3600);
    }

    @Test void differentUserAndPhoneHaveIndependentQuota() {
        UUID first = user();
        for (int i = 0; i < 5; i++) limiter.acquire(first, PHONE);
        assertTrue(limiter.acquire(user(), "+919876543211").allowed());
    }

    @Test void userQuotaAppliesAcrossPhoneChanges() {
        UUID u = user();
        for (int i = 0; i < 5; i++)
            assertTrue(limiter.acquire(u, "+91987654321" + i).allowed());
        assertFalse(limiter.acquire(u, "+919876543219").allowed());
    }

    @Test void phoneQuotaAppliesAcrossUsers() {
        for (int i = 0; i < 5; i++) assertTrue(limiter.acquire(user(), PHONE).allowed());
        assertFalse(limiter.acquire(user(), PHONE).allowed());
        assertEquals(5, count("PHONE"));
    }

    @Test void phoneStoredAsHashNotPlaintext() {
        limiter.acquire(user(), PHONE);
        String stored = jdbc.queryForObject("SELECT subject_key FROM otp_request_rate_limits WHERE scope = 'PHONE'", String.class);
        assertNotEquals(PHONE, stored);
        assertEquals(64, stored.length());
    }

    @Test void phoneHashDeterministic() {
        limiter.acquire(user(), PHONE);
        limiter.acquire(user(), PHONE);
        assertEquals(1, countRows("PHONE"));
    }

    @Test void noRowsCreatedForNullUser() {
        assertThrows(IllegalArgumentException.class, () -> limiter.acquire(null, PHONE));
        assertEquals(0, count("USER"));
    }

    @Test void noRowsCreatedForNullPhone() {
        assertThrows(IllegalArgumentException.class, () -> limiter.acquire(user(), null));
        assertEquals(0, count("USER"));
    }

    @Test void noRowsCreatedForMalformedPhone() {
        assertThrows(IllegalArgumentException.class, () -> limiter.acquire(user(), "9876543210"));
        assertEquals(0, count("USER"));
    }

    @Test void overlongPhoneRejected() {
        assertThrows(IllegalArgumentException.class, () -> limiter.acquire(user(), "+1234567890123456"));
    }

    @Test void newWindowResetsCounters() {
        UUID u = user();
        for (int i = 0; i < 5; i++) limiter.acquire(u, PHONE);
        jdbc.update("UPDATE otp_request_rate_limits SET window_start = window_start - INTERVAL '2 hours'");
        assertTrue(limiter.acquire(u, PHONE).allowed());
        assertEquals(1, count("USER"));
        assertEquals(1, count("PHONE"));
    }

    @Test void resetWindowKeepsSameRows() {
        UUID u = user();
        limiter.acquire(u, PHONE);
        jdbc.update("UPDATE otp_request_rate_limits SET window_start = window_start - INTERVAL '2 hours'");
        limiter.acquire(u, PHONE);
        assertEquals(1, countRows("USER"));
        assertEquals(1, countRows("PHONE"));
    }

    @Test void concurrentSameUserAllowsExactlyFive() throws Exception {
        concurrentBurst(16, true);
    }

    @Test void concurrentSamePhoneAllowsExactlyFive() throws Exception {
        concurrentBurst(16, false);
    }

    private void concurrentBurst(int size, boolean sharedUser) throws Exception {
        UUID u = user();
        ExecutorService executor = Executors.newFixedThreadPool(size);
        CountDownLatch ready = new CountDownLatch(size);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Boolean>> futures = new ArrayList<>();
        try {
            for (int i = 0; i < size; i++) {
                UUID subject = sharedUser ? u : user();
                String phone = sharedUser ? "+91987654321" + i : PHONE;
                futures.add(executor.submit(() -> {
                    ready.countDown();
                    if (!start.await(15, TimeUnit.SECONDS)) throw new TimeoutException("Start latch");
                    return limiter.acquire(subject, phone).allowed();
                }));
            }
            assertTrue(ready.await(15, TimeUnit.SECONDS));
            start.countDown();
            int accepted = 0;
            for (Future<Boolean> f : futures) if (f.get(40, TimeUnit.SECONDS)) accepted++;
            assertEquals(5, accepted);
            assertEquals(5, count("USER"));
            assertEquals(5, count("PHONE"));
        } finally {
            start.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(15, TimeUnit.SECONDS));
        }
    }
}
