
package com.giftplatform.security.recovery;
import com.giftplatform.security.delivery.MessageDelivery;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class Batch10C2MobilePolicyTest {
 @Test void platformMinimumCannotBeWeakened() {
   var platform=new EffectiveRecoveryPolicyService.Policy(true,true,true,true,false);
   var event=new EffectiveRecoveryPolicyService.Policy(false,false,false,false,true);
   var effective=platform.merge(event);
   assertTrue(effective.requireMobile());
   assertTrue(effective.verifyMobile());
   assertFalse(effective.allowGroupPin());
 }
 @Test void simulatorCannotMarkMobileVerifiedInProduction() {
   var controller=new GroupMobileEnrollmentController(mock(JdbcTemplate.class),
      mock(RecoveryChallengeStore.class),mock(CredentialSessionVersionStore.class),
      mock(MessageDelivery.class),"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",false,false,"prod");
   var error=assertThrows(org.springframework.web.server.ResponseStatusException.class,
      ()->controller.start(java.util.UUID.randomUUID(),
          new GroupMobileEnrollmentController.Start("+919876543210",null),null));
   assertEquals(503,error.getStatusCode().value());
 }
 @Test void developmentOtpCannotClaimOwnership() {
   var controller=new GroupMobileEnrollmentController(mock(JdbcTemplate.class),
      mock(RecoveryChallengeStore.class),mock(CredentialSessionVersionStore.class),
      mock(MessageDelivery.class),"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",true,true,"local");
   assertThrows(org.springframework.web.server.ResponseStatusException.class,
      ()->controller.start(java.util.UUID.randomUUID(),
          new GroupMobileEnrollmentController.Start("invalid",null),null));
 }
}
