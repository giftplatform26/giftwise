package com.giftplatform.security.verification;

import com.giftplatform.security.ActorContext;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@RestController
@RequestMapping("/api/auth/contact-mobile")
public class AccountMobileVerificationController {
 private final MobileVerificationService service;private final ActorContext actor;
 public AccountMobileVerificationController(MobileVerificationService service,ActorContext actor){this.service=service;this.actor=actor;}
 public record Start(String mobile){}
 public record Complete(UUID challengeId,String code){}
 private String kind(){return actor.isPlatformAdmin()?"PLATFORM_ADMIN":"HOST";}
 @GetMapping("/status") public MobileVerificationService.Status status(){return service.status(kind(),actor.requireAuthenticatedUserId());}
 @PostMapping("/start") public MobileVerificationService.Started start(@RequestBody Start input){if(input==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST);return service.start(kind(),actor.requireAuthenticatedUserId(),input.mobile());}
 @PostMapping("/complete") public Map<String,String> complete(@RequestBody Complete input){if(input==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST);service.complete(kind(),actor.requireAuthenticatedUserId(),input.challengeId(),input.code());return Map.of("message","Mobile number verified");}
}
