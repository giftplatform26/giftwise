package com.giftplatform.payment.upi;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

/** Public guest endpoints for direct UPI intent payments. CSRF-protected; rate limited per client and invitation. */
@RestController
@RequestMapping("/api/public/payments/upi-intent/{token}/attempts")
public class UpiIntentPaymentController {
    private final UpiIntentPaymentService service;
    private final PaymentReportRateLimiter limiter;

    public UpiIntentPaymentController(UpiIntentPaymentService service, PaymentReportRateLimiter limiter) {
        this.service = service; this.limiter = limiter;
    }

    @PostMapping
    public ResponseEntity<UpiIntentPaymentService.AttemptView> start(@PathVariable UUID token,
            @RequestBody UpiIntentPaymentService.StartRequest body, HttpServletRequest request) {
        limiter.check(PaymentReportRateLimiter.Action.START, request.getRemoteAddr(), token);
        return ok(service.start(token, body));
    }

    @GetMapping("/{attemptId}")
    public ResponseEntity<UpiIntentPaymentService.AttemptView> status(@PathVariable UUID token, @PathVariable UUID attemptId) {
        return ok(service.status(token, attemptId));
    }

    @PostMapping("/{attemptId}/response")
    public ResponseEntity<UpiIntentPaymentService.AttemptView> response(@PathVariable UUID token, @PathVariable UUID attemptId,
            @RequestBody UpiIntentPaymentService.ResponseRequest body, HttpServletRequest request) {
        limiter.check(PaymentReportRateLimiter.Action.RESPONSE, request.getRemoteAddr(), token);
        return ok(service.recordResponse(token, attemptId, body));
    }

    @PostMapping("/{attemptId}/report")
    public ResponseEntity<UpiIntentPaymentService.AttemptView> report(@PathVariable UUID token, @PathVariable UUID attemptId,
            @RequestBody UpiIntentPaymentService.ReportRequest body, HttpServletRequest request) {
        limiter.check(PaymentReportRateLimiter.Action.REPORT, request.getRemoteAddr(), token);
        return ok(service.report(token, attemptId, body));
    }

    @PostMapping(path = "/{attemptId}/evidence", consumes = "multipart/form-data")
    public ResponseEntity<UpiIntentPaymentService.AttemptView> evidence(@PathVariable UUID token, @PathVariable UUID attemptId,
            @RequestPart("file") MultipartFile file, HttpServletRequest request) throws IOException {
        limiter.check(PaymentReportRateLimiter.Action.EVIDENCE, request.getRemoteAddr(), token);
        if (file == null || file.isEmpty() || file.getSize() > PaymentEvidenceImage.MAX_UPLOAD_BYTES)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a PNG or JPEG screenshot up to 5 MB.");
        return ok(service.attachEvidence(token, attemptId, file.getBytes()));
    }

    private static <T> ResponseEntity<T> ok(T body) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).header("X-Content-Type-Options", "nosniff").body(body);
    }
}
