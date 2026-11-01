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
 private final JdbcTemplate db; private final GroupContributionController groups; private final ActorContext actor;
 private final GroupGiftLifecycleService lifecycle;
 @org.springframework.beans.factory.annotation.Autowired
 public GroupGiftCollectionController(JdbcTemplate db,GroupContributionController groups,ActorContext actor,GroupGiftLifecycleService lifecycle){this.db=db;this.groups=groups;this.actor=actor;this.lifecycle=lifecycle;}
 public GroupGiftCollectionController(JdbcTemplate db,GroupContributionController groups,ActorContext actor){this(db,groups,actor,new GroupGiftLifecycleService(db));}
 public record Reservation(UUID giftId){}
 @ExceptionHandler(ResponseStatusException.class)
 public org.springframework.http.ResponseEntity<org.springframework.http.ProblemDetail> failure(ResponseStatusException ex){return org.springframework.http.ResponseEntity.status(ex.getStatusCode()).body(org.springframework.http.ProblemDetail.forStatusAndDetail(ex.getStatusCode(),ex.getReason()==null?"Unable to update the group":ex.getReason()));}
 @PostMapping("/api/public/groups/{token}/gift-reservation/{groupId}") @Transactional
 public Map<String,Object> reserve(@PathVariable UUID token,@PathVariable UUID groupId,@RequestBody Reservation input,HttpServletRequest req){
  UUID creator=groups.requireGroupCreator(token,groupId,req);
  return lifecycle.select(groupId,creator,input==null?null:input.giftId());
 }
 @PostMapping("/api/public/groups/{token}/gift-release/{groupId}") @Transactional
 public Map<String,String> release(@PathVariable UUID token,@PathVariable UUID groupId,HttpServletRequest req){return lifecycle.release(groupId,groups.requireGroupCreator(token,groupId,req));}
 @GetMapping("/api/public/groups/{token}/activity/{groupId}")
 public List<Map<String,Object>> activity(@PathVariable UUID token,@PathVariable UUID groupId,@RequestParam(defaultValue="0") int offset,HttpServletRequest req){
  groups.requireGroupCreator(token,groupId,req);
  if(offset<0||offset>100000)throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
  return db.queryForList("SELECT id,actor_id,action,details::text AS details,created_at FROM gift_group_activity WHERE group_id=? ORDER BY created_at DESC,id DESC LIMIT 100 OFFSET ?",groupId,offset);
 }
 private void own(UUID eventId){if(!Integer.valueOf(1).equals(db.queryForObject("SELECT count(*) FROM events WHERE id=? AND owner_id=?",Integer.class,eventId,actor.requireAuthenticatedUserId())))throw new ResponseStatusException(HttpStatus.NOT_FOUND);}
 @GetMapping("/api/v1/events/{eventId}/contributions/groups") @Transactional(readOnly=true)
 public List<Map<String,Object>> report(@PathVariable UUID eventId){own(eventId);return db.queryForList("""
  SELECT g.id,g.name,g.creator_name,COALESCE(g.gift_name,gift.name) AS gift_name,g.selected_gift_id,g.reserved_gift_id,g.target_paise,g.collection_published_at,
   g.gift_published_at,g.fulfilment_status,g.tracking_details,g.creator_mobile,a.email AS creator_email,
   p.collected_paise,p.completion_percentage,
   CASE WHEN g.gift_published_at IS NULL THEN 'Something is Cooking' ELSE '100% Contributed' END AS display_status,
   CASE WHEN p.collected_paise>=g.target_paise THEN 'FULLY_FUNDED'
        WHEN p.collected_paise>0 THEN 'PARTIALLY_FUNDED' ELSE 'CONTRIBUTIONS_OPEN' END AS funding_status,
   COALESCE(SUM(r.amount_paise) FILTER(WHERE r.state='REPORTED_UNVERIFIED'),0) AS reported_unverified_paise,
   count(r.id) FILTER(WHERE r.state='REPORTED_UNVERIFIED') AS reported_count
  FROM gift_groups g JOIN group_gift_progress p ON p.id=g.id JOIN gift_group_accounts a ON a.id=g.admin_id LEFT JOIN gifts gift ON gift.id=g.reserved_gift_id LEFT JOIN group_collection_transactions r ON r.group_id=g.id
  WHERE g.event_id=?
  GROUP BY g.id,gift.name,a.email,p.collected_paise,p.completion_percentage ORDER BY g.created_at DESC
  """,eventId);}
 @GetMapping("/api/v1/events/{eventId}/contributions/groups/{groupId}")
 public List<Map<String,Object>> details(@PathVariable UUID eventId,@PathVariable UUID groupId){
  own(eventId);
  if(!Integer.valueOf(1).equals(db.queryForObject("SELECT count(*) FROM gift_groups WHERE id=? AND event_id=?",Integer.class,groupId,eventId)))throw new ResponseStatusException(HttpStatus.NOT_FOUND);
  return db.queryForList("SELECT r.id,r.guest_name,r.guest_contact,r.amount_paise,r.method,r.transaction_reference,r.state,r.created_at,r.gift_id_snapshot,r.contributor_type,p.creator_received_at FROM group_collection_transactions r JOIN group_gift_receipts p ON p.id=r.id AND p.group_id=r.group_id WHERE r.group_id=? ORDER BY r.created_at DESC",groupId);
 }
 @GetMapping("/api/v1/events/{eventId}/contributions/groups/{groupId}/activity")
 public List<Map<String,Object>> hostActivity(@PathVariable UUID eventId,@PathVariable UUID groupId,@RequestParam(defaultValue="0") int offset){
  own(eventId);
  if(offset<0||offset>100000)throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
  if(!Integer.valueOf(1).equals(db.queryForObject("SELECT count(*) FROM gift_groups WHERE id=? AND event_id=?",Integer.class,groupId,eventId)))throw new ResponseStatusException(HttpStatus.NOT_FOUND);
  return db.queryForList("SELECT id,actor_id,action,details::text AS details,created_at FROM gift_group_activity WHERE group_id=? ORDER BY created_at DESC,id DESC LIMIT 100 OFFSET ?",groupId,offset);
 }
 @GetMapping("/api/v1/events/{eventId}/group-notifications")
 public List<Map<String,Object>> notifications(@PathVariable UUID eventId){own(eventId);return db.queryForList("SELECT n.id,n.group_id,n.created_at,n.read_at,g.name,'Group collection published' AS message FROM group_collection_notifications n JOIN gift_groups g ON g.id=n.group_id WHERE n.event_id=? AND g.collection_published_at IS NOT NULL ORDER BY n.created_at DESC",eventId);}
 @PostMapping("/api/v1/events/{eventId}/group-notifications/{id}/read")
 public Map<String,String> read(@PathVariable UUID eventId,@PathVariable UUID id){own(eventId);if(db.update("UPDATE group_collection_notifications SET read_at=COALESCE(read_at,now()) WHERE id=? AND event_id=?",id,eventId)!=1)throw new ResponseStatusException(HttpStatus.NOT_FOUND);return Map.of("message","Notification read");}
}
