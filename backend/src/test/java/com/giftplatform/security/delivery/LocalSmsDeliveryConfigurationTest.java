package com.giftplatform.security.delivery;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.env.MockEnvironment;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import static org.junit.jupiter.api.Assertions.*;

class LocalSmsDeliveryConfigurationTest {
    @TempDir Path temp;
    private final SmsDeliveryConfiguration config = new SmsDeliveryConfiguration();
    @Test void disabledProviderFailsClosed() {
        MessageDelivery delivery = config.smsDelivery("disabled", false, new MockEnvironment());
        assertThrows(IllegalStateException.class, () -> delivery.send("+919999999999", "123456"));
    }
    @Test void simulatorRequiresLocalProfile() {
        MockEnvironment env = new MockEnvironment();
        assertThrows(IllegalStateException.class, () -> config.smsDelivery("simulator", true, env));
    }
    @Test void simulatorRequiresExplicitOptIn() {
        MockEnvironment env = new MockEnvironment().withProperty("gift.otp.sms.simulator.inbox-dir", temp.toString());
        env.setActiveProfiles("local");
        assertThrows(IllegalStateException.class, () -> config.smsDelivery("simulator", false, env));
    }
    @Test void localSimulatorWritesPrivateInboxFile() throws Exception {
        Files.setPosixFilePermissions(temp, PosixFilePermissions.fromString("rwx------"));
        MockEnvironment env = new MockEnvironment().withProperty("gift.otp.sms.simulator.inbox-dir", temp.toString());
        env.setActiveProfiles("local");
        config.smsDelivery("simulator", true, env).send("+919999999999", "Your development code is 123456");
        try (var files = Files.list(temp)) {
            Path message = files.findFirst().orElseThrow();
            assertTrue(Files.readString(message).contains("123456"));
            assertEquals(PosixFilePermissions.fromString("rw-------"), Files.getPosixFilePermissions(message));
        }
    }
    @Test void insecureInboxIsRejected() throws Exception {
        Files.setPosixFilePermissions(temp, PosixFilePermissions.fromString("rwxr-xr-x"));
        MockEnvironment env = new MockEnvironment().withProperty("gift.otp.sms.simulator.inbox-dir", temp.toString());
        env.setActiveProfiles("local");
        MessageDelivery delivery = config.smsDelivery("simulator", true, env);
        assertThrows(IllegalStateException.class, () -> delivery.send("+919999999999", "123456"));
    }
}
