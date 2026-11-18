
package com.giftplatform.user.controller;

import com.giftplatform.user.dto.RegisterRequest;
import com.giftplatform.user.dto.RegistrationResponse;
import com.giftplatform.user.service.RegistrationService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class RegistrationController {

    private final RegistrationService registrationService;
    @org.springframework.beans.factory.annotation.Autowired(required = false) private com.giftplatform.otp.service.OtpEndpointRateLimiter limiter;

    public RegistrationController(
            RegistrationService registrationService
    ) {
        this.registrationService = registrationService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegistrationResponse register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return registrationService.register(request);
    }

    public record AvailabilityRequest(String username, String email) {}

    /** Lets the first sign-up step report a taken username or email before the guest reaches the password step. */
    @PostMapping("/register/availability")
    public org.springframework.http.ResponseEntity<java.util.Map<String, RegistrationService.FieldAvailability>> availability(
            @RequestBody AvailabilityRequest body, jakarta.servlet.http.HttpServletRequest http) {
        String remote = http.getRemoteAddr() == null ? "unknown" : http.getRemoteAddr();
        if (limiter != null && limiter.isSignupCheckBlocked(remote))
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many checks. Please wait a little and try again.");
        return org.springframework.http.ResponseEntity.ok()
                .cacheControl(org.springframework.http.CacheControl.noStore())
                .body(registrationService.availability(body == null ? null : body.username(), body == null ? null : body.email()));
    }
}
