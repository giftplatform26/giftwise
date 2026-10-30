package com.giftplatform.hostpayment.verification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Separate operator approval and provider credentials are required before live calls.
 * This component intentionally has no network or destination-status side effects.
 */
@Component
public class ProviderVerificationGate {
    private final boolean enabled;
    private final String provider;
    private final String keyId;
    private final String keySecret;

    public ProviderVerificationGate(
            @Value("${gift.payment.destination-verification.enabled:false}") boolean enabled,
            @Value("${gift.payment.destination-verification.provider:DISABLED}") String provider,
            @Value("${gift.payment.destination-verification.key-id:}") String keyId,
            @Value("${gift.payment.destination-verification.key-secret:}") String keySecret) {
        this.enabled=enabled;
        this.provider=provider == null ? "DISABLED" : provider;
        this.keyId=keyId;
        this.keySecret=keySecret;
    }

    public boolean liveConfigured() {
        return enabled && ("RAZORPAYX".equals(provider) || "CASHFREE".equals(provider))
                && nonblank(keyId) && nonblank(keySecret);
    }

    public void requireLiveConfigured() {
        if (!liveConfigured()) throw new IllegalStateException("Live destination verification unavailable");
    }

    public String providerName() { return liveConfigured() ? provider : "DISABLED"; }
    private static boolean nonblank(String value) { return value != null && !value.isBlank(); }
}
