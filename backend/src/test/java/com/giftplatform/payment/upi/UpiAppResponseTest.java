package com.giftplatform.payment.upi;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UpiAppResponseTest {
    @Test void parsesTheCommonAndroidResponseString() {
        var r = UpiAppResponse.parse("txnId=AXI0123abc&responseCode=00&Status=SUCCESS&txnRef=GWTABCDEFGHJK2345&ApprovalRefNo=412345678901");
        assertEquals("SUCCESS", r.status());
        assertEquals("00", r.responseCode());
        assertEquals("AXI0123abc", r.txnId());
        assertEquals("GWTABCDEFGHJK2345", r.txnRef());
        assertEquals("412345678901", r.approvalRef());
        assertEquals("412345678901", r.utrCandidate());
        assertNull(r.amountPaise());
    }

    @Test void normalisesStatusVariantsAndKeyCase() {
        assertEquals("FAILURE", UpiAppResponse.parse("status=Failed&responsecode=ZD").status());
        assertEquals("SUBMITTED", UpiAppResponse.parse("STATUS=submitted").status());
        assertEquals("UNKNOWN", UpiAppResponse.parse("Status=whatever").status());
        assertEquals("UNKNOWN", UpiAppResponse.parse("txnId=ABC").status());
    }

    @Test void readsAnOptionalAmount() {
        assertEquals(250000L, UpiAppResponse.parse("Status=SUCCESS&amount=2500.00").amountPaise());
        assertEquals(150L, UpiAppResponse.parse("Status=SUCCESS&amount=1.5").amountPaise());
        assertThrows(IllegalArgumentException.class, () -> UpiAppResponse.parse("Status=SUCCESS&amount=-1"));
        assertThrows(IllegalArgumentException.class, () -> UpiAppResponse.parse("Status=SUCCESS&amount=1.234"));
        assertThrows(IllegalArgumentException.class, () -> UpiAppResponse.parse("Status=SUCCESS&amount=abc"));
    }

    @Test void nullAndBlankFieldsFromAppsAreTreatedAsMissing() {
        var r = UpiAppResponse.parse("Status=SUCCESS&txnId=null&ApprovalRefNo=&txnRef=undefined");
        assertNull(r.txnId()); assertNull(r.approvalRef()); assertNull(r.txnRef()); assertNull(r.utrCandidate());
    }

    @Test void nonTwelveDigitApprovalRefsAreNotUtrs() {
        assertNull(UpiAppResponse.parse("Status=SUCCESS&ApprovalRefNo=ABC123").utrCandidate());
        assertNull(UpiAppResponse.parse("Status=SUCCESS&ApprovalRefNo=1234567890123").utrCandidate());
    }

    @Test void rejectsMalformedOversizedOrInjectedResponses() {
        for (String bad : new String[]{null, "", "   ", "garbage", "=x", "Status=SUCCESS&Status=FAILURE", "txnId=a<script>",
                "txnId=" + "A".repeat(65), "x".repeat(1025), "Status=SUCCESS\n&txnId=1", "Status=%E0%A4%A", "responseCode=00"})
            assertThrows(IllegalArgumentException.class, () -> UpiAppResponse.parse(bad), String.valueOf(bad));
    }
}
