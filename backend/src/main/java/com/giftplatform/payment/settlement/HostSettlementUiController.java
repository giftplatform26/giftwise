package com.giftplatform.payment.settlement;

import java.util.List;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Feature-gated host UI API. CSRF is enforced by the existing security filter. */
@RestController
@RequestMapping("/api/v1/host/settlement/events/{eventId}")
@ConditionalOnProperty(name="gift.payment.settlement-ui.enabled",havingValue="true")
public class HostSettlementUiController {
    private final HostSettlementUiService service;
    public HostSettlementUiController(HostSettlementUiService service) {this.service=service;}
    public record PayoutRequestBody(UUID destinationId,long amountPaise,String idempotencyKey) {}
    @GetMapping("/wallet")
    public ResponseEntity<HostSettlementUiService.Wallet> wallet(@PathVariable UUID eventId) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.wallet(eventId));
    }
    @GetMapping("/banks")
    public ResponseEntity<List<HostSettlementUiService.Bank>> banks(@PathVariable UUID eventId) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.banks(eventId));
    }
    @GetMapping("/payouts")
    public ResponseEntity<List<HostSettlementUiService.Payout>> payouts(@PathVariable UUID eventId) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.payouts(eventId));
    }
    @PostMapping("/payouts")
    public ResponseEntity<HostPayoutRequestService.Result> request(@PathVariable UUID eventId,
            @RequestBody PayoutRequestBody body) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
            .body(service.request(eventId,body.destinationId(),body.amountPaise(),body.idempotencyKey()));
    }
    @PostMapping("/payouts/{requestId}/cancel")
    public ResponseEntity<PayoutSafetyFinalizationService.Cancellation> cancel(
            @PathVariable UUID eventId,@PathVariable UUID requestId) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.cancel(eventId,requestId));
    }
}
