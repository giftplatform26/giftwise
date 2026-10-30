package com.giftplatform.hostpayment.verification.provider;

/** Fail-closed translation. VERIFIED_CLAIMED remains PENDING until authenticated,
 * request-bound provider evidence is independently reconciled. */
public final class ProviderResponseInterpreter {
    private ProviderResponseInterpreter() {}
    public enum SafeOutcome { PENDING, REJECTED, UNAVAILABLE }
    public static SafeOutcome interpret(ProviderVerificationRequest request,
                                        ProviderVerificationResponse response) {
        ProviderResponseCorrelation.requireMatch(request, response);
        return switch (response.outcome()) {
            case PENDING, VERIFIED_CLAIMED -> SafeOutcome.PENDING;
            case REJECTED -> SafeOutcome.REJECTED;
            case UNAVAILABLE -> SafeOutcome.UNAVAILABLE;
        };
    }
}
