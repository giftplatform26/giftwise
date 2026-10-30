package com.giftplatform.gift.controller;

import com.giftplatform.gift.dto.GiftRequest;
import com.giftplatform.gift.dto.GiftResponse;
import com.giftplatform.gift.entity.GiftVisibility;
import jakarta.validation.constraints.Size;
import com.giftplatform.gift.service.GiftManagementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events/{eventId}/gifts")
public class GiftController {
    private final GiftManagementService service;
    public GiftController(GiftManagementService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<GiftResponse> create(@PathVariable UUID eventId, @Valid @RequestBody GiftRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(eventId, request));
    }
    @GetMapping
    public List<GiftResponse> list(@PathVariable UUID eventId) { return service.list(eventId); }
    @GetMapping("/{giftId}")
    public GiftResponse get(@PathVariable UUID eventId, @PathVariable UUID giftId) { return service.get(eventId, giftId); }
    @PutMapping("/{giftId}")
    public GiftResponse update(@PathVariable UUID eventId, @PathVariable UUID giftId, @Valid @RequestBody GiftRequest request) {
        return service.update(eventId, giftId, request);
    }
    public record PublicationRequest(GiftVisibility visibility) {}
    public record ProductLinkRequest(@Size(max = 2048) String productUrl) {}
    @PutMapping("/{giftId}/publication")
    public GiftResponse publication(@PathVariable UUID eventId, @PathVariable UUID giftId, @RequestBody PublicationRequest request) {
        if (request.visibility() == null) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST, "Visibility required");
        return service.setPublication(eventId, giftId, request.visibility());
    }
    @PutMapping("/{giftId}/product-link")
    public GiftResponse productLink(@PathVariable UUID eventId, @PathVariable UUID giftId, @Valid @RequestBody ProductLinkRequest request) {
        return service.setProductLink(eventId, giftId, request.productUrl());
    }
    @PostMapping("/{giftId}/cancel")
    public GiftResponse cancel(@PathVariable UUID eventId, @PathVariable UUID giftId) { return service.cancel(eventId, giftId); }
}
