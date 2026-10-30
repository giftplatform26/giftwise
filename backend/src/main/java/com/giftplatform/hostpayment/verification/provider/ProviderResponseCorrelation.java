package com.giftplatform.hostpayment.verification.provider;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

/** Correlation is necessary but not sufficient: it does not authenticate a response. */
public final class ProviderResponseCorrelation {
    private ProviderResponseCorrelation() {}
    public static void requireMatch(ProviderVerificationRequest request, ProviderVerificationResponse response) {
        if (request == null || response == null || response.requestReference() == null
                || !MessageDigest.isEqual(request.requestReference().getBytes(StandardCharsets.UTF_8),
                                           response.requestReference().getBytes(StandardCharsets.UTF_8)))
            throw new SecurityException("Provider response reference mismatch");
    }
}
