
package com.giftplatform.otp.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        prefix = "gift.sms",
        name = "provider",
        havingValue = "disabled",
        matchIfMissing = true
)
public class DisabledSmsSender implements SmsSender {

    @Override
    public void sendVerificationCode(
            String phoneNumber,
            String otp
    ) {
        throw new IllegalStateException(
                "SMS delivery is not configured"
        );
    }
}
