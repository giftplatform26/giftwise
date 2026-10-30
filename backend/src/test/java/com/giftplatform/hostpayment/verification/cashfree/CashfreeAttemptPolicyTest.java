package com.giftplatform.hostpayment.verification.cashfree;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CashfreeAttemptPolicyTest {
    @Test void sandboxSuccessCannotVerify() {
        assertEquals(CashfreeAttemptPolicy.Outcome.UNAVAILABLE,
            CashfreeAttemptPolicy.resolve(true,true,true,true,"VALID","ref-1","MATCH"));
    }
    @Test void unauthenticatedCannotVerify() {
        assertEquals(CashfreeAttemptPolicy.Outcome.UNAVAILABLE,
            CashfreeAttemptPolicy.resolve(false,false,true,true,"VALID","ref-1","MATCH"));
    }
    @Test void wrongDestinationCannotVerify() {
        assertEquals(CashfreeAttemptPolicy.Outcome.UNAVAILABLE,
            CashfreeAttemptPolicy.resolve(false,true,true,false,"VALID","ref-1","MATCH"));
    }
    @Test void wrongRequestCannotVerify() {
        assertEquals(CashfreeAttemptPolicy.Outcome.UNAVAILABLE,
            CashfreeAttemptPolicy.resolve(false,true,false,true,"VALID","ref-1","MATCH"));
    }
    @Test void nameMismatchRejected() {
        assertEquals(CashfreeAttemptPolicy.Outcome.REJECTED,
            CashfreeAttemptPolicy.resolve(false,true,true,true,"VALID","ref-1","MISMATCH"));
    }
    @Test void missingProviderReferenceNotVerified() {
        assertNotEquals(CashfreeAttemptPolicy.Outcome.VERIFIED,
            CashfreeAttemptPolicy.resolve(false,true,true,true,"VALID",null,"MATCH"));
    }
}
