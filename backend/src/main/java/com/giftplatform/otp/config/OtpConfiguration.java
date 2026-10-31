
package com.giftplatform.otp.config;

import com.giftplatform.otp.service.OtpCryptoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.util.Base64;

@Configuration
public class OtpConfiguration {

    @Bean
    public Clock otpClock() {
        return Clock.systemUTC();
    }

    @Bean
    public OtpCryptoService otpCryptoService(
            @Value("${gift.otp.secret-base64:}")
            String encodedSecret,
            @Value("${gift.otp.hmac-key-base64:}") String contactSigningKey
    ) {
        if(encodedSecret.isBlank())encodedSecret=contactSigningKey;
        byte[] secret;

        try {
            if(encodedSecret.isBlank()){
                // Legacy OTP endpoints are gated globally. No persistent challenge should use this
                // process-local key; production enablement requires a configured signing key.
                secret=new byte[32];new java.security.SecureRandom().nextBytes(secret);
                return new OtpCryptoService(secret);
            }
            secret = Base64.getDecoder()
                    .decode(encodedSecret);
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException(
                    "Invalid OTP secret configuration", ex);
        }

        return new OtpCryptoService(secret);
    }
}
