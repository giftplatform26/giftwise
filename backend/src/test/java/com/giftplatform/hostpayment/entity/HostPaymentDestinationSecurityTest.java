package com.giftplatform.hostpayment.entity;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class HostPaymentDestinationSecurityTest {
    private HostUpiAccount upi() {
        return new HostUpiAccount(UUID.randomUUID(), UUID.randomUUID(), "encrypted", "fingerprint", "a***@bank");
    }
    private HostBankAccount bank() {
        return new HostBankAccount(UUID.randomUUID(), UUID.randomUUID(), "encrypted", "fingerprint", "****1234", "encrypted", "ABCD*******", "encrypted");
    }
    @Test void newDestinationsArePendingAndCannotBePreferred() {
        var u = upi(); var b = bank();
        assertEquals(VerificationStatus.PENDING, u.getVerificationStatus());
        assertEquals(VerificationStatus.PENDING, b.getVerificationStatus());
        assertThrows(IllegalStateException.class, u::prefer);
        assertThrows(IllegalStateException.class, b::prefer);
    }
    @Test void trustedVerificationEnablesPreference() {
        var u = upi(); var b = bank();
        u.applyTrustedVerification(VerificationStatus.VERIFIED);
        b.applyTrustedVerification(VerificationStatus.VERIFIED);
        u.prefer(); b.prefer();
        assertTrue(u.isPreferred()); assertTrue(b.isPreferred());
    }
    @Test void failedVerificationRevokesPreference() {
        var u = upi(); var b = bank();
        u.applyTrustedVerification(VerificationStatus.VERIFIED);
        b.applyTrustedVerification(VerificationStatus.VERIFIED);
        u.prefer(); b.prefer();
        u.applyTrustedVerification(VerificationStatus.FAILED);
        b.applyTrustedVerification(VerificationStatus.REVOKED);
        assertFalse(u.isPreferred()); assertFalse(b.isPreferred());
        assertThrows(IllegalStateException.class, u::prefer);
        assertThrows(IllegalStateException.class, b::prefer);
    }
    @Test void disabledAccountsCannotBeVerifiedOrPreferred() {
        var u = upi(); var b = bank();
        u.disable(); b.disable();
        assertThrows(IllegalStateException.class, () -> u.applyTrustedVerification(VerificationStatus.VERIFIED));
        assertThrows(IllegalStateException.class, () -> b.applyTrustedVerification(VerificationStatus.VERIFIED));
        assertThrows(IllegalStateException.class, u::prefer);
        assertThrows(IllegalStateException.class, b::prefer);
    }
    @Test void revokedAccountsCannotBeReverified() {
        var u = upi(); var b = bank();
        u.applyTrustedVerification(VerificationStatus.REVOKED);
        b.applyTrustedVerification(VerificationStatus.REVOKED);
        assertThrows(IllegalStateException.class, () -> u.applyTrustedVerification(VerificationStatus.VERIFIED));
        assertThrows(IllegalStateException.class, () -> b.applyTrustedVerification(VerificationStatus.VERIFIED));
    }
    @Test void pendingOrNullCannotBeAppliedAsTrustedResult() {
        var u = upi(); var b = bank();
        assertThrows(IllegalArgumentException.class, () -> u.applyTrustedVerification(null));
        assertThrows(IllegalArgumentException.class, () -> b.applyTrustedVerification(VerificationStatus.PENDING));
    }
}
