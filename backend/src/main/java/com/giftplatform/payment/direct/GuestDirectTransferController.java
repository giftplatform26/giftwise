package com.giftplatform.payment.direct;

import java.util.List;
import java.util.UUID;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/payments/direct")
public class GuestDirectTransferController {
 private final GuestDirectTransferService service;
 public GuestDirectTransferController(GuestDirectTransferService service){this.service=service;}
 @GetMapping("/{token}/destinations")
 public ResponseEntity<List<GuestDirectTransferService.Destination>> destinations(@PathVariable UUID token){
  return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.destinations(token));
 }
 @PostMapping("/{token}/claims")
 public ResponseEntity<GuestDirectTransferService.ClaimResult> claim(@PathVariable UUID token,
  @RequestBody GuestDirectTransferService.ClaimRequest request){
  return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.claim(token,request));
 }
 @GetMapping("/receipts/{receiptToken}")
 public ResponseEntity<GuestDirectTransferService.Receipt> receipt(@PathVariable UUID receiptToken){
  return ResponseEntity.ok().cacheControl(CacheControl.noStore()).header("X-Robots-Tag","noindex").body(service.receipt(receiptToken));
 }
}
