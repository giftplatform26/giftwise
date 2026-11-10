package com.giftplatform.admin;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
public class GrowthSettingsController {
 private final GrowthSettingsService service;
 public GrowthSettingsController(GrowthSettingsService service){this.service=service;}
 @GetMapping("/api/public/growth-settings")
 public ResponseEntity<GrowthSettingsService.Settings> current(){return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.current());}
 @PutMapping("/api/admin/console/growth-settings")
 public GrowthSettingsService.Settings save(@RequestBody GrowthSettingsService.Settings input){return service.update(input);}
}
