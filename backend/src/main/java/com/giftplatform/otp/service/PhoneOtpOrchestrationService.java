
package com.giftplatform.otp.service;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PhoneOtpOrchestrationService {

    private final OtpCryptoService crypto;
    private final PhoneOtpIssuanceTransactionService issuance;
    private final PhoneOtpDeliveryTransactionService delivery;
    private final SmsSender smsSender;
    private final OtpRequestRateLimiter rateLimiter;

    public PhoneOtpOrchestrationService(
            OtpCryptoService crypto,
            PhoneOtpIssuanceTransactionService issuance,
            PhoneOtpDeliveryTransactionService delivery,
            SmsSender smsSender,
            OtpRequestRateLimiter rateLimiter
    ) {
        this.crypto = crypto;
        this.issuance = issuance;
        this.delivery = delivery;
        this.smsSender = smsSender;
        this.rateLimiter = rateLimiter;
    }


    public PhoneOtpIssuanceResult requestOtp(
            UUID userId,
            String phoneNumber
    ) {
        var quota = rateLimiter.acquire(userId, phoneNumber);
        if (!quota.allowed()) {
            return PhoneOtpIssuanceResult.cooldown(quota.retryAfterSeconds());
        }

        String otp = crypto.generateOtp();
        UUID challengeId = UUID.randomUUID();

        PhoneOtpIssuanceResult result = issuance.reserve(
                userId,
                phoneNumber,
                otp,
                challengeId
        );

        if (result.status()
                != PhoneOtpIssuanceResult.Status.RESERVED) {
            return result;
        }

        try {
            smsSender.sendVerificationCode(phoneNumber, otp);

        } catch (OtpSmsRejectedException rejected) {
            // Explicit rejection: provider did not accept the SMS.
            if (!delivery.markFailed(userId, challengeId)) {
                throw new OtpDeliveryUncertainException(
                        "OTP delivery state could not be finalized",
                        rejected
                );
            }

            throw new OtpDispatchException(
                    "OTP dispatch was rejected",
                    rejected
            );

        } catch (RuntimeException uncertain) {
            // Timeout, network failure, or ambiguous provider outcome.
            // Keep PENDING for bounded stale-challenge recovery.
            throw new OtpDeliveryUncertainException(
                    "OTP delivery outcome is unknown",
                    uncertain
            );
        }

        // Provider accepted the SMS request.
        // This is not proof of delivery to the handset.
        try {
            if (!delivery.markSent(userId, challengeId)) {
                throw new OtpSmsRejectedException(
                        "SMS delivery is not configured"
                );
            }
        } catch (RuntimeException finalizationFailure) {
            // Do not mark FAILED: SMS may already have been sent.
            throw new OtpDeliveryUncertainException(
                    "OTP delivery state could not be finalized",
                    finalizationFailure
            );
        }

        return result;
    }

}
