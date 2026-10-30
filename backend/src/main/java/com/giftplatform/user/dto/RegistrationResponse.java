package com.giftplatform.user.dto;

import java.util.UUID;

public record RegistrationResponse(
        UUID id,
        String username,
        String email,
        String role
) {}