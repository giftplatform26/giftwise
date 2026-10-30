package com.giftplatform.hostpayment.verification;
import java.util.UUID;
/** Provider-neutral boundary. Never return VERIFIED without authenticated provider evidence. */
public interface HostDestinationVerifier {
 enum Kind { UPI, BANK }
 record Result(String providerReference, boolean verified) {}
 Result verify(UUID hostId,UUID destinationId,Kind kind);
}
