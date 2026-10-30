package com.giftplatform.hostpayment.verification.provider;

/** Future provider HTTP adapter contract. No implementation makes live calls in 5S. */
public interface VerificationProviderTransport {
    ProviderVerificationResponse submit(ProviderVerificationRequest request);
}
