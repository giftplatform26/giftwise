package com.giftplatform.payment;

import com.giftplatform.payment.service.*;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GuestPaymentConfirmationServiceTest {
    @Test void recordsNotificationOnlyAfterVerifiedCapture() {
        var capture = mock(RazorpayCaptureService.class);
        var notifications = mock(PaymentNotificationService.class);
        var service = new GuestPaymentConfirmationService(capture, notifications);
        UUID order = UUID.randomUUID(), contribution = UUID.randomUUID();
        var result = new RazorpayCaptureService.CaptureResult(order, contribution, "pay_123", 500, 100, false);
        when(capture.confirmCheckout(order, "order_123", "pay_123", "sig")).thenReturn(result);
        assertEquals(result, service.confirm(order, "order_123", "pay_123", "sig"));
        var sequence = inOrder(capture, notifications);
        sequence.verify(capture).confirmCheckout(order, "order_123", "pay_123", "sig");
        sequence.verify(notifications).recordVerifiedContribution(result);
    }
    @Test void doesNotNotifyWhenCaptureFails() {
        var capture = mock(RazorpayCaptureService.class);
        var notifications = mock(PaymentNotificationService.class);
        var service = new GuestPaymentConfirmationService(capture, notifications);
        UUID order = UUID.randomUUID();
        when(capture.confirmCheckout(order, "order_123", "pay_123", "bad"))
            .thenThrow(new IllegalStateException("verification failed"));
        assertThrows(IllegalStateException.class, () -> service.confirm(order, "order_123", "pay_123", "bad"));
        verifyNoInteractions(notifications);
    }
}
