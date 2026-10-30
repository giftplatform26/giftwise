package com.giftplatform.otp.controller.dto;

import java.util.UUID;

public record PhoneOtpResponse(
        String status,
        UUID challengeId,
        long retryAfterSeconds
) {
    public static PhoneOtpResponse of(String status) {
        return new PhoneOtpResponse(status, null, 0);
    }
}