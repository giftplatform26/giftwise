package com.giftplatform.hostpayment.verification.provider;

import com.giftplatform.hostpayment.verification.*;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DisabledVerificationLifecycleTest {
    final UUID host = UUID.randomUUID(), destination = UUID.randomUUID(), attempt = UUID.randomUUID();
    final VerificationAttemptCoordinator coordinator = mock(VerificationAttemptCoordinator.class);

    @Test void disabledUpiFinalizesUnavailableNotVerified() {
        when(coordinator.begin(host,destination,HostDestinationVerifier.Kind.UPI)).thenReturn(attempt);
        var service = new DisabledVerificationLifecycle(coordinator,new DisabledVerificationProviderTransport());
        var result = service.request(host,destination,HostDestinationVerifier.Kind.UPI);
        assertEquals(attempt,result.attemptId());
        assertEquals(VerificationAttemptState.UNAVAILABLE,result.state());
        verify(coordinator).finalizeAttempt(eq(host),eq(attempt),argThat(e ->
            e.outcome()==VerificationOutcome.UNAVAILABLE && !e.verified()));
    }

    @Test void disabledBankFinalizesUnavailableNotVerified() {
        when(coordinator.begin(host,destination,HostDestinationVerifier.Kind.BANK)).thenReturn(attempt);
        var service = new DisabledVerificationLifecycle(coordinator,new DisabledVerificationProviderTransport());
        assertEquals(VerificationAttemptState.UNAVAILABLE,
            service.request(host,destination,HostDestinationVerifier.Kind.BANK).state());
    }

    @Test void claimedVerifiedResponseNeverFinalizesAccountOrAttempt() {
        when(coordinator.begin(host,destination,HostDestinationVerifier.Kind.BANK)).thenReturn(attempt);
        VerificationProviderTransport untrusted = r -> new ProviderVerificationResponse(
            r.requestReference(),"fake-success",ProviderVerificationResponse.Outcome.VERIFIED_CLAIMED,"ok");
        var service = new DisabledVerificationLifecycle(coordinator,untrusted);
        assertEquals(VerificationAttemptState.PENDING,
            service.request(host,destination,HostDestinationVerifier.Kind.BANK).state());
        verify(coordinator,never()).finalizeAttempt(any(),any(),any());
    }

    @Test void mismatchedResponseFailsClosed() {
        when(coordinator.begin(host,destination,HostDestinationVerifier.Kind.UPI)).thenReturn(attempt);
        VerificationProviderTransport untrusted = r -> new ProviderVerificationResponse(
            "wrong",null,ProviderVerificationResponse.Outcome.VERIFIED_CLAIMED,"ok");
        assertThrows(SecurityException.class,()->new DisabledVerificationLifecycle(coordinator,untrusted)
            .request(host,destination,HostDestinationVerifier.Kind.UPI));
        verify(coordinator,never()).finalizeAttempt(any(),any(),any());
    }

    @Test void unauthorizedOwnershipFailurePreventsTransport() {
        when(coordinator.begin(host,destination,HostDestinationVerifier.Kind.UPI))
            .thenThrow(new java.util.NoSuchElementException("Destination not found"));
        VerificationProviderTransport transport=mock(VerificationProviderTransport.class);
        assertThrows(java.util.NoSuchElementException.class,()->
            new DisabledVerificationLifecycle(coordinator,transport)
                .request(host,destination,HostDestinationVerifier.Kind.UPI));
        verifyNoInteractions(transport);
    }
}
