package com.giftplatform.hostpayment.verification;

import com.giftplatform.audit.service.AuditService;
import com.giftplatform.hostpayment.entity.*;
import com.giftplatform.hostpayment.repository.*;
import com.giftplatform.security.ActorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class HostDestinationVerificationServiceTest {

    private HostBankAccountRepository banks;
    private HostUpiAccountRepository upis;
    private HostDestinationVerifier verifier;
    private ActorContext actor;
    private AuditService audit;
    private HostDestinationVerificationService service;

    private final UUID hostId = UUID.randomUUID();
    private final UUID destinationId = UUID.randomUUID();

    @BeforeEach
    void setup() {
        banks = mock(HostBankAccountRepository.class);
        upis = mock(HostUpiAccountRepository.class);
        verifier = mock(HostDestinationVerifier.class);
        actor = mock(ActorContext.class);
        audit = mock(AuditService.class);

        when(actor.requireAuthenticatedUserId())
            .thenReturn(hostId);

        service = new HostDestinationVerificationService(
            banks, upis, verifier, actor, audit
        );
    }

    private HostBankAccount bank() {
        return new HostBankAccount(
            destinationId,
            hostId,
            "encrypted-account",
            "fingerprint",
            "****1234",
            "encrypted-ifsc",
            "ABCD*******",
            "encrypted-holder"
        );
    }

    private HostUpiAccount upi() {
        return new HostUpiAccount(
            destinationId,
            hostId,
            "encrypted-upi",
            "fingerprint",
            "ab***@bank"
        );
    }

    @Test
    void verifiedBankChangesState() {
        var account = bank();

        when(banks.findByIdAndHostId(destinationId, hostId))
            .thenReturn(Optional.of(account));

        when(verifier.verify(
            hostId, destinationId,
            HostDestinationVerifier.Kind.BANK
        )).thenReturn(
            new HostDestinationVerifier.Result("ref-123", true)
        );

        var result = service.verifyBank(destinationId);

        assertTrue(result.verified());
        assertEquals(
            VerificationStatus.VERIFIED,
            account.getVerificationStatus()
        );
        verify(audit, times(1)).recordChange(
            eq(hostId), any(), eq("HOST_BANK_ACCOUNT"),
            eq(destinationId), any(), isNull(), any(), isNull()
        );
    }

    @Test
    void verifiedUpiChangesState() {
        var account = upi();

        when(upis.findByIdAndHostId(destinationId, hostId))
            .thenReturn(Optional.of(account));

        when(verifier.verify(
            hostId, destinationId,
            HostDestinationVerifier.Kind.UPI
        )).thenReturn(
            new HostDestinationVerifier.Result("upi-ref", true)
        );

        assertTrue(service.verifyUpi(destinationId).verified());

        assertEquals(
            VerificationStatus.VERIFIED,
            account.getVerificationStatus()
        );
    }

    @Test
    void inconclusiveBankRemainsPending() {
        var account = bank();

        when(banks.findByIdAndHostId(destinationId, hostId))
            .thenReturn(Optional.of(account));

        when(verifier.verify(
            hostId, destinationId,
            HostDestinationVerifier.Kind.BANK
        )).thenReturn(
            new HostDestinationVerifier.Result(null, false)
        );

        var result = service.verifyBank(destinationId);

        assertFalse(result.verified());
        assertEquals(
            VerificationStatus.PENDING,
            account.getVerificationStatus()
        );
        verifyNoInteractions(audit);
    }

    @Test
    void inconclusiveUpiRemainsPending() {
        var account = upi();

        when(upis.findByIdAndHostId(destinationId, hostId))
            .thenReturn(Optional.of(account));

        when(verifier.verify(
            hostId, destinationId,
            HostDestinationVerifier.Kind.UPI
        )).thenReturn(
            new HostDestinationVerifier.Result(null, false)
        );

        assertFalse(service.verifyUpi(destinationId).verified());
        assertEquals(
            VerificationStatus.PENDING,
            account.getVerificationStatus()
        );
        verifyNoInteractions(audit);
    }

    @Test
    void missingProviderReferenceCannotVerifyBank() {
        var account = bank();

        when(banks.findByIdAndHostId(destinationId, hostId))
            .thenReturn(Optional.of(account));

        when(verifier.verify(
            hostId, destinationId,
            HostDestinationVerifier.Kind.BANK
        )).thenReturn(
            new HostDestinationVerifier.Result(" ", true)
        );

        assertThrows(
            IllegalStateException.class,
            () -> service.verifyBank(destinationId)
        );

        assertEquals(
            VerificationStatus.PENDING,
            account.getVerificationStatus()
        );
        verifyNoInteractions(audit);
    }

    @Test
    void missingProviderResultCannotVerify() {
        var account = bank();

        when(banks.findByIdAndHostId(destinationId, hostId))
            .thenReturn(Optional.of(account));

        assertThrows(
            IllegalStateException.class,
            () -> service.verifyBank(destinationId)
        );

        assertEquals(
            VerificationStatus.PENDING,
            account.getVerificationStatus()
        );
    }

    @Test
    void disabledBankIsRejectedBeforeProviderCall() {
        var account = bank();
        account.disable();

        when(banks.findByIdAndHostId(destinationId, hostId))
            .thenReturn(Optional.of(account));

        assertThrows(
            IllegalStateException.class,
            () -> service.verifyBank(destinationId)
        );

        verifyNoInteractions(verifier);
    }

    @Test
    void disabledUpiIsRejectedBeforeProviderCall() {
        var account = upi();
        account.disable();

        when(upis.findByIdAndHostId(destinationId, hostId))
            .thenReturn(Optional.of(account));

        assertThrows(
            IllegalStateException.class,
            () -> service.verifyUpi(destinationId)
        );

        verifyNoInteractions(verifier);
    }

    @Test
    void revokedBankIsRejected() {
        var account = bank();
        account.applyTrustedVerification(
            VerificationStatus.REVOKED
        );

        when(banks.findByIdAndHostId(destinationId, hostId))
            .thenReturn(Optional.of(account));

        assertThrows(
            IllegalStateException.class,
            () -> service.verifyBank(destinationId)
        );

        verifyNoInteractions(verifier);
    }

    @Test
    void crossHostBankAccessIsRejected() {
        when(banks.findByIdAndHostId(destinationId, hostId))
            .thenReturn(Optional.empty());

        assertThrows(
            NoSuchElementException.class,
            () -> service.verifyBank(destinationId)
        );

        verifyNoInteractions(verifier);
    }

    @Test
    void crossHostUpiAccessIsRejected() {
        when(upis.findByIdAndHostId(destinationId, hostId))
            .thenReturn(Optional.empty());

        assertThrows(
            NoSuchElementException.class,
            () -> service.verifyUpi(destinationId)
        );

        verifyNoInteractions(verifier);
    }

    @Test
    void unavailableProviderCannotVerify() {
        var account = bank();

        when(banks.findByIdAndHostId(destinationId, hostId))
            .thenReturn(Optional.of(account));

        when(verifier.verify(
            hostId, destinationId,
            HostDestinationVerifier.Kind.BANK
        )).thenThrow(
            new IllegalStateException(
                "Live destination verification is disabled"
            )
        );

        assertThrows(
            IllegalStateException.class,
            () -> service.verifyBank(destinationId)
        );

        assertEquals(
            VerificationStatus.PENDING,
            account.getVerificationStatus()
        );
        verifyNoInteractions(audit);
    }
}
