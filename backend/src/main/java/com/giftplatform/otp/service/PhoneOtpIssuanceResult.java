
package com.giftplatform.otp.service;

import java.util.UUID;

public record PhoneOtpIssuanceResult(
        Status status,
        UUID challengeId,
        long retryAfterSeconds
) {
    public enum Status {
        RESERVED,
        COOLDOWN,
        PHONE_IN_USE,
        USER_NOT_AVAILABLE,
        DELIVERY_IN_PROGRESS
    }

    public static PhoneOtpIssuanceResult reserved(UUID id) {
        return new PhoneOtpIssuanceResult(
                Status.RESERVED, id, 0);
    }

    public static PhoneOtpIssuanceResult cooldown(long seconds) {
        return new PhoneOtpIssuanceResult(
                Status.COOLDOWN, null, seconds);
    }

    public static PhoneOtpIssuanceResult phoneInUse() {
        return new PhoneOtpIssuanceResult(
                Status.PHONE_IN_USE, null, 0);
    }

    public static PhoneOtpIssuanceResult unavailable() {
        return new PhoneOtpIssuanceResult(
                Status.USER_NOT_AVAILABLE, null, 0);
    }

    public static PhoneOtpIssuanceResult deliveryInProgress() {
        return new PhoneOtpIssuanceResult(
                Status.DELIVERY_IN_PROGRESS, null, 0);
    }
}
