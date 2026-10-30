package com.giftplatform.hostpayment.security;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;

/** Encrypts host payout details; ciphertext is bound to host, record and field. */
public final class PaymentDataProtector {
    private static final int NONCE_SIZE = 12;
    private static final int TAG_BITS = 128;
    private static final SecureRandom RANDOM = new SecureRandom();
    private final SecretKeySpec encryptionKey;
    private final SecretKeySpec fingerprintKey;

    public PaymentDataProtector(byte[] encryptionKey, byte[] fingerprintKey) {
        if (encryptionKey == null || encryptionKey.length != 32 || fingerprintKey == null || fingerprintKey.length != 32) {
            throw new IllegalArgumentException("Independent 256-bit encryption and fingerprint keys required");
        }
        if (java.security.MessageDigest.isEqual(encryptionKey, fingerprintKey)) {
            throw new IllegalArgumentException("Encryption and fingerprint keys must differ");
        }
        this.encryptionKey = new SecretKeySpec(encryptionKey.clone(), "AES");
        this.fingerprintKey = new SecretKeySpec(fingerprintKey.clone(), "HmacSHA256");
    }

    public String encrypt(UUID hostId, UUID recordId, String field, String value) {
        validateContext(hostId, recordId, field);
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Sensitive value is required");
        try {
            byte[] nonce = new byte[NONCE_SIZE];
            RANDOM.nextBytes(nonce);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, new GCMParameterSpec(TAG_BITS, nonce));
            cipher.updateAAD(context(hostId, recordId, field));
            byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
            return "v1:" + Base64.getEncoder().encodeToString(ByteBuffer.allocate(nonce.length + encrypted.length).put(nonce).put(encrypted).array());
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Sensitive data encryption failed", e);
        }
    }

    public String decrypt(UUID hostId, UUID recordId, String field, String stored) {
        validateContext(hostId, recordId, field);
        if (stored == null || !stored.startsWith("v1:")) throw new IllegalArgumentException("Unsupported ciphertext version");
        try {
            byte[] packed = Base64.getDecoder().decode(stored.substring(3));
            if (packed.length < NONCE_SIZE + 16) throw new IllegalArgumentException("Invalid ciphertext");
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, encryptionKey, new GCMParameterSpec(TAG_BITS, packed, 0, NONCE_SIZE));
            cipher.updateAAD(context(hostId, recordId, field));
            return new String(cipher.doFinal(packed, NONCE_SIZE, packed.length - NONCE_SIZE), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new IllegalArgumentException("Unable to decrypt sensitive data", e);
        }
    }

    /** Fingerprints must never be logged or returned by an API. */
    public String fingerprint(String type, String normalizedValue) {
        if (type == null || !type.matches("[A-Z_]{2,30}") || normalizedValue == null || normalizedValue.isBlank()) {
            throw new IllegalArgumentException("Invalid fingerprint input");
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(fingerprintKey);
            return HexFormat.of().formatHex(mac.doFinal((type + ":" + normalizedValue).getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Fingerprint generation failed", e);
        }
    }

    public static String normalizeUpi(String upi) {
        if (upi == null) throw new IllegalArgumentException("UPI ID is required");
        String value = upi.trim().toLowerCase(Locale.ROOT);
        if (value.length() > 100 || !value.matches("[a-z0-9._-]{2,64}@[a-z0-9.-]{2,32}")) {
            throw new IllegalArgumentException("Invalid UPI ID format");
        }
        return value;
    }

    public static String normalizeAccountNumber(String account) {
        if (account == null || !account.matches("[0-9]{6,20}")) throw new IllegalArgumentException("Invalid account number");
        return account;
    }

    public static String normalizeIfsc(String ifsc) {
        if (ifsc == null) throw new IllegalArgumentException("IFSC required");
        String value = ifsc.trim().toUpperCase(Locale.ROOT);
        if (!value.matches("[A-Z]{4}0[A-Z0-9]{6}")) throw new IllegalArgumentException("Invalid IFSC format");
        return value;
    }

    public static String maskAccount(String account) {
        String normalized = normalizeAccountNumber(account);
        int visible = Math.min(4, Math.max(1, normalized.length() / 3));
        return normalized.substring(0, visible) + "*".repeat(normalized.length() - visible * 2)
                + normalized.substring(normalized.length() - visible);
    }

    public static String maskUpi(String upi) {
        String normalized = normalizeUpi(upi);
        int at = normalized.indexOf('@');
        String local = normalized.substring(0, at);
        int keepStart = Math.min(3, Math.max(1, local.length() / 3));
        int keepEnd = local.length() >= 5 ? 1 : 0;
        return local.substring(0, keepStart) + "*".repeat(Math.max(3, local.length() - keepStart - keepEnd))
                + (keepEnd == 1 ? local.substring(local.length() - 1) : "") + normalized.substring(at);
    }

    private static void validateContext(UUID hostId, UUID recordId, String field) {
        if (hostId == null || recordId == null || field == null || !field.matches("[a-z_]{2,40}")) {
            throw new IllegalArgumentException("Invalid encryption context");
        }
    }

    private static byte[] context(UUID hostId, UUID recordId, String field) {
        return ("host-payment:v1:" + hostId + ":" + recordId + ":" + field).getBytes(StandardCharsets.UTF_8);
    }
}
