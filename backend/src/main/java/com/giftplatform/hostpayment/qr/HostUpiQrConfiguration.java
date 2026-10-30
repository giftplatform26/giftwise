package com.giftplatform.hostpayment.qr;

import com.giftplatform.hostpayment.repository.HostUpiAccountRepository;
import com.giftplatform.hostpayment.security.PaymentDataProtector;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@ConditionalOnProperty(prefix="gift.payment.profiles",name="enabled",havingValue="true")
public class HostUpiQrConfiguration {
    @Bean RegisteredUpiResolver registeredUpiResolver(HostUpiAccountRepository accounts,PaymentDataProtector protector) {
        return new JpaRegisteredUpiResolver(accounts,protector);
    }
    @Bean UpiQrImageStorage upiQrImageStorage(JdbcTemplate jdbc) {
        return new JdbcUpiQrImageStorage(jdbc);
    }
    @Bean UpiQrImageService upiQrImageService(RegisteredUpiResolver resolver,UpiQrImageStorage storage) {
        return new UpiQrImageService(resolver,storage);
    }
}
