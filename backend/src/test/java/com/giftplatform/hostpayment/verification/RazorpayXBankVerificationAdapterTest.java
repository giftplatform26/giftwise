package com.giftplatform.hostpayment.verification;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RazorpayXBankVerificationAdapterTest {

    private final RazorpayXBankVerificationAdapter adapter =
        new RazorpayXBankVerificationAdapter(new ObjectMapper());

    @Test
    void parsesValidCompletedResponse() {
        String json = """
            {
              "id":"fav_123",
              "entity":"fund_account.validation",
              "status":"completed",
              "fund_account":{
                "id":"fa_123",
                "account_type":"bank_account"
              },
              "results":{
                "account_status":"active",
                "registered_name":"Asha Kumar"
              }
            }
            """;

        var result = adapter.interpretAuthenticatedResponse(
            json, "fa_123", "Asha Kumar");

        assertTrue(result.verified());
    }

    @Test
    void malformedJsonIsRejected() {
        assertThrows(IllegalArgumentException.class,
            () -> adapter.interpretAuthenticatedResponse(
                "{bad-json", "fa_123", "Asha Kumar"));
    }

    @Test
    void emptyJsonCannotVerify() {
        assertThrows(IllegalArgumentException.class,
            () -> adapter.interpretAuthenticatedResponse(
                "null", "fa_123", "Asha Kumar"));
    }

    @Test
    void missingResultsCannotVerify() {
        String json = """
            {
              "id":"fav_123",
              "entity":"fund_account.validation",
              "status":"completed",
              "fund_account":{
                "id":"fa_123",
                "account_type":"bank_account"
              }
            }
            """;
        assertFalse(adapter.interpretAuthenticatedResponse(
            json, "fa_123", "Asha Kumar").verified());
    }

    @Test
    void configurationDefaultsToDisabled() {
        var config = new RazorpayXVerificationConfiguration(
            null, null, null, null);
        assertFalse(config.isEnabled());
        assertThrows(IllegalStateException.class,
            config::requireConfigured);
    }

    @Test
    void enabledWithoutCredentialsFailsClosed() {
        var config = new RazorpayXVerificationConfiguration(
            true, "", "", "");
        assertThrows(IllegalStateException.class,
            config::requireConfigured);
    }

    @Test
    void fullyConfiguredRequiresExplicitEnablement() {
        var config = new RazorpayXVerificationConfiguration(
            false, "id", "secret", "source");
        assertThrows(IllegalStateException.class,
            config::requireConfigured);
    }
}
