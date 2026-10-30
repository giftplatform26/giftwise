
package com.giftplatform.otp.service;

public class OtpSmsRejectedException extends RuntimeException {

    public OtpSmsRejectedException(String message) {
        super(message);
    }
}
