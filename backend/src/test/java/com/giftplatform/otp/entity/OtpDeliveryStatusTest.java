
package com.giftplatform.otp.entity;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OtpDeliveryStatusTest {

    private final Instant now =
            Instant.parse("2026-10-12T05:30:00Z");

    private PhoneOtpChallenge challenge() {
        return new PhoneOtpChallenge(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "+919876543210",
                "test-digest",
                now,
                now.plusSeconds(300)
        );
    }

    @Test
    void newChallengeStartsPending() {
        var challenge = challenge();

        assertEquals(OtpDeliveryStatus.PENDING,
                challenge.getDeliveryStatus());
        assertFalse(challenge.isReadyForVerification());
    }

    @Test
    void successfulDispatchEnablesVerification() {
        var challenge = challenge();

        challenge.markSent(now.plusSeconds(1));

        assertEquals(OtpDeliveryStatus.SENT,
                challenge.getDeliveryStatus());
        assertTrue(challenge.isReadyForVerification());
    }

    @Test
    void failedDispatchCannotBeVerified() {
        var challenge = challenge();

        challenge.markFailed(now.plusSeconds(1));

        assertEquals(OtpDeliveryStatus.FAILED,
                challenge.getDeliveryStatus());
        assertFalse(challenge.isReadyForVerification());
    }

    @Test
    void cannotMarkFailedChallengeAsSent() {
        var challenge = challenge();

        challenge.markFailed(now.plusSeconds(1));

        assertThrows(IllegalStateException.class,
                () -> challenge.markSent(
                        now.plusSeconds(2)));
    }

    @Test
    void invalidationDisablesVerification() {
        var challenge = challenge();

        challenge.markSent(now.plusSeconds(1));
        challenge.invalidate(now.plusSeconds(2));

        assertEquals(OtpDeliveryStatus.INVALIDATED,
                challenge.getDeliveryStatus());
        assertFalse(challenge.isReadyForVerification());
    }

    @Test
    void pendingChallengeCannotBeConsumed() {
        var challenge = challenge();

        assertThrows(IllegalStateException.class,
                () -> challenge.consume(now.plusSeconds(1)));
    }

    @Test
    void deliveredChallengeCanBeConsumedOnce() {
        var challenge = challenge();

        challenge.markSent(now.plusSeconds(1));
        challenge.consume(now.plusSeconds(2));

        assertTrue(challenge.isConsumed());

        assertThrows(IllegalStateException.class,
                () -> challenge.consume(now.plusSeconds(3)));
    }

    @Test
    void cannotMarkSentTwice() {
        var challenge = challenge();

        challenge.markSent(now.plusSeconds(1));

        assertThrows(IllegalStateException.class,
                () -> challenge.markSent(now.plusSeconds(2)));
    }

    @Test
    void cannotInvalidateConsumedChallenge() {
        var challenge = challenge();

        challenge.markSent(now.plusSeconds(1));
        challenge.consume(now.plusSeconds(2));

        assertThrows(IllegalStateException.class,
                () -> challenge.invalidate(now.plusSeconds(3)));
    }
}
