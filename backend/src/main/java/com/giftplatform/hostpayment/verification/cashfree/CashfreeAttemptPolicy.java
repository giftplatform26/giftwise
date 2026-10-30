package com.giftplatform.hostpayment.verification.cashfree;

/** Fail-closed attempt state policy; sandbox results never activate destinations. */
public final class CashfreeAttemptPolicy {
    private CashfreeAttemptPolicy() {}
    public enum Outcome { PENDING, REJECTED, UNAVAILABLE, VERIFIED }
    public static Outcome resolve(boolean sandbox, boolean authenticated,
                                  boolean requestBound, boolean destinationBound,
                                  String providerStatus, String providerReference,
                                  String nameMatch) {
        var result = CashfreeVerificationDecision.decide(authenticated, sandbox,
                requestBound, destinationBound, providerStatus, providerReference, nameMatch);
        return switch (result.state()) {
            case VERIFIED -> Outcome.VERIFIED;
            case REJECTED -> Outcome.REJECTED;
            case PENDING -> Outcome.PENDING;
            case UNAVAILABLE -> Outcome.UNAVAILABLE;
        };
    }
}
