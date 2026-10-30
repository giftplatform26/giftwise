package com.giftplatform.hostpayment.verification.cashfree;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class CashfreeSecureIdTransportTest {
  @Test void disabledByDefault() {
    var transport=new CashfreeSecureIdTransport(false,"","");
    assertFalse(transport.available());
    assertThrows(IllegalStateException.class,()->transport.submitSandbox("/verification/bank-account/sync","{}"));
  }
  @Test void cannotSendArbitraryUrls() {
    var transport=new CashfreeSecureIdTransport(true,"test-id","test-secret");
    assertThrows(IllegalArgumentException.class,()->transport.submitSandbox("https://evil.example/","{}"));
    assertThrows(IllegalArgumentException.class,()->transport.submitSandbox("/verification/unknown","{}"));
  }
  @Test void oversizeRequestRejectedBeforeNetwork() {
    var transport=new CashfreeSecureIdTransport(true,"test-id","test-secret");
    assertThrows(IllegalArgumentException.class,()->transport.submitSandbox("/verification/bank-account/sync","x".repeat(4097)));
  }
}
