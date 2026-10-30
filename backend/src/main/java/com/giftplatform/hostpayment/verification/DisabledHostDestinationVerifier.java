package com.giftplatform.hostpayment.verification;

import java.util.UUID;

public class DisabledHostDestinationVerifier implements HostDestinationVerifier {

 @Override
 public Result verify(UUID hostId, UUID destinationId, Kind kind) {
  throw new IllegalStateException(
          "Live destination verification is disabled"
  );
 }
}