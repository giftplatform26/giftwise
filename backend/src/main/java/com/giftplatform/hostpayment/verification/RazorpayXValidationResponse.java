package com.giftplatform.hostpayment.verification;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Strict projection of RazorpayX account-validation response.
 * No account numbers or beneficiary names are logged.
 */
public record RazorpayXValidationResponse(
        String id,
        String entity,
        String status,
        String accountStatus,
        String registeredName,
        String fundAccountId,
        String accountType) {

    public static RazorpayXValidationResponse parse(
            ObjectMapper mapper, String json) {
        try {
            JsonNode root = mapper.readTree(json);
            if (root == null || !root.isObject()) {
                throw new IllegalArgumentException(
                    "Invalid validation response");
            }
            JsonNode results = root.path("results");
            JsonNode fund = root.path("fund_account");
            return new RazorpayXValidationResponse(
                text(root, "id"),
                text(root, "entity"),
                text(root, "status"),
                text(results, "account_status"),
                text(results, "registered_name"),
                text(fund, "id"),
                text(fund, "account_type")
            );
        } catch (Exception ex) {
            throw new IllegalArgumentException(
                "Invalid validation response");
        }
    }

    private static String text(JsonNode node, String key) {
        JsonNode value = node.path(key);
        return value.isTextual() ? value.asText() : null;
    }
}
