package com.giftplatform.otp.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class OtpRequestRateLimiter {
    private static final int USER_LIMIT = 5;
    private static final int PHONE_LIMIT = 5;
    private final JdbcTemplate jdbc;
    private final TransactionTemplate transactions;
    private final Clock clock;

    public OtpRequestRateLimiter(JdbcTemplate jdbc,
                                 TransactionTemplate transactions,
                                 Clock clock) {
        this.jdbc = jdbc;
        this.transactions = transactions;
        this.clock = clock;
    }

    /** Atomic across both scopes; denied requests do not consume partial quotas. */
    public Decision acquire(UUID userId, String phoneNumber) {
        if (userId == null || phoneNumber == null ||
                !phoneNumber.matches("^\\+[1-9][0-9]{1,14}$")) {
            throw new IllegalArgumentException("Invalid rate-limit subject");
        }
        Instant now = clock.instant();
        Instant window = now.truncatedTo(ChronoUnit.HOURS);
        long retryAfter = Math.max(1,
                Duration.between(now, window.plus(1, ChronoUnit.HOURS)).getSeconds());
        return transactions.execute(tx -> {
            if (!increment("USER", userId.toString(), window, USER_LIMIT)) {
                tx.setRollbackOnly();
                return new Decision(false, retryAfter);
            }
            if (!increment("PHONE", sha256(phoneNumber), window, PHONE_LIMIT)) {
                tx.setRollbackOnly();
                return new Decision(false, retryAfter);
            }
            return new Decision(true, 0);
        });
    }

    private boolean increment(String scope, String key, Instant window, int limit) {
        return Boolean.TRUE.equals(jdbc.query("""
                INSERT INTO otp_request_rate_limits
                    (scope, subject_key, window_start, request_count)
                VALUES (?, ?, ?, 1)
                ON CONFLICT (scope, subject_key) DO UPDATE SET
                    request_count = CASE
                        WHEN otp_request_rate_limits.window_start < EXCLUDED.window_start
                        THEN 1 ELSE otp_request_rate_limits.request_count + 1 END,
                    window_start = EXCLUDED.window_start
                WHERE otp_request_rate_limits.window_start < EXCLUDED.window_start
                   OR otp_request_rate_limits.request_count < ?
                RETURNING request_count
                """, (org.springframework.jdbc.core.ResultSetExtractor<Boolean>) rs -> rs.next(),
                scope, key, java.sql.Timestamp.from(window), limit));
    }

    private static String sha256(String value) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public record Decision(boolean allowed, long retryAfterSeconds) { }
}
