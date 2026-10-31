package com.giftplatform.group;

import com.giftplatform.security.ActorContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@RestController
public class GroupGiftCollectionController {
 @org.springframework.beans.factory.annotation.Autowired(required=false) private com.giftplatform.security.verification.VerificationSettings verification;
 private final JdbcTemplate db; private final GroupContributionController groups; private final ActorContext actor;
 public GroupGiftCollectionController(JdbcTemplate db,GroupContributionController groups,ActorContext actor){this.db=db;this.groups=groups;this.actor=actor;}
 public record Reservation(UUID giftId){}
 @PostMapping("/api/public/groups/{token}/gift-reservation/{groupId}") @Transactional
 public Map<String,String> reserve(@PathVariable UUID token,@PathVariable UUID groupId,@RequestBody Reservation input,HttpServletRequest req){
  UUID creator=groups.requireGroupCreator(token,groupId,req);
  if(input==null||input.giftId()==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Select a gift");
  if(verification!=null){
   boolean mail=verification.emailEnabled(),phone=verification.mobileEnabled();
   if((mail||phone)&&!Boolean.TRUE.equals(db.queryForObject("SELECT (? AND email_verified_at IS NOT NULL) OR (? AND mobile_verified_at IS NOT NULL) FROM gift_group_accounts WHERE id=?",Boolean.class,mail,phone,creator)))
    throw new ResponseStatusException(HttpStatus.CONFLICT,"Verify your "+(mail&&phone?"email or mobile":mail?"email":"mobile")+" in Account before reserving a gift");
  }
  UUID eventId=db.queryForObject("SELECT event_id FROM gift_groups WHERE id=? FOR UPDATE",UUID.class,groupId);
  var gifts=db.queryForList("SELECT id FROM gifts WHERE id=? AND event_id=? AND status='ACTIVE' AND visibility='PUBLIC' FOR UPDATE",input.giftId(),eventId);
  if(gifts.isEmpty())throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Gift is unavailable");
  if(Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM gift_groups WHERE reserved_gift_id=?)",Boolean.class,input.giftId())))throw new ResponseStatusException(HttpStatus.CONFLICT,"A surprise is waiting for this gift");
  int updated=db.update("UPDATE gift_groups SET reserved_gift_id=? WHERE id=? AND reserved_gift_id IS NULL AND collection_published_at IS NULL",input.giftId(),groupId);
  if(updated!=1)throw new ResponseStatusException(HttpStatus.CONFLICT,"This group already reserved a gift or published its collection");
  return Map.of("message","A surprise is waiting");
 }
 private void own(UUID eventId){if(!Integer.valueOf(1).equals(db.queryForObject("SELECT count(*) FROM events WHERE id=? AND owner_id=?",Integer.class,eventId,actor.requireAuthenticatedUserId())))throw new ResponseStatusException(HttpStatus.NOT_FOUND);}
 @GetMapping("/api/v1/events/{eventId}/contributions/groups") @Transactional(readOnly=true)
 public List<Map<String,Object>> report(@PathVariable UUID eventId){own(eventId);return db.queryForList("""
  SELECT g.id,g.name,g.creator_name,COALESCE(g.gift_name,gift.name) AS gift_name,g.reserved_gift_id,g.target_paise,g.collection_published_at,
   COALESCE(SUM(r.amount_paise) FILTER(WHERE r.state='REPORTED_UNVERIFIED'),0) AS reported_unverified_paise,
   count(r.id) FILTER(WHERE r.state='REPORTED_UNVERIFIED') AS reported_count
  FROM gift_groups g LEFT JOIN gifts gift ON gift.id=g.reserved_gift_id LEFT JOIN gift_group_guest_reports r ON r.group_id=g.id
  WHERE g.event_id=? AND g.collection_published_at IS NOT NULL
  GROUP BY g.id,gift.name ORDER BY g.collection_published_at DESC
  """,eventId);}
 @GetMapping("/api/v1/events/{eventId}/contributions/groups/{groupId}")
 public List<Map<String,Object>> details(@PathVariable UUID eventId,@PathVariable UUID groupId){
  own(eventId);
  if(!Integer.valueOf(1).equals(db.queryForObject("SELECT count(*) FROM gift_groups WHERE id=? AND event_id=? AND collection_published_at IS NOT NULL",Integer.class,groupId,eventId)))throw new ResponseStatusException(HttpStatus.NOT_FOUND);
  return db.queryForList("SELECT r.id,r.guest_name,r.amount_paise,r.state,r.created_at FROM gift_group_guest_reports r WHERE r.group_id=? ORDER BY r.created_at DESC",groupId);
 }
 @GetMapping("/api/v1/events/{eventId}/group-notifications")
 public List<Map<String,Object>> notifications(@PathVariable UUID eventId){own(eventId);return db.queryForList("SELECT n.id,n.group_id,n.created_at,n.read_at,g.name,'Group collection published' AS message FROM group_collection_notifications n JOIN gift_groups g ON g.id=n.group_id WHERE n.event_id=? AND g.collection_published_at IS NOT NULL ORDER BY n.created_at DESC",eventId);}
 @PostMapping("/api/v1/events/{eventId}/group-notifications/{id}/read")
 public Map<String,String> read(@PathVariable UUID eventId,@PathVariable UUID id){own(eventId);if(db.update("UPDATE group_collection_notifications SET read_at=COALESCE(read_at,now()) WHERE id=? AND event_id=?",id,eventId)!=1)throw new ResponseStatusException(HttpStatus.NOT_FOUND);return Map.of("message","Notification read");}
}
