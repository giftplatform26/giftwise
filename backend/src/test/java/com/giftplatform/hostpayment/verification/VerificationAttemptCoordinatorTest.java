package com.giftplatform.hostpayment.verification;
import com.giftplatform.hostpayment.entity.*;
import com.giftplatform.hostpayment.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
class VerificationAttemptCoordinatorTest {
 VerificationAttemptRepository attempts;
 HostBankAccountRepository banks;
 HostUpiAccountRepository upis;
 VerificationAttemptCoordinator coordinator;
 UUID host=UUID.randomUUID(),dest=UUID.randomUUID();
 @BeforeEach void init(){attempts=mock(VerificationAttemptRepository.class);banks=mock(HostBankAccountRepository.class);upis=mock(HostUpiAccountRepository.class);coordinator=new VerificationAttemptCoordinator(attempts,banks,upis);}
 HostBankAccount bank(){return new HostBankAccount(dest,host,"encrypted","fingerprint","****1234","ifsc","ABCD*******","holder");}
 HostUpiAccount upi(){return new HostUpiAccount(dest,host,"encrypted","fingerprint","ab***@bank");}
 @Test void createsBankAttempt(){when(banks.findByIdAndHostId(dest,host)).thenReturn(Optional.of(bank()));UUID id=coordinator.begin(host,dest,HostDestinationVerifier.Kind.BANK);assertNotNull(id);verify(attempts).saveAndFlush(any(VerificationAttempt.class));}
 @Test void createsUpiAttempt(){when(upis.findByIdAndHostId(dest,host)).thenReturn(Optional.of(upi()));assertNotNull(coordinator.begin(host,dest,HostDestinationVerifier.Kind.UPI));}
 @Test void reusesPendingAttempt(){when(banks.findByIdAndHostId(dest,host)).thenReturn(Optional.of(bank()));UUID existing=UUID.randomUUID();when(attempts.findFirstByHostIdAndDestinationKindAndDestinationIdAndState(host,HostDestinationVerifier.Kind.BANK,dest,VerificationAttemptState.PENDING)).thenReturn(Optional.of(new VerificationAttempt(existing,host,dest,HostDestinationVerifier.Kind.BANK)));assertEquals(existing,coordinator.begin(host,dest,HostDestinationVerifier.Kind.BANK));verify(attempts,never()).saveAndFlush(any());}
 @Test void missingDestinationRejected(){assertThrows(NoSuchElementException.class,()->coordinator.begin(host,dest,HostDestinationVerifier.Kind.BANK));verifyNoInteractions(attempts);}
 @Test void disabledBankRejected(){var a=bank();a.disable();when(banks.findByIdAndHostId(dest,host)).thenReturn(Optional.of(a));assertThrows(IllegalStateException.class,()->coordinator.begin(host,dest,HostDestinationVerifier.Kind.BANK));}
 @Test void revokedUpiRejected(){var a=upi();a.applyTrustedVerification(VerificationStatus.REVOKED);when(upis.findByIdAndHostId(dest,host)).thenReturn(Optional.of(a));assertThrows(IllegalStateException.class,()->coordinator.begin(host,dest,HostDestinationVerifier.Kind.UPI));}
 @Test void verifiedEvidenceFinalizesOnlyAttempt(){UUID id=UUID.randomUUID();var a=new VerificationAttempt(id,host,dest,HostDestinationVerifier.Kind.BANK);when(attempts.findByIdAndHostId(id,host)).thenReturn(Optional.of(a));coordinator.finalizeAttempt(host,id,new VerificationEvidence(VerificationOutcome.VERIFIED,"RazorpayX","fav_1"));assertEquals(VerificationAttemptState.VERIFIED,a.getState());verifyNoInteractions(banks,upis);}
 @Test void pendingCannotFinalize(){UUID id=UUID.randomUUID();var a=new VerificationAttempt(id,host,dest,HostDestinationVerifier.Kind.BANK);when(attempts.findByIdAndHostId(id,host)).thenReturn(Optional.of(a));assertThrows(IllegalArgumentException.class,()->coordinator.finalizeAttempt(host,id,new VerificationEvidence(VerificationOutcome.PENDING,null,null)));}
 @Test void finalizationIsNotRepeatable(){var a=new VerificationAttempt(UUID.randomUUID(),host,dest,HostDestinationVerifier.Kind.BANK);a.complete(new VerificationEvidence(VerificationOutcome.UNAVAILABLE,null,null));assertThrows(IllegalStateException.class,()->a.complete(new VerificationEvidence(VerificationOutcome.VERIFIED,"RazorpayX","fav_1")));}
 @Test void missingAttemptRejected(){assertThrows(NoSuchElementException.class,()->coordinator.finalizeAttempt(host,UUID.randomUUID(),new VerificationEvidence(VerificationOutcome.UNAVAILABLE,null,null)));}
 @Test void nullIdentityRejected(){assertThrows(IllegalArgumentException.class,()->coordinator.begin(null,dest,HostDestinationVerifier.Kind.BANK));}
 @Test void rejectedOutcomeCannotActivate(){var a=new VerificationAttempt(UUID.randomUUID(),host,dest,HostDestinationVerifier.Kind.BANK);a.complete(new VerificationEvidence(VerificationOutcome.REJECTED,"RazorpayX","fav_1"));assertEquals(VerificationAttemptState.REJECTED,a.getState());}
}
