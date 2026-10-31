package com.giftplatform.security.delivery;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import java.util.Arrays;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.LinkOption;
import java.nio.charset.StandardCharsets;
import java.nio.file.attribute.PosixFilePermissions;
import java.io.IOException;
import java.util.UUID;

@Configuration
public class SmsDeliveryConfiguration {
    @Bean("otpSmsDelivery")
    MessageDelivery smsDelivery(
            @Value("${gift.otp.sms.provider:disabled}") String provider,
            @Value("${gift.otp.sms.simulator.enabled:false}") boolean simulatorEnabled,
            Environment environment) {
        if ("disabled".equals(provider)) return new MessageDelivery(){
            public boolean isAvailable(){return false;}
            public void send(String to,String body){throw new IllegalStateException("SMS OTP delivery is not configured");}
        };
        if ("msg91".equals(provider)) return new Msg91SmsDelivery(
            environment.getProperty("gift.otp.sms.msg91.auth-key",""),
            environment.getProperty("gift.otp.sms.msg91.template-id",""),
            environment.getProperty("gift.otp.sms.expiry-minutes",Integer.class,5));
        if ("simulator".equals(provider)) {
            boolean localProfile = Arrays.stream(environment.getActiveProfiles()).anyMatch("local"::equals);
            if (!localProfile || !simulatorEnabled) throw new IllegalStateException("SMS simulator requires explicit local profile and opt-in");
            // Development only. Messages go to a private local inbox, never to logs or an HTTP endpoint.
            return (to, body) -> {
                String folder = environment.getProperty("gift.otp.sms.simulator.inbox-dir", "");
                if (folder.isBlank()) throw new IllegalStateException("Local SMS inbox directory is not configured");
                try {
                    Path dir = Path.of(folder).toAbsolutePath().normalize();
                    if (!Files.isDirectory(dir, LinkOption.NOFOLLOW_LINKS) || Files.isSymbolicLink(dir))
                        throw new IllegalStateException("SMS inbox must be an existing non-symlink directory");
                    // Directory must be private; fail closed on filesystems without POSIX permissions.
                    var permissions = Files.getPosixFilePermissions(dir);
                    if (!permissions.equals(PosixFilePermissions.fromString("rwx------")))
                        throw new IllegalStateException("SMS inbox directory permissions must be 0700");
                    Path file = dir.resolve("sms-" + UUID.randomUUID() + ".txt");
                    Files.writeString(file, "TO: " + to + "\n" + body + "\n", StandardCharsets.UTF_8,
                        java.nio.file.StandardOpenOption.CREATE_NEW, java.nio.file.StandardOpenOption.WRITE);
                    Files.setPosixFilePermissions(file, PosixFilePermissions.fromString("rw-------"));
                } catch (IOException ex) { throw new IllegalStateException("Local SMS inbox write failed", ex); }
            };
        }
        // An MSG91 adapter can be selected when implemented and tested; never pretend delivery succeeded.
        throw new IllegalStateException("Unsupported SMS OTP provider: " + provider);
    }
}
