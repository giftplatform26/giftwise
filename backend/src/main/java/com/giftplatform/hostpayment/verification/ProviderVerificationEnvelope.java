package com.giftplatform.hostpayment.verification;

import java.util.UUID;

/** Internal result only. Caller must authenticate the provider transport separately.
 * Correlation alone is not authentication and never changes a destination's status.
 */
public record ProviderVerificationEnvelope(
        UUID hostId, UUID destinationId, HostDestinationVerifier.Kind kind,
        VerificationRequestBinding requestBinding, VerificationEvidence evidence,
        boolean authenticatedProviderResponse) {
    public ProviderVerificationEnvelope {
        if (hostId == null || destinationId == null || kind == null
                || requestBinding == null || evidence == null)
            throw new IllegalArgumentException("Missing verification response identity");
    }
}
