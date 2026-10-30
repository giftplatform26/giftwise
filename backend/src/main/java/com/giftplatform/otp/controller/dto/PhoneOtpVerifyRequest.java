package com.giftplatform.otp.controller.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record PhoneOtpVerifyRequest(
        @NotNull
        @Pattern(regexp = "^[0-9]{6}$")
        String otp
) {
}