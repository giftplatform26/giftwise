
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
    @org.springframework.beans.factory.annotation.Autowired(required=false)
    @org.springframework.beans.factory.annotation.Qualifier("otpSmsDelivery")
    private com.giftplatform.security.delivery.MessageDelivery delivery;

    @Override
    public void sendVerificationCode(
            String phoneNumber,
            String otp
    ) {
        if(delivery!=null&&delivery.isAvailable()&&delivery.establishesMobileOwnership()){
            delivery.sendCode(phoneNumber,otp);return;
        }
        throw new IllegalStateException(
                "SMS delivery is not configured"
        );
    }
}
