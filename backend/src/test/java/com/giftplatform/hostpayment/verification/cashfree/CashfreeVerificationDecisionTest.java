package com.giftplatform.hostpayment.verification.cashfree;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CashfreeVerificationDecisionTest {
    @Test void sandboxCannotVerifyEvenWithSuccessPayload() {
        assertFalse(CashfreeVerificationDecision.decide(true,true,true,true,"VALID","ref-1","MATCH").verified());
    }
    @Test void unauthenticatedPayloadCannotVerify() {
        assertFalse(CashfreeVerificationDecision.decide(false,false,true,true,"VALID","ref-1","MATCH").verified());
    }
    @Test void missingRequestOrDestinationBindingCannotVerify() {
        assertFalse(CashfreeVerificationDecision.decide(true,false,false,true,"VALID","ref-1","MATCH").verified());
        assertFalse(CashfreeVerificationDecision.decide(true,false,true,false,"VALID","ref-1","MATCH").verified());
    }
    @Test void missingReferenceOrNameMatchCannotVerify() {
        assertFalse(CashfreeVerificationDecision.decide(true,false,true,true,"VALID"," ","MATCH").verified());
        assertFalse(CashfreeVerificationDecision.decide(true,false,true,true,"VALID","ref-1","MISMATCH").verified());
    }
    @Test void pendingAndFailedAreNotVerified() {
        assertFalse(CashfreeVerificationDecision.decide(true,false,true,true,"PENDING","ref-1","MATCH").verified());
        assertEquals(CashfreeVerificationDecision.State.REJECTED,
            CashfreeVerificationDecision.decide(true,false,true,true,"FAILED","ref-1","MATCH").state());
    }
    @Test void verifiedRequiresEveryGuard() {
        assertTrue(CashfreeVerificationDecision.decide(true,false,true,true,"VALID","ref-1","MATCH").verified());
    }
    @Test void environmentConfigurationAlwaysDisabled() {
        assertFalse(CashfreeVerificationSettings.fromEnvironment().enabled());
    }
}
