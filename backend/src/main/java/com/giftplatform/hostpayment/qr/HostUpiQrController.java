package com.giftplatform.hostpayment.qr;

import com.giftplatform.security.ActorContext;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/host/payment-profiles/upi/{accountId}/qr")
@ConditionalOnProperty(prefix="gift.payment.profiles",name="enabled",havingValue="true")
public class HostUpiQrController {
    private final UpiQrImageService service;
    private final ActorContext actors;
    public HostUpiQrController(UpiQrImageService service,ActorContext actors) {
        this.service=service; this.actors=actors;
    }
    @PostMapping(value="/upload",consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void upload(@PathVariable UUID accountId,@RequestPart("file") MultipartFile file) {
        if(file.isEmpty() || file.getSize()>UpiQrImageCodec.MAX_UPLOAD_BYTES)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid QR image size");
        try { service.upload(actors.requireAuthenticatedUserId(),accountId,file.getBytes()); }
        catch(java.io.IOException e) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Unreadable QR image"); }
        catch(IllegalArgumentException e) {
            String reason = e.getMessage();
            String safeReason = switch (reason == null ? "" : reason) {
                case "Invalid QR image size" -> "QR image size is invalid";
                case "Invalid QR image dimensions" -> "QR image dimensions are unsupported";
                case "Unsupported QR destination" -> "QR must contain a valid UPI payment link";
                case "Malformed UPI parameter", "Malformed UPI encoding" ->
                    "QR contains malformed UPI payment information";
                case "QR payee does not match registered UPI ID" ->
                    "QR UPI ID does not match the registered UPI account";
                default -> "Invalid or unreadable UPI QR image";
            };
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, safeReason);
        }
    }
    @PostMapping("/generate")
    public ResponseEntity<byte[]> generate(@PathVariable UUID accountId) {
        try { return png(service.generate(actors.requireAuthenticatedUserId(),accountId)); }
        catch(IllegalStateException e) { throw new ResponseStatusException(HttpStatus.CONFLICT,"UPI account not verified"); }
    }
    @GetMapping("/uploaded/status")
    public java.util.Map<String,Boolean> uploadedStatus(@PathVariable UUID accountId) {
        return java.util.Map.of("uploaded",service.hasUploaded(actors.requireAuthenticatedUserId(),accountId));
    }
    @GetMapping("/uploaded")
    public ResponseEntity<byte[]> uploaded(@PathVariable UUID accountId) {
        return png(service.uploaded(actors.requireAuthenticatedUserId(),accountId));
    }
    private static ResponseEntity<byte[]> png(byte[] bytes) {
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG)
            .header(HttpHeaders.CACHE_CONTROL,"no-store")
            .header("X-Content-Type-Options","nosniff")
            .body(bytes);
    }
}
