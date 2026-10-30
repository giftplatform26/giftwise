package com.giftplatform.qr.design.ai;

import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events/{eventId}/qr/ai-design")
public class AiInvitationController {
    private final AiInvitationService service;
    public AiInvitationController(AiInvitationService service){this.service=service;}
    @GetMapping("/context") public ResponseEntity<AiInvitationService.EventContext> context(@PathVariable UUID eventId){return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.context(eventId));}
    @GetMapping public ResponseEntity<AiInvitationService.Settings> settings(@PathVariable UUID eventId){return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.settings(eventId));}
    @PostMapping public ResponseEntity<AiInvitationService.Suggestion> generate(@PathVariable UUID eventId,@Valid @RequestBody AiInvitationService.Preferences input){return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.generate(eventId,input));}
}
