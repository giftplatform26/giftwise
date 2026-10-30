
package com.giftplatform.otp.service;

import com.giftplatform.otp.entity.PhoneOtpChallenge;
import com.giftplatform.otp.repository.PhoneOtpChallengeRepository;
import com.giftplatform.user.entity.User;
import com.giftplatform.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PhoneOtpTransactionServiceTest {

    private UserRepository users;
    private PhoneOtpChallengeRepository challenges;
    private OtpCryptoService crypto;
    private PhoneOtpTransactionService service;

    private final Instant now =
            Instant.parse("2026-10-12T06:00:00Z");

    private User user;
    private PhoneOtpChallenge challenge;

    @BeforeEach
    void setup() {
        users = mock(UserRepository.class);
        challenges = mock(PhoneOtpChallengeRepository.class);
        crypto = mock(OtpCryptoService.class);

        service = new PhoneOtpTransactionService(
                users,
                challenges,
                crypto,
                Clock.fixed(now, ZoneOffset.UTC)
        );

        user = new User(
                "otpuser",
                "otp@example.com",
                "hashed-password"
        );

        UUID challengeId = UUID.randomUUID();

        challenge = new PhoneOtpChallenge(
                challengeId,
                user.getId(),
                "+919876543210",
                "test-digest",
                now.minusSeconds(60),
                now.plusSeconds(240)
        );

        challenge.markSent(now.minusSeconds(59));

        when(users.findForPhoneEnrollment(user.getId()))
                .thenReturn(Optional.of(user));

        when(challenges.findForVerification(
                challengeId, user.getId()))
                .thenReturn(Optional.of(challenge));
    }

    @Test
    void correctCodeVerifiesPhoneAndConsumesChallenge() {
        when(crypto.matches(
                eq(challenge.getId()),
                eq("123456"),
                anyString()
        )).thenReturn(true);

        var result = service.verify(
                user.getId(),
                challenge.getId(),
                "123456"
        );

        assertEquals(
                PhoneOtpVerificationResult.VERIFIED,
                result
        );
        assertEquals(
                "+919876543210",
                user.getPhoneNumber()
        );
        assertEquals(now, user.getPhoneVerifiedAt());
        assertTrue(challenge.isConsumed());
    }

    @Test
    void incorrectCodeIncrementsAttemptCount() {
        var result = service.verify(
                user.getId(),
                challenge.getId(),
                "000000"
        );

        assertEquals(
                PhoneOtpVerificationResult.INVALID_CODE,
                result
        );
        assertEquals(1, challenge.getAttemptCount());
        assertFalse(challenge.isConsumed());
    }

    @Test
    void fifthIncorrectAttemptExhaustsChallenge() {
        for (int i = 0; i < 5; i++) {
            assertEquals(
                    PhoneOtpVerificationResult.INVALID_CODE,
                    service.verify(
                            user.getId(),
                            challenge.getId(),
                            "000000"
                    )
            );
        }

        assertEquals(5, challenge.getAttemptCount());

        assertEquals(
                PhoneOtpVerificationResult.MAX_ATTEMPTS,
                service.verify(
                        user.getId(),
                        challenge.getId(),
                        "123456"
                )
        );
    }

    @Test
    void consumedChallengeCannotBeReplayed() {
        challenge.consume(now);

        assertEquals(
                PhoneOtpVerificationResult.ALREADY_USED,
                service.verify(
                        user.getId(),
                        challenge.getId(),
                        "123456"
                )
        );
    }

    @Test
    void expiredChallengeIsRejected() {
        var expired = new PhoneOtpChallenge(
                UUID.randomUUID(),
                user.getId(),
                "+919876543210",
                "test-digest",
                now.minusSeconds(400),
                now.minusSeconds(100)
        );

        expired.markSent(now.minusSeconds(399));

        when(challenges.findForVerification(
                expired.getId(), user.getId()))
                .thenReturn(Optional.of(expired));

        assertEquals(
                PhoneOtpVerificationResult.EXPIRED,
                service.verify(
                        user.getId(),
                        expired.getId(),
                        "123456"
                )
        );
    }

    @Test
    void anotherUsersChallengeIsUnavailable() {
        UUID anotherUser = UUID.randomUUID();

        assertEquals(
                PhoneOtpVerificationResult.NOT_AVAILABLE,
                service.verify(
                        anotherUser,
                        challenge.getId(),
                        "123456"
                )
        );
    }

    @Test
    void duplicatePhoneIsRejected() {
        when(crypto.matches(
                eq(challenge.getId()),
                eq("123456"),
                anyString()
        )).thenReturn(true);

        when(users.existsByPhoneNumberAndIdNot(
                "+919876543210",
                user.getId()
        )).thenReturn(true);

        assertEquals(
                PhoneOtpVerificationResult.NOT_AVAILABLE,
                service.verify(
                        user.getId(),
                        challenge.getId(),
                        "123456"
                )
        );

        assertNull(user.getPhoneNumber());
        assertFalse(challenge.isConsumed());
    }

    @Test
    void pendingChallengeCannotBeVerified() {
        var pending = new PhoneOtpChallenge(
                UUID.randomUUID(),
                user.getId(),
                "+919876543210",
                "test-digest",
                now.minusSeconds(60),
                now.plusSeconds(240)
        );

        when(challenges.findForVerification(
                pending.getId(), user.getId()))
                .thenReturn(Optional.of(pending));

        assertEquals(
                PhoneOtpVerificationResult.NOT_AVAILABLE,
                service.verify(
                        user.getId(),
                        pending.getId(),
                        "123456"
                )
        );
    }
}
