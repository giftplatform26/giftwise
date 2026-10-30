package com.giftplatform.otp.service;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sns.model.SnsException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AwsSnsSmsSenderTest {
    private final SnsClient sns = mock(SnsClient.class);

    @Test void publishesTransactionalSms() {
        when(sns.publish(any(PublishRequest.class))).thenReturn(PublishResponse.builder().messageId("abc").build());
        new AwsSnsSmsSender(sns, "", "Transactional").sendVerificationCode("+919876543210", "123456");
        var captor = org.mockito.ArgumentCaptor.forClass(PublishRequest.class);
        verify(sns).publish(captor.capture());
        assertEquals("+919876543210", captor.getValue().phoneNumber());
        assertTrue(captor.getValue().message().contains("123456"));
        assertEquals("Transactional", captor.getValue().messageAttributes().get("AWS.SNS.SMS.SMSType").stringValue());
        assertFalse(captor.getValue().messageAttributes().containsKey("AWS.SNS.SMS.SenderID"));
    }
    @Test void includesConfiguredSenderId() {
        when(sns.publish(any(PublishRequest.class))).thenReturn(PublishResponse.builder().messageId("abc").build());
        new AwsSnsSmsSender(sns, "GIFTP", "Transactional").sendVerificationCode("+919876543210", "123456");
        var captor = org.mockito.ArgumentCaptor.forClass(PublishRequest.class);
        verify(sns).publish(captor.capture());
        assertEquals("GIFTP", captor.getValue().messageAttributes().get("AWS.SNS.SMS.SenderID").stringValue());
    }
    @Test void rejectsInvalidPhoneBeforeCallingAws() {
        assertThrows(IllegalArgumentException.class, () -> new AwsSnsSmsSender(sns, "", "Transactional").sendVerificationCode("9876543210", "123456"));
        verifyNoInteractions(sns);
    }
    @Test void rejectsInvalidOtpBeforeCallingAws() {
        assertThrows(IllegalArgumentException.class, () -> new AwsSnsSmsSender(sns, "", "Transactional").sendVerificationCode("+919876543210", "12345"));
        verifyNoInteractions(sns);
    }
    @Test void rejectsInvalidSmsTypeAtConstruction() {
        assertThrows(IllegalArgumentException.class, () -> new AwsSnsSmsSender(sns, "", "Other"));
    }
    @Test void nullPublishResponseIsUncertain() {
        assertThrows(OtpDeliveryUncertainException.class, () -> new AwsSnsSmsSender(sns, "", "Transactional").sendVerificationCode("+919876543210", "123456"));
    }
    @Test void missingMessageIdIsUncertain() {
        when(sns.publish(any(PublishRequest.class))).thenReturn(PublishResponse.builder().build());
        assertThrows(OtpDeliveryUncertainException.class, () -> new AwsSnsSmsSender(sns, "", "Transactional").sendVerificationCode("+919876543210", "123456"));
    }
    @Test void clientTimeoutIsUncertain() {
        when(sns.publish(any(PublishRequest.class))).thenThrow(SdkClientException.builder().message("timeout").build());
        assertThrows(OtpDeliveryUncertainException.class, () -> new AwsSnsSmsSender(sns, "", "Transactional").sendVerificationCode("+919876543210", "123456"));
    }
    @Test void invalidParameterIsExplicitRejection() {
        when(sns.publish(any(PublishRequest.class))).thenThrow(SnsException.builder().statusCode(400)
            .awsErrorDetails(software.amazon.awssdk.awscore.exception.AwsErrorDetails.builder().errorCode("InvalidParameter").build()).build());
        assertThrows(OtpSmsRejectedException.class, () -> new AwsSnsSmsSender(sns, "", "Transactional").sendVerificationCode("+919876543210", "123456"));
    }
    @Test void throttlingIsUncertain() {
        when(sns.publish(any(PublishRequest.class))).thenThrow(SnsException.builder().statusCode(429).build());
        assertThrows(OtpDeliveryUncertainException.class, () -> new AwsSnsSmsSender(sns, "", "Transactional").sendVerificationCode("+919876543210", "123456"));
    }
}
