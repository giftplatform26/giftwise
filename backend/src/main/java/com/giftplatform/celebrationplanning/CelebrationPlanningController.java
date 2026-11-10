package com.giftplatform.celebrationplanning;

import com.giftplatform.admin.EffectiveModulePermissionService;
import com.giftplatform.event.entity.*;
import com.giftplatform.event.repository.EventRepository;
import com.giftplatform.security.ActorContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.*;
import java.util.*;

/** New settings never modify old events until explicitly granted and saved by their owner. */
@RestController
public class CelebrationPlanningController {
 private final EventRepository events;
 private final ActorContext actor;
 private final EffectiveModulePermissionService permissions;
 private final JdbcTemplate db;
 public CelebrationPlanningController(EventRepository events,ActorContext actor,EffectiveModulePermissionService permissions,JdbcTemplate db){this.events=events;this.actor=actor;this.permissions=permissions;this.db=db;}
 public record Settings(@NotNull Boolean giftingEnabled,@NotNull @Size(max=500) String welcomeMessage,
   @NotNull Boolean shareVenue,LocalTime startTime,@NotBlank @Size(max=80) String timezone,
   @Min(15) @Max(1440) int durationMinutes,@NotNull @Size(max=1000) String arrivalNotes,@Min(0) long version){}
 public record PublicDetails(boolean giftingEnabled,String welcomeMessage,String venue,String arrivalNotes,
                             LocalDate date,LocalTime startTime,String timezone,int durationMinutes){}
 public record RepeatRequest(@NotBlank @Size(max=200) String title){}
 private Event owned(UUID id){UUID who=actor.requireAuthenticatedUserId();return events.findById(id).filter(e->who.equals(e.getOwnerId())).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));}
 private Event published(UUID token){return events.findByPublicToken(token).filter(e->e.getStatus()==EventStatus.PUBLISHED).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));}
 private Settings settings(Event e){return db.query("SELECT * FROM event_celebration_settings WHERE event_id=?",(rs,i)->new Settings(e.isGiftingEnabled(),rs.getString("welcome_message"),rs.getBoolean("share_venue"),rs.getObject("start_time",LocalTime.class),rs.getString("timezone"),rs.getInt("duration_minutes"),rs.getString("arrival_notes"),rs.getLong("version")),e.getId()).stream().findFirst().orElse(new Settings(e.isGiftingEnabled(),"",false,null,"Asia/Kolkata",120,"",0));}
 private void audit(Event e,String action){db.update("INSERT INTO platform_admin_action_audit(id,actor_id,action,target_id,module_code) VALUES(?,?,?,?,?)",UUID.randomUUID(),actor.requireAuthenticatedUserId(),action,e.getId(),"CELEBRATION_PLANNING");}
 @GetMapping("/api/v1/events/{id}/celebration-settings")
 @Transactional(readOnly=true)
 public ResponseEntity<Settings> get(@PathVariable UUID id){Event e=owned(id);permissions.requireEvent(e.getOwnerId(),id,"CELEBRATION_PLANNING");return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(settings(e));}
 @PutMapping("/api/v1/events/{id}/celebration-settings")
 @Transactional
 public Settings save(@PathVariable UUID id,@Valid @RequestBody Settings input){
  Event e=owned(id);
  // Paired with the shared insert locks in V96; acquire before locking the row.
  db.queryForObject("SELECT pg_advisory_xact_lock(1195984468,hashtext(?))",Object.class,id.toString());
  e=events.findForUpdate(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
  permissions.requireEvent(e.getOwnerId(),id,"CELEBRATION_PLANNING");
  if(e.getStatus()!=EventStatus.DRAFT)throw new ResponseStatusException(HttpStatus.CONFLICT,"Make the invitation private before changing celebration settings");
  try{CelebrationCalendar.validateTime(e.getEventDate(),input.startTime(),input.timezone());}catch(DateTimeException ex){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Choose a valid timezone and a time outside daylight-saving gaps or overlaps");}
  Settings before=settings(e);
  if(before.version()!=input.version())throw new ResponseStatusException(HttpStatus.CONFLICT,"Settings changed. Reload before saving");
  if(!Objects.equals(before.shareVenue(),input.shareVenue())||!Objects.equals(before.startTime(),input.startTime())||!before.timezone().equals(input.timezone())||before.durationMinutes()!=input.durationMinutes()||!before.arrivalNotes().equals(input.arrivalNotes()))permissions.requireEvent(e.getOwnerId(),id,"EVENT_ESSENTIALS");
  // Do not hide an active gift collection or payment history behind a new invitation mode.
  if(!input.giftingEnabled()&&e.isGiftingEnabled()&&Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM payment_orders WHERE event_id=? UNION ALL SELECT 1 FROM gift_groups WHERE event_id=? UNION ALL SELECT 1 FROM guest_direct_transfer_claims WHERE event_id=? UNION ALL SELECT 1 FROM guest_gifts WHERE event_id=?)",Boolean.class,id,id,id,id)))throw new ResponseStatusException(HttpStatus.CONFLICT,"This event already has gifting activity. Keep gifting enabled or create a separate invitation-only event");
  e.setGiftingEnabled(input.giftingEnabled());events.saveAndFlush(e);
  db.update("INSERT INTO event_celebration_settings(event_id,welcome_message,share_venue,start_time,timezone,duration_minutes,arrival_notes,version) VALUES(?,?,?,?,?,?,?,?) ON CONFLICT(event_id) DO UPDATE SET welcome_message=EXCLUDED.welcome_message,share_venue=EXCLUDED.share_venue,start_time=EXCLUDED.start_time,timezone=EXCLUDED.timezone,duration_minutes=EXCLUDED.duration_minutes,arrival_notes=EXCLUDED.arrival_notes,version=EXCLUDED.version,changed_at=now()",id,input.welcomeMessage().strip(),input.shareVenue(),input.startTime(),input.timezone(),input.durationMinutes(),input.arrivalNotes().strip(),before.version()+1);
  audit(e,"CELEBRATION_SETTINGS_CHANGED");return settings(e);
 }
 @GetMapping("/api/public/events/{token}/celebration-details")
 @Transactional(readOnly=true)
 public ResponseEntity<PublicDetails> details(@PathVariable UUID token){Event e=published(token);Settings s=settings(e);boolean planning=permissions.eventEnabled(e.getOwnerId(),e.getId(),"CELEBRATION_PLANNING"),essentials=planning&&permissions.eventEnabled(e.getOwnerId(),e.getId(),"EVENT_ESSENTIALS");return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new PublicDetails(e.isGiftingEnabled(),planning?s.welcomeMessage():"",essentials&&s.shareVenue()?venue(e):null,essentials&&s.shareVenue()?s.arrivalNotes():"",e.getEventDate(),essentials?s.startTime():null,essentials?s.timezone():"Asia/Kolkata",s.durationMinutes()));}
 private String venue(Event e){return java.util.stream.Stream.of(e.getAddressLine1(),e.getCity(),e.getState(),e.getPostalCode(),e.getCountry()).filter(v->v!=null&&!v.isBlank()).collect(java.util.stream.Collectors.joining(", "));}
 @GetMapping(value="/api/public/events/{token}/calendar",produces="text/calendar")
 @Transactional(readOnly=true)
 public ResponseEntity<String> calendar(@PathVariable UUID token){Event e=published(token);permissions.requireEvent(e.getOwnerId(),e.getId(),"EVENT_ESSENTIALS");permissions.requireEvent(e.getOwnerId(),e.getId(),"CELEBRATION_PLANNING");if(e.getEventDate()==null)throw new ResponseStatusException(HttpStatus.CONFLICT,"The host has not set a date yet");Settings s=settings(e);try{CelebrationCalendar.validateTime(e.getEventDate(),s.startTime(),s.timezone());}catch(DateTimeException ex){throw new ResponseStatusException(HttpStatus.CONFLICT,"The host needs to review the event time and timezone");}return ResponseEntity.ok().cacheControl(CacheControl.noStore()).header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=celebration.ics").body(CelebrationCalendar.render(e.getPublicToken(),e.getTitle(),e.getEventDate(),s.startTime(),s.timezone(),s.durationMinutes(),s.shareVenue()?venue(e):""));}
 @PostMapping("/api/v1/events/{id}/repeat")
 @ResponseStatus(HttpStatus.CREATED)
 @Transactional
 public Map<String,UUID> repeat(@PathVariable UUID id,@Valid @RequestBody RepeatRequest input){Event source=owned(id);permissions.requireEvent(source.getOwnerId(),id,"EVENT_REUSE");
  Event e=new Event(input.title().strip(),source.getOccasionType());e.assignOwner(source.getOwnerId());e.setHostDisplayName(source.getHostDisplayName());e.setEventContact(source.getAddressLine1(),source.getCity(),source.getState(),source.getPostalCode(),source.getCountry(),source.getContactName(),source.getContactPhone(),source.getContactEmail());e.setDeliveryAddress(source.isDeliverySameAsEvent(),source.getDeliveryAddress());e.update(input.title().strip(),source.getDescription(),source.getOccasionType(),null);e.setGiftingEnabled(source.isGiftingEnabled());events.saveAndFlush(e);audit(e,"EVENT_REPEATED_AS_DRAFT");return Map.of("id",e.getId());
 }
}
