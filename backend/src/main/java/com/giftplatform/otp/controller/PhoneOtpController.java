package com.giftplatform.otp.controller;

import com.giftplatform.otp.controller.dto.PhoneOtpRequest;
import com.giftplatform.otp.controller.dto.PhoneOtpResponse;
import com.giftplatform.otp.controller.dto.PhoneOtpVerifyRequest;
import com.giftplatform.otp.service.*;
import com.giftplatform.security.GiftUserPrincipal;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/phone/otp")
public class PhoneOtpController {

    private final PhoneOtpOrchestrationService orchestration;
    private final PhoneOtpTransactionService verification;
    private final OtpEndpointRateLimiter endpointLimits;

    public PhoneOtpController(
            PhoneOtpOrchestrationService orchestration,
            PhoneOtpTransactionService verification,
            OtpEndpointRateLimiter endpointLimits
    ) {
        this.orchestration = orchestration;
        this.verification = verification;
        this.endpointLimits = endpointLimits;
    }

    @PostMapping("/request")
    public ResponseEntity<PhoneOtpResponse> requestOtp(
            @AuthenticationPrincipal GiftUserPrincipal principal,
            @Valid @RequestBody PhoneOtpRequest request,
            HttpServletRequest httpRequest
    ) {
        UUID userId = principal.getUserId();
        if (endpointLimits.isRequestBlocked(httpRequest.getRemoteAddr())) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new PhoneOtpResponse("RETRY_LATER", null, 3600));
        }

        try {
            PhoneOtpIssuanceResult result =
                    orchestration.requestOtp(
                            userId,
                            request.phoneNumber()
                    );

            return switch (result.status()) {

                case RESERVED -> ResponseEntity
                        .accepted()
                        .body(PhoneOtpResponse.of(
                                "OTP_REQUEST_ACCEPTED"
                        ));

                case PHONE_IN_USE -> ResponseEntity
                        .accepted()
                        .body(PhoneOtpResponse.of(
                                "OTP_REQUEST_ACCEPTED"
                        ));

                case COOLDOWN -> ResponseEntity
                        .status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(new PhoneOtpResponse(
                                "RETRY_LATER",
                                null,
                                result.retryAfterSeconds()
                        ));

                case DELIVERY_IN_PROGRESS -> ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(PhoneOtpResponse.of(
                                "REQUEST_IN_PROGRESS"
                        ));

                case USER_NOT_AVAILABLE -> ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(PhoneOtpResponse.of(
                                "REQUEST_UNAVAILABLE"
                        ));
            };

        } catch (OtpDispatchException |
                 OtpDeliveryUncertainException ex) {

            return ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(PhoneOtpResponse.of(
                            "DELIVERY_UNAVAILABLE"
                    ));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<PhoneOtpResponse> verifyOtp(
            @AuthenticationPrincipal GiftUserPrincipal principal,
            @Valid @RequestBody PhoneOtpVerifyRequest request,
            HttpServletRequest httpRequest
    ) {
        if (endpointLimits.isVerificationBlocked(principal.getUserId(), httpRequest.getRemoteAddr())) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new PhoneOtpResponse("RETRY_LATER", null, 3600));
        }
        PhoneOtpVerificationResult result =
                verification.verifyLatest(
                        principal.getUserId(),
                        request.otp()
                );

        return switch (result) {

            case VERIFIED -> ResponseEntity
                    .ok(PhoneOtpResponse.of(
                            "PHONE_VERIFIED"
                    ));

            case INVALID_CODE, EXPIRED, ALREADY_USED,
                 MAX_ATTEMPTS, NOT_AVAILABLE ->
                    ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body(PhoneOtpResponse.of(
                                    "VERIFICATION_FAILED"
                            ));
        };
    }
}