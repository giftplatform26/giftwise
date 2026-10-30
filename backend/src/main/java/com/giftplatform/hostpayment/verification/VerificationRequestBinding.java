package com.giftplatform.hostpayment.verification;

/** Non-secret provider identifiers captured when an authenticated provider request is accepted. */
public record VerificationRequestBinding(String provider, String requestReference, String fundAccountId) {
 public VerificationRequestBinding {
  if(blank(provider)||blank(requestReference)||blank(fundAccountId))
   throw new IllegalArgumentException("Complete request binding required");
  if(provider.length()>40||requestReference.length()>120||fundAccountId.length()>120)
   throw new IllegalArgumentException("Request binding exceeds storage limits");
 }
 private static boolean blank(String s){return s==null||s.isBlank();}
}
