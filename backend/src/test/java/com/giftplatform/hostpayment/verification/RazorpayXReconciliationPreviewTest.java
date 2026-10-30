package com.giftplatform.hostpayment.verification;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class RazorpayXReconciliationPreviewTest {
 private final UUID host=UUID.randomUUID(), destination=UUID.randomUUID();
 private VerificationAttempt attempt(){
  var a=new VerificationAttempt(UUID.randomUUID(),host,destination,HostDestinationVerifier.Kind.BANK);
  a.bindRequest(new VerificationRequestBinding("RAZORPAYX","fav_123","fa_123"));return a;
 }
 private final String good="""
 {"id":"fav_123","entity":"fund_account.validation","status":"completed",
 "fund_account":{"id":"fa_123","account_type":"bank_account"},
 "results":{"account_status":"active","registered_name":"Alex Kumar"}}
 """;
 @Test void matchingProviderResultCanBePreviewed(){
  assertEquals(VerificationOutcome.VERIFIED,new RazorpayXReconciliationPreview(id->{assertEquals("fav_123",id);return good;}).preview(attempt(),host,destination,"Alex Kumar"));
 }
 @Test void unrelatedReferenceNeverVerifies(){
  assertEquals(VerificationOutcome.UNAVAILABLE,new RazorpayXReconciliationPreview(id->good.replace("fav_123","fav_other")).preview(attempt(),host,destination,"Alex Kumar"));
 }
 @Test void wrongFundAccountNeverVerifies(){
  assertNotEquals(VerificationOutcome.VERIFIED,new RazorpayXReconciliationPreview(id->good.replace("fa_123","fa_other")).preview(attempt(),host,destination,"Alex Kumar"));
 }
 @Test void wrongHolderNeverVerifies(){
  assertNotEquals(VerificationOutcome.VERIFIED,new RazorpayXReconciliationPreview(id->good).preview(attempt(),host,destination,"Different Person"));
 }
 @Test void wrongHostNeverCallsProvider(){
  AtomicInteger calls=new AtomicInteger();var p=new RazorpayXReconciliationPreview(id->{calls.incrementAndGet();return good;});
  assertThrows(IllegalArgumentException.class,()->p.preview(attempt(),UUID.randomUUID(),destination,"Alex Kumar"));assertEquals(0,calls.get());
 }
 @Test void wrongDestinationNeverCallsProvider(){
  AtomicInteger calls=new AtomicInteger();var p=new RazorpayXReconciliationPreview(id->{calls.incrementAndGet();return good;});
  assertThrows(IllegalArgumentException.class,()->p.preview(attempt(),host,UUID.randomUUID(),"Alex Kumar"));assertEquals(0,calls.get());
 }
 @Test void unboundAttemptRejected(){
  var a=new VerificationAttempt(UUID.randomUUID(),host,destination,HostDestinationVerifier.Kind.BANK);
  assertThrows(IllegalStateException.class,()->new RazorpayXReconciliationPreview(id->good).preview(a,host,destination,"Alex Kumar"));
 }
 @Test void completedAttemptRejected(){
  var a=attempt();a.complete(new VerificationEvidence(VerificationOutcome.UNAVAILABLE,null,null));
  assertThrows(IllegalStateException.class,()->new RazorpayXReconciliationPreview(id->good).preview(a,host,destination,"Alex Kumar"));
 }
 @Test void transportTimeoutFailsClosed(){
  assertEquals(VerificationOutcome.UNAVAILABLE,new RazorpayXReconciliationPreview(id->{throw new java.io.IOException("timeout");}).preview(attempt(),host,destination,"Alex Kumar"));
 }
 @Test void invalidJsonFailsClosed(){
  assertEquals(VerificationOutcome.UNAVAILABLE,new RazorpayXReconciliationPreview(id->"not json").preview(attempt(),host,destination,"Alex Kumar"));
 }
 @Test void noDestinationMutation(){
  var a=attempt();new RazorpayXReconciliationPreview(id->good).preview(a,host,destination,"Alex Kumar");
  assertEquals(VerificationAttemptState.PENDING,a.getState());
 }
 @Test void emptyResultFailsClosed(){
  assertEquals(VerificationOutcome.UNAVAILABLE,new RazorpayXReconciliationPreview(id->"").preview(attempt(),host,destination,"Alex Kumar"));
 }
}
