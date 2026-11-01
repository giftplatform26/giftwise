package com.giftplatform.admin;

import com.giftplatform.event.service.EventManagementService;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.UUID;

@RestController
public class ExperienceSettingsController {
 private final ExperienceSettingsService settings;
 private final EventManagementService events;
 private final EffectiveModulePermissionService permissions;
 public ExperienceSettingsController(ExperienceSettingsService settings,EventManagementService events,EffectiveModulePermissionService permissions){this.settings=settings;this.events=events;this.permissions=permissions;}
 @GetMapping("/api/public/experience")
 public ResponseEntity<ExperienceSettingsService.Settings> current(){return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(settings.current());}
 @PostMapping("/api/admin/console/experience")
 public ExperienceSettingsService.Settings update(@RequestBody ExperienceSettingsService.Settings input){return settings.update(input);}
 @GetMapping("/api/v1/events/{id}/features")
 public ResponseEntity<Map<String,Boolean>> features(@PathVariable UUID id){
  var event=events.get(id); // Existing ownership check also covers draft previews.
  return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(permissions.eventModules(event.ownerId(),id));
 }
}
