package com.giftplatform.hostpayment.verification.provider;

/** Deliberately refuses all live operations, including when an endpoint is configured. */
public final class DisabledVerificationProviderTransport implements VerificationProviderTransport {
    @Override public ProviderVerificationResponse submit(ProviderVerificationRequest request) {
        throw new VerificationProviderUnavailableException("Live verification transport disabled");
    }
}
