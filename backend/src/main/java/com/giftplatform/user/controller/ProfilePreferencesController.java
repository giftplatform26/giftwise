package com.giftplatform.user.controller;

import com.giftplatform.security.ActorContext;
import com.giftplatform.branding.SafeBrandImage;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.ZoneId;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/auth/profile")
public class ProfilePreferencesController {
 @org.springframework.beans.factory.annotation.Autowired(required=false) private com.giftplatform.audit.service.AuditService audit;
 private final JdbcTemplate db;private final ActorContext actor;
 public ProfilePreferencesController(JdbcTemplate db,ActorContext actor){this.db=db;this.actor=actor;}
 public record Input(String timezone,Boolean eventNotifications,Boolean contributionNotifications,Boolean marketingNotifications){}
 @GetMapping public ResponseEntity<Map<String,Object>> get(){UUID id=actor.requireAuthenticatedUserId();
  var data=db.queryForMap("SELECT COALESCE(p.timezone,'Asia/Kolkata') AS timezone,COALESCE(p.event_notifications,true) AS \"eventNotifications\",COALESCE(p.contribution_notifications,true) AS \"contributionNotifications\",COALESCE(p.marketing_notifications,false) AS \"marketingNotifications\",(p.picture_png IS NOT NULL) AS \"hasPicture\" FROM users u LEFT JOIN user_profile_preferences p ON p.user_id=u.id WHERE u.id=?",id);
  return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(data);
 }
 public static void validate(Input input){if(input==null||input.timezone()==null||!ZoneId.getAvailableZoneIds().contains(input.timezone()))throw new IllegalArgumentException("Choose a valid time zone.");if(input.eventNotifications()==null||input.contributionNotifications()==null||input.marketingNotifications()==null)throw new IllegalArgumentException("Choose your notification preferences.");}
 @PostMapping @Transactional public ResponseEntity<Map<String,Object>> save(@RequestBody Input input){UUID id=actor.requireAuthenticatedUserId();validate(input);db.update("INSERT INTO user_profile_preferences(user_id,timezone,event_notifications,contribution_notifications,marketing_notifications) VALUES(?,?,?,?,?) ON CONFLICT(user_id) DO UPDATE SET timezone=EXCLUDED.timezone,event_notifications=EXCLUDED.event_notifications,contribution_notifications=EXCLUDED.contribution_notifications,marketing_notifications=EXCLUDED.marketing_notifications,updated_at=now()",id,input.timezone(),input.eventNotifications(),input.contributionNotifications(),input.marketingNotifications());log(id,"preferences");return get();}
 @PostMapping(value="/picture",consumes=MediaType.MULTIPART_FORM_DATA_VALUE) @Transactional public Map<String,String> picture(@RequestPart("file") MultipartFile file)throws IOException{UUID id=actor.requireAuthenticatedUserId();if(file.getSize()>2*1024*1024)throw new IllegalArgumentException("Choose a profile picture up to 2 MB.");byte[] data=SafeBrandImage.png(file.getBytes());db.update("INSERT INTO user_profile_preferences(user_id,picture_png) VALUES(?,?) ON CONFLICT(user_id) DO UPDATE SET picture_png=EXCLUDED.picture_png,updated_at=now()",id,data);log(id,"picture");return Map.of("message","Profile picture updated successfully.");}
 @GetMapping("/picture") public ResponseEntity<byte[]> picture(){UUID id=actor.requireAuthenticatedUserId();var rows=db.query("SELECT picture_png FROM user_profile_preferences WHERE user_id=?",(rs,n)->rs.getBytes(1),id);if(rows.isEmpty()||rows.get(0)==null)return ResponseEntity.notFound().build();return ResponseEntity.ok().cacheControl(CacheControl.noStore()).contentType(MediaType.IMAGE_PNG).header("X-Content-Type-Options","nosniff").body(rows.get(0));}
 @DeleteMapping("/picture") @Transactional public ResponseEntity<Void> remove(){UUID id=actor.requireAuthenticatedUserId();db.update("UPDATE user_profile_preferences SET picture_png=NULL,updated_at=now() WHERE user_id=?",id);log(id,"picture removed");return ResponseEntity.noContent().build();}
 private void log(UUID id,String change){if(audit!=null)audit.recordChange(id,com.giftplatform.audit.entity.AuditActorType.CUSTOMER,"USER",id,com.giftplatform.audit.entity.AuditAction.UPDATE,Map.of(),Map.of("updated",change),null);}
 @ExceptionHandler(IllegalArgumentException.class) @ResponseStatus(HttpStatus.BAD_REQUEST) public Map<String,String> invalid(IllegalArgumentException ex){return Map.of("message",ex.getMessage());}
}
