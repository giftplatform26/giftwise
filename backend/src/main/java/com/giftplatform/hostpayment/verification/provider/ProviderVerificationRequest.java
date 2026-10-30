package com.giftplatform.hostpayment.verification.provider;

import java.util.UUID;

/** Only identifiers: never log or serialize plaintext UPI/bank details here. */
public record ProviderVerificationRequest(UUID attemptId, UUID hostId,
        UUID destinationId, Kind kind, String requestReference) {
    public enum Kind { UPI, BANK }
    public ProviderVerificationRequest {
        if (attemptId == null || hostId == null || destinationId == null || kind == null
                || requestReference == null || requestReference.isBlank()
                || requestReference.length() > 120)
            throw new IllegalArgumentException("Invalid verification request identity");
    }
}
