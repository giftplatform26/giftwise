package com.giftplatform.publicevent;

import com.giftplatform.admin.*;
import com.giftplatform.security.ActorContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

/** Share approved public wishes, never private album media or RSVP/contact information. */
@RestController
public class PublicKeepsakeController {
 private final JdbcTemplate db;private final ActorContext actor;private final GrowthSettingsService growth;private final EffectiveModulePermissionService permissions;
 public PublicKeepsakeController(JdbcTemplate db,ActorContext actor,GrowthSettingsService growth,EffectiveModulePermissionService permissions){this.db=db;this.actor=actor;this.growth=growth;this.permissions=permissions;}
 public record Consent(@NotNull Boolean enabled){}
 private Map<String,Object> owned(UUID id){return db.queryForList("SELECT owner_id,status,keepsake_enabled,public_token FROM events WHERE id=? AND owner_id=?",id,actor.requireAuthenticatedUserId()).stream().findFirst().orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Event not found"));}
 private Map<String,Object> preference(Map<String,Object> event){return Map.of("enabled",event.get("keepsake_enabled"),"platformEnabled",growth.enabled("PUBLIC_KEEPSAKES"),"guestbookEnabled",permissions.enabled((UUID)event.get("owner_id"),"GUESTBOOK"),"token",event.get("public_token"),"status",event.get("status"));}
 @GetMapping("/api/v1/events/{id}/keepsake-sharing")
 public ResponseEntity<Map<String,Object>> get(@PathVariable UUID id){return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(preference(owned(id)));}
 @PutMapping("/api/v1/events/{id}/keepsake-sharing") @Transactional
 public Map<String,Object> save(@PathVariable UUID id,@Valid @RequestBody Consent input){var e=owned(id);
  if(input.enabled()){
   if(!growth.enabled("PUBLIC_KEEPSAKES"))throw new ResponseStatusException(HttpStatus.CONFLICT,"Public keepsakes are disabled by the administrator");
   permissions.require((UUID)e.get("owner_id"),"GUESTBOOK");
   if(!Set.of("PUBLISHED","COMPLETED").contains(e.get("status")))throw new ResponseStatusException(HttpStatus.CONFLICT,"Publish or complete this event before sharing a keepsake");
  }
  db.update("UPDATE events SET keepsake_enabled=? WHERE id=?",input.enabled(),id);db.update("INSERT INTO platform_admin_action_audit(id,actor_id,action,target_id) VALUES(?,?,?,?)",UUID.randomUUID(),actor.requireAuthenticatedUserId(),input.enabled()?"KEEPSAKE_SHARING_ENABLED":"KEEPSAKE_SHARING_DISABLED",id);return preference(owned(id));
 }
 @GetMapping("/api/public/keepsakes/{token}")
 public ResponseEntity<Map<String,Object>> view(@PathVariable UUID token,@RequestParam(defaultValue="0") int page){
  if(page<0||page>10000)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Choose a valid page");
  if(!growth.enabled("PUBLIC_KEEPSAKES"))throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Keepsake unavailable");
  var e=db.queryForList("SELECT id,owner_id,title,event_date FROM events WHERE public_token=? AND status IN ('PUBLISHED','COMPLETED') AND keepsake_enabled",token).stream().findFirst().orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Keepsake unavailable"));
  permissions.require((UUID)e.get("owner_id"),"GUESTBOOK");
  var wishes=db.queryForList("SELECT id,guest_name,message FROM event_guestbook WHERE event_id=? AND status='APPROVED' ORDER BY created_at DESC,id LIMIT 21 OFFSET ?",e.get("id"),page*20);
  Map<String,Object> result=new LinkedHashMap<>();result.put("title",e.get("title"));result.put("date",e.get("event_date"));result.put("wishes",wishes.stream().limit(20).toList());result.put("hasMore",wishes.size()>20);result.put("page",page);
  return ResponseEntity.ok().cacheControl(CacheControl.noStore()).header("X-Robots-Tag","noindex, nofollow, noarchive").body(result);
 }
}
