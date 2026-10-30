package com.giftplatform.payment.direct;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.util.List;
import java.util.UUID;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@ConditionalOnProperty(name="gift.payment.profiles.enabled",havingValue="true")
@RestController
@RequestMapping("/api/public/payments/verified-destinations")
public class GuestVerifiedDestinationController {
 private final GuestVerifiedDestinationService service;
 public GuestVerifiedDestinationController(GuestVerifiedDestinationService service){this.service=service;}
 @GetMapping("/{token}")
 public ResponseEntity<List<GuestVerifiedDestinationService.Destination>> list(@PathVariable UUID token){
  return ResponseEntity.ok().cacheControl(CacheControl.noStore()).header("X-Content-Type-Options","nosniff")
   .body(service.list(token));
 }
}
