package com.giftplatform.otp.controller;

import com.giftplatform.otp.controller.dto.*;
import com.giftplatform.otp.service.*;
import com.giftplatform.security.GiftUserPrincipal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PhoneOtpControllerTest {

    private PhoneOtpOrchestrationService orchestration;
    private PhoneOtpTransactionService verification;
    private PhoneOtpController controller;
    private OtpEndpointRateLimiter endpointLimits;
    private GiftUserPrincipal principal;

    private final UUID userId = UUID.randomUUID();
    private final UUID challengeId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        orchestration = mock(PhoneOtpOrchestrationService.class);
        verification = mock(PhoneOtpTransactionService.class);
        endpointLimits = mock(OtpEndpointRateLimiter.class);

        controller = new PhoneOtpController(
                orchestration, verification, endpointLimits
        );

        principal = new GiftUserPrincipal(
                userId,
                "customer@example.com",
                "unused-hash",
                "CUSTOMER",
                true
        );
    }

    @Test
    void shouldRequestOtpSuccessfully() {
        when(orchestration.requestOtp(
                userId, "+919876543210"
        )).thenReturn(
                PhoneOtpIssuanceResult.reserved(challengeId)
        );

        var response = controller.requestOtp(
                principal,
                new PhoneOtpRequest("+919876543210"), new MockHttpServletRequest()
        );

        assertEquals(HttpStatus.ACCEPTED,
                response.getStatusCode());

        assertEquals("OTP_REQUEST_ACCEPTED", response.getBody().status());
        assertNull(response.getBody().challengeId());
    }

    @Test
    void shouldReturnCooldown() {
        when(orchestration.requestOtp(
                userId, "+919876543210"
        )).thenReturn(
                PhoneOtpIssuanceResult.cooldown(45)
        );

        var response = controller.requestOtp(
                principal,
                new PhoneOtpRequest("+919876543210"), new MockHttpServletRequest()
        );

        assertEquals(HttpStatus.TOO_MANY_REQUESTS,
                response.getStatusCode());

        assertEquals(45,
                response.getBody().retryAfterSeconds());
    }

    @Test
    void shouldHidePhoneOwnership() {
        when(orchestration.requestOtp(
                userId, "+919876543210"
        )).thenReturn(
                PhoneOtpIssuanceResult.phoneInUse()
        );

        var response = controller.requestOtp(
                principal,
                new PhoneOtpRequest("+919876543210"), new MockHttpServletRequest()
        );

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertEquals("OTP_REQUEST_ACCEPTED", response.getBody().status());
        assertNull(response.getBody().challengeId());
    }

    @Test
    void shouldHandleDeliveryInProgress() {
        when(orchestration.requestOtp(
                userId, "+919876543210"
        )).thenReturn(
                PhoneOtpIssuanceResult.deliveryInProgress()
        );

        var response = controller.requestOtp(
                principal,
                new PhoneOtpRequest("+919876543210"), new MockHttpServletRequest()
        );

        assertEquals(HttpStatus.CONFLICT,
                response.getStatusCode());
    }

    @Test
    void shouldHandleDispatchFailure() {
        when(orchestration.requestOtp(
                userId, "+919876543210"
        )).thenThrow(
                new OtpDispatchException(
                        "Provider rejected", null
                )
        );

        var response = controller.requestOtp(
                principal,
                new PhoneOtpRequest("+919876543210"), new MockHttpServletRequest()
        );

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE,
                response.getStatusCode());

        assertFalse(response.getBody().status()
                .contains("Provider"));
    }

    @Test
    void shouldVerifyOtpSuccessfully() {
        when(verification.verifyLatest(
                userId, "123456"
        )).thenReturn(
                PhoneOtpVerificationResult.VERIFIED
        );

        var response = controller.verifyOtp(
                principal,
                new PhoneOtpVerifyRequest("123456"), new MockHttpServletRequest()
        );

        assertEquals(HttpStatus.OK,
                response.getStatusCode());

        assertEquals("PHONE_VERIFIED",
                response.getBody().status());
    }

    @Test
    void shouldRejectInvalidOtp() {
        when(verification.verifyLatest(
                userId, "000000"
        )).thenReturn(
                PhoneOtpVerificationResult.INVALID_CODE
        );

        var response = controller.verifyOtp(
                principal,
                new PhoneOtpVerifyRequest("000000"), new MockHttpServletRequest()
        );

        assertEquals(HttpStatus.BAD_REQUEST,
                response.getStatusCode());

        assertEquals("VERIFICATION_FAILED",
                response.getBody().status());
    }

    @Test
    void shouldRejectReplayedOtp() {
        when(verification.verifyLatest(
                userId, "123456"
        )).thenReturn(
                PhoneOtpVerificationResult.ALREADY_USED
        );

        var response = controller.verifyOtp(
                principal,
                new PhoneOtpVerifyRequest("123456"), new MockHttpServletRequest()
        );

        assertEquals(HttpStatus.BAD_REQUEST,
                response.getStatusCode());
        assertEquals("VERIFICATION_FAILED", response.getBody().status());
    }
}