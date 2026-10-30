package com.giftplatform.otp.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.UUID;

/** Database-atomic fixed-hour limits. The caller must use the trusted socket remote address. */
@Service
public class OtpEndpointRateLimiter {
    private static final int REQUEST_IP_LIMIT = 20;
    private static final int VERIFY_USER_LIMIT = 20;
    private static final int VERIFY_IP_LIMIT = 60;
    private final JdbcTemplate jdbc;
    private final TransactionTemplate transactions;
    private final Clock clock;

    public OtpEndpointRateLimiter(JdbcTemplate jdbc, TransactionTemplate transactions, Clock clock) {
        this.jdbc = jdbc;
        this.transactions = transactions;
        this.clock = clock;
    }

    public boolean isRequestBlocked(String remoteAddress) {
        requireIp(remoteAddress);
        Instant window = clock.instant().truncatedTo(ChronoUnit.HOURS);
        return !Boolean.TRUE.equals(transactions.execute(tx ->
                increment("REQUEST_IP", digest(remoteAddress), window, REQUEST_IP_LIMIT)));
    }

    public boolean isVerificationBlocked(UUID userId, String remoteAddress) {
        if (userId == null) throw new IllegalArgumentException("Missing user");
        requireIp(remoteAddress);
        Instant window = clock.instant().truncatedTo(ChronoUnit.HOURS);
        return !Boolean.TRUE.equals(transactions.execute(tx -> {
            if (!increment("VERIFY_USER", userId.toString(), window, VERIFY_USER_LIMIT)) {
                tx.setRollbackOnly();
                return false;
            }
            if (!increment("VERIFY_IP", digest(remoteAddress), window, VERIFY_IP_LIMIT)) {
                tx.setRollbackOnly();
                return false;
            }
            return true;
        }));
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
                   OR (otp_request_rate_limits.window_start = EXCLUDED.window_start
                       AND otp_request_rate_limits.request_count < ?)
                RETURNING request_count
                """, (ResultSetExtractor<Boolean>) rs -> rs.next(),
                scope, key, java.sql.Timestamp.from(window), limit));
    }

    private static void requireIp(String address) {
        if (address == null || address.isBlank() || address.length() > 256) {
            throw new IllegalArgumentException("Invalid remote address");
        }
    }

    private static String digest(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
