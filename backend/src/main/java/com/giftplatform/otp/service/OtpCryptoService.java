
package com.giftplatform.otp.service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.UUID;

public final class OtpCryptoService {

    private final SecureRandom random = new SecureRandom();
    private final byte[] secret;

    public OtpCryptoService(byte[] secret) {
        if (secret == null || secret.length < 32) {
            throw new IllegalArgumentException(
                    "OTP secret must be at least 32 bytes");
        }
        this.secret = secret.clone();
    }

    public String generateOtp() {
        return "%06d".formatted(
                random.nextInt(1_000_000));
    }

    public String digest(
            UUID challengeId,
            String otp
    ) {
        if (challengeId == null ||
                otp == null ||
                !otp.matches("[0-9]{6}")) {
            throw new IllegalArgumentException(
                    "Invalid OTP input");
        }

        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                    secret, "HmacSHA256"));

            byte[] value = mac.doFinal(
                    (challengeId + ":" + otp)
                            .getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(value);
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "OTP digest calculation failed", ex);
        }
    }

    public boolean matches(
            UUID challengeId,
            String otp,
            String expectedDigest
    ) {
        if (expectedDigest == null ||
                !expectedDigest.matches("[0-9a-f]{64}")) {
            return false;
        }

        if (otp == null || !otp.matches("[0-9]{6}")) {
            return false;
        }

        byte[] actual = HexFormat.of().parseHex(
                digest(challengeId, otp));

        byte[] expected = HexFormat.of().parseHex(
                expectedDigest);

        return MessageDigest.isEqual(actual, expected);
    }
}
