package com.giftplatform.notification;
import java.util.*;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1/events/{eventId}/thank-you")
public class HostThankYouController {
 private final HostThankYouService service;
 public HostThankYouController(HostThankYouService service){this.service=service;}
 @GetMapping public ResponseEntity<HostThankYouService.Settings> get(@PathVariable UUID eventId){
  return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.settings(eventId));
 }
 @PutMapping public ResponseEntity<HostThankYouService.Settings> put(@PathVariable UUID eventId,@RequestBody HostThankYouService.Update r){
  return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.update(eventId,r));
 }
 @GetMapping("/history") public ResponseEntity<List<Map<String,Object>>> history(@PathVariable UUID eventId){
  return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.history(eventId));
 }
}
