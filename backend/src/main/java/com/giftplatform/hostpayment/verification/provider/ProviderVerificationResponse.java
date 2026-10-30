package com.giftplatform.hostpayment.verification.provider;

/** Untrusted parsed data; NOT proof of provider authenticity or account ownership. */
public record ProviderVerificationResponse(String requestReference, String providerReference,
        Outcome outcome, String providerCode) {
    public enum Outcome { PENDING, REJECTED, UNAVAILABLE, VERIFIED_CLAIMED }
    public ProviderVerificationResponse {
        if (outcome == null) throw new IllegalArgumentException("Outcome required");
    }
}
