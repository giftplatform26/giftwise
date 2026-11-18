package com.giftplatform.payment.upi;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class PaymentReportRateLimiterTest {
    @Test void reportingIsLimitedPerClientAndEventAndResetsAfterTheWindow() {
        AtomicReference<Instant> now = new AtomicReference<>(Instant.parse("2026-11-18T10:00:00Z"));
        Clock clock = new Clock() {
            public ZoneOffset getZone() { return ZoneOffset.UTC; }
            public Clock withZone(java.time.ZoneId z) { return this; }
            public Instant instant() { return now.get(); }
        };
        var limiter = new PaymentReportRateLimiter(clock);
        for (int i = 0; i < 12; i++) limiter.check(PaymentReportRateLimiter.Action.REPORT, "1.2.3.4", "event-a");
        var blocked = assertThrows(ResponseStatusException.class, () -> limiter.check(PaymentReportRateLimiter.Action.REPORT, "1.2.3.4", "event-a"));
        assertEquals(429, blocked.getStatusCode().value());
        assertDoesNotThrow(() -> limiter.check(PaymentReportRateLimiter.Action.REPORT, "5.6.7.8", "event-a"), "other client");
        assertDoesNotThrow(() -> limiter.check(PaymentReportRateLimiter.Action.REPORT, "1.2.3.4", "event-b"), "other event");
        assertDoesNotThrow(() -> limiter.check(PaymentReportRateLimiter.Action.START, "1.2.3.4", "event-a"), "other action");
        now.set(now.get().plus(Duration.ofMinutes(16)));
        assertDoesNotThrow(() -> limiter.check(PaymentReportRateLimiter.Action.REPORT, "1.2.3.4", "event-a"));
    }
}
