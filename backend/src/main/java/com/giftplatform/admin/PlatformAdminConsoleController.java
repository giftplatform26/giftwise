package com.giftplatform.admin;

import com.giftplatform.security.ActorContext;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@RestController
@RequestMapping("/api/admin/console")
public class PlatformAdminConsoleController {
 private static final Set<String> MODULES=Set.of("GROUP_GIFTING","GUEST_GIFTS","RSVP","GUEST_ATTENDANCE","WHATSAPP_SHARING","GUESTBOOK","SOCIAL_SHARING","MEMORY_ALBUM","AI_INVITATION","CELEBRATION_PLANNING","EVENT_ESSENTIALS","EVENT_REUSE");
 private final JdbcTemplate db;
 private final ActorContext actor;
 public PlatformAdminConsoleController(JdbcTemplate db,ActorContext actor){this.db=db;this.actor=actor;}
 private UUID admin(){if(!actor.isPlatformAdmin())throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Platform admin only");return actor.requireAuthenticatedUserId();}
 public record Toggle(boolean enabled){}
 public record Mode(String mode){}
 private String module(String code){if(code==null||!MODULES.contains(code))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Unknown module");return code;}
 private void audit(UUID admin,String action,UUID target,String code){db.update("INSERT INTO platform_admin_action_audit(id,actor_id,action,target_id,module_code) VALUES(?,?,?,?,?)",UUID.randomUUID(),admin,action,target,code);}
 @GetMapping("/summary")
 public Map<String,Object> summary(){admin();return Map.of(
  "hosts",db.queryForObject("SELECT count(*) FROM users WHERE role='CUSTOMER'",Long.class),
  "events",db.queryForObject("SELECT count(*) FROM events",Long.class),
  "publishedEvents",db.queryForObject("SELECT count(*) FROM events WHERE status='PUBLISHED'",Long.class),
  "groups",db.queryForObject("SELECT count(*) FROM gift_groups",Long.class));}
 @GetMapping("/hosts")
 public List<Map<String,Object>> hosts(@RequestParam(defaultValue="") String search){admin();String q=search.strip();if(q.length()>100)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Search too long");return db.queryForList("SELECT id,username,email FROM users WHERE role='CUSTOMER' AND (LOWER(username) LIKE LOWER(?) OR LOWER(email) LIKE LOWER(?)) ORDER BY username LIMIT 100","%"+q+"%","%"+q+"%");}
 @GetMapping("/modules")
 public List<Map<String,Object>> modules(){admin();return db.queryForList("SELECT module_code,enabled,changed_at FROM platform_module_controls ORDER BY module_code");}
 @PostMapping("/modules/{code}") @Transactional
 public Map<String,Object> platformModule(@PathVariable String code,@RequestBody Toggle input){UUID a=admin();code=module(code);if(input==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST);db.update("UPDATE platform_module_controls SET enabled=?,changed_by=?,changed_at=now() WHERE module_code=?",input.enabled(),a,code);audit(a,"PLATFORM_MODULE_CHANGED",null,code);return Map.of("module",code,"enabled",input.enabled());}
 @GetMapping("/hosts/{hostId}/modules")
 public List<Map<String,Object>> hostModules(@PathVariable UUID hostId){admin();return db.queryForList("SELECT p.module_code,p.enabled AS platform_enabled,COALESCE(h.enabled,FALSE) AS host_enabled,(p.enabled AND COALESCE(h.enabled,FALSE) AND (p.module_code<>'GUEST_GIFTS' OR EXISTS(SELECT 1 FROM platform_module_controls gp JOIN platform_host_module_grants gh ON gh.module_code=gp.module_code WHERE gp.module_code='GROUP_GIFTING' AND gp.enabled AND gh.host_id=h.host_id AND gh.enabled))) AS effective_enabled FROM platform_module_controls p LEFT JOIN platform_host_module_grants h ON h.module_code=p.module_code AND h.host_id=? ORDER BY p.module_code",hostId);}
 @PostMapping("/hosts/{hostId}/modules/{code}") @Transactional
 public Map<String,Object> hostModule(@PathVariable UUID hostId,@PathVariable String code,@RequestBody Toggle input){UUID a=admin();code=module(code);if(input==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST);Integer n=db.queryForObject("SELECT count(*) FROM users WHERE id=? AND role='CUSTOMER'",Integer.class,hostId);if(n==null||n!=1)throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Host not found");db.update("INSERT INTO platform_host_module_grants(host_id,module_code,enabled,changed_by) VALUES(?,?,?,?) ON CONFLICT(host_id,module_code) DO UPDATE SET enabled=EXCLUDED.enabled,changed_by=EXCLUDED.changed_by,changed_at=now()",hostId,code,input.enabled(),a);audit(a,"HOST_MODULE_CHANGED",hostId,code);return Map.of("hostId",hostId,"module",code,"enabled",input.enabled());}
 @GetMapping("/events")
 public List<Map<String,Object>> events(@RequestParam(defaultValue="") String search){admin();if(search.length()>100)throw new ResponseStatusException(HttpStatus.BAD_REQUEST);String q="%"+search.strip()+"%";return db.queryForList("SELECT e.id,e.title,e.status,e.occasion_type,e.owner_id,u.username AS host_name,COALESCE(a.enabled,FALSE) AS album_enabled,  (COALESCE(a.enabled,FALSE) AND EXISTS(SELECT 1 FROM platform_module_controls p JOIN platform_host_module_grants h ON h.module_code=p.module_code WHERE p.module_code='MEMORY_ALBUM' AND p.enabled AND h.host_id=e.owner_id AND h.enabled)) AS album_available,  COALESCE(ai.enabled,FALSE) AS ai_enabled,  (COALESCE(ai.enabled,FALSE) AND EXISTS(SELECT 1 FROM platform_module_controls p JOIN platform_host_module_grants h ON h.module_code=p.module_code WHERE p.module_code='AI_INVITATION' AND p.enabled AND h.host_id=e.owner_id AND h.enabled)) AS ai_available  FROM events e LEFT JOIN platform_event_ai_design_settings ai ON ai.event_id=e.id LEFT JOIN platform_event_album_settings a ON a.event_id=e.id LEFT JOIN users u ON u.id=e.owner_id WHERE LOWER(e.title) LIKE LOWER(?) OR LOWER(COALESCE(u.username,'')) LIKE LOWER(?) ORDER BY e.event_date DESC NULLS LAST LIMIT 100",q,q);}
 @PostMapping("/events/{eventId}/album-settings") @Transactional
 public Map<String,Object> albumSettings(@PathVariable UUID eventId,@RequestBody Toggle input){
  UUID a=admin();if(input==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
  if(!Integer.valueOf(1).equals(db.queryForObject("SELECT count(*) FROM events WHERE id=?",Integer.class,eventId)))throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Event not found");
  db.update("INSERT INTO platform_event_album_settings(event_id,enabled,changed_by) VALUES(?,?,?) ON CONFLICT(event_id) DO UPDATE SET enabled=EXCLUDED.enabled,changed_by=EXCLUDED.changed_by,changed_at=now()",eventId,input.enabled(),a);
  audit(a,"EVENT_ALBUM_CHANGED",eventId,"MEMORY_ALBUM");return Map.of("eventId",eventId,"enabled",input.enabled());
 }
 @PostMapping("/events/{eventId}/ai-design-settings") @Transactional
 public Map<String,Object> aiDesignSettings(@PathVariable UUID eventId,@RequestBody Toggle input){
  UUID a=admin();if(input==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
  if(!Integer.valueOf(1).equals(db.queryForObject("SELECT count(*) FROM events WHERE id=?",Integer.class,eventId)))throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Event not found");
  db.update("INSERT INTO platform_event_ai_design_settings(event_id,enabled,changed_by) VALUES(?,?,?) ON CONFLICT(event_id) DO UPDATE SET enabled=EXCLUDED.enabled,changed_by=EXCLUDED.changed_by,changed_at=now()",eventId,input.enabled(),a);
  audit(a,"EVENT_AI_DESIGN_CHANGED",eventId,"AI_INVITATION");return Map.of("eventId",eventId,"enabled",input.enabled());
 }
  // Read-only counts. Guest reports are self-reported, never financial settlement evidence.
  @GetMapping("/reports/group-transfers")
  public Map<String,Object> groupTransferReport(){
   admin();
   return Map.of(
    "reportedUnverified",db.queryForObject("SELECT COUNT(*) FROM gift_group_guest_reports WHERE state='REPORTED_UNVERIFIED'",Long.class),
    "reportedUnverifiedPaise",db.queryForObject("SELECT COALESCE(SUM(amount_paise),0) FROM gift_group_guest_reports WHERE state='REPORTED_UNVERIFIED'",Long.class),
    "verifiedRecords",db.queryForObject("SELECT COUNT(*) FROM gift_group_guest_reports WHERE state='VERIFIED'",Long.class),
    "rejectedRecords",db.queryForObject("SELECT COUNT(*) FROM gift_group_guest_reports WHERE state='REJECTED'",Long.class));
  }
  @GetMapping("/reports/guestbook")
  public Map<String,Object> guestbookReport(){
   admin();
   return Map.of(
    "pending",db.queryForObject("SELECT COUNT(*) FROM event_guestbook WHERE status='PENDING'",Long.class),
    "approved",db.queryForObject("SELECT COUNT(*) FROM event_guestbook WHERE status='APPROVED'",Long.class),
    "hidden",db.queryForObject("SELECT COUNT(*) FROM event_guestbook WHERE status='HIDDEN'",Long.class));
  }
  @GetMapping("/reports/notifications")
  public Map<String,Object> notificationReport(){
   admin();
   return Map.of("manualClaimOutbox",db.queryForObject("SELECT COUNT(*) FROM notification_outbox WHERE source_kind='MANUAL_CLAIM'",Long.class));
  }
 @GetMapping("/audit")
 public List<Map<String,Object>> audit(){admin();return db.queryForList("SELECT actor_id,action,target_id,module_code,created_at FROM platform_admin_action_audit ORDER BY created_at DESC LIMIT 100");}
}
