package com.giftplatform.hostpayment.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import java.util.Base64;

@Configuration
@ConditionalOnProperty(name = "gift.payment.profiles.enabled", havingValue = "true")
public class PaymentDataProtectionConfig {
    @Bean
    PaymentDataProtector paymentDataProtector(Environment environment) {
        String encryption = environment.getProperty("gift.payment.encryption-key-base64");
        String fingerprint = environment.getProperty("gift.payment.fingerprint-key-base64");
        if (encryption == null || fingerprint == null) {
            // Fail closed: production should refuse to start until secrets are provisioned.
            throw new IllegalStateException("Missing gift payment encryption/fingerprint secrets");
        }
        try {
            return new PaymentDataProtector(Base64.getDecoder().decode(encryption), Base64.getDecoder().decode(fingerprint));
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Invalid payment protection key configuration", e);
        }
    }
}
