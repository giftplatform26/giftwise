package com.giftplatform.otp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OtpEndpointRateLimiterIntegrationTest {
    private static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");
    static { postgres.start(); }
    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
    }
    @Autowired OtpEndpointRateLimiter limiter;
    @Autowired JdbcTemplate jdbc;
    private UUID user;
    @BeforeEach void reset() {
        jdbc.update("DELETE FROM otp_request_rate_limits");
        user = UUID.randomUUID();
    }
    @Test void ipRequestAllowsTwenty() {
        for (int i = 0; i < 20; i++) assertFalse(limiter.isRequestBlocked("192.0.2.1"));
    }
    @Test void ipRequestBlocksTwentyFirst() {
        for (int i = 0; i < 20; i++) limiter.isRequestBlocked("192.0.2.1");
        assertTrue(limiter.isRequestBlocked("192.0.2.1"));
    }
    @Test void ipRequestBlockDoesNotIncrementCounter() {
        for (int i = 0; i < 25; i++) limiter.isRequestBlocked("192.0.2.1");
        assertEquals(20, count("REQUEST_IP"));
    }
    @Test void separateIpsHaveSeparateQuotas() {
        for (int i = 0; i < 20; i++) limiter.isRequestBlocked("192.0.2.1");
        assertFalse(limiter.isRequestBlocked("192.0.2.2"));
    }
    @Test void verificationAllowsTwentyPerUser() {
        for (int i = 0; i < 20; i++) assertFalse(limiter.isVerificationBlocked(user,"192.0.2.1"));
    }
    @Test void verificationBlocksTwentyFirstPerUser() {
        for (int i = 0; i < 20; i++) limiter.isVerificationBlocked(user,"192.0.2.1");
        assertTrue(limiter.isVerificationBlocked(user,"192.0.2.1"));
    }
    @Test void userLimitDoesNotConsumeExtraIpQuota() {
        for (int i = 0; i < 25; i++) limiter.isVerificationBlocked(user,"192.0.2.1");
        assertEquals(20, count("VERIFY_IP"));
    }
    @Test void differentUserGetsIndependentUserQuota() {
        for (int i = 0; i < 20; i++) limiter.isVerificationBlocked(user,"192.0.2.1");
        assertFalse(limiter.isVerificationBlocked(UUID.randomUUID(),"192.0.2.1"));
    }
    @Test void ipVerificationQuotaIsSharedAcrossUsers() {
        for (int i = 0; i < 60; i++)
            assertFalse(limiter.isVerificationBlocked(UUID.randomUUID(),"192.0.2.1"));
        assertTrue(limiter.isVerificationBlocked(UUID.randomUUID(),"192.0.2.1"));
    }
    @Test void blockedIpDoesNotConsumeNewUserQuota() {
        for (int i = 0; i < 60; i++) limiter.isVerificationBlocked(UUID.randomUUID(),"192.0.2.1");
        assertTrue(limiter.isVerificationBlocked(user,"192.0.2.1"));
        assertEquals(60, count("VERIFY_USER"));
    }
    @Test void requestAndVerificationQuotasAreIndependent() {
        for (int i = 0; i < 20; i++) limiter.isRequestBlocked("192.0.2.1");
        assertFalse(limiter.isVerificationBlocked(user,"192.0.2.1"));
    }
    @Test void nullUserRejected() {
        assertThrows(IllegalArgumentException.class, () -> limiter.isVerificationBlocked(null,"192.0.2.1"));
    }
    @Test void blankIpRejected() {
        assertThrows(IllegalArgumentException.class, () -> limiter.isRequestBlocked(" "));
    }
    @Test void nullIpRejected() {
        assertThrows(IllegalArgumentException.class, () -> limiter.isVerificationBlocked(user,null));
    }
    @Test void parallelRequestsNeverExceedTwenty() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(8);
        try {
            List<Callable<Boolean>> jobs = new ArrayList<>();
            for (int i = 0; i < 40; i++) jobs.add(() -> limiter.isRequestBlocked("192.0.2.5"));
            int allowed = 0;
            for (Future<Boolean> f : pool.invokeAll(jobs)) if (!f.get(30, TimeUnit.SECONDS)) allowed++;
            assertEquals(20, allowed);
        } finally { pool.shutdownNow(); }
    }
    @Test void parallelVerificationNeverExceedsTwentyPerUser() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(8);
        try {
            List<Callable<Boolean>> jobs = new ArrayList<>();
            for (int i = 0; i < 35; i++) jobs.add(() -> limiter.isVerificationBlocked(user,"192.0.2.6"));
            int allowed = 0;
            for (Future<Boolean> f : pool.invokeAll(jobs)) if (!f.get(30, TimeUnit.SECONDS)) allowed++;
            assertEquals(20, allowed);
        } finally { pool.shutdownNow(); }
    }
    private int count(String scope) {
        return jdbc.queryForObject("SELECT COALESCE(SUM(request_count),0) FROM otp_request_rate_limits WHERE scope = ?", Integer.class, scope);
    }
}
