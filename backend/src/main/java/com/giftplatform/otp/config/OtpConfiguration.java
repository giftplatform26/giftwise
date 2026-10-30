
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
            @Value("${gift.otp.secret-base64}")
            String encodedSecret
    ) {
        byte[] secret;

        try {
            secret = Base64.getDecoder()
                    .decode(encodedSecret);
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException(
                    "Invalid OTP secret configuration", ex);
        }

        return new OtpCryptoService(secret);
    }
}
