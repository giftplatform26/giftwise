package com.giftplatform.hostpayment.security;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class PaymentDataProtectorTest {
    private static PaymentDataProtector protector() {
        byte[] a = new byte[32];
        byte[] b = new byte[32];
        java.util.Arrays.fill(a, (byte) 1);
        java.util.Arrays.fill(b, (byte) 2);
        return new PaymentDataProtector(a, b);
    }

    @Test void roundTripAndRandomNonces() {
        var p = protector(); var host = UUID.randomUUID(); var id = UUID.randomUUID();
        String one = p.encrypt(host, id, "upi_id", "name@bank");
        String two = p.encrypt(host, id, "upi_id", "name@bank");
        assertNotEquals(one, two);
        assertEquals("name@bank", p.decrypt(host, id, "upi_id", one));
        assertFalse(one.contains("name@bank"));
    }

    @Test void crossHostCrossRecordAndCrossFieldDecryptDenied() {
        var p = protector(); var host = UUID.randomUUID(); var id = UUID.randomUUID();
        String encrypted = p.encrypt(host, id, "account_number", "1234567890");
        assertThrows(IllegalArgumentException.class, () -> p.decrypt(UUID.randomUUID(), id, "account_number", encrypted));
        assertThrows(IllegalArgumentException.class, () -> p.decrypt(host, UUID.randomUUID(), "account_number", encrypted));
        assertThrows(IllegalArgumentException.class, () -> p.decrypt(host, id, "upi_id", encrypted));
    }

    @Test void tamperingDenied() {
        var p = protector(); var host = UUID.randomUUID(); var id = UUID.randomUUID();
        String encrypted = p.encrypt(host, id, "upi_id", "name@bank");
        byte[] bytes = java.util.Base64.getDecoder().decode(encrypted.substring(3));
        bytes[bytes.length - 1] ^= 1;
        String tampered = "v1:" + java.util.Base64.getEncoder().encodeToString(bytes);
        assertThrows(IllegalArgumentException.class, () -> p.decrypt(host, id, "upi_id", tampered));
    }

    @Test void fingerprintsAreDeterministicAndDomainSeparated() {
        var p = protector();
        assertEquals(p.fingerprint("UPI", "name@bank"), p.fingerprint("UPI", "name@bank"));
        assertNotEquals(p.fingerprint("UPI", "name@bank"), p.fingerprint("BANK", "name@bank"));
        assertEquals(64, p.fingerprint("UPI", "name@bank").length());
    }

    @Test void validationAndMasking() {
        assertEquals("name@bank", PaymentDataProtector.normalizeUpi(" Name@Bank "));
        assertEquals("n***@bank", PaymentDataProtector.maskUpi("name@bank"));
        assertEquals("123****890", PaymentDataProtector.maskAccount("1234567890"));
        assertEquals("HDFC0001234", PaymentDataProtector.normalizeIfsc("hdfc0001234"));
        assertThrows(IllegalArgumentException.class, () -> PaymentDataProtector.normalizeAccountNumber("1234"));
        assertThrows(IllegalArgumentException.class, () -> PaymentDataProtector.normalizeUpi("bad upi"));
        assertThrows(IllegalArgumentException.class, () -> new PaymentDataProtector(new byte[31], new byte[32]));
    }
}
