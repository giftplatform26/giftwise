package com.giftplatform.security.delivery;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import static org.junit.jupiter.api.Assertions.*;

class DeliveryConfigurationTest {
    @Test void disabledEmailFailsClosed() {
        MessageDelivery sender = new EmailDeliveryConfiguration().emailDelivery("disabled", "", "");
        assertThrows(IllegalStateException.class, () -> sender.send("person@example.com", "code"));
    }
    @Test void resendRequiresCredentials() {
        assertFalse(new EmailDeliveryConfiguration().emailDelivery("resend", "", "").isAvailable());
    }
    @Test void smsSimulatorIsNotAvailableOutsideLocal() {
        assertThrows(IllegalStateException.class, () -> new SmsDeliveryConfiguration().smsDelivery("simulator", true, new MockEnvironment()));
    }
    @Test void simulatorNeverClaimsRealDelivery() {
        MockEnvironment env = new MockEnvironment(); env.setActiveProfiles("local");
        MessageDelivery sender = new SmsDeliveryConfiguration().smsDelivery("simulator", true, env);
        assertThrows(IllegalStateException.class, () -> sender.send("+919876543210", "code"));
    }
    @Test void unknownProviderFailsStartup() {
        assertThrows(IllegalStateException.class, () -> new SmsDeliveryConfiguration().smsDelivery("unknown", false, new MockEnvironment()));
    }
}
