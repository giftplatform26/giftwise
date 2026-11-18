package com.giftplatform.payment.upi;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Parses the response string a UPI app hands back to its caller, for example
 * {@code txnId=AXI123&responseCode=00&Status=SUCCESS&txnRef=GWT...&ApprovalRefNo=412345678901}.
 *
 * <p>Only native callers (an Android activity result) ever receive this; browsers do not. The content is
 * produced on the guest's device and is UNTRUSTED: it is never proof that the host's account was credited.
 */
public record UpiAppResponse(String status, String responseCode, String txnId, String txnRef,
                             String approvalRef, Long amountPaise) {
    private static final int MAX_LENGTH = 1024;

    public static UpiAppResponse parse(String raw) {
        if (raw == null || raw.isBlank() || raw.length() > MAX_LENGTH || raw.chars().anyMatch(Character::isISOControl))
            throw new IllegalArgumentException("Invalid UPI response");
        Map<String, String> values = new HashMap<>();
        for (String pair : raw.strip().split("&")) {
            if (pair.isEmpty()) continue;
            int eq = pair.indexOf('=');
            if (eq <= 0) throw new IllegalArgumentException("Invalid UPI response");
            String key, value;
            try {
                key = URLDecoder.decode(pair.substring(0, eq), StandardCharsets.UTF_8).toLowerCase(Locale.ROOT);
                value = URLDecoder.decode(pair.substring(eq + 1), StandardCharsets.UTF_8).strip();
            } catch (IllegalArgumentException ex) { throw new IllegalArgumentException("Invalid UPI response"); }
            if (values.putIfAbsent(key, value) != null) throw new IllegalArgumentException("Repeated UPI response field");
        }
        String status = normaliseStatus(values.get("status"));
        String code = token(values.get("responsecode"), 16);
        String txnId = token(values.get("txnid"), 64);
        String txnRef = token(values.get("txnref"), 64);
        // Apps disagree on the key for the bank reference; most send ApprovalRefNo (the UTR/RRN).
        String approval = token(values.getOrDefault("approvalrefno", values.get("approvalref")), 64);
        Long amount = null;
        if (values.containsKey("amount") && !values.get("amount").isEmpty()) {
            try {
                BigDecimal value = new BigDecimal(values.get("amount"));
                if (value.signum() <= 0 || value.scale() > 2 || value.precision() > 12) throw new NumberFormatException();
                amount = value.movePointRight(2).longValueExact();
            } catch (NumberFormatException | ArithmeticException ex) { throw new IllegalArgumentException("Invalid UPI response amount"); }
        }
        if (status == null && txnId == null && approval == null) throw new IllegalArgumentException("Empty UPI response");
        return new UpiAppResponse(status == null ? "UNKNOWN" : status, code, txnId, txnRef, approval, amount);
    }

    private static String normaliseStatus(String value) {
        if (value == null || value.isBlank()) return null;
        return switch (value.strip().toUpperCase(Locale.ROOT)) {
            case "SUCCESS" -> "SUCCESS";
            case "FAILURE", "FAILED" -> "FAILURE";
            case "SUBMITTED", "PENDING" -> "SUBMITTED";
            default -> "UNKNOWN";
        };
    }

    private static String token(String value, int max) {
        if (value == null || value.isEmpty() || value.equalsIgnoreCase("null") || value.equalsIgnoreCase("undefined")) return null;
        if (value.length() > max || !value.matches("[A-Za-z0-9._:\\-]+")) throw new IllegalArgumentException("Invalid UPI response field");
        return value;
    }

    /** A 12-digit approval reference is the UPI UTR/RRN; anything else is not usable as one. */
    public String utrCandidate() {
        return approvalRef != null && approvalRef.matches("[0-9]{12}") ? approvalRef : null;
    }
}
