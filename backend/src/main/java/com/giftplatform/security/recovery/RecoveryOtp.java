package com.giftplatform.security.recovery;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/** OTP primitive only. Persistence, delivery, rate limiting and authorization are handled separately. */
public final class RecoveryOtp {
    private static final SecureRandom RANDOM = new SecureRandom();
    private RecoveryOtp() {}
    public static String generate() { return String.format(java.util.Locale.ROOT, "%06d", RANDOM.nextInt(1_000_000)); }
    public static String fingerprint(byte[] key, UUID challengeId, String accountKind, UUID accountId,
                                     String purpose, String destination, String code) {
        if (key == null || key.length < 32) throw new IllegalArgumentException("OTP HMAC key must be at least 32 bytes");
        if (challengeId == null || accountId == null || accountKind == null || purpose == null || destination == null || code == null)
            throw new IllegalArgumentException("Missing challenge binding");
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            String value = challengeId + "\u0000" + accountKind + "\u0000" + accountId + "\u0000" + purpose + "\u0000" + destination + "\u0000" + code;
            return HexFormat.of().formatHex(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.GeneralSecurityException ex) { throw new IllegalStateException("HMAC unavailable", ex); }
    }
    public static boolean matches(String expectedHex, String actualHex) {
        if (expectedHex == null || actualHex == null || expectedHex.length() != 64 || actualHex.length() != 64) return false;
        try { return MessageDigest.isEqual(HexFormat.of().parseHex(expectedHex), HexFormat.of().parseHex(actualHex)); }
        catch (IllegalArgumentException ex) { return false; }
    }
    public static boolean usable(Instant expiry, Instant consumedAt, int attempts, int maxAttempts, Clock clock) {
        return expiry != null && consumedAt == null && attempts >= 0 && maxAttempts > 0 && attempts < maxAttempts &&
               Instant.now(clock).isBefore(expiry);
    }
}
