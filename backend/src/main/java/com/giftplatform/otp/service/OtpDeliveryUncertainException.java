
package com.giftplatform.otp.service;

public class OtpDeliveryUncertainException extends RuntimeException {

    public OtpDeliveryUncertainException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}
