package com.giftplatform.payment.hybrid;

import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/payments/hybrid")
public class HybridCheckoutQuoteController {
    private final HybridCheckoutQuoteService service;
    public HybridCheckoutQuoteController(HybridCheckoutQuoteService service) { this.service=service; }
    public record Request(UUID eventId, UUID giftId, long contributionPaise) {}
    @PostMapping("/quote")
    public HybridCheckoutQuoteService.Quote quote(@RequestBody Request request) {
        if (request == null) throw new IllegalArgumentException("Request required");
        return service.quote(request.eventId(),request.giftId(),request.contributionPaise());
    }
}
