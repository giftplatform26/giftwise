package com.giftplatform.hostpayment.verification.provider;

/** 5S does not provide a live HTTP transport. Future providers must be separately reviewed. */
public final class ProviderVerificationTransportFactory {
    private ProviderVerificationTransportFactory() {}
    public static VerificationProviderTransport create(VerificationProviderSettings settings) {
        if (settings == null) throw new IllegalArgumentException("Settings required");
        return new DisabledVerificationProviderTransport();
    }
}
