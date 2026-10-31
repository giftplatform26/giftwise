package com.giftplatform.monetization;

import java.util.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/console/monetization")
public class MonetizationController {
    private final MonetizationService service;
    public MonetizationController(MonetizationService service) {this.service=service;}
    @GetMapping public ResponseEntity<MonetizationService.Settings> current() {
        service.requireAdmin();
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.current());
    }
    @PutMapping public MonetizationService.Settings update(@RequestBody MonetizationService.Update input) {return service.update(input);}
    @GetMapping("/history") public ResponseEntity<List<Map<String,Object>>> history(
            @RequestParam(defaultValue="50") int limit,@RequestParam(defaultValue="9223372036854775807") long beforeRevision) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.history(limit,beforeRevision));
    }
}
