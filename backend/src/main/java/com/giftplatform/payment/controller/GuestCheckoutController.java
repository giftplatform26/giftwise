package com.giftplatform.payment.controller;

import com.giftplatform.payment.service.GuestCheckoutService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/payments")
public class GuestCheckoutController {
    private final GuestCheckoutService service;

    public GuestCheckoutController(GuestCheckoutService service) {
        this.service = service;
    }

    @PostMapping("/checkout")
    public GuestCheckoutService.Response checkout(
            @RequestBody GuestCheckoutService.Request request) {
        return service.create(request);
    }
}
