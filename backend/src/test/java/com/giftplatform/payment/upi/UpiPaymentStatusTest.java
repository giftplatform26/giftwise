package com.giftplatform.payment.upi;

import static com.giftplatform.payment.upi.UpiPaymentStatus.*;
import static com.giftplatform.payment.upi.UpiPaymentStatus.Actor.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class UpiPaymentStatusTest {
    @Test void guestsCanNeverConfirmOrVerifyTheirOwnPayment() {
        for (UpiPaymentStatus from : UpiPaymentStatus.values()) {
            assertFalse(allowed(from, HOST_CONFIRMED, GUEST), from + "->HOST_CONFIRMED");
            assertFalse(allowed(from, INDEPENDENTLY_VERIFIED, GUEST), from + "->INDEPENDENTLY_VERIFIED");
            assertFalse(allowed(from, REJECTED, GUEST), from + "->REJECTED");
        }
    }

    @Test void hostsCanConfirmRejectAndUndoButNeverIndependentlyVerify() {
        assertTrue(allowed(PAYMENT_REPORTED, HOST_CONFIRMED, HOST));
        assertTrue(allowed(PAYMENT_REPORTED, REJECTED, HOST));
        assertTrue(allowed(HOST_CONFIRMED, PAYMENT_REPORTED, HOST));
        assertTrue(allowed(REJECTED, PAYMENT_REPORTED, HOST));
        for (UpiPaymentStatus from : UpiPaymentStatus.values()) assertFalse(allowed(from, INDEPENDENTLY_VERIFIED, HOST), from.name());
        assertFalse(allowed(INITIATED, HOST_CONFIRMED, HOST), "nothing to confirm before the guest reports");
        assertFalse(allowed(EXPIRED, HOST_CONFIRMED, HOST));
    }

    @Test void onlyAnIndependentVerifierReachesIndependentlyVerified() {
        assertTrue(allowed(PAYMENT_REPORTED, INDEPENDENTLY_VERIFIED, PLATFORM_VERIFIER));
        assertTrue(allowed(HOST_CONFIRMED, INDEPENDENTLY_VERIFIED, PLATFORM_VERIFIER));
        assertFalse(allowed(INITIATED, INDEPENDENTLY_VERIFIED, PLATFORM_VERIFIER), "an app callback alone is never verification");
        assertFalse(allowed(PENDING, INDEPENDENTLY_VERIFIED, PLATFORM_VERIFIER));
        assertFalse(allowed(PAYMENT_REPORTED, INDEPENDENTLY_VERIFIED, SYSTEM));
    }

    @Test void guestAttemptLifecycle() {
        assertTrue(allowed(INITIATED, PENDING, GUEST));
        assertTrue(allowed(INITIATED, FAILED, GUEST));
        assertTrue(allowed(INITIATED, PAYMENT_REPORTED, GUEST));
        assertTrue(allowed(PENDING, PAYMENT_REPORTED, GUEST));
        assertTrue(allowed(FAILED, PAYMENT_REPORTED, GUEST));
        assertFalse(allowed(PAYMENT_REPORTED, PENDING, GUEST), "reports cannot be withdrawn by the guest");
        assertFalse(allowed(INITIATED, EXPIRED, GUEST));
        assertTrue(allowed(INITIATED, EXPIRED, SYSTEM));
        assertFalse(allowed(PAYMENT_REPORTED, EXPIRED, SYSTEM), "a reported claim does not silently expire");
    }

    @Test void terminalStatesAndNoOps() {
        for (UpiPaymentStatus to : UpiPaymentStatus.values()) for (Actor a : Actor.values()) {
            assertFalse(allowed(EXPIRED, to, a));
            assertFalse(allowed(INDEPENDENTLY_VERIFIED, to, a));
        }
        assertFalse(allowed(PENDING, PENDING, GUEST));
        assertFalse(allowed(null, PENDING, GUEST));
        assertFalse(allowed(PENDING, null, GUEST));
        assertFalse(allowed(PENDING, FAILED, null));
    }

    @Test void claimColumnMappingKeepsTheExistingStoredValues() {
        assertEquals(PAYMENT_REPORTED, fromStored("AWAITING_VERIFICATION"));
        assertEquals(HOST_CONFIRMED, fromStored("HOST_CONFIRMED"));
        assertEquals("AWAITING_VERIFICATION", PAYMENT_REPORTED.claimColumnValue());
        assertEquals("REJECTED", REJECTED.claimColumnValue());
        assertThrows(IllegalArgumentException.class, INDEPENDENTLY_VERIFIED::claimColumnValue);
        assertNull(fromStored(null));
    }
}
