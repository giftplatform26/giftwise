package com.giftplatform.hostpayment.entity;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
class HostPaymentAccountManagement5JTest {
 @Test void upiArchiveClearsPreferenceAndPreventsReuse(){
  var a=new HostUpiAccount(UUID.randomUUID(),UUID.randomUUID(),"cipher","fingerprint","u***@bank");
  a.rename("Personal UPI");a.confirmByHost();a.prefer();a.archive();
  assertEquals("Personal UPI",a.getAccountLabel());assertTrue(a.isArchived());assertFalse(a.isPreferred());assertFalse(a.isEnabled());
  assertThrows(IllegalStateException.class,a::enable);assertThrows(IllegalStateException.class,()->a.rename("Other"));
 }
 @Test void bankArchiveClearsPreferenceAndPreventsReuse(){
  var a=new HostBankAccount(UUID.randomUUID(),UUID.randomUUID(),"cipher","fingerprint","****1234","ifsc","IFSC***","holder");
  a.rename("Salary bank");a.confirmByHost();a.prefer();a.archive();
  assertEquals("Salary bank",a.getAccountLabel());assertTrue(a.isArchived());assertFalse(a.isPreferred());
  assertThrows(IllegalStateException.class,a::enable);
 }
}
