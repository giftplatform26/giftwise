package com.giftplatform.hostpayment.qr;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UpiQrPayloadTest {
    @Test void parsesNormalQr() {
        var p=UpiQrPayload.parse("upi://pay?pa=Host%40okbank&pn=Test+Host&cu=INR");
        assertEquals("host@okbank",p.payeeUpiId());
        assertEquals("Test Host",p.payeeName());
        assertFalse(p.hasAmount());
    }
    @Test void matchesRegisteredDestination() {
        assertDoesNotThrow(()->UpiQrPayload.requireMatchingPayee("upi://pay?pa=host@okbank","HOST@OKBANK"));
    }
    @Test void rejectsDifferentDestination() {
        assertThrows(IllegalArgumentException.class,()->UpiQrPayload.requireMatchingPayee("upi://pay?pa=attacker@bank","host@bank"));
    }
    @Test void rejectsHttpQr() {
        assertThrows(IllegalArgumentException.class,()->UpiQrPayload.parse("https://example.com/?pa=host@bank"));
    }
    @Test void rejectsDuplicatePayee() {
        assertThrows(IllegalArgumentException.class,()->UpiQrPayload.parse("upi://pay?pa=host@bank&pa=attacker@bank"));
    }
    @Test void rejectsMalformedEncoding() {
        assertThrows(IllegalArgumentException.class,()->UpiQrPayload.parse("upi://pay?pa=%ZZ"));
    }
    @Test void rejectsUnknownParameters() {
        assertThrows(IllegalArgumentException.class,()->UpiQrPayload.parse("upi://pay?pa=host@bank&redirect=https://evil.test"));
    }
    @Test void rejectsForeignCurrency() {
        assertThrows(IllegalArgumentException.class,()->UpiQrPayload.parse("upi://pay?pa=host@bank&cu=USD"));
    }
    @Test void rejectsNonPositiveAmount() {
        assertThrows(IllegalArgumentException.class,()->UpiQrPayload.parse("upi://pay?pa=host@bank&am=-1"));
    }
    @Test void rejectsFragment() {
        assertThrows(IllegalArgumentException.class,()->UpiQrPayload.parse("upi://pay?pa=host@bank#spoof"));
    }
    @Test void generatesAmountFreePayload() {
        String qr=UpiQrPayload.generate("HOST@OKBANK","Test Host");
        assertEquals("host@okbank",UpiQrPayload.parse(qr).payeeUpiId());
        assertFalse(qr.contains("am="));
    }
    @Test void encodesPayeeName() {
        assertTrue(UpiQrPayload.generate("host@bank","A & B").contains("pn=A+%26+B"));
    }
    @Test void rejectsInvalidGeneratedPayee() {
        assertThrows(IllegalArgumentException.class,()->UpiQrPayload.generate("not-upi","Host"));
    }
}
