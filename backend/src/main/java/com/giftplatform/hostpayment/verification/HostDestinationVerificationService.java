package com.giftplatform.hostpayment.verification;

import com.giftplatform.audit.entity.AuditAction;
import com.giftplatform.audit.entity.AuditActorType;
import com.giftplatform.audit.service.AuditService;
import com.giftplatform.hostpayment.entity.*;
import com.giftplatform.hostpayment.repository.*;
import com.giftplatform.security.ActorContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Trusted provider verification boundary.
 *
 * Never accepts a verification status from a host request.
 * Never considers a QR match or payment screenshot proof of ownership.
 * An inconclusive result leaves the destination unverified.
 */
@Service
@ConditionalOnProperty(
    name = "gift.payment.profiles.enabled",
    havingValue = "true"
)
public class HostDestinationVerificationService {

    private final HostBankAccountRepository banks;
    private final HostUpiAccountRepository upis;
    private final HostDestinationVerifier verifier;
    private final ActorContext actor;
    private final AuditService audit;

    public HostDestinationVerificationService(
            HostBankAccountRepository banks,
            HostUpiAccountRepository upis,
            HostDestinationVerifier verifier,
            ActorContext actor,
            AuditService audit) {
        this.banks = banks;
        this.upis = upis;
        this.verifier = verifier;
        this.actor = actor;
        this.audit = audit;
    }

    @Transactional
    public VerificationEvidence verifyBank(UUID destinationId) {
        UUID hostId = actor.requireAuthenticatedUserId();

        HostBankAccount account = banks
            .findByIdAndHostId(destinationId, hostId)
            .orElseThrow(() ->
                new NoSuchElementException("Destination not found"));

        ensureEligible(
            account.isEnabled(),
            account.getVerificationStatus()
        );

        HostDestinationVerifier.Result result = verifier.verify(
            hostId,
            destinationId,
            HostDestinationVerifier.Kind.BANK
        );

        VerificationEvidence evidence = interpret(result);

        if (evidence.verified()) {
            account.applyTrustedVerification(
                VerificationStatus.VERIFIED
            );

            audit.recordChange(
                hostId,
                AuditActorType.CUSTOMER,
                "HOST_BANK_ACCOUNT",
                destinationId,
                AuditAction.STATUS_CHANGE,
                null,
                Map.of("status", "VERIFIED"),
                null
            );
        }

        return evidence;
    }

    @Transactional
    public VerificationEvidence verifyUpi(UUID destinationId) {
        UUID hostId = actor.requireAuthenticatedUserId();

        HostUpiAccount account = upis
            .findByIdAndHostId(destinationId, hostId)
            .orElseThrow(() ->
                new NoSuchElementException("Destination not found"));

        ensureEligible(
            account.isEnabled(),
            account.getVerificationStatus()
        );

        HostDestinationVerifier.Result result = verifier.verify(
            hostId,
            destinationId,
            HostDestinationVerifier.Kind.UPI
        );

        VerificationEvidence evidence = interpret(result);

        if (evidence.verified()) {
            account.applyTrustedVerification(
                VerificationStatus.VERIFIED
            );

            audit.recordChange(
                hostId,
                AuditActorType.CUSTOMER,
                "HOST_UPI_ACCOUNT",
                destinationId,
                AuditAction.STATUS_CHANGE,
                null,
                Map.of("status", "VERIFIED"),
                null
            );
        }

        return evidence;
    }

    private static void ensureEligible(
            boolean enabled,
            VerificationStatus status) {

        if (!enabled) {
            throw new IllegalStateException(
                "Disabled destination cannot be verified"
            );
        }

        if (status == VerificationStatus.REVOKED) {
            throw new IllegalStateException(
                "Revoked destination cannot be verified"
            );
        }
    }

    private static VerificationEvidence interpret(
            HostDestinationVerifier.Result result) {

        if (result == null) {
            throw new IllegalStateException(
                "Verification provider returned no result"
            );
        }

        if (!result.verified()) {
            // The existing provider contract does not distinguish
            // rejection from pending or unavailable. Fail closed.
            return new VerificationEvidence(
                VerificationOutcome.PENDING,
                null,
                null
            );
        }

        if (result.providerReference() == null ||
            result.providerReference().isBlank()) {
            throw new IllegalStateException(
                "Verified result lacks provider evidence"
            );
        }

        return new VerificationEvidence(
            VerificationOutcome.VERIFIED,
            "TRUSTED_PROVIDER",
            result.providerReference()
        );
    }
}
