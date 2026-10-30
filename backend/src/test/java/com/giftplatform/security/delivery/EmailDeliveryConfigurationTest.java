package com.giftplatform.security.delivery;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EmailDeliveryConfigurationTest {
    private final EmailDeliveryConfiguration configuration=new EmailDeliveryConfiguration();
    @Test void disabledDeliveryReportsUnavailableAndStillRejectsSending(){
        var reminder=configuration.usernameReminderDelivery("disabled","","");
        assertFalse(reminder.isAvailable());
        assertThrows(IllegalStateException.class,()->reminder.send("recipient@example.test","message"));
        var otp=configuration.emailDelivery("disabled","","");
        assertFalse(otp.isAvailable());
        assertThrows(IllegalStateException.class,()->otp.send("recipient@example.test","message"));
    }
    @Test void configuredProviderReportsLocalReadinessWithoutSendingEmail(){
        assertTrue(configuration.usernameReminderDelivery("resend","test-key","sender@example.test").isAvailable());
    }
    @Test void enablingProviderWithoutCredentialsFailsConfiguration(){
        assertThrows(IllegalStateException.class,()->configuration.usernameReminderDelivery("resend","",""));
    }
    @Test void domainErrorIsActionableWithoutExposingProviderBody(){
        var failure=EmailDeliveryException.rejected(403,"domain not verified; private@example.test; secret-provider-value");
        assertTrue(failure.getMessage().contains("sender domain is not verified"));
        assertFalse(failure.getMessage().contains("private@example.test"));
        assertFalse(failure.getMessage().contains("secret-provider-value"));
    }
    @Test void otherProviderErrorsUseSafeExplanations(){
        assertTrue(EmailDeliveryException.rejected(401,"private").getMessage().contains("API key"));
        assertTrue(EmailDeliveryException.rejected(429,"private").getMessage().contains("rate limited"));
        assertFalse(EmailDeliveryException.rejected(500,"private").getMessage().contains("private"));
    }
}
