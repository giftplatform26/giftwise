package com.giftplatform.hostpayment.verification;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Objects;

/**
 * Fail-closed interpretation of an authenticated provider response.
 * Parsing alone does not authenticate a response.
 */
public final class RazorpayXValidationInterpreter {

    public VerificationEvidence interpretBank(
            RazorpayXValidationResponse response,
            String expectedFundAccountId,
            String expectedHolderName) {

        Objects.requireNonNull(response, "response");

        if (!"fund_account.validation".equals(response.entity())
                || !validId(response.id(), "fav_")
                || !validId(expectedFundAccountId, "fa_")
                || !expectedFundAccountId.equals(response.fundAccountId())
                || !"bank_account".equals(response.accountType())) {
            return unavailable();
        }

        if ("failed".equals(response.status())) {
            // Technical failure is not proof that account is invalid.
            return unavailable();
        }

        if (!"completed".equals(response.status())) {
            return pending();
        }

        if (!"active".equals(response.accountStatus())) {
            return unavailable();
        }

        if (expectedHolderName == null
                || expectedHolderName.isBlank()
                || response.registeredName() == null
                || response.registeredName().isBlank()) {
            return unavailable();
        }

        if (!normalizeName(expectedHolderName).equals(
                normalizeName(response.registeredName()))) {
            return new VerificationEvidence(
                VerificationOutcome.REJECTED,
                "RazorpayX",
                response.id()
            );
        }

        return new VerificationEvidence(
            VerificationOutcome.VERIFIED,
            "RazorpayX",
            response.id()
        );
    }

    private static String normalizeName(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFKC)
            .strip()
            .replaceAll("\\s+", " ")
            .toUpperCase(Locale.ROOT);
    }

    private static boolean validId(String value, String prefix) {
        return value != null
            && value.startsWith(prefix)
            && value.length() > prefix.length()
            && value.length() <= 100
            && value.matches("[A-Za-z0-9_]+");
    }

    private static VerificationEvidence pending() {
        return new VerificationEvidence(
            VerificationOutcome.PENDING, null, null);
    }

    private static VerificationEvidence unavailable() {
        return new VerificationEvidence(
            VerificationOutcome.UNAVAILABLE, null, null);
    }
}
