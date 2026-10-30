package com.giftplatform.otp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "gift.sms", name = "provider", havingValue = "sns")
public class AwsSnsSmsConfiguration {
    @Bean(destroyMethod = "close")
    SnsClient giftSnsClient(@Value("${gift.sms.aws.region:ap-south-1}") String region) {
        if (region == null || region.isBlank()) {
            throw new IllegalArgumentException("SNS region must be configured");
        }
        return SnsClient.builder().region(Region.of(region.trim())).build();
    }
}
