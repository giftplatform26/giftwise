package com.giftplatform.qr.design.ai;

import java.util.List;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
public class AiInvitationOptionsController {
 private final AiInvitationService service;
 public AiInvitationOptionsController(AiInvitationService service){this.service=service;}
 @GetMapping("/api/v1/events/ai-design-options")
 public ResponseEntity<List<AiInvitationService.Option>> options(){return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.options());}
}
