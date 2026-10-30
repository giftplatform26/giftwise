
package com.giftplatform.otp.service;

import com.giftplatform.otp.entity.OtpDeliveryStatus;
import com.giftplatform.otp.entity.PhoneOtpChallenge;
import com.giftplatform.otp.repository.PhoneOtpChallengeRepository;
import com.giftplatform.user.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class PhoneOtpIssuanceTransactionService {

    private static final Duration OTP_LIFETIME =
            Duration.ofMinutes(5);

    private static final Duration RESEND_COOLDOWN =
            Duration.ofSeconds(60);

    private final UserRepository users;
    private final PhoneOtpChallengeRepository challenges;
    private final OtpCryptoService crypto;
    private final Clock clock;
    private static final Duration PENDING_TIMEOUT =
            Duration.ofMinutes(2);
    public PhoneOtpIssuanceTransactionService(
            UserRepository users,
            PhoneOtpChallengeRepository challenges,
            OtpCryptoService crypto,
            Clock clock
    ) {
        this.users = users;
        this.challenges = challenges;
        this.crypto = crypto;
        this.clock = clock;
    }

    @Transactional
    public PhoneOtpIssuanceResult reserve(
            UUID userId,
            String phoneNumber,
            String otp,
            UUID challengeId
    ) {
        if (userId == null || challengeId == null
                || phoneNumber == null
                || !phoneNumber.matches(
                "^\\+[1-9][0-9]{1,14}$")
                || otp == null
                || !otp.matches("[0-9]{6}")) {
            throw new IllegalArgumentException(
                    "Invalid OTP reservation input");
        }

        // Serializes issuance requests for the same user.
        var user = users.findForPhoneEnrollment(userId);

        if (user.isEmpty() || !user.get().isEnabled()) {
            return PhoneOtpIssuanceResult.unavailable();
        }

        Instant now = clock.instant();

        if (users.existsByPhoneNumberAndIdNot(
                phoneNumber, userId)) {
            return PhoneOtpIssuanceResult.phoneInUse();
        }
        var latest = challenges
                .findTopByUserIdOrderByCreatedAtDesc(userId);

        if (latest.isPresent()) {
            Instant nextAllowed = latest.get()
                    .getCreatedAt()
                    .plus(RESEND_COOLDOWN);

            if (now.isBefore(nextAllowed)) {
                long remaining = Duration.between(
                        now, nextAllowed).getSeconds();

                return PhoneOtpIssuanceResult.cooldown(
                        Math.max(1, remaining));
            }
        }
        List<PhoneOtpChallenge> active =
                challenges.findActiveForUpdate(
                        userId,
                        List.of(
                                OtpDeliveryStatus.PENDING,
                                OtpDeliveryStatus.SENT
                        )
                );

// Do not replace an OTP while SMS delivery
// is still pending.
        boolean pendingDelivery = false;

        for (PhoneOtpChallenge previous : active) {
            if (previous.getDeliveryStatus()
                    != OtpDeliveryStatus.PENDING) {
                continue;
            }

            Instant staleAt = previous.getCreatedAt()
                    .plus(PENDING_TIMEOUT);

            if (now.isBefore(staleAt)) {
                pendingDelivery = true;
            } else {
                // The dispatch outcome is unknown.
                // Invalidate rather than mark SENT or FAILED.
                previous.invalidate(now);
            }
        }

        if (pendingDelivery) {
            return PhoneOtpIssuanceResult.deliveryInProgress();
        }

// Invalidate previous active OTPs.
        for (PhoneOtpChallenge previous : active) {
            previous.invalidate(now);
        }

// Ensure PostgreSQL processes invalidations
// before inserting the replacement challenge.
        challenges.flush();

        var challenge = new PhoneOtpChallenge(
                challengeId,
                userId,
                phoneNumber,
                crypto.digest(challengeId, otp),
                now,
                now.plus(OTP_LIFETIME)
        );

        challenges.saveAndFlush(challenge);

        return PhoneOtpIssuanceResult.reserved(
                challengeId);
    }


}
