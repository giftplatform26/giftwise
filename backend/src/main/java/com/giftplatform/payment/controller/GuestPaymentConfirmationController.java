package com.giftplatform.payment.controller;

import com.giftplatform.payment.service.GuestPaymentConfirmationService;
import com.giftplatform.payment.service.RazorpayCaptureService;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/public/payments")
public class GuestPaymentConfirmationController {
    private final GuestPaymentConfirmationService service;
    public GuestPaymentConfirmationController(GuestPaymentConfirmationService service) {
        this.service = service;
    }
    public record Request(UUID localOrderId, String razorpayOrderId,
                          String razorpayPaymentId, String razorpaySignature) {}
    @PostMapping("/confirm")
    public RazorpayCaptureService.CaptureResult confirm(@RequestBody Request request) {
        return service.confirm(request.localOrderId(), request.razorpayOrderId(),
                request.razorpayPaymentId(), request.razorpaySignature());
    }
}
