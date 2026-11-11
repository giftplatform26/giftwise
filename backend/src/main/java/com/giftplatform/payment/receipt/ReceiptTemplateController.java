package com.giftplatform.payment.receipt;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/** Host-only receipt design for one event. Covered by the authenticated /api/v1/events/** rule. */
@RestController
@RequestMapping("/api/v1/events/{eventId}/receipt-template")
public class ReceiptTemplateController {
    private final ReceiptTemplateService receipts;
    public ReceiptTemplateController(ReceiptTemplateService receipts) { this.receipts = receipts; }

    @GetMapping
    public ResponseEntity<ReceiptTemplateService.Template> get(@PathVariable UUID eventId) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(receipts.get(eventId));
    }
    @PutMapping
    public ReceiptTemplateService.Template save(@PathVariable UUID eventId, @RequestBody ReceiptTemplateService.Input input) {
        return receipts.save(eventId, input);
    }
    @PostMapping(value = "/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ReceiptTemplateService.Template logo(@PathVariable UUID eventId, @RequestPart("file") MultipartFile file) throws IOException {
        return receipts.uploadLogo(eventId, file.getBytes());
    }
    @PostMapping("/logo/remove")
    public ReceiptTemplateService.Template removeLogo(@PathVariable UUID eventId) { return receipts.removeLogo(eventId); }

    /** Validation and conflict reasons are written for the host, so they are returned even when server messages are hidden. */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> reason(ResponseStatusException ex) {
        int code = ex.getStatusCode().value();
        if ((code == 400 || code == 409) && ex.getReason() != null) return ResponseEntity.status(code).body(Map.of("error", ex.getReason()));
        return ResponseEntity.status(ex.getStatusCode()).build();
    }
}
