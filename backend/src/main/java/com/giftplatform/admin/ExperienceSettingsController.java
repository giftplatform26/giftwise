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
 private final com.giftplatform.cohost.EventAccess access;
 public ExperienceSettingsController(ExperienceSettingsService settings,EventManagementService events,EffectiveModulePermissionService permissions,com.giftplatform.cohost.EventAccess access){this.settings=settings;this.events=events;this.permissions=permissions;this.access=access;}
 @GetMapping("/api/public/experience")
 public ResponseEntity<ExperienceSettingsService.Settings> current(){return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(settings.current());}
 @PostMapping("/api/admin/console/experience")
 public ExperienceSettingsService.Settings update(@RequestBody ExperienceSettingsService.Settings input){return settings.update(input);}
 @GetMapping("/api/v1/events/{id}/features")
 public ResponseEntity<Map<String,Boolean>> features(@PathVariable UUID id){
  // Owner or an active co-host/helper; flags are always evaluated for the owner.
  var event=access.require(id,com.giftplatform.cohost.EventAccess.Capability.REGISTER_ADD,null).event();
  return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(permissions.eventModules(event.getOwnerId(),id));
 }
}
