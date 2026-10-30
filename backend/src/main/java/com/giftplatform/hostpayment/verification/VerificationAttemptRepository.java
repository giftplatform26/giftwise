package com.giftplatform.hostpayment.verification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;
public interface VerificationAttemptRepository extends JpaRepository<VerificationAttempt,UUID> {
 Optional<VerificationAttempt> findByIdAndHostId(UUID id,UUID hostId);
 Optional<VerificationAttempt> findByRequestProviderAndRequestReference(String requestProvider,String requestReference);

 Optional<VerificationAttempt> findFirstByHostIdAndDestinationKindAndDestinationIdAndState(UUID hostId,HostDestinationVerifier.Kind kind,UUID destinationId,VerificationAttemptState state);
}
