package com.giftplatform.admin;

import com.giftplatform.qr.design.ai.AiInvitationClient;
import com.giftplatform.security.ActorContext;
import java.util.*;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin/console")
public class AiInvitationAdminController {
 private final JdbcTemplate db;private final ActorContext actors;private final EffectiveModulePermissionService permissions;private final AiInvitationClient client;
 public AiInvitationAdminController(JdbcTemplate db,ActorContext actors,EffectiveModulePermissionService permissions,AiInvitationClient client){this.db=db;this.actors=actors;this.permissions=permissions;this.client=client;}
 private void admin(){if(!actors.isPlatformAdmin())throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Platform admin only");actors.requireAuthenticatedUserId();}
 private Map<String,Object> overview(UUID host,UUID event){
  var out=new LinkedHashMap<String,Object>();
  out.put("platformEnabled",Boolean.TRUE.equals(db.queryForObject("SELECT enabled FROM platform_module_controls WHERE module_code='AI_INVITATION'",Boolean.class)));
  out.put("hostGranted",Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM platform_host_module_grants WHERE host_id=? AND module_code='AI_INVITATION' AND enabled)",Boolean.class,host)));
  out.put("providerConfigured",client.available());
  var args=new ArrayList<Object>();args.add(host);if(event!=null)args.add(event);
  out.put("usage",db.queryForMap("SELECT count(*) AS requests,count(*) FILTER(WHERE status='COMPLETED') AS completed,count(*) FILTER(WHERE status='FAILED') AS failed,max(created_at) AS last_request_at FROM invitation_ai_requests WHERE host_id=? AND created_at>now()-interval '24 hours'"+(event==null?"":" AND event_id=?"),args.toArray()));
  return out;
 }
 @GetMapping("/hosts/{hostId}/ai-design")
 public ResponseEntity<Map<String,Object>> host(@PathVariable UUID hostId){
  admin();var rows=db.queryForList("SELECT id,username,email FROM users WHERE id=? AND role='CUSTOMER'",hostId);if(rows.isEmpty())throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Host not found");
  var out=overview(hostId,null);out.put("host",rows.getFirst());
  out.put("events",db.queryForList("SELECT e.id,e.title,e.status,COALESCE(a.enabled,FALSE) AS enabled FROM events e LEFT JOIN platform_event_ai_design_settings a ON a.event_id=e.id WHERE e.owner_id=? ORDER BY e.created_at DESC LIMIT 100",hostId));
  return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(out);
 }
 @GetMapping("/events/{eventId}/ai-design-settings")
 public ResponseEntity<Map<String,Object>> event(@PathVariable UUID eventId){
  admin();var rows=db.queryForList("SELECT e.id,e.title,e.status,e.occasion_type,e.event_date,e.host_display_name,e.owner_id,u.username AS host_name,u.email AS host_email,COALESCE(a.enabled,FALSE) AS enabled FROM events e JOIN users u ON u.id=e.owner_id LEFT JOIN platform_event_ai_design_settings a ON a.event_id=e.id WHERE e.id=?",eventId);
  if(rows.isEmpty())throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Event not found");
  var row=rows.getFirst();UUID host=(UUID)row.get("owner_id");var out=overview(host,eventId);out.put("event",row);out.put("effectiveEnabled",permissions.aiInvitationEnabled(host,eventId));
  out.put("detailsCollected",List.of("Event title and occasion","Names displayed on the invitation","Event date and time","Public venue","Language, tone and visual style","Personal message"));
  return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(out);
 }
}
