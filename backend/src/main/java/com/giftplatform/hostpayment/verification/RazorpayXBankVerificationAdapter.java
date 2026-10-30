package com.giftplatform.hostpayment.verification;

import tools.jackson.databind.ObjectMapper;

/**
 * RazorpayX validation response adapter.
 *
 * No HTTP requests are performed here.
 * Caller must authenticate provider responses, bind them to
 * a stored verification attempt and reconcile transaction state.
 */
public final class RazorpayXBankVerificationAdapter {

    private final ObjectMapper mapper;
    private final RazorpayXValidationInterpreter interpreter;

    public RazorpayXBankVerificationAdapter(ObjectMapper mapper) {
        this.mapper = mapper;
        this.interpreter = new RazorpayXValidationInterpreter();
    }

    public VerificationEvidence interpretAuthenticatedResponse(
            String json,
            String expectedFundAccountId,
            String expectedHolderName) {
        var response = RazorpayXValidationResponse.parse(
            mapper, json);
        return interpreter.interpretBank(
            response, expectedFundAccountId, expectedHolderName);
    }
}
