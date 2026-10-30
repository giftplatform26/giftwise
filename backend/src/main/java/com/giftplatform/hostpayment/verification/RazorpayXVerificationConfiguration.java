package com.giftplatform.hostpayment.verification;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Safe defaults. No live verification without explicit configuration.
 */
@ConfigurationProperties(prefix = "gift.payment.razorpayx.verification")
public record RazorpayXVerificationConfiguration(
        Boolean enabled,
        String keyId,
        String keySecret,
        String sourceAccountNumber) {

    public boolean isEnabled() {
        return Boolean.TRUE.equals(enabled);
    }

    public void requireConfigured() {
        if (!isEnabled()
                || blank(keyId)
                || blank(keySecret)
                || blank(sourceAccountNumber)) {
            throw new IllegalStateException(
                "RazorpayX verification is unavailable");
        }
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
