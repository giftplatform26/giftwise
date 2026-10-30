package com.giftplatform.user.service;

import com.giftplatform.user.dto.RegisterRequest;
import com.giftplatform.user.dto.RegistrationResponse;

public interface RegistrationService {
    RegistrationResponse register(RegisterRequest request);
}