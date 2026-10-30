
package com.giftplatform.otp.service;

public enum PhoneOtpVerificationResult {
    VERIFIED,
    INVALID_CODE,
    EXPIRED,
    ALREADY_USED,
    MAX_ATTEMPTS,
    NOT_AVAILABLE
}
