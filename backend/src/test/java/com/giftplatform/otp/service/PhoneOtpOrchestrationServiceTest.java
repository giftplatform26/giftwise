
package com.giftplatform.otp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PhoneOtpOrchestrationServiceTest {

    private OtpCryptoService crypto;
    private PhoneOtpIssuanceTransactionService issuance;
    private PhoneOtpDeliveryTransactionService delivery;
    private SmsSender sms;
    private OtpRequestRateLimiter rateLimiter;
    private PhoneOtpOrchestrationService service;

    private final UUID userId = UUID.randomUUID();
    private static final String PHONE = "+919876543210";

    @BeforeEach
    void setup() {
        crypto = mock(OtpCryptoService.class);
        issuance = mock(PhoneOtpIssuanceTransactionService.class);
        delivery = mock(PhoneOtpDeliveryTransactionService.class);
        sms = mock(SmsSender.class);
        rateLimiter = mock(OtpRequestRateLimiter.class);
        when(rateLimiter.acquire(any(UUID.class), anyString()))
                .thenReturn(new OtpRequestRateLimiter.Decision(true, 0));

        service = new PhoneOtpOrchestrationService(
                crypto, issuance, delivery, sms, rateLimiter
        );

        when(crypto.generateOtp()).thenReturn("123456");
    }

    @Test
    void successfulDispatchMarksChallengeSent() {
        when(issuance.reserve(
                eq(userId), eq(PHONE),
                eq("123456"), any(UUID.class)
        )).thenAnswer(invocation ->
                PhoneOtpIssuanceResult.reserved(
                        invocation.getArgument(3)
                )
        );

        when(delivery.markSent(
                eq(userId), any(UUID.class)
        )).thenReturn(true);

        var result = service.requestOtp(userId, PHONE);

        assertEquals(
                PhoneOtpIssuanceResult.Status.RESERVED,
                result.status()
        );

        verify(sms).sendVerificationCode(PHONE, "123456");
        verify(delivery).markSent(
                eq(userId), eq(result.challengeId())
        );
        verify(delivery, never()).markFailed(
                any(), any()
        );
    }

    @Test
    void cooldownDoesNotSendSms() {
        when(issuance.reserve(
                eq(userId), eq(PHONE),
                anyString(), any(UUID.class)
        )).thenReturn(PhoneOtpIssuanceResult.cooldown(30));

        var result = service.requestOtp(userId, PHONE);

        assertEquals(
                PhoneOtpIssuanceResult.Status.COOLDOWN,
                result.status()
        );

        verifyNoInteractions(sms, delivery);
    }


    @Test
    void explicitSmsRejectionMarksChallengeFailed() {
        when(issuance.reserve(
                eq(userId), eq(PHONE),
                anyString(), any(UUID.class)
        )).thenAnswer(invocation ->
                PhoneOtpIssuanceResult.reserved(
                        invocation.getArgument(3)
                )
        );

        doThrow(new OtpSmsRejectedException("Rejected"))
                .when(sms)
                .sendVerificationCode(eq(PHONE), anyString());

        when(delivery.markFailed(
                eq(userId), any(UUID.class)
        )).thenReturn(true);

        assertThrows(
                OtpDispatchException.class,
                () -> service.requestOtp(userId, PHONE)
        );
        verify(delivery, times(1)).markFailed(
                eq(userId), any(UUID.class)
        );
        verify(delivery, never()).markSent(
                any(), any()
        );
    }

    @Test
    void uncertainSmsFailureLeavesChallengePending() {
        when(issuance.reserve(
                eq(userId), eq(PHONE),
                anyString(), any(UUID.class)
        )).thenAnswer(invocation ->
                PhoneOtpIssuanceResult.reserved(
                        invocation.getArgument(3)
                )
        );

        doThrow(new IllegalStateException("Network timeout"))
                .when(sms)
                .sendVerificationCode(eq(PHONE), anyString());

        assertThrows(
                OtpDeliveryUncertainException.class,
                () -> service.requestOtp(userId, PHONE)
        );

        verifyNoInteractions(delivery);
    }


    @Test
    void failedFinalizationRaisesException() {
        when(issuance.reserve(
                eq(userId), eq(PHONE),
                anyString(), any(UUID.class)
        )).thenAnswer(invocation ->
                PhoneOtpIssuanceResult.reserved(
                        invocation.getArgument(3)
                )
        );

        when(delivery.markSent(
                eq(userId), any(UUID.class)
        )).thenReturn(false);

        assertThrows(
                OtpDeliveryUncertainException.class,
                () -> service.requestOtp(userId, PHONE)
        );

        verify(sms).sendVerificationCode(
                PHONE, "123456"
        );
        verify(delivery, never()).markFailed(any(), any());
    }

    @Test
    void pendingDeliveryDoesNotSendAnotherSms() {
        when(issuance.reserve(
                eq(userId), eq(PHONE),
                anyString(), any(UUID.class)
        )).thenReturn(
                PhoneOtpIssuanceResult.deliveryInProgress()
        );

        var result = service.requestOtp(userId, PHONE);

        assertEquals(
                PhoneOtpIssuanceResult.Status.DELIVERY_IN_PROGRESS,
                result.status()
        );

        verifyNoInteractions(sms, delivery);
    }
    @Test
    void rateLimitedRequestDoesNotGenerateOtpReserveOrSendSms() {
        when(rateLimiter.acquire(userId, PHONE))
                .thenReturn(new OtpRequestRateLimiter.Decision(false, 900));

        var result = service.requestOtp(userId, PHONE);

        assertEquals(PhoneOtpIssuanceResult.Status.COOLDOWN, result.status());
        assertEquals(900, result.retryAfterSeconds());
        verifyNoInteractions(crypto, issuance, delivery, sms);
    }

}
