package com.giftplatform.hostpayment.verification.provider;

import org.junit.jupiter.api.Test;
import java.net.URI;
import java.time.Duration;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class ProviderVerificationContractTest {
    private ProviderVerificationRequest request() {
        return new ProviderVerificationRequest(UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), ProviderVerificationRequest.Kind.UPI, "req_123");
    }
    @Test void bothProvidersDisabledWithoutCredentials() {
        for (var p : VerificationProviderSettings.Provider.values()) {
            var settings = new VerificationProviderSettings(p, false, null, null, null, Duration.ofSeconds(5));
            assertThrows(VerificationProviderUnavailableException.class, settings::requireEnabled);
            assertThrows(VerificationProviderUnavailableException.class,
                    () -> ProviderVerificationTransportFactory.create(settings).submit(request()));
        }
    }
    @Test void enabledProviderRequiresCredentialsAndHttps() {
        assertThrows(IllegalStateException.class, () -> new VerificationProviderSettings(
                VerificationProviderSettings.Provider.CASHFREE, true, null, null,
                URI.create("https://example.com"), Duration.ofSeconds(5)));
        assertThrows(IllegalStateException.class, () -> new VerificationProviderSettings(
                VerificationProviderSettings.Provider.RAZORPAYX, true, "id", "secret",
                URI.create("http://example.com"), Duration.ofSeconds(5)));
        assertThrows(IllegalArgumentException.class, () -> new VerificationProviderSettings(
                VerificationProviderSettings.Provider.CASHFREE, false, null, null, null, Duration.ofSeconds(16)));
    }
    @Test void evenConfiguredProviderCannotMakeLiveCalls() {
        var settings = new VerificationProviderSettings(VerificationProviderSettings.Provider.CASHFREE,
                true, "id", "secret", URI.create("https://example.com"), Duration.ofSeconds(3));
        assertThrows(VerificationProviderUnavailableException.class,
                () -> ProviderVerificationTransportFactory.create(settings).submit(request()));
    }
    @Test void mismatchedResponseIsRejected() {
        var r = request();
        var response = new ProviderVerificationResponse("wrong", "provider_1",
                ProviderVerificationResponse.Outcome.VERIFIED_CLAIMED, "ok");
        assertThrows(SecurityException.class, () -> ProviderResponseInterpreter.interpret(r, response));
    }
    @Test void claimedVerificationNeverBecomesVerified() {
        var r = request();
        var response = new ProviderVerificationResponse(r.requestReference(), "provider_1",
                ProviderVerificationResponse.Outcome.VERIFIED_CLAIMED, "ok");
        assertEquals(ProviderResponseInterpreter.SafeOutcome.PENDING,
                ProviderResponseInterpreter.interpret(r, response));
    }
    @Test void rejectedAndUnavailableStayNonVerified() {
        var r = request();
        assertEquals(ProviderResponseInterpreter.SafeOutcome.REJECTED,
                ProviderResponseInterpreter.interpret(r, new ProviderVerificationResponse(
                        r.requestReference(), null, ProviderVerificationResponse.Outcome.REJECTED, null)));
        assertEquals(ProviderResponseInterpreter.SafeOutcome.UNAVAILABLE,
                ProviderResponseInterpreter.interpret(r, new ProviderVerificationResponse(
                        r.requestReference(), null, ProviderVerificationResponse.Outcome.UNAVAILABLE, null)));
    }
    @Test void bankAndUpiIdentitiesRequireValidFields() {
        assertThrows(IllegalArgumentException.class, () -> new ProviderVerificationRequest(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                ProviderVerificationRequest.Kind.BANK, " "));
        assertNotNull(new ProviderVerificationRequest(UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), ProviderVerificationRequest.Kind.BANK, "bank_ref"));
    }
}
