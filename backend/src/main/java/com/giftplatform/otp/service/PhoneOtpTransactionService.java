package com.giftplatform.otp.service;

import com.giftplatform.otp.entity.PhoneOtpChallenge;
import com.giftplatform.otp.repository.PhoneOtpChallengeRepository;
import com.giftplatform.user.entity.User;
import com.giftplatform.user.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class PhoneOtpTransactionService {

    private final UserRepository users;
    private final PhoneOtpChallengeRepository challenges;
    private final OtpCryptoService crypto;
    private final Clock clock;

    public PhoneOtpTransactionService(
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
    public PhoneOtpVerificationResult verify(
            UUID userId,
            UUID challengeId,
            String code
    ) {
        if (userId == null || challengeId == null) {
            return PhoneOtpVerificationResult.NOT_AVAILABLE;
        }

        User user = users.findForPhoneEnrollment(userId).orElse(null);
        if (user == null || !user.isEnabled()) {
            return PhoneOtpVerificationResult.NOT_AVAILABLE;
        }

        PhoneOtpChallenge challenge = challenges
                .findForVerification(challengeId, userId)
                .orElse(null);

        if (challenge == null) {
            return PhoneOtpVerificationResult.NOT_AVAILABLE;
        }

        return verifyLockedChallenge(user, challenge, code);
    }

    @Transactional
    public PhoneOtpVerificationResult verifyLatest(UUID userId, String code) {
        if (userId == null) {
            return PhoneOtpVerificationResult.NOT_AVAILABLE;
        }

        User user = users.findForPhoneEnrollment(userId).orElse(null);
        if (user == null || !user.isEnabled()) {
            return PhoneOtpVerificationResult.NOT_AVAILABLE;
        }

        var latest = challenges.findLatestForVerification(
                userId, PageRequest.of(0, 1));

        if (latest.isEmpty()) {
            return PhoneOtpVerificationResult.NOT_AVAILABLE;
        }

        return verifyLockedChallenge(user, latest.get(0), code);
    }

    private PhoneOtpVerificationResult verifyLockedChallenge(
            User user,
            PhoneOtpChallenge challenge,
            String code
    ) {
        Instant now = clock.instant();

        if (challenge.isConsumed()) {
            return PhoneOtpVerificationResult.ALREADY_USED;
        }
        if (challenge.isExpired(now)) {
            return PhoneOtpVerificationResult.EXPIRED;
        }
        if (!challenge.isReadyForVerification()) {
            return PhoneOtpVerificationResult.NOT_AVAILABLE;
        }
        if (!challenge.hasAttemptsRemaining()) {
            return PhoneOtpVerificationResult.MAX_ATTEMPTS;
        }
        if (!crypto.matches(challenge.getId(), code, challenge.getOtpDigest())) {
            challenge.recordFailedAttempt();
            return PhoneOtpVerificationResult.INVALID_CODE;
        }
        if (users.existsByPhoneNumberAndIdNot(
                challenge.getPhoneNumber(), user.getId())) {
            return PhoneOtpVerificationResult.NOT_AVAILABLE;
        }

        user.verifyPhoneNumber(challenge.getPhoneNumber(), now);
        challenge.consume(now);
        return PhoneOtpVerificationResult.VERIFIED;
    }
}
