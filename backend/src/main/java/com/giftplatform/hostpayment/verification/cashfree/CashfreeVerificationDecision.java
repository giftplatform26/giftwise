package com.giftplatform.hostpayment.verification.cashfree;

import java.util.Locale;
import java.util.Objects;

/** Policy-only adapter. No HTTP, no persistence and no destination activation. */
public final class CashfreeVerificationDecision {
    public enum State { VERIFIED, REJECTED, PENDING, UNAVAILABLE }
    public record Result(State state, String providerReference) {
        public boolean verified() { return state == State.VERIFIED; }
    }
    private CashfreeVerificationDecision() {}

    /**
     * Provider payloads are untrusted until obtained over authenticated provider transport
     * and correlated to the specific stored request and destination.
     * Sandbox evidence can NEVER produce a production VERIFIED result.
     */
    public static Result decide(boolean authenticatedProviderTransport,
                                boolean sandbox, boolean requestBound,
                                boolean destinationBound, String providerStatus,
                                String providerReference, String nameMatchResult) {
        if (!authenticatedProviderTransport || sandbox || !requestBound || !destinationBound)
            return new Result(State.UNAVAILABLE, null);
        if (providerStatus == null) return new Result(State.PENDING, null);
        String status = providerStatus.trim().toUpperCase(Locale.ROOT);
        if (status.equals("REJECTED") || status.equals("FAILED") || status.equals("INVALID"))
            return new Result(State.REJECTED, null);
        if (!status.equals("VALID") && !status.equals("VERIFIED"))
            return new Result(State.PENDING, null);
        if (providerReference == null || providerReference.isBlank())
            return new Result(State.PENDING, null);
        if (nameMatchResult == null || !nameMatchResult.trim().equalsIgnoreCase("MATCH"))
            return new Result(State.REJECTED, null);
        return new Result(State.VERIFIED, providerReference.trim());
    }
}
