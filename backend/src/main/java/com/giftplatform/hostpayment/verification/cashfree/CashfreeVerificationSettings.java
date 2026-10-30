package com.giftplatform.hostpayment.verification.cashfree;

/** Intentionally disabled by default. Test credentials are never production credentials. */
public record CashfreeVerificationSettings(boolean enabled, boolean sandbox) {
    public static CashfreeVerificationSettings fromEnvironment() {
        // Never permit enabling production via a single accidental environment toggle.
        return new CashfreeVerificationSettings(false, true);
    }
}
