package com.giftplatform.qr;

import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/events/{eventId}/qr")
public class EventQrController {
    private final EventQrService service;
    public EventQrController(EventQrService service) { this.service = service; }
    @GetMapping public EventQrService.QrDetails details(@PathVariable UUID eventId) { return service.details(eventId); }
    @GetMapping(value = "/png", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> png(@PathVariable UUID eventId) {
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(service.png(eventId));
    }
    @PutMapping("/theme")
    public EventQrService.QrDetails theme(@PathVariable UUID eventId, @RequestBody ThemeRequest request) {
        return service.changeTheme(eventId, request.theme());
    }
    public record ThemeRequest(QrTheme theme) {}
}
