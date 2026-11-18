package com.giftplatform.user.service;

import com.giftplatform.user.dto.RegisterRequest;
import com.giftplatform.user.dto.RegistrationResponse;

public interface RegistrationService {
    RegistrationResponse register(RegisterRequest request);

    /** Per-field result of a sign-up availability check; a field that was not sent (or is not well-formed) is omitted. */
    record FieldAvailability(boolean available, String message) {}

    /** Checks the same uniqueness rules register() enforces, so the first sign-up step can catch them early. */
    java.util.Map<String, FieldAvailability> availability(String username, String email);
}