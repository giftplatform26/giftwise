package com.giftplatform.analytics;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
public class InteractionService {
 private final JdbcTemplate db;private final InteractionScope scopes;
 @org.springframework.beans.factory.annotation.Autowired private com.giftplatform.admin.GrowthSettingsService growth;
 private boolean collectionEnabled(){return growth==null||growth.enabled("INTERACTION_ANALYTICS");}
 public InteractionService(JdbcTemplate db,InteractionScope scopes){this.db=db;this.scopes=scopes;}
 public record Input(@NotNull UUID id,UUID parentId,@Pattern(regexp="CLICK|PAGE_VIEW|OPERATION") @NotNull String kind,
  @Pattern(regexp="[A-Z][A-Z0-9_]{0,63}") @NotNull String action,@Size(max=240) @NotNull String page,
  @Size(max=160) String control,@Size(max=240) String destination,
  @Pattern(regexp="CLICKED|OPENED|HANDED_OFF|BROWSER_READY|SUCCESS|FAILURE|CANCELLED|UNKNOWN") @NotNull String outcome,
  @Pattern(regexp="[A-Z0-9_]{0,120}") String detail,UUID eventId,UUID token,@Min(0) @Max(86400000) Long durationMs){}
 // Only route structure is stored: never query strings, public tokens, names, emails, or entered values.
 public static String safeRoute(String value){
  if(value==null)return "";
  if(!value.startsWith("/"))return Set.of("external","upi","mailto","tel","blob").contains(value)?value:"external";
  String path=value.split("[?#]",2)[0];
  return Arrays.stream(path.split("/",-1)).map(segment->segment.matches("[a-z][a-z-]{0,40}")||segment.isEmpty()?segment:":id").collect(java.util.stream.Collectors.joining("/"));
 }
 public static String visitor(HttpServletRequest request){if(request.getAttribute("gift.analytics.visitor") instanceof String key)return key;try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(request.getSession().getId().getBytes(StandardCharsets.UTF_8)));}catch(java.security.NoSuchAlgorithmException e){throw new IllegalStateException(e);}}
 public void browser(Input input,HttpServletRequest request){
  if(!collectionEnabled())return;
  var s=scopes.resolve(input.eventId(),input.token());
  db.update("""
   INSERT INTO application_interactions(id,parent_id,visitor_key,kind,source,actor_id,actor_role,host_id,event_id,action,page,control,destination,outcome,detail,duration_ms)
   VALUES(?,?,?,?,'BROWSER',?,?,?,?,?,?,?,?,?,?,?)
   ON CONFLICT(id) DO UPDATE SET action=EXCLUDED.action,outcome=EXCLUDED.outcome,detail=EXCLUDED.detail,duration_ms=EXCLUDED.duration_ms,updated_at=now()
   WHERE application_interactions.visitor_key=EXCLUDED.visitor_key AND application_interactions.source='BROWSER'
     AND application_interactions.kind=EXCLUDED.kind
   """,input.id(),input.parentId(),visitor(request),input.kind(),s.actor(),s.role(),s.host(),s.event(),input.action(),safeRoute(input.page()),safeControl(input.control()),safeRoute(input.destination()),input.outcome(),input.detail()==null?"":input.detail(),input.durationMs());
 }
 private String safeControl(String value){return value!=null&&value.matches("[a-zA-Z0-9_:.>() -]{0,160}")?value:"control";}
 public void request(HttpServletRequest request,int status,long duration,UUID event,UUID token,String action){
  if(!collectionEnabled())return;
  var s=scopes.resolve(event,token);UUID parent=null;
  try{parent=UUID.fromString(request.getHeader("X-Gift-Interaction"));}catch(Exception ignored){}
  db.update("""
   INSERT INTO application_interactions(id,parent_id,visitor_key,kind,source,actor_id,actor_role,host_id,event_id,action,page,outcome,detail,http_status,duration_ms)
   VALUES(?,?,?,'REQUEST','SERVER',?,?,?,?,?,?,?,?,?,?)
   """,UUID.randomUUID(),parent,visitor(request),s.actor(),s.role(),s.host(),s.event(),action,safeRoute(request.getRequestURI()),status>=400?"FAILURE":status>=300?"REDIRECTED":"SUCCESS","HTTP_"+status,status,duration);
 }
}
