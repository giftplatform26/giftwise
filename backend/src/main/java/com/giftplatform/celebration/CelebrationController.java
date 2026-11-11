package com.giftplatform.celebration;

import com.giftplatform.admin.EffectiveModulePermissionService;
import com.giftplatform.admin.EffectiveModulePermissionService;
import com.giftplatform.event.entity.Event;
import com.giftplatform.event.entity.EventStatus;
import com.giftplatform.event.repository.EventRepository;
import com.giftplatform.security.ActorContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.*;

@RestController
public class CelebrationController {
 private final JdbcTemplate db;
 private final EventRepository events;
 private final ActorContext actor;
 private final EffectiveModulePermissionService permissions;
 private final com.giftplatform.cohost.EventAccess access;
 public CelebrationController(JdbcTemplate db, EventRepository events, ActorContext actor, EffectiveModulePermissionService permissions, com.giftplatform.cohost.EventAccess access) {
  this.db=db;this.events=events;this.actor=actor;this.permissions=permissions;this.access=access;
 }
 public record RsvpRequest(String guestName,String guestEmail,String attendance,Integer partySize) {}
 public record WishRequest(String guestName,String message) {}
 public record Wish(UUID id,String guestName,String message,boolean photoAvailable) {}
 public record RsvpSummary(long yes,long no,long maybe,long attendees) {}
 public record HostRsvp(UUID id,String guestName,String guestEmail,String attendance,int partySize) {}
 public record HostWish(UUID id,String guestName,String message,String status,boolean photoAvailable) {}
 private Event published(UUID token) {
  return events.findByPublicToken(token).filter(e->e.getStatus()==EventStatus.PUBLISHED)
   .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
 }
 /** Owner, or a co-host whose role includes guests and wishes. */
 private Event owned(UUID id) {
  return access.require(id,com.giftplatform.cohost.EventAccess.Capability.GUESTS,null).event();
 }
 private String field(String value,int max,String label) {
  if(value==null || value.isBlank() || value.strip().length()>max || value.chars().anyMatch(c->Character.isISOControl(c) && c!='\n'))
   throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid "+label);
  return value.strip();
 }
 private String fingerprint(HttpServletRequest request) {
  // Only the socket peer address; never trust user-controlled X-Forwarded-For.
  try {
   byte[] digest=MessageDigest.getInstance("SHA-256").digest(request.getRemoteAddr().getBytes(StandardCharsets.UTF_8));
   return HexFormat.of().formatHex(digest);
  } catch(Exception e) {throw new IllegalStateException(e);}
 }
 private void limit(UUID eventId,HttpServletRequest request,String type,int max) {
  String fp=fingerprint(request);
  int updated=db.update("INSERT INTO event_guest_submission_limits(event_id,fingerprint,submission_date,submission_type,count) VALUES (?,?,?, ?,1) " +
    "ON CONFLICT(event_id,fingerprint,submission_date,submission_type) DO UPDATE SET count=event_guest_submission_limits.count+1 " +
    "WHERE event_guest_submission_limits.count < ?",eventId,fp,LocalDate.now(java.time.ZoneOffset.UTC),type,max);
  if(updated==0) throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,"Please try again later");
 }
 @PostMapping("/api/public/celebrations/{token}/rsvp")
 @ResponseStatus(HttpStatus.CREATED)
 @Transactional
 public Map<String,String> rsvp(@PathVariable UUID token,@RequestBody RsvpRequest req,HttpServletRequest http) {
  Event e=published(token);
  permissions.require(e.getOwnerId(),"RSVP");
  if(req==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
  String name=field(req.guestName(),120,"guest name");
  String email=field(req.guestEmail(),254,"email").toLowerCase(Locale.ROOT);
  if(!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid email");
  String attendance=req.attendance();
  if(attendance==null || !Set.of("YES","NO","MAYBE").contains(attendance))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid attendance");
  int size=req.partySize()==null?0:req.partySize();
  if(("YES".equals(attendance) && (size<1||size>10)) || (!"YES".equals(attendance) && size!=0))
   throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid party size");
  limit(e.getId(),http,"RSVP",5);
  try {
   db.update("INSERT INTO event_rsvps(id,event_id,guest_name,guest_email,attendance,party_size) VALUES (?,?,?,?,?,?)",
    UUID.randomUUID(),e.getId(),name,email,attendance,size);
  }catch(DuplicateKeyException ex){throw new ResponseStatusException(HttpStatus.CONFLICT,"An RSVP already exists for this email");}
  return Map.of("message","Thank you for replying!");
 }
 @PostMapping("/api/public/celebrations/{token}/wishes")
 @ResponseStatus(HttpStatus.CREATED)
 @Transactional
 public Map<String,String> wish(@PathVariable UUID token,@RequestBody WishRequest req,HttpServletRequest http) {
  Event e=published(token);
  permissions.require(e.getOwnerId(),"GUESTBOOK");
  if(req==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
  String name=field(req.guestName(),120,"guest name");
  String message=field(req.message(),1000,"message");
  limit(e.getId(),http,"WISH",5);
  boolean auto="AUTO".equals(permissions.guestbookMode(e.getId()));
  db.update("INSERT INTO event_guestbook(id,event_id,guest_name,message,status) VALUES (?,?,?,?,?)",UUID.randomUUID(),e.getId(),name,message,auto?"APPROVED":"PENDING");
  return Map.of("message",auto?"Thank you for sharing your wishes!":"Your wishes have been sent to the host for approval.");
 }
 @GetMapping("/api/public/celebrations/{token}/wishes")
 public List<Wish> publicWishes(@PathVariable UUID token) {
  Event e=published(token);
  permissions.require(e.getOwnerId(),"GUESTBOOK");
  return db.query("SELECT id,guest_name,message,(photo_path IS NOT NULL) AS photo_available FROM event_guestbook WHERE event_id=? AND status='APPROVED' ORDER BY created_at DESC LIMIT 100",
   (rs,i)->new Wish(rs.getObject("id",UUID.class),rs.getString("guest_name"),rs.getString("message"),rs.getBoolean("photo_available")),e.getId());
 }
 @GetMapping("/api/v1/events/{id}/rsvps")
 public List<HostRsvp> rsvps(@PathVariable UUID id) {
  Event e=owned(id);permissions.require(e.getOwnerId(),"RSVP");
  return db.query("SELECT id,guest_name,guest_email,attendance,party_size FROM event_rsvps WHERE event_id=? ORDER BY created_at DESC LIMIT 1000",
   (rs,i)->new HostRsvp(rs.getObject("id",UUID.class),rs.getString("guest_name"),rs.getString("guest_email"),rs.getString("attendance"),rs.getInt("party_size")),id);
 }
 @GetMapping("/api/v1/events/{id}/rsvps/summary")
 public RsvpSummary summary(@PathVariable UUID id) {
  Event e=owned(id);permissions.require(e.getOwnerId(),"RSVP");
  return db.queryForObject("SELECT COUNT(*) FILTER(WHERE attendance='YES') AS yes, COUNT(*) FILTER(WHERE attendance='NO') AS no, " +
   "COUNT(*) FILTER(WHERE attendance='MAYBE') AS maybe, COALESCE(SUM(party_size),0) AS attendees FROM event_rsvps WHERE event_id=?",
   (rs,i)->new RsvpSummary(rs.getLong("yes"),rs.getLong("no"),rs.getLong("maybe"),rs.getLong("attendees")),id);
 }
 @GetMapping("/api/v1/events/{id}/wishes")
 public List<HostWish> hostWishes(@PathVariable UUID id) {
  Event e=owned(id);permissions.require(e.getOwnerId(),"GUESTBOOK");
  return db.query("SELECT id,guest_name,message,status,(photo_path IS NOT NULL) AS photo_available FROM event_guestbook WHERE event_id=? ORDER BY created_at DESC LIMIT 1000",
   (rs,i)->new HostWish(rs.getObject("id",UUID.class),rs.getString("guest_name"),rs.getString("message"),rs.getString("status"),rs.getBoolean("photo_available")),id);
 }
 @GetMapping("/api/v1/events/{id}/wishes/settings")
 public Map<String,String> guestbookSettings(@PathVariable UUID id){
  Event e=owned(id);permissions.require(e.getOwnerId(),"GUESTBOOK");
  return Map.of("mode",permissions.guestbookMode(id));
 }
 public record GuestbookModeRequest(String mode){}
 @PostMapping("/api/v1/events/{id}/wishes/settings")
 @Transactional
 public Map<String,String> setGuestbookSettings(@PathVariable UUID id,@RequestBody GuestbookModeRequest request){
  Event e=owned(id);permissions.require(e.getOwnerId(),"GUESTBOOK");
  if(request==null||!Set.of("AUTO","APPROVAL").contains(request.mode()))
   throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid guestbook mode");
  db.update("INSERT INTO platform_event_message_settings(event_id,display_mode) VALUES(?,?) ON CONFLICT(event_id) DO UPDATE SET display_mode=EXCLUDED.display_mode,changed_at=now()",id,request.mode());
  return Map.of("mode",request.mode());
 }
 @PostMapping("/api/v1/events/{id}/wishes/{wishId}/moderate")
 @Transactional
 public Map<String,String> moderate(@PathVariable UUID id,@PathVariable UUID wishId,@RequestParam String status) {
  Event e=owned(id);permissions.require(e.getOwnerId(),"GUESTBOOK");
  if(status==null || !Set.of("APPROVED","HIDDEN").contains(status))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid moderation status");
  int changed=db.update("UPDATE event_guestbook SET status=?,moderated_at=now() WHERE id=? AND event_id=?",status,wishId,id);
  if(changed==0)throw new ResponseStatusException(HttpStatus.NOT_FOUND);
  return Map.of("status",status);
 }
}
