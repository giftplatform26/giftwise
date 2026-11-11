package com.giftplatform.hostpayment.qr;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UpiPaymentLinkTest {
    @Test void paymentLinkCarriesAmountAndCodeNote(){
        String link=UpiQrPayload.generatePayment("asha@okhdfc","Gift recipient",50050,"GiftWise GW-7K3P9Q");
        assertEquals("upi://pay?pa=asha%40okhdfc&pn=Gift+recipient&cu=INR&am=500.50&tn=GiftWise+GW-7K3P9Q",link);
    }
    @Test void rejectsBadAmountsAndNotes(){
        assertThrows(IllegalArgumentException.class,()->UpiQrPayload.generatePayment("asha@okhdfc","Gift recipient",50,"GiftWise GW-7K3P9Q"));
        assertThrows(IllegalArgumentException.class,()->UpiQrPayload.generatePayment("asha@okhdfc","Gift recipient",5000,"pay&am=1"));
        assertThrows(IllegalArgumentException.class,()->UpiQrPayload.generatePayment("not-a-upi","Gift recipient",5000,"GiftWise GW-7K3P9Q"));
    }
    @Test void paymentQrIsAValidPng(){
        byte[] png=UpiQrImageCodec.generatePaymentPng("asha@okhdfc","Gift recipient",50000,"GiftWise GW-7K3P9Q");
        assertTrue(png.length>100);assertEquals((byte)0x89,png[0]);assertEquals('P',png[1]);
    }
}
