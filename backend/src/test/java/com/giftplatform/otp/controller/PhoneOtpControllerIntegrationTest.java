package com.giftplatform.otp.controller;

import com.giftplatform.otp.service.*;
import com.giftplatform.security.GiftUserPrincipal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest(properties="gift.verification.mobile.enabled=true")
@AutoConfigureMockMvc
class PhoneOtpControllerIntegrationTest {

    private static final PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17");

    static {
        postgres.start();
    }

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.datasource.url",
                postgres::getJdbcUrl
        );

        registry.add(
                "spring.datasource.username",
                postgres::getUsername
        );

        registry.add(
                "spring.datasource.password",
                postgres::getPassword
        );
    }

    private static final String REQUEST_URL =
            "/api/v1/phone/otp/request";

    private static final String VERIFY_URL =
            "/api/v1/phone/otp/verify";

    private final UUID userId = UUID.randomUUID();
    private final UUID challengeId = UUID.randomUUID();

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private PhoneOtpOrchestrationService orchestration;

    @MockitoBean
    private PhoneOtpTransactionService verification;

    @MockitoBean
    private OtpEndpointRateLimiter endpointLimits;

    private UsernamePasswordAuthenticationToken customer() {
        GiftUserPrincipal principal = new GiftUserPrincipal(
                userId,
                "customer@example.com",
                "unused-hash",
                "CUSTOMER",
                true
        );

        return UsernamePasswordAuthenticationToken.authenticated(
                principal,
                null,
                principal.getAuthorities()
        );
    }

    private String requestBody() {
        return """
                {"phoneNumber":"+919876543210"}
                """;
    }

    private String verifyBody() {
        return """
                {"otp":"123456"}
                """;
    }

    @Test
    void anonymousRequestMustBeRejected() throws Exception {
        mvc.perform(post(REQUEST_URL)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody()))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(orchestration);
    }

    @Test
    void requestWithoutCsrfMustBeRejected() throws Exception {
        mvc.perform(post(REQUEST_URL)
                        .with(authentication(customer())).sessionAttr("gift.credential.epoch",0L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(orchestration);
    }

    @Test
    void authenticatedRequestMustUsePrincipalUserId()
            throws Exception {

        when(orchestration.requestOtp(
                eq(userId), eq("+919876543210")
        )).thenReturn(
                PhoneOtpIssuanceResult.reserved(challengeId)
        );

        mvc.perform(post(REQUEST_URL)
                        .with(authentication(customer())).sessionAttr("gift.credential.epoch",0L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody()))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status")
                        .value("OTP_REQUEST_ACCEPTED"))
                .andExpect(jsonPath("$.challengeId").value(
                        org.hamcrest.Matchers.nullValue()));

        verify(orchestration).requestOtp(
                userId, "+919876543210"
        );
    }


    @Test
    void phoneInUseMustHaveSamePublicResponseAsReservation()
            throws Exception {
        when(orchestration.requestOtp(
                userId, "+919876543210"
        )).thenReturn(PhoneOtpIssuanceResult.phoneInUse());

        mvc.perform(post(REQUEST_URL)
                        .with(authentication(customer())).sessionAttr("gift.credential.epoch",0L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody()))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status")
                        .value("OTP_REQUEST_ACCEPTED"))
                .andExpect(jsonPath("$.challengeId")
                        .value(org.hamcrest.Matchers.nullValue()));
    }

    @Test
    void invalidPhoneMustBeRejected() throws Exception {
        mvc.perform(post(REQUEST_URL)
                        .with(authentication(customer())).sessionAttr("gift.credential.epoch",0L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"phoneNumber":"9876543210"}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orchestration);
    }

    @Test
    void missingPhoneMustBeRejected() throws Exception {
        mvc.perform(post(REQUEST_URL)
                        .with(authentication(customer())).sessionAttr("gift.credential.epoch",0L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orchestration);
    }

    @Test
    void anonymousVerificationMustBeRejected()
            throws Exception {

        mvc.perform(post(VERIFY_URL)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(verifyBody()))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(verification);
    }

    @Test
    void verificationWithoutCsrfMustBeRejected()
            throws Exception {

        mvc.perform(post(VERIFY_URL)
                        .with(authentication(customer())).sessionAttr("gift.credential.epoch",0L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(verifyBody()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(verification);
    }

    @Test
    void authenticatedVerificationMustUsePrincipalUserId()
            throws Exception {

        when(verification.verifyLatest(
                userId, "123456"
        )).thenReturn(
                PhoneOtpVerificationResult.VERIFIED
        );

        mvc.perform(post(VERIFY_URL)
                        .with(authentication(customer())).sessionAttr("gift.credential.epoch",0L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(verifyBody()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("PHONE_VERIFIED"));

        verify(verification).verifyLatest(
                userId, "123456"
        );
    }

    @Test
    void invalidOtpMustReturnGenericError()
            throws Exception {

        when(verification.verifyLatest(
                userId, "123456"
        )).thenReturn(
                PhoneOtpVerificationResult.INVALID_CODE
        );

        mvc.perform(post(VERIFY_URL)
                        .with(authentication(customer())).sessionAttr("gift.credential.epoch",0L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(verifyBody()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status")
                        .value("VERIFICATION_FAILED"));
    }

    @Test
    void replayedOtpMustBeRejected() throws Exception {
        when(verification.verifyLatest(
                userId, "123456"
        )).thenReturn(
                PhoneOtpVerificationResult.ALREADY_USED
        );

        mvc.perform(post(VERIFY_URL)
                        .with(authentication(customer())).sessionAttr("gift.credential.epoch",0L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(verifyBody()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status")
                        .value("VERIFICATION_FAILED"));
    }

    @Test
    void malformedOtpMustBeRejectedBeforeService()
            throws Exception {

        mvc.perform(post(VERIFY_URL)
                        .with(authentication(customer())).sessionAttr("gift.credential.epoch",0L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"otp":"12"}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(verification);
    }

    @Test
    void smsDispatchFailureMustNotExposeProviderDetails()
            throws Exception {

        when(orchestration.requestOtp(
                userId, "+919876543210"
        )).thenThrow(new OtpDispatchException(
                "Internal SMS provider credentials rejected",
                null
        ));

        mvc.perform(post(REQUEST_URL)
                        .with(authentication(customer())).sessionAttr("gift.credential.epoch",0L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody()))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status")
                        .value("DELIVERY_UNAVAILABLE"))
                .andExpect(content().string(
                        org.hamcrest.Matchers.not(
                                org.hamcrest.Matchers.containsString(
                                        "credentials rejected"
                                )
                        )
                ));
    }

    @Test
    void ipRequestLimitStopsOrchestration() throws Exception {
        when(endpointLimits.isRequestBlocked(anyString())).thenReturn(true);
        mvc.perform(post(REQUEST_URL).with(authentication(customer())).sessionAttr("gift.credential.epoch",0L).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(requestBody()))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value("RETRY_LATER"));
        verifyNoInteractions(orchestration);
    }

    @Test
    void verificationLimitStopsVerificationService() throws Exception {
        when(endpointLimits.isVerificationBlocked(eq(userId), anyString())).thenReturn(true);
        mvc.perform(post(VERIFY_URL).with(authentication(customer())).sessionAttr("gift.credential.epoch",0L).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(verifyBody()))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value("RETRY_LATER"));
        verifyNoInteractions(verification);
    }

    @Test
    void forwardedForHeaderCannotOverrideRemoteAddress() throws Exception {
        when(endpointLimits.isRequestBlocked(anyString())).thenReturn(true);
        mvc.perform(post(REQUEST_URL).with(authentication(customer())).sessionAttr("gift.credential.epoch",0L).with(csrf())
                .header("X-Forwarded-For", "203.0.113.123")
                .contentType(MediaType.APPLICATION_JSON).content(requestBody()))
                .andExpect(status().isTooManyRequests());
        verify(endpointLimits).isRequestBlocked("127.0.0.1");
        verifyNoInteractions(orchestration);
    }
}
