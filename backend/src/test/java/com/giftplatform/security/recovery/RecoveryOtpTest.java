package com.giftplatform.security.recovery;

import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class RecoveryOtpTest {
    private final byte[] key = new byte[32];
    @Test void codeIsSixDigits() { for (int i=0;i<200;i++) assertTrue(RecoveryOtp.generate().matches("[0-9]{6}")); }
    @Test void bindsAccountPurposeAndDestination() {
        UUID challenge=UUID.randomUUID(), account=UUID.randomUUID();
        String hash=RecoveryOtp.fingerprint(key,challenge,"HOST",account,"RESET_PASSWORD","a@example.com","012345");
        assertTrue(RecoveryOtp.matches(hash,RecoveryOtp.fingerprint(key,challenge,"HOST",account,"RESET_PASSWORD","a@example.com","012345")));
        assertFalse(RecoveryOtp.matches(hash,RecoveryOtp.fingerprint(key,challenge,"HOST",account,"RESET_PIN","a@example.com","012345")));
        assertFalse(RecoveryOtp.matches(hash,RecoveryOtp.fingerprint(key,challenge,"HOST",account,"RESET_PASSWORD","b@example.com","012345")));
        assertFalse(RecoveryOtp.matches(hash,RecoveryOtp.fingerprint(key,challenge,"HOST",UUID.randomUUID(),"RESET_PASSWORD","a@example.com","012345")));
        assertFalse(RecoveryOtp.matches(hash,RecoveryOtp.fingerprint(key,challenge,"HOST",account,"RESET_PASSWORD","a@example.com","012346")));
    }
    @Test void expiryConsumptionAndAttempts() {
        Clock clock=Clock.fixed(Instant.parse("2026-10-26T00:00:00Z"),ZoneOffset.UTC);
        Instant future=Instant.now(clock).plusSeconds(300);
        assertTrue(RecoveryOtp.usable(future,null,0,5,clock));
        assertFalse(RecoveryOtp.usable(future,Instant.now(clock),0,5,clock));
        assertFalse(RecoveryOtp.usable(future,null,5,5,clock));
        assertFalse(RecoveryOtp.usable(Instant.now(clock),null,0,5,clock));
    }
    @Test void requiresStrongKey() { assertThrows(IllegalArgumentException.class,()->RecoveryOtp.fingerprint(new byte[8],UUID.randomUUID(),"HOST",UUID.randomUUID(),"RESET_PASSWORD","x","123456")); }
}
