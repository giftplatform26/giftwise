package com.giftplatform.hostpayment.service;
import com.giftplatform.hostpayment.entity.*;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
class HostPaymentProfileModelTest {
 @Test void newUpiIsPendingAndCannotBePreferred(){var a=new HostUpiAccount(UUID.randomUUID(),UUID.randomUUID(),"cipher","fingerprint","ab***@bank");assertEquals(VerificationStatus.PENDING,a.getVerificationStatus());assertThrows(IllegalStateException.class,a::prefer);}
 @Test void disabledVerifiedUpiCannotBePreferred(){var a=new HostUpiAccount(UUID.randomUUID(),UUID.randomUUID(),"cipher","fingerprint","ab***@bank");a.applyTrustedVerification(VerificationStatus.VERIFIED);a.disable();assertThrows(IllegalStateException.class,a::prefer);}
 @Test void verifiedUpiCanBePreferredThenRevoked(){var a=new HostUpiAccount(UUID.randomUUID(),UUID.randomUUID(),"cipher","fingerprint","ab***@bank");a.applyTrustedVerification(VerificationStatus.VERIFIED);a.prefer();assertTrue(a.isPreferred());a.applyTrustedVerification(VerificationStatus.REVOKED);assertFalse(a.isPreferred());}
 @Test void pendingBankCannotBePreferred(){var a=new HostBankAccount(UUID.randomUUID(),UUID.randomUUID(),"cipher","fingerprint","****1234","ifsc","HDFC*******","holder");assertThrows(IllegalStateException.class,a::prefer);}
 @Test void verifiedBankCanBeDisabled(){var a=new HostBankAccount(UUID.randomUUID(),UUID.randomUUID(),"cipher","fingerprint","****1234","ifsc","HDFC*******","holder");a.applyTrustedVerification(VerificationStatus.VERIFIED);a.prefer();a.disable();assertFalse(a.isPreferred());assertFalse(a.isEnabled());}
}
