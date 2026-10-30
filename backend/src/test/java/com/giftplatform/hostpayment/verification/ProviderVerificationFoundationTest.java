package com.giftplatform.hostpayment.verification;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProviderVerificationFoundationTest {
    private final UUID host=UUID.randomUUID();
    private final UUID destination=UUID.randomUUID();
    private final VerificationRequestBinding binding=
        new VerificationRequestBinding("RAZORPAYX", "request-1", "fund-1");
    private final VerificationEvidence verified=
        new VerificationEvidence(VerificationOutcome.VERIFIED, "RAZORPAYX", "evidence-1");

    private ProviderVerificationEnvelope response(boolean authenticated, UUID hostId,
            UUID destinationId, HostDestinationVerifier.Kind kind,
            VerificationRequestBinding request, VerificationEvidence evidence) {
        return new ProviderVerificationEnvelope(hostId,destinationId,kind,request,evidence,authenticated);
    }

    @Test void disabledByDefaultEvenWhenProviderNamed() {
        assertFalse(new ProviderVerificationGate(false,"RAZORPAYX","id","secret").liveConfigured());
        assertEquals("DISABLED",new ProviderVerificationGate(false,"RAZORPAYX","id","secret").providerName());
    }
    @Test void missingCredentialsAndUnknownProviderFailClosed() {
        assertFalse(new ProviderVerificationGate(true,"RAZORPAYX","id","").liveConfigured());
        assertFalse(new ProviderVerificationGate(true,"OTHER","id","secret").liveConfigured());
        assertThrows(IllegalStateException.class,
            () -> new ProviderVerificationGate(true,"CASHFREE","", "secret").requireLiveConfigured());
    }
    @Test void explicitConfigurationCanBeRecognizedWithoutMakingCalls() {
        assertTrue(new ProviderVerificationGate(true,"CASHFREE","id","secret").liveConfigured());
    }
    @Test void unauthenticatedPositiveResponseRejected() {
        assertThrows(SecurityException.class, () -> ProviderVerificationEvidenceGuard.requireTrusted(
            host,destination,HostDestinationVerifier.Kind.BANK,VerificationAttemptState.PENDING,binding,
            response(false,host,destination,HostDestinationVerifier.Kind.BANK,binding,verified)));
    }
    @Test void wrongHostDestinationOrKindRejected() {
        for (var r : new ProviderVerificationEnvelope[]{
            response(true,UUID.randomUUID(),destination,HostDestinationVerifier.Kind.BANK,binding,verified),
            response(true,host,UUID.randomUUID(),HostDestinationVerifier.Kind.BANK,binding,verified),
            response(true,host,destination,HostDestinationVerifier.Kind.UPI,binding,verified)}) {
            assertThrows(SecurityException.class, () -> ProviderVerificationEvidenceGuard.requireTrusted(
                host,destination,HostDestinationVerifier.Kind.BANK,VerificationAttemptState.PENDING,binding,r));
        }
    }
    @Test void wrongRequestReferenceOrFundAccountRejected() {
        for (var b : new VerificationRequestBinding[]{
            new VerificationRequestBinding("RAZORPAYX","other","fund-1"),
            new VerificationRequestBinding("RAZORPAYX","request-1","other")}) {
            assertThrows(SecurityException.class, () -> ProviderVerificationEvidenceGuard.requireTrusted(
                host,destination,HostDestinationVerifier.Kind.BANK,VerificationAttemptState.PENDING,binding,
                response(true,host,destination,HostDestinationVerifier.Kind.BANK,b,verified)));
        }
    }
    @Test void finalizedAttemptRejected() {
        assertThrows(IllegalStateException.class, () -> ProviderVerificationEvidenceGuard.requireTrusted(
            host,destination,HostDestinationVerifier.Kind.BANK,VerificationAttemptState.UNAVAILABLE,binding,
            response(true,host,destination,HostDestinationVerifier.Kind.BANK,binding,verified)));
    }
    @Test void mismatchedEvidenceProviderRejected() {
        var evidence=new VerificationEvidence(VerificationOutcome.VERIFIED,"CASHFREE","evidence-1");
        assertThrows(SecurityException.class, () -> ProviderVerificationEvidenceGuard.requireTrusted(
            host,destination,HostDestinationVerifier.Kind.BANK,VerificationAttemptState.PENDING,binding,
            response(true,host,destination,HostDestinationVerifier.Kind.BANK,binding,evidence)));
    }
    @Test void authenticatedCorrelatedEvidenceReturnedWithoutMutatingAccounts() {
        assertSame(verified,ProviderVerificationEvidenceGuard.requireTrusted(
            host,destination,HostDestinationVerifier.Kind.BANK,VerificationAttemptState.PENDING,binding,
            response(true,host,destination,HostDestinationVerifier.Kind.BANK,binding,verified)));
    }
}
