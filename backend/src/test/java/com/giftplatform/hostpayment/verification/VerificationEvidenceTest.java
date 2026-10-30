
package com.giftplatform.hostpayment.verification;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VerificationEvidenceTest {

    @Test
    void verifiedRequiresProviderAndReference() {
        assertThrows(IllegalArgumentException.class,
                () -> new VerificationEvidence(
                        VerificationOutcome.VERIFIED, null, "ref-1"));

        assertThrows(IllegalArgumentException.class,
                () -> new VerificationEvidence(
                        VerificationOutcome.VERIFIED, "RazorpayX", null));

        assertThrows(IllegalArgumentException.class,
                () -> new VerificationEvidence(
                        VerificationOutcome.VERIFIED, " ", "ref-1"));

        assertThrows(IllegalArgumentException.class,
                () -> new VerificationEvidence(
                        VerificationOutcome.VERIFIED, "RazorpayX", " "));
    }

    @Test
    void verifiedEvidenceIsRecognized() {
        var evidence = new VerificationEvidence(
                VerificationOutcome.VERIFIED,
                "RazorpayX",
                "verification-123"
        );

        assertTrue(evidence.verified());
        assertEquals("RazorpayX", evidence.provider());
        assertEquals("verification-123", evidence.providerReference());
    }

    @Test
    void nonVerifiedOutcomesNeverReportVerified() {
        for (var outcome : new VerificationOutcome[]{
                VerificationOutcome.REJECTED,
                VerificationOutcome.PENDING,
                VerificationOutcome.UNAVAILABLE
        }) {
            var evidence = new VerificationEvidence(
                    outcome, null, null
            );

            assertFalse(evidence.verified());
        }
    }

    @Test
    void nullOutcomeIsRejected() {
        assertThrows(NullPointerException.class,
                () -> new VerificationEvidence(
                        null, "RazorpayX", "ref-1"));
    }
}
