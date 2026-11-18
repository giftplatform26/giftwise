package com.giftplatform.hostpayment.qr;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UpiIntentUriTest {
    static final String TR = "GWTABCDEFGHJK2345";

    @Test void buildsAStandardsCompatibleIntentWithEveryParameterEncoded() {
        String uri = UpiQrPayload.generateIntent("Asha.Rao@OKHDFC", "Asha & Ravi", 250000, TR, "GiftWise GW-7K3P9Q", true);
        assertEquals("upi://pay?pa=asha.rao@okhdfc&pn=Asha%20%26%20Ravi&am=2500.00&cu=INR&tr=" + TR + "&tn=GiftWise%20GW-7K3P9Q", uri);
        var parsed = UpiQrPayload.parse(uri);
        assertEquals("asha.rao@okhdfc", parsed.payeeUpiId());
        assertEquals("Asha & Ravi", parsed.payeeName());
        assertTrue(parsed.hasAmount());
    }

    @Test void unicodeNamesArePercentEncodedAsUtf8AndRoundTrip() {
        String uri = UpiQrPayload.generateIntent("priya@upi", "प्रिया", 100, TR, "GiftWise GW-ABCDEF", true);
        assertFalse(uri.contains("प्रिया"));
        assertFalse(uri.contains("+"), "spaces/plus must never be form-encoded");
        assertEquals("प्रिया", UpiQrPayload.parse(uri).payeeName());
    }

    @Test void trCanBeOmittedForPayerAppsThatRejectItOnPersonalUpiIds() {
        String uri = UpiQrPayload.generateIntent("asha@upi", "Asha", 100, TR, "GiftWise GW-ABCDEF", false);
        assertFalse(uri.contains("tr="));
        assertTrue(uri.contains("tn=GiftWise%20GW-ABCDEF"));
    }

    @Test void amountsUseTwoDecimalsAndStayWithinLimits() {
        assertTrue(UpiQrPayload.generateIntent("a1@upi", "A", 100, TR, "n", true).contains("&am=1.00&"));
        assertTrue(UpiQrPayload.generateIntent("a1@upi", "A", 100_000_000L, TR, "n", true).contains("&am=1000000.00&"));
        for (long bad : new long[]{0, 99, -500, 100_000_001L})
            assertThrows(IllegalArgumentException.class, () -> UpiQrPayload.generateIntent("a1@upi", "A", bad, TR, "n", true), "amount " + bad);
    }

    @Test void invalidUpiIdsAreRejected() {
        for (String bad : new String[]{null, "", "nohandle", "a@", "@upi", "a b@upi", "a@upi&am=1", "x@upi?pa=evil@upi", "a@up%69"})
            assertThrows(IllegalArgumentException.class, () -> UpiQrPayload.generateIntent(bad, "A", 100, TR, "n", true), String.valueOf(bad));
    }

    @Test void invalidReferencesNotesAndNamesAreRejected() {
        for (String bad : new String[]{null, "", "GWT123", "gwtabcdefghjk2345", "XYZABCDEFGHJK2345", "GWTABCDEFGHJK2345&am=1"})
            assertThrows(IllegalArgumentException.class, () -> UpiQrPayload.generateIntent("a1@upi", "A", 100, bad, "n", true), String.valueOf(bad));
        assertThrows(IllegalArgumentException.class, () -> UpiQrPayload.generateIntent("a1@upi", "A", 100, TR, "note&am=1", true));
        assertThrows(IllegalArgumentException.class, () -> UpiQrPayload.generateIntent("a1@upi", " ", 100, TR, "n", true));
        assertThrows(IllegalArgumentException.class, () -> UpiQrPayload.generateIntent("a1@upi", "A\nB", 100, TR, "n", true));
    }

    @Test void intentQrRendersOnlyValidUpiPayloads() {
        byte[] png = UpiQrImageCodec.generateIntentPng(UpiQrPayload.generateIntent("a1@upi", "A", 100, TR, "n", true));
        assertEquals((byte) 0x89, png[0]);
        assertThrows(IllegalArgumentException.class, () -> UpiQrImageCodec.generateIntentPng("https://evil.example/pay"));
    }
}
