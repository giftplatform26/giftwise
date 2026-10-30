
package com.giftplatform.hostpayment.verification;

import java.util.Objects;

public record VerificationEvidence(
        VerificationOutcome outcome,
        String provider,
        String providerReference
) {
    public VerificationEvidence {
        Objects.requireNonNull(outcome, "outcome is required");

        if (outcome == VerificationOutcome.VERIFIED) {
            if (provider == null || provider.isBlank()
                    || providerReference == null
                    || providerReference.isBlank()) {
                throw new IllegalArgumentException(
                        "Verified outcome requires provider evidence"
                );
            }
        }
    }

    public boolean verified() {
        return outcome == VerificationOutcome.VERIFIED;
    }
}
