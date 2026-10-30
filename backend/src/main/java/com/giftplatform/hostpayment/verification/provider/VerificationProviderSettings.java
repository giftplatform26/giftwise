package com.giftplatform.hostpayment.verification.provider;

import java.net.URI;
import java.time.Duration;
import java.util.Locale;

/** Configuration model only. It never opens a network connection. */
public record VerificationProviderSettings(
        Provider provider, boolean enabled, String keyId, String keySecret,
        URI endpoint, Duration timeout) {
    public enum Provider { RAZORPAYX, CASHFREE }
    public VerificationProviderSettings {
        if (provider == null) throw new IllegalArgumentException("Provider required");
        if (timeout == null || timeout.isNegative() || timeout.isZero()
                || timeout.compareTo(Duration.ofSeconds(15)) > 0)
            throw new IllegalArgumentException("Timeout must be 1ms to 15s");
        if (enabled) {
            if (blank(keyId) || blank(keySecret))
                throw new IllegalStateException("Enabled provider requires credentials");
            if (endpoint == null || !"https".equalsIgnoreCase(endpoint.getScheme())
                    || endpoint.getHost() == null || endpoint.getUserInfo() != null
                    || endpoint.getFragment() != null)
                throw new IllegalStateException("Enabled provider requires HTTPS endpoint");
        }
    }
    private static boolean blank(String s) { return s == null || s.isBlank(); }
    public void requireEnabled() {
        if (!enabled) throw new VerificationProviderUnavailableException("Provider verification disabled");
    }
}
