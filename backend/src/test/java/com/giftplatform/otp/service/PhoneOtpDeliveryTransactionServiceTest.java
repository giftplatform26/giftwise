
package com.giftplatform.otp.service;

import com.giftplatform.otp.entity.OtpDeliveryStatus;
import com.giftplatform.otp.entity.PhoneOtpChallenge;
import com.giftplatform.otp.repository.PhoneOtpChallengeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PhoneOtpDeliveryTransactionServiceTest {

    private static final Instant NOW =
            Instant.parse("2026-10-12T06:00:00Z");

    private final UUID userId = UUID.randomUUID();
    private final UUID challengeId = UUID.randomUUID();

    private PhoneOtpChallengeRepository repository;
    private PhoneOtpDeliveryTransactionService service;
    private PhoneOtpChallenge challenge;

    @BeforeEach
    void setup() {
        repository = mock(PhoneOtpChallengeRepository.class);

        service = new PhoneOtpDeliveryTransactionService(
                repository,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );

        challenge = new PhoneOtpChallenge(
                challengeId,
                userId,
                "+919876543210",
                "test-digest",
                NOW,
                NOW.plusSeconds(300)
        );
    }

    @Test
    void successfulDeliveryMarksChallengeSent() {
        when(repository.findForVerification(challengeId, userId))
                .thenReturn(Optional.of(challenge));

        assertTrue(service.markSent(userId, challengeId));

        assertEquals(
                OtpDeliveryStatus.SENT,
                challenge.getDeliveryStatus()
        );

        assertEquals(NOW, challenge.getDeliveryUpdatedAt());
    }

    @Test
    void failedDeliveryMarksChallengeFailed() {
        when(repository.findForVerification(challengeId, userId))
                .thenReturn(Optional.of(challenge));

        assertTrue(service.markFailed(userId, challengeId));

        assertEquals(
                OtpDeliveryStatus.FAILED,
                challenge.getDeliveryStatus()
        );
    }

    @Test
    void missingChallengeCannotBeMarkedSent() {
        when(repository.findForVerification(challengeId, userId))
                .thenReturn(Optional.empty());

        assertFalse(service.markSent(userId, challengeId));
    }

    @Test
    void sentChallengeCannotBeMarkedFailed() {
        challenge.markSent(NOW);

        when(repository.findForVerification(challengeId, userId))
                .thenReturn(Optional.of(challenge));

        assertFalse(service.markFailed(userId, challengeId));

        assertEquals(
                OtpDeliveryStatus.SENT,
                challenge.getDeliveryStatus()
        );
    }

    @Test
    void failedChallengeCannotBeMarkedSent() {
        challenge.markFailed(NOW);

        when(repository.findForVerification(challengeId, userId))
                .thenReturn(Optional.of(challenge));

        assertFalse(service.markSent(userId, challengeId));

        assertEquals(
                OtpDeliveryStatus.FAILED,
                challenge.getDeliveryStatus()
        );
    }

    @Test
    void invalidatedChallengeCannotBeReactivated() {
        challenge.invalidate(NOW);

        when(repository.findForVerification(challengeId, userId))
                .thenReturn(Optional.of(challenge));

        assertFalse(service.markSent(userId, challengeId));

        assertEquals(
                OtpDeliveryStatus.INVALIDATED,
                challenge.getDeliveryStatus()
        );
    }

    @Test
    void duplicateSuccessfulCallbackIsRejected() {
        when(repository.findForVerification(challengeId, userId))
                .thenReturn(Optional.of(challenge));

        assertTrue(service.markSent(userId, challengeId));
        assertFalse(service.markSent(userId, challengeId));

        assertEquals(
                OtpDeliveryStatus.SENT,
                challenge.getDeliveryStatus()
        );
    }
}
