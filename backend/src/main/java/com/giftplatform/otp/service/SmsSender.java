
package com.giftplatform.otp.service;

public interface SmsSender {

    void sendVerificationCode(
            String phoneNumber,
            String otp
    );
}
