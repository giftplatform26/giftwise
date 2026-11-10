package com.giftplatform.analytics;

import com.giftplatform.security.ActorContext;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import java.time.*;
import java.sql.Timestamp;

@RestController
@RequestMapping("/api/admin/console/interactions")
public class InteractionAdminController {
 private final ActorContext actor;private final JdbcTemplate db;
 public InteractionAdminController(ActorContext actor,JdbcTemplate db){this.actor=actor;this.db=db;}
 @GetMapping public ResponseEntity<Map<String,Object>> report(@RequestParam(required=false) UUID hostId,@RequestParam(required=false) UUID eventId,
  @RequestParam(required=false) LocalDate from,@RequestParam(required=false) LocalDate to,@RequestParam(defaultValue="") String action,
  @RequestParam(defaultValue="") String outcome,@RequestParam(defaultValue="") String kind,@RequestParam(defaultValue="0") int page){
  if(!actor.isPlatformAdmin())throw new ResponseStatusException(HttpStatus.FORBIDDEN);
  if(from==null)from=LocalDate.now(ZoneOffset.UTC).minusDays(29);if(to==null)to=LocalDate.now(ZoneOffset.UTC);
  if(to.isBefore(from)||from.getYear()<2000||to.getYear()>9998||page<0||page>100000||!action.matches("[A-Z0-9_]{0,64}")||!outcome.matches("[A-Z_]{0,32}")||!Set.of("","CLICK","PAGE_VIEW","REQUEST","OPERATION").contains(kind))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid analytics filters");
  StringBuilder where=new StringBuilder(" WHERE a.created_at>=? AND a.created_at<?");List<Object> args=new ArrayList<>(List.of(Timestamp.from(from.atStartOfDay(ZoneOffset.UTC).toInstant()),Timestamp.from(to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant())));
  if(hostId!=null){where.append(" AND a.host_id=?");args.add(hostId);}if(eventId!=null){where.append(" AND a.event_id=?");args.add(eventId);}
  if(!action.isEmpty()){where.append(" AND a.action=?");args.add(action);}if(!outcome.isEmpty()){where.append(" AND a.outcome=?");args.add(outcome);}if(!kind.isEmpty()){where.append(" AND a.kind=?");args.add(kind);}
  String source=" FROM application_interactions a LEFT JOIN users u ON u.id=a.host_id LEFT JOIN events e ON e.id=a.event_id"+where;
  var summary=db.queryForMap("SELECT count(*) AS records,count(*) FILTER(WHERE a.kind='CLICK') AS clicks,count(*) FILTER(WHERE a.kind='CLICK' AND a.outcome IN ('SUCCESS','BROWSER_READY')) AS successful_clicks,count(*) FILTER(WHERE a.kind='CLICK' AND a.outcome='FAILURE') AS failed_clicks,count(*) FILTER(WHERE a.kind='CLICK' AND a.outcome='CANCELLED') AS cancelled_clicks,count(*) FILTER(WHERE a.kind='PAGE_VIEW') AS views,count(*) FILTER(WHERE a.kind='REQUEST') AS requests,count(*) FILTER(WHERE a.outcome='FAILURE') AS failures,count(*) FILTER(WHERE a.outcome IN ('CLICKED','UNKNOWN','HANDED_OFF')) AS unconfirmed"+source,args.toArray());
  long total=((Number)summary.get("records")).longValue();long pages=(total+49)/50;int current=(int)Math.min(page,Math.max(0,pages-1));
  List<Object> paged=new ArrayList<>(args);paged.add(current*50);
  var entries=db.queryForList("SELECT a.id,a.parent_id,a.kind,a.source,a.actor_role,a.host_id,a.event_id,a.action,a.page,a.control,a.destination,a.outcome,a.detail,a.http_status,a.duration_ms,a.created_at,a.updated_at,(SELECT source_text FROM localization_sources WHERE message_key=split_part(a.control,' ',1)) AS control_name,u.username AS host_name,e.title AS event_name"+source+" ORDER BY a.created_at DESC,a.id DESC LIMIT 50 OFFSET ?",paged.toArray());
  var byHost=db.queryForList("SELECT a.host_id,u.username AS host_name,count(*) FILTER(WHERE a.kind='CLICK') AS clicks,count(*) AS records,count(*) FILTER(WHERE a.outcome='FAILURE') AS failures"+source+" GROUP BY a.host_id,u.username ORDER BY clicks DESC,records DESC LIMIT 100",args.toArray());
  var byEvent=db.queryForList("SELECT a.event_id,e.title AS event_name,a.host_id,u.username AS host_name,count(*) FILTER(WHERE a.kind='CLICK') AS clicks,count(*) AS records,count(*) FILTER(WHERE a.outcome='FAILURE') AS failures"+source+" GROUP BY a.event_id,e.title,a.host_id,u.username ORDER BY clicks DESC,records DESC LIMIT 100",args.toArray());
  var byAction=db.queryForList("SELECT a.action,a.kind,a.outcome,count(*) AS count"+source+" GROUP BY a.action,a.kind,a.outcome ORDER BY count DESC LIMIT 200",args.toArray());
  var byPage=db.queryForList("SELECT a.page,count(*) FILTER(WHERE a.kind='CLICK') AS clicks,count(*) AS records,count(*) FILTER(WHERE a.outcome='FAILURE') AS failures"+source+" GROUP BY a.page ORDER BY clicks DESC,records DESC LIMIT 100",args.toArray());
  return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(Map.of("byPage",byPage,"summary",summary,"entries",entries,"byHost",byHost,"byEvent",byEvent,"byAction",byAction,"page",current,"totalPages",pages,"from",from,"to",to));
 }
 @GetMapping("/funnel")
 public ResponseEntity<List<Map<String,Object>>> funnel(){
  if(!actor.isPlatformAdmin())throw new ResponseStatusException(HttpStatus.FORBIDDEN);
  return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(db.queryForList("""
   SELECT action,count(DISTINCT visitor_key) AS visitors,count(*) AS occurrences
   FROM application_interactions
   WHERE created_at>=now()-interval '30 days'
    AND ((source='SERVER' AND outcome='SUCCESS' AND action IN ('HOST_REGISTER','EVENT_CREATE','EVENT_PUBLISH','INVITATION_OPEN','GUEST_RSVP'))
      OR (source='BROWSER' AND action IN ('INVITATION_LINK_COPY','INVITATION_SHARE','INVITATION_WHATSAPP') AND outcome IN ('SUCCESS','BROWSER_READY','HANDED_OFF')))
   GROUP BY action ORDER BY action
   """));
 }
 @GetMapping("/{id}") public ResponseEntity<List<Map<String,Object>>> timeline(@PathVariable UUID id){
  if(!actor.isPlatformAdmin())throw new ResponseStatusException(HttpStatus.FORBIDDEN);
  return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(db.queryForList("SELECT id,parent_id,kind,source,action,page,outcome,detail,http_status,duration_ms,created_at,updated_at FROM application_interactions WHERE id=? OR parent_id=? ORDER BY created_at LIMIT 100",id,id));
 }

}
