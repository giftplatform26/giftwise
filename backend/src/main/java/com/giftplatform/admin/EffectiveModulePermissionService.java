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
  for(String code:List.of("GROUP_GIFTING","GUEST_GIFTS","RSVP","GUEST_ATTENDANCE","WHATSAPP_SHARING","GUESTBOOK","SOCIAL_SHARING","MEMORY_ALBUM","AI_INVITATION","CELEBRATION_PLANNING","EVENT_ESSENTIALS","EVENT_REUSE","LIVE_WALL","SHAGUN_REGISTER","GIFT_LEDGER","EVENT_FUNCTIONS","CO_HOSTS","WHATSAPP_MESSAGING","OCCASION_REMINDERS","HOST_REFERRALS","YEARLY_REHOST","MEMORY_BOOK","VENDOR_DIRECTORY","EXPERIENCE_GIFTS","PLANNING_CHECKLIST","EVENT_BUDGET","RUN_SHEET","CATERING_HEADCOUNT","GUEST_CHECKIN","LIVE_UPDATES","RETURN_GIFTS","TRY_WITHOUT_ACCOUNT","STORY_CARDS","PARTNER_ACCOUNTS","GROUP_COLLECTIONS","GUEST_FIRST_FREE","CELEBRATION_SHOWCASE","SEATING_PLAN","GUEST_PASSES","HOST_NOTIFICATIONS","GUEST_HOUSEHOLDS","GUEST_TRAVEL","SHARED_ALBUM","GUEST_FEEDBACK","VENDOR_BOOKINGS","BUDGET_SHARING"))result.put(code,false);
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
 /**
  * Every module's effective state for one event, in two queries (host-level modules, then the event's own switches).
  * Same rules as enabled/eventEnabled/albumEnabled/aiInvitationEnabled; this runs on every invitation view, so it must stay cheap.
  */
 public Map<String,Boolean> eventModules(UUID hostId,UUID eventId){
  var result=new LinkedHashMap<>(modules(hostId));
  var rows=hostId==null?List.<Map<String,Object>>of():db.queryForList("""
    SELECT e.gifting_enabled, COALESCE(a.enabled,FALSE) AS album, COALESCE(ai.enabled,FALSE) AS ai,
           ARRAY(SELECT g.module_code FROM platform_event_module_grants g WHERE g.event_id=e.id AND g.enabled) AS grants
    FROM events e LEFT JOIN platform_event_album_settings a ON a.event_id=e.id LEFT JOIN platform_event_ai_design_settings ai ON ai.event_id=e.id
    WHERE e.id=? AND e.owner_id=?""",eventId,hostId);
  boolean owned=!rows.isEmpty();
  var row=owned?rows.getFirst():Map.<String,Object>of();
  java.util.Set<String> grants=new java.util.HashSet<>();
  if(owned&&row.get("grants") instanceof java.sql.Array array){try{for(Object o:(Object[])array.getArray())grants.add(String.valueOf(o));}catch(java.sql.SQLException ex){throw new IllegalStateException(ex);}}
  result.put("MEMORY_ALBUM",Boolean.TRUE.equals(result.get("MEMORY_ALBUM"))&&owned&&Boolean.TRUE.equals(row.get("album")));
  result.put("AI_INVITATION",Boolean.TRUE.equals(result.get("AI_INVITATION"))&&owned&&Boolean.TRUE.equals(row.get("ai")));
  for(String code:EventModuleGrantsController.CODES)result.put(code,Boolean.TRUE.equals(result.get(code))&&grants.contains(code));
  if(!Boolean.TRUE.equals(result.get("CELEBRATION_PLANNING")))result.put("EVENT_ESSENTIALS",false);
  boolean gifting=owned&&Boolean.TRUE.equals(row.get("gifting_enabled"));
  result.put("GIFTING_ENABLED",gifting);
  if(!gifting){result.put("GROUP_GIFTING",false);result.put("GUEST_GIFTS",false);}
  return Map.copyOf(result);
 }
 public boolean eventEnabled(UUID hostId,UUID eventId,String code){
  if("EVENT_ESSENTIALS".equals(code)&&!eventEnabled(hostId,eventId,"CELEBRATION_PLANNING"))return false;
  return enabled(hostId,code)&&Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM platform_event_module_grants g JOIN events e ON e.id=g.event_id WHERE g.event_id=? AND e.owner_id=? AND g.module_code=? AND g.enabled)",Boolean.class,eventId,hostId,code));
 }
 public void requireEvent(UUID hostId,UUID eventId,String code){if(!eventEnabled(hostId,eventId,code))throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Feature unavailable for this event");}
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
