package com.giftplatform.admin;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.UUID;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;

@Service
public class EffectiveModulePermissionService {
 private final JdbcTemplate db;
 public EffectiveModulePermissionService(JdbcTemplate db){this.db=db;}
 public boolean enabled(UUID hostId,String module){
  if(hostId==null)return false;
  if("GUEST_GIFTS".equals(module)&&!enabled(hostId,"GROUP_GIFTING"))return false;
  Boolean value=db.queryForObject("SELECT EXISTS(SELECT 1 FROM platform_module_controls p JOIN platform_host_module_grants h ON h.module_code=p.module_code WHERE p.module_code=? AND p.enabled=TRUE AND h.host_id=? AND h.enabled=TRUE)",Boolean.class,module,hostId);
  return Boolean.TRUE.equals(value);
 }
 public Map<String,Boolean> modules(UUID hostId){
  Map<String,Boolean> result=new LinkedHashMap<>();
  for(String code:List.of("GROUP_GIFTING","GUEST_GIFTS","RSVP","GUEST_ATTENDANCE","WHATSAPP_SHARING","GUESTBOOK","SOCIAL_SHARING","MEMORY_ALBUM","AI_INVITATION"))result.put(code,false);
  if(hostId==null)return Map.copyOf(result);
  db.query("SELECT p.module_code,(p.enabled AND COALESCE(h.enabled,FALSE)) AS enabled FROM platform_module_controls p LEFT JOIN platform_host_module_grants h ON h.module_code=p.module_code AND h.host_id=?",
    (org.springframework.jdbc.core.RowCallbackHandler)rs->{String code=rs.getString("module_code");if(result.containsKey(code))result.put(code,rs.getBoolean("enabled"));},hostId);
  result.put("GUEST_GIFTS",Boolean.TRUE.equals(result.get("GUEST_GIFTS"))&&Boolean.TRUE.equals(result.get("GROUP_GIFTING")));
  return Map.copyOf(result);
 }
 public void require(UUID hostId,String module){
  if(!enabled(hostId,module))throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Feature unavailable");
 }
 public boolean albumEnabled(UUID hostId,UUID eventId){
  return enabled(hostId,"MEMORY_ALBUM")&&Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM platform_event_album_settings a JOIN events e ON e.id=a.event_id WHERE a.event_id=? AND e.owner_id=? AND a.enabled)",Boolean.class,eventId,hostId));
 }
 public Map<String,Boolean> eventModules(UUID hostId,UUID eventId){
  var result=new LinkedHashMap<>(modules(hostId));result.put("MEMORY_ALBUM",albumEnabled(hostId,eventId));result.put("AI_INVITATION",aiInvitationEnabled(hostId,eventId));return Map.copyOf(result);
 }
 public void requireAlbum(UUID hostId,UUID eventId){
  if(!albumEnabled(hostId,eventId))throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Memory album unavailable");
 }
 public boolean aiInvitationEnabled(UUID hostId,UUID eventId){
  return enabled(hostId,"AI_INVITATION")&&Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM platform_event_ai_design_settings a JOIN events e ON e.id=a.event_id WHERE a.event_id=? AND e.owner_id=? AND a.enabled)",Boolean.class,eventId,hostId));
 }
 public void requireAiInvitation(UUID hostId,UUID eventId){
  if(!aiInvitationEnabled(hostId,eventId))throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Design with AI is not enabled for this host and event");
 }
 public String guestbookMode(UUID eventId){
  return db.query("SELECT display_mode FROM platform_event_message_settings WHERE event_id=?",(rs,i)->rs.getString(1),eventId)
   .stream().findFirst().orElse("AUTO");
 }
}
