package com.giftplatform.hostpayment.verification;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(
    name = "gift.payment.profiles.enabled",
    havingValue = "true"
)
public class HostDestinationVerifierConfiguration {

    @Bean
    HostDestinationVerifier hostDestinationVerifier() {
        return new DisabledHostDestinationVerifier();
    }
}
