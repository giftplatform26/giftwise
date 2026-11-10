package com.giftplatform.otp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sns.model.SnsException;

import java.util.Map;

@Component
@ConditionalOnProperty(prefix = "gift.sms", name = "provider", havingValue = "sns")
public class AwsSnsSmsSender implements SmsSender {
    @org.springframework.beans.factory.annotation.Autowired(required=false) private com.giftplatform.localization.LocalizationService localization;
    private final SnsClient sns;
    private final String senderId;
    private final String smsType;

    public AwsSnsSmsSender(SnsClient sns,
            @Value("${gift.sms.aws.sender-id:}") String senderId,
            @Value("${gift.sms.aws.sms-type:Transactional}") String smsType) {
        this.sns = sns;
        this.senderId = senderId == null ? "" : senderId.trim();
        if (!"Transactional".equals(smsType) && !"Promotional".equals(smsType)) {
            throw new IllegalArgumentException("Invalid SNS SMS type");
        }
        this.smsType = smsType;
    }

    @Override
    public void sendVerificationCode(String phoneNumber, String otp) {
        if (phoneNumber == null || !phoneNumber.matches("^\\+[1-9][0-9]{1,14}$")) {
            throw new IllegalArgumentException("Invalid phone number");
        }
        if (otp == null || !otp.matches("^[0-9]{6}$")) {
            throw new IllegalArgumentException("Invalid OTP format");
        }
        var attrs = new java.util.HashMap<String, MessageAttributeValue>();
        attrs.put("AWS.SNS.SMS.SMSType", attr(smsType));
        if (!senderId.isBlank()) attrs.put("AWS.SNS.SMS.SenderID", attr(senderId));
        PublishRequest request = PublishRequest.builder()
                .phoneNumber(phoneNumber)
                .message(localization==null?"Your Gift Platform verification code is " + otp + ". Do not share this code.":localization.render("sms.sns.code",Map.of("code",otp),org.springframework.context.i18n.LocaleContextHolder.getLocale().toLanguageTag()))
                .messageAttributes(attrs)
                .build();
        try {
            PublishResponse response = sns.publish(request);
            if (response == null || response.messageId() == null || response.messageId().isBlank()) {
                throw new OtpDeliveryUncertainException("SNS acceptance could not be confirmed", null);
            }
        } catch (SnsException ex) {
            // Only known, non-retryable validation rejections can be treated as definitely rejected.
            String code = ex.awsErrorDetails() == null ? "" : ex.awsErrorDetails().errorCode();
            if (ex.statusCode() == 400 && ("InvalidParameter".equals(code)
                    || "InvalidParameterValue".equals(code))) {
                throw new OtpSmsRejectedException("AWS SNS rejected SMS");
            }
            throw new OtpDeliveryUncertainException("SNS delivery acceptance is uncertain", ex);
        } catch (SdkClientException ex) {
            throw new OtpDeliveryUncertainException("SNS delivery acceptance is uncertain", ex);
        }
    }

    private static MessageAttributeValue attr(String value) {
        return MessageAttributeValue.builder().dataType("String").stringValue(value).build();
    }
}
