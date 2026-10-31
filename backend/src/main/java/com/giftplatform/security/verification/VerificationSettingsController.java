package com.giftplatform.security.verification;

import com.giftplatform.security.ActorContext;
import com.giftplatform.security.delivery.MessageDelivery;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@RestController
public class VerificationSettingsController {
 private final VerificationSettings settings; private final JdbcTemplate db; private final ActorContext actor;
 private final MessageDelivery sms,email; private final boolean keyReady;
 public VerificationSettingsController(VerificationSettings settings,JdbcTemplate db,ActorContext actor,
   @Qualifier("otpSmsDelivery") MessageDelivery sms,@Qualifier("otpEmailDelivery") MessageDelivery email,
   @Value("${gift.otp.hmac-key-base64:${gift.otp.secret-base64:}}") String encoded){
  this.settings=settings;this.db=db;this.actor=actor;this.sms=sms;this.email=email;
  boolean valid;try{valid=Base64.getDecoder().decode(encoded).length>=32;}catch(IllegalArgumentException ex){valid=false;}keyReady=valid;
 }
 private void admin(){if(!actor.isPlatformAdmin())throw new ResponseStatusException(HttpStatus.FORBIDDEN);}
 @GetMapping("/api/public/verification-settings")
 public ResponseEntity<VerificationSettings.Settings> current(){return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(settings.current());}
 @GetMapping("/api/admin/verification-settings")
 public ResponseEntity<Map<String,Object>> read(){admin();return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(details());}
 private Map<String,Object> details(){return Map.of("settings",settings.current(),"mobileConfigured",keyReady&&sms.isAvailable()&&sms.establishesMobileOwnership(),"emailConfigured",keyReady&&email.isAvailable());}
 @PutMapping("/api/admin/verification-settings") @Transactional
 public Map<String,Object> update(@RequestBody VerificationSettings.Settings input){
  admin();if(input==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
  var previous=settings.current();
  if(input.mobileOtpEnabled()&&!previous.mobileOtpEnabled()&&(!keyReady||!sms.isAvailable()||!sms.establishesMobileOwnership()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Configure MSG91 credentials, template and OTP signing key before enabling mobile verification");
  if(input.emailVerificationEnabled()&&!previous.emailVerificationEnabled()&&(!keyReady||!email.isAvailable()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Configure the email provider, sender and OTP signing key before enabling email verification");
  UUID who=actor.requireAuthenticatedUserId();
  db.update("INSERT INTO platform_verification_settings(id,mobile_otp_enabled,email_verification_enabled,changed_by) VALUES(1,?,?,?) ON CONFLICT(id) DO UPDATE SET mobile_otp_enabled=EXCLUDED.mobile_otp_enabled,email_verification_enabled=EXCLUDED.email_verification_enabled,changed_by=EXCLUDED.changed_by,changed_at=now()",input.mobileOtpEnabled(),input.emailVerificationEnabled(),who);
  db.update("INSERT INTO platform_admin_action_audit(id,actor_id,action) VALUES(?,?,?)",UUID.randomUUID(),who,"VERIFICATION_MOBILE_"+input.mobileOtpEnabled()+"_EMAIL_"+input.emailVerificationEnabled());
  return details();
 }
}
