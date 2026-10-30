
package com.giftplatform.otp.service;

import com.giftplatform.otp.entity.PhoneOtpChallenge;
import com.giftplatform.otp.repository.PhoneOtpChallengeRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

@Service
public class PhoneOtpDeliveryTransactionService {

    private final PhoneOtpChallengeRepository challenges;
    private final Clock clock;

    public PhoneOtpDeliveryTransactionService(
            PhoneOtpChallengeRepository challenges,
            Clock clock
    ) {
        this.challenges = challenges;
        this.clock = clock;
    }

    @Transactional
    public boolean markSent(UUID userId, UUID challengeId) {
        return transition(userId, challengeId, true);
    }

    @Transactional
    public boolean markFailed(UUID userId, UUID challengeId) {
        return transition(userId, challengeId, false);
    }

    private boolean transition(
            UUID userId,
            UUID challengeId,
            boolean sent
    ) {
        PhoneOtpChallenge challenge = challenges
                .findForVerification(challengeId, userId)
                .orElse(null);

        if (challenge == null
                || challenge.getDeliveryStatus()
                != com.giftplatform.otp.entity.OtpDeliveryStatus.PENDING) {
            return false;
        }

        if (sent) {
            challenge.markSent(clock.instant());
        } else {
            challenge.markFailed(clock.instant());
        }

        return true;
    }
}
