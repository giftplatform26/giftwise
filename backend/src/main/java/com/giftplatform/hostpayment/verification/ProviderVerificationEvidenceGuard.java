package com.giftplatform.hostpayment.verification;

import java.util.UUID;

/** Fail-closed trust checks for a future provider adapter.
 * Does NOT apply VERIFIED to accounts, finalize attempts, or issue HTTP calls.
 */
public final class ProviderVerificationEvidenceGuard {
    private ProviderVerificationEvidenceGuard() {}

    public static VerificationEvidence requireTrusted(
            UUID expectedHostId, UUID expectedDestinationId,
            HostDestinationVerifier.Kind expectedKind,
            VerificationAttemptState currentState,
            VerificationRequestBinding storedRequest,
            ProviderVerificationEnvelope response) {
        VerificationResultMatcher.requirePending(currentState);
        if (response == null || !response.authenticatedProviderResponse())
            throw new SecurityException("Unauthenticated verification provider response");
        if (!VerificationResultMatcher.matches(expectedHostId, expectedDestinationId,
                expectedKind, storedRequest, response.hostId(), response.destinationId(),
                response.kind(), response.requestBinding()))
            throw new SecurityException("Verification request correlation mismatch");
        VerificationEvidence evidence = response.evidence();
        if (evidence.outcome() == VerificationOutcome.VERIFIED) {
            if (!evidence.verified() || !storedRequest.provider().equals(evidence.provider()))
                throw new SecurityException("Verified evidence provider mismatch");
        }
        return evidence;
    }
}
