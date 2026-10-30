package com.giftplatform.security.recovery;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import java.nio.file.*;
import java.util.Properties;
import static org.junit.jupiter.api.Assertions.*;

class RecoverySigningConfigurationTest {
    private String expression() throws Exception {
        var properties=new Properties();
        try(var stream=Files.newInputStream(Path.of("src/main/resources/application.properties"))){properties.load(stream);}
        return properties.getProperty("gift.otp.hmac-key-base64");
    }
    @Test void recoveryUsesExistingOtpSecretWhenDedicatedKeyIsAbsent() throws Exception {
        var env=new MockEnvironment().withProperty("gift.otp.secret-base64","existing-secret");
        assertEquals("existing-secret",env.resolveRequiredPlaceholders(expression()));
    }
    @Test void explicitRecoveryKeyKeepsPriority() throws Exception {
        var env=new MockEnvironment().withProperty("gift.otp.secret-base64","existing-secret").withProperty("GIFT_OTP_HMAC_KEY_BASE64","dedicated-secret");
        assertEquals("dedicated-secret",env.resolveRequiredPlaceholders(expression()));
    }
}
