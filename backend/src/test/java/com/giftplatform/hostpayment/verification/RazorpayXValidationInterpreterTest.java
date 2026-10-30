package com.giftplatform.hostpayment.verification;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RazorpayXValidationInterpreterTest {

    private final RazorpayXValidationInterpreter interpreter =
        new RazorpayXValidationInterpreter();

    private RazorpayXValidationResponse response(
            String status, String accountStatus, String name) {
        return new RazorpayXValidationResponse(
            "fav_123", "fund_account.validation",
            status, accountStatus, name, "fa_123",
            "bank_account");
    }

    private VerificationEvidence interpret(
            RazorpayXValidationResponse response) {
        return interpreter.interpretBank(
            response, "fa_123", "Asha Kumar");
    }

    @Test
    void completedActiveMatchingNameIsVerified() {
        var result = interpret(
            response("completed", "active", " asha   kumar "));
        assertTrue(result.verified());
        assertEquals("fav_123", result.providerReference());
    }

    @Test
    void nameMismatchIsRejected() {
        assertEquals(VerificationOutcome.REJECTED,
            interpret(response(
                "completed", "active", "Different Person"
            )).outcome());
    }

    @Test
    void createdTransactionRemainsPending() {
        assertEquals(VerificationOutcome.PENDING,
            interpret(response(
                "created", null, null
            )).outcome());
    }

    @Test
    void failedTransactionIsUnavailable() {
        assertEquals(VerificationOutcome.UNAVAILABLE,
            interpret(response(
                "failed", null, null
            )).outcome());
    }

    @Test
    void inactiveAccountIsNotVerified() {
        assertFalse(interpret(response(
            "completed", "inactive", "Asha Kumar"
        )).verified());
    }

    @Test
    void missingRegisteredNameIsNotVerified() {
        assertFalse(interpret(response(
            "completed", "active", null
        )).verified());
    }

    @Test
    void wrongFundAccountIsNotVerified() {
        var wrong = new RazorpayXValidationResponse(
            "fav_123", "fund_account.validation",
            "completed", "active", "Asha Kumar",
            "fa_other", "bank_account");
        assertFalse(interpret(wrong).verified());
    }

    @Test
    void wrongAccountTypeIsNotVerified() {
        var wrong = new RazorpayXValidationResponse(
            "fav_123", "fund_account.validation",
            "completed", "active", "Asha Kumar",
            "fa_123", "vpa");
        assertFalse(interpret(wrong).verified());
    }

    @Test
    void missingTransactionIdIsNotVerified() {
        var wrong = new RazorpayXValidationResponse(
            null, "fund_account.validation",
            "completed", "active", "Asha Kumar",
            "fa_123", "bank_account");
        assertFalse(interpret(wrong).verified());
    }

    @Test
    void wrongEntityIsNotVerified() {
        var wrong = new RazorpayXValidationResponse(
            "fav_123", "payment",
            "completed", "active", "Asha Kumar",
            "fa_123", "bank_account");
        assertFalse(interpret(wrong).verified());
    }
}
