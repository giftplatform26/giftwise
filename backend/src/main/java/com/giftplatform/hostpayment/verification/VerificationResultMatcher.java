package com.giftplatform.hostpayment.verification;

import java.util.Objects;
import java.util.UUID;

/** Pure correlation checks only. Never authenticates responses or marks a destination verified. */
public final class VerificationResultMatcher {
 private VerificationResultMatcher() {}
 public static boolean matches(UUID expectedHostId, UUID expectedDestinationId,
       HostDestinationVerifier.Kind expectedKind, VerificationRequestBinding stored,
       UUID responseHostId, UUID responseDestinationId, HostDestinationVerifier.Kind responseKind,
       VerificationRequestBinding response) {
  return expectedHostId!=null && expectedDestinationId!=null && expectedKind!=null
   && stored!=null && response!=null
   && expectedHostId.equals(responseHostId)
   && expectedDestinationId.equals(responseDestinationId)
   && expectedKind==responseKind
   && Objects.equals(stored.provider(),response.provider())
   && Objects.equals(stored.requestReference(),response.requestReference())
   && Objects.equals(stored.fundAccountId(),response.fundAccountId());
 }
 public static void requirePending(VerificationAttemptState state) {
  if(state!=VerificationAttemptState.PENDING)
   throw new IllegalStateException("Verification attempt is not pending");
 }
}
