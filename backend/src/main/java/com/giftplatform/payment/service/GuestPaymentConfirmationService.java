package com.giftplatform.payment.service;

import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class GuestPaymentConfirmationService {
    private final RazorpayCaptureService capture;
    private final PaymentNotificationService notifications;
    public GuestPaymentConfirmationService(RazorpayCaptureService capture,
                                           PaymentNotificationService notifications) {
        this.capture = capture;
        this.notifications = notifications;
    }
    public RazorpayCaptureService.CaptureResult confirm(UUID localOrderId, String providerOrderId,
                                                         String paymentId, String signature) {
        var result = capture.confirmCheckout(localOrderId, providerOrderId, paymentId, signature);
        notifications.recordVerifiedContribution(result);
        return result;
    }
}
