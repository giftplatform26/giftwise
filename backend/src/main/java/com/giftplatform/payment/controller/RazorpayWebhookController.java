package com.giftplatform.payment.controller;

import com.giftplatform.payment.service.RazorpayWebhookService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks/razorpay")
public class RazorpayWebhookController {
    private final RazorpayWebhookService service;

    public RazorpayWebhookController(RazorpayWebhookService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Void> receive(
            @RequestBody byte[] body,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature,
            @RequestHeader(value = "X-Razorpay-Event-Id", required = false) String eventId) {
        service.receive(body, signature, eventId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
