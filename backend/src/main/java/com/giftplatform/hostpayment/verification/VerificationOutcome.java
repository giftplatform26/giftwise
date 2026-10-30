
package com.giftplatform.hostpayment.verification;

/**
 * Outcome reported by a trusted destination verification provider.
 *
 * VERIFIED requires authenticated provider evidence.
 * PENDING and UNAVAILABLE must never activate a destination.
 */
public enum VerificationOutcome {
    VERIFIED,
    REJECTED,
    PENDING,
    UNAVAILABLE
}
