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
  Boolean value=db.queryForObject("SELECT EXISTS(SELECT 1 FROM platform_module_controls p JOIN platform_host_module_grants h ON h.module_code=p.module_code WHERE p.module_code=? AND p.enabled=TRUE AND h.host_id=? AND h.enabled=TRUE)",Boolean.class,module,hostId);
  return Boolean.TRUE.equals(value);
 }
 public Map<String,Boolean> modules(UUID hostId){
  Map<String,Boolean> result=new LinkedHashMap<>();
  for(String code:List.of("GROUP_GIFTING","RSVP","GUEST_ATTENDANCE","WHATSAPP_SHARING","GUESTBOOK","SOCIAL_SHARING"))result.put(code,false);
  if(hostId==null)return Map.copyOf(result);
  db.query("SELECT p.module_code,(p.enabled AND COALESCE(h.enabled,FALSE)) AS enabled FROM platform_module_controls p LEFT JOIN platform_host_module_grants h ON h.module_code=p.module_code AND h.host_id=?",
    (org.springframework.jdbc.core.RowCallbackHandler)rs->{String code=rs.getString("module_code");if(result.containsKey(code))result.put(code,rs.getBoolean("enabled"));},hostId);
  return Map.copyOf(result);
 }
 public void require(UUID hostId,String module){
  if(!enabled(hostId,module))throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Feature unavailable");
 }
 public String guestbookMode(UUID eventId){
  return db.query("SELECT display_mode FROM platform_event_message_settings WHERE event_id=?",(rs,i)->rs.getString(1),eventId)
   .stream().findFirst().orElse("AUTO");
 }
}
