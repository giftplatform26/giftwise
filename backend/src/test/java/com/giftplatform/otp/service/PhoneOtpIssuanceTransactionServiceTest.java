
package com.giftplatform.otp.service;

import com.giftplatform.otp.entity.OtpDeliveryStatus;
import com.giftplatform.otp.entity.PhoneOtpChallenge;
import com.giftplatform.otp.repository.PhoneOtpChallengeRepository;
import com.giftplatform.user.entity.User;
import com.giftplatform.user.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PhoneOtpIssuanceTransactionServiceTest {

    @Mock
    private UserRepository users;

    @Mock
    private PhoneOtpChallengeRepository challenges;

    private PhoneOtpIssuanceTransactionService service;
    private OtpCryptoService crypto;

    private final UUID userId = UUID.randomUUID();
    private final UUID challengeId = UUID.randomUUID();

    private final Instant now =
            Instant.parse("2026-10-12T06:00:00Z");

    private static final String PHONE = "+919876543210";
    private static final String CODE = "123456";

    @BeforeEach
    void setup() {
        crypto = new OtpCryptoService(new byte[32]);

        service = new PhoneOtpIssuanceTransactionService(
                users,
                challenges,
                crypto,
                Clock.fixed(now, ZoneOffset.UTC)
        );
    }

    private void availableUser() {
        User user = new User(
                "otpuser",
                "otp@example.com",
                "hashed-password"
        );

        when(users.findForPhoneEnrollment(userId))
                .thenReturn(Optional.of(user));
    }

    @Test
    void createsPendingChallengeWithFiveMinuteExpiry() {
        availableUser();

        when(challenges.findActiveForUpdate(
                eq(userId), anyCollection()))
                .thenReturn(List.of());

        var result = service.reserve(
                userId, PHONE, CODE, challengeId);

        assertEquals(
                PhoneOtpIssuanceResult.Status.RESERVED,
                result.status());

        assertEquals(challengeId, result.challengeId());

        ArgumentCaptor<PhoneOtpChallenge> captor =
                ArgumentCaptor.forClass(
                        PhoneOtpChallenge.class);

        verify(challenges).saveAndFlush(captor.capture());

        PhoneOtpChallenge saved = captor.getValue();

        assertEquals(
                OtpDeliveryStatus.PENDING,
                saved.getDeliveryStatus());

        assertEquals(now, saved.getCreatedAt());
        assertEquals(now.plusSeconds(300),
                saved.getExpiresAt());

        assertEquals(
                crypto.digest(challengeId, CODE),
                saved.getOtpDigest());
    }

    @Test
    void rejectsRequestDuringCooldown() {
        availableUser();

        var previous = new PhoneOtpChallenge(
                userId,
                PHONE,
                crypto.digest(UUID.randomUUID(), CODE),
                now.minusSeconds(30),
                now.plusSeconds(270)
        );

        when(challenges.findTopByUserIdOrderByCreatedAtDesc(
                userId))
                .thenReturn(Optional.of(previous));

        var result = service.reserve(
                userId, PHONE, CODE, challengeId);

        assertEquals(
                PhoneOtpIssuanceResult.Status.COOLDOWN,
                result.status());

        assertEquals(30, result.retryAfterSeconds());

        verify(challenges, never()).saveAndFlush(any());
    }

    @Test
    void allowsRequestAtExactCooldownBoundary() {
        availableUser();

        var previous = new PhoneOtpChallenge(
                userId,
                PHONE,
                crypto.digest(UUID.randomUUID(), CODE),
                now.minusSeconds(60),
                now.plusSeconds(240)
        );
        previous.markSent(
                previous.getCreatedAt().plusSeconds(1)
        );

        when(challenges.findTopByUserIdOrderByCreatedAtDesc(
                userId))
                .thenReturn(Optional.of(previous));

        when(challenges.findActiveForUpdate(
                eq(userId), anyCollection()))
                .thenReturn(List.of(previous));

        var result = service.reserve(
                userId, PHONE, CODE, challengeId);

        assertEquals(
                PhoneOtpIssuanceResult.Status.RESERVED,
                result.status());

        assertEquals(
                OtpDeliveryStatus.INVALIDATED,
                previous.getDeliveryStatus());


    }

    @Test
    void rejectsPhoneAlreadyUsedByAnotherUser() {
        availableUser();

        when(users.existsByPhoneNumberAndIdNot(
                PHONE, userId))
                .thenReturn(true);

        var result = service.reserve(
                userId, PHONE, CODE, challengeId);

        assertEquals(
                PhoneOtpIssuanceResult.Status.PHONE_IN_USE,
                result.status());

        verify(challenges, never()).saveAndFlush(any());
    }

    @Test
    void rejectsUnknownUser() {
        when(users.findForPhoneEnrollment(userId))
                .thenReturn(Optional.empty());

        var result = service.reserve(
                userId, PHONE, CODE, challengeId);

        assertEquals(
                PhoneOtpIssuanceResult.Status.USER_NOT_AVAILABLE,
                result.status());

        verify(challenges, never()).saveAndFlush(any());
    }

    @Test
    void rejectsInvalidPhoneNumber() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.reserve(
                        userId,
                        "9876543210",
                        CODE,
                        challengeId)
        );

        verifyNoInteractions(users, challenges);
    }

    @Test
    void rejectsInvalidOtpFormat() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.reserve(
                        userId,
                        PHONE,
                        "123",
                        challengeId)
        );

        verifyNoInteractions(users, challenges);
    }


}
