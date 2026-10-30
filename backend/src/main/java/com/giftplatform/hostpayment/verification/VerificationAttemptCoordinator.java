package com.giftplatform.hostpayment.verification;
import com.giftplatform.hostpayment.repository.HostBankAccountRepository;
import com.giftplatform.hostpayment.repository.HostUpiAccountRepository;
import com.giftplatform.hostpayment.entity.VerificationStatus;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.NoSuchElementException;
import java.util.UUID;
/** Internal attempt lifecycle only; does not call a provider or activate a destination. */
@Service
@ConditionalOnProperty(name="gift.payment.profiles.enabled",havingValue="true")
public class VerificationAttemptCoordinator {
 private final VerificationAttemptRepository attempts;
 private final HostBankAccountRepository banks;
 private final HostUpiAccountRepository upis;
 public VerificationAttemptCoordinator(VerificationAttemptRepository attempts,HostBankAccountRepository banks,HostUpiAccountRepository upis){this.attempts=attempts;this.banks=banks;this.upis=upis;}
 @Transactional
 public UUID begin(UUID hostId,UUID destinationId,HostDestinationVerifier.Kind kind){
  if(hostId==null||destinationId==null||kind==null)throw new IllegalArgumentException("Missing identity");
  if(kind==HostDestinationVerifier.Kind.BANK){
   var a=banks.findByIdAndHostId(destinationId,hostId).orElseThrow(()->new NoSuchElementException("Destination not found"));
   if(!a.isEnabled()||a.getVerificationStatus()==VerificationStatus.REVOKED)throw new IllegalStateException("Destination ineligible");
  }else{
   var a=upis.findByIdAndHostId(destinationId,hostId).orElseThrow(()->new NoSuchElementException("Destination not found"));
   if(!a.isEnabled()||a.getVerificationStatus()==VerificationStatus.REVOKED)throw new IllegalStateException("Destination ineligible");
  }
  var existing=attempts.findFirstByHostIdAndDestinationKindAndDestinationIdAndState(hostId,kind,destinationId,VerificationAttemptState.PENDING);
  if(existing.isPresent())return existing.get().getId();
  var a=new VerificationAttempt(UUID.randomUUID(),hostId,destinationId,kind);
  attempts.saveAndFlush(a); // Partial unique index rejects concurrent duplicate creation.
  return a.getId();
 }
 @Transactional
 public void finalizeAttempt(UUID hostId,UUID attemptId,VerificationEvidence evidence){
  if(hostId==null||attemptId==null)throw new IllegalArgumentException("Missing identity");
  var attempt=attempts.findByIdAndHostId(attemptId,hostId).orElseThrow(()->new NoSuchElementException("Attempt not found"));
  attempt.complete(evidence);
  // Deliberately do not change destination verification status here.
  // That requires authenticated, request-bound provider evidence and reconciliation.
 }
}
