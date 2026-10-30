
package com.giftplatform.otp.entity;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class PhoneOtpChallengeTest {

    private PhoneOtpChallenge challenge(
            Instant createdAt,
            Instant expiresAt
    ) {
        return new PhoneOtpChallenge(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "+919876543210",
                "test-digest",
                createdAt,
                expiresAt
        );
    }

    @Test
    void shouldPreserveProvidedChallengeId() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        PhoneOtpChallenge challenge =
                new PhoneOtpChallenge(
                        id,
                        UUID.randomUUID(),
                        "+919876543210",
                        "test-digest",
                        now,
                        now.plusSeconds(300)
                );

        assertThat(challenge.getId()).isEqualTo(id);
    }

    @Test
    void shouldRejectNullChallengeId() {
        Instant now = Instant.now();

        assertThatThrownBy(() ->
                new PhoneOtpChallenge(
                        null,
                        UUID.randomUUID(),
                        "+919876543210",
                        "test-digest",
                        now,
                        now.plusSeconds(300)
                )
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldNotBeExpiredBeforeExpiry() {
        Instant now = Instant.now();

        assertThat(challenge(
                now,
                now.plusSeconds(300)
        ).isExpired(now)).isFalse();
    }

    @Test
    void shouldExpireAtExactExpiryTime() {
        Instant now = Instant.now();

        assertThat(challenge(
                now,
                now.plusSeconds(300)
        ).isExpired(now.plusSeconds(300))).isTrue();
    }

    @Test
    void shouldRejectSecondConsumption() {
        Instant now = Instant.now();

        PhoneOtpChallenge challenge =
                challenge(now, now.plusSeconds(300));
        challenge.markSent(challenge.getCreatedAt().plusSeconds(1));
        challenge.consume(now.plusSeconds(10));

        assertThatThrownBy(() ->
                challenge.consume(now.plusSeconds(20))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldRejectConsumptionAfterFiveFailures() {
        Instant now = Instant.now();

        PhoneOtpChallenge challenge =
                challenge(now, now.plusSeconds(300));

        for (int i = 0; i < 5; i++) {
            challenge.recordFailedAttempt();
        }

        assertThatThrownBy(() ->
                challenge.consume(now.plusSeconds(10))
        ).isInstanceOf(IllegalStateException.class);
    }
}
