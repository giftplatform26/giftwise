package com.giftplatform.otp.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PhoneOtpRequest(
        @NotBlank
        @Pattern(regexp = "^\\+[1-9][0-9]{1,14}$")
        String phoneNumber
) {
}