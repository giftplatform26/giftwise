package com.giftplatform.admin;

import com.giftplatform.security.ActorContext;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@RestController
@RequestMapping("/api/admin/console/events/{eventId}/modules")
public class EventModuleGrantsController {
 public static final Set<String> CODES=Set.of("CELEBRATION_PLANNING","EVENT_ESSENTIALS","EVENT_REUSE","LIVE_WALL","SHAGUN_REGISTER","EVENT_FUNCTIONS","CO_HOSTS","WHATSAPP_MESSAGING","MEMORY_BOOK","EXPERIENCE_GIFTS","PLANNING_CHECKLIST","EVENT_BUDGET","RUN_SHEET","CATERING_HEADCOUNT","GUEST_CHECKIN","LIVE_UPDATES","RETURN_GIFTS","STORY_CARDS","PARTNER_ACCOUNTS","GROUP_COLLECTIONS","GUEST_FIRST_FREE","CELEBRATION_SHOWCASE","SEATING_PLAN","GUEST_PASSES");
 private final JdbcTemplate db;private final ActorContext actor;private final EffectiveModulePermissionService permissions;
 public EventModuleGrantsController(JdbcTemplate db,ActorContext actor,EffectiveModulePermissionService permissions){this.db=db;this.actor=actor;this.permissions=permissions;}
 private UUID owner(UUID event){if(!actor.isPlatformAdmin())throw new ResponseStatusException(HttpStatus.FORBIDDEN);actor.requireAuthenticatedUserId();return db.query("SELECT owner_id FROM events WHERE id=?",(rs,i)->rs.getObject(1,UUID.class),event).stream().findFirst().orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));}
 @GetMapping public ResponseEntity<List<Map<String,Object>>> list(@PathVariable UUID eventId){UUID host=owner(eventId);return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(CODES.stream().sorted().map(code->{Map<String,Object> out=new LinkedHashMap<>();out.put("module",code);out.put("eventEnabled",Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM platform_event_module_grants WHERE event_id=? AND module_code=? AND enabled)",Boolean.class,eventId,code)));out.put("hostAvailable",permissions.enabled(host,code));out.put("effectiveEnabled",permissions.eventEnabled(host,eventId,code));return out;}).toList());}
 public record GrantChange(@jakarta.validation.constraints.NotNull Boolean enabled){}
 @PostMapping("/{code}") @Transactional
 public Map<String,Object> set(@PathVariable UUID eventId,@PathVariable String code,@jakarta.validation.Valid @RequestBody GrantChange input){owner(eventId);if(!CODES.contains(code)||input==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Unknown event module");UUID who=actor.requireAuthenticatedUserId();db.update("INSERT INTO platform_event_module_grants(event_id,module_code,enabled,changed_by) VALUES(?,?,?,?) ON CONFLICT(event_id,module_code) DO UPDATE SET enabled=EXCLUDED.enabled,changed_by=EXCLUDED.changed_by,changed_at=now()",eventId,code,input.enabled(),who);db.update("INSERT INTO platform_admin_action_audit(id,actor_id,action,target_id,module_code) VALUES(?,?,?,?,?)",UUID.randomUUID(),who,"EVENT_MODULE_CHANGED",eventId,code);return Map.of("enabled",input.enabled());}
}
