package com.giftplatform.hostpayment.verification;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class VerificationResultMatcherTest {
 private final UUID host=UUID.randomUUID(), destination=UUID.randomUUID();
 private final HostDestinationVerifier.Kind kind=HostDestinationVerifier.Kind.BANK;
 private final VerificationRequestBinding binding=new VerificationRequestBinding("RAZORPAYX","fav_123","fa_123");
 private boolean match(UUID h,UUID d,HostDestinationVerifier.Kind k,VerificationRequestBinding b){
  return VerificationResultMatcher.matches(host,destination,kind,binding,h,d,k,b);
 }
 @Test void exactBindingMatches(){assertTrue(match(host,destination,kind,binding));}
 @Test void otherHostRejected(){assertFalse(match(UUID.randomUUID(),destination,kind,binding));}
 @Test void otherDestinationRejected(){assertFalse(match(host,UUID.randomUUID(),kind,binding));}
 @Test void otherKindRejected(){assertFalse(match(host,destination,HostDestinationVerifier.Kind.UPI,binding));}
 @Test void otherProviderRejected(){assertFalse(match(host,destination,kind,new VerificationRequestBinding("OTHER","fav_123","fa_123")));}
 @Test void otherRequestRejected(){assertFalse(match(host,destination,kind,new VerificationRequestBinding("RAZORPAYX","fav_456","fa_123")));}
 @Test void otherFundAccountRejected(){assertFalse(match(host,destination,kind,new VerificationRequestBinding("RAZORPAYX","fav_123","fa_456")));}
 @Test void missingBindingRejected(){assertFalse(match(host,destination,kind,null));}
 @Test void missingIdentityRejected(){assertFalse(match(null,destination,kind,binding));}
 @Test void incompleteBindingRejected(){assertThrows(IllegalArgumentException.class,()->new VerificationRequestBinding("RAZORPAYX"," ","fa_123"));}
 @Test void overlongBindingRejected(){assertThrows(IllegalArgumentException.class,()->new VerificationRequestBinding("X".repeat(41),"fav_123","fa_123"));}
 @Test void pendingAllowed(){assertDoesNotThrow(()->VerificationResultMatcher.requirePending(VerificationAttemptState.PENDING));}
 @Test void finalizedRejected(){for(var s:VerificationAttemptState.values())if(s!=VerificationAttemptState.PENDING)assertThrows(IllegalStateException.class,()->VerificationResultMatcher.requirePending(s));}
}
