package com.giftplatform.hostpayment.verification.provider;

import com.giftplatform.hostpayment.verification.*;
import java.util.Objects;
import java.util.UUID;

/**
 * Provider-disabled attempt lifecycle. This service never changes a destination status.
 * A future authenticated provider integration must use a separate, reviewed workflow.
 */
public final class DisabledVerificationLifecycle {
    private final VerificationAttemptCoordinator coordinator;
    private final VerificationProviderTransport transport;

    public DisabledVerificationLifecycle(VerificationAttemptCoordinator coordinator,
                                         VerificationProviderTransport transport) {
        this.coordinator = Objects.requireNonNull(coordinator);
        this.transport = Objects.requireNonNull(transport);
    }

    public record Result(UUID attemptId, VerificationAttemptState state) {}

    public Result request(UUID hostId, UUID destinationId, HostDestinationVerifier.Kind kind) {
        // begin() checks host ownership, enabled state and revocation.
        UUID attemptId = coordinator.begin(hostId, destinationId, kind);
        ProviderVerificationRequest request = new ProviderVerificationRequest(
            attemptId, hostId, destinationId,
            kind == HostDestinationVerifier.Kind.UPI
                ? ProviderVerificationRequest.Kind.UPI : ProviderVerificationRequest.Kind.BANK,
            attemptId.toString());
        try {
            // Disabled transport must refuse network access. No response is trusted here.
            ProviderVerificationResponse response = transport.submit(request);
            // Even a claimed success cannot verify a destination.
            var safe = ProviderResponseInterpreter.interpret(request, response);
            if (safe == ProviderResponseInterpreter.SafeOutcome.UNAVAILABLE) {
                coordinator.finalizeAttempt(hostId, attemptId,
                    new VerificationEvidence(VerificationOutcome.UNAVAILABLE, null, null));
                return new Result(attemptId, VerificationAttemptState.UNAVAILABLE);
            }
            // Provider response without authenticated evidence is not finalizable as verified.
            return new Result(attemptId, VerificationAttemptState.PENDING);
        } catch (VerificationProviderUnavailableException unavailable) {
            coordinator.finalizeAttempt(hostId, attemptId,
                new VerificationEvidence(VerificationOutcome.UNAVAILABLE, null, null));
            return new Result(attemptId, VerificationAttemptState.UNAVAILABLE);
        }
    }
}
