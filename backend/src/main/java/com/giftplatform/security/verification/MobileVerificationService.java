package com.giftplatform.security.verification;

import com.giftplatform.security.delivery.MessageDelivery;
import com.giftplatform.security.recovery.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.Instant;
import java.util.*;

@Service
public class MobileVerificationService {
 private final JdbcTemplate db; private final TransactionTemplate tx; private final RecoveryChallengeStore challenges;
 private final VerificationSettings settings; private final MessageDelivery sms; private final byte[] key; private final int expiryMinutes;
 public record Started(UUID challengeId,String message){}
 public record Status(String mobile,boolean verified){}
 public MobileVerificationService(JdbcTemplate db,TransactionTemplate tx,RecoveryChallengeStore challenges,VerificationSettings settings,
     @Qualifier("otpSmsDelivery") MessageDelivery sms,@Value("${gift.otp.hmac-key-base64:${gift.otp.secret-base64:}}") String encoded,
     @Value("${gift.otp.sms.expiry-minutes:5}") int expiryMinutes){
  this.db=db;this.tx=tx;this.challenges=challenges;this.settings=settings;this.sms=sms;this.expiryMinutes=expiryMinutes;
  if(expiryMinutes<1||expiryMinutes>10)throw new IllegalArgumentException("SMS OTP expiry must be 1–10 minutes");
  try{key=encoded.isBlank()?new byte[0]:Base64.getDecoder().decode(encoded);}catch(IllegalArgumentException ex){throw new IllegalStateException("Invalid OTP signing key");}
 }
 private void ready(){settings.requireMobile();if(key.length<32||!sms.isAvailable()||!sms.establishesMobileOwnership())throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Mobile verification is not configured");}
 private static String table(String kind){return switch(kind){case "HOST","PLATFORM_ADMIN"->"users";case "GROUP_ADMIN"->"gift_group_accounts";case "GROUP_MEMBER"->"gift_group_members";default->throw new IllegalArgumentException("Invalid account kind");};}
 private static String digest(String value){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));}catch(NoSuchAlgorithmException ex){throw new IllegalStateException(ex);}}
 private String signed(UUID challenge,String kind,UUID id,String mobile,String code){return RecoveryOtp.fingerprint(key,challenge,kind,id,"VERIFY_CONTACT",mobile,code);}
 public Status status(String kind,UUID id){settings.requireMobile();String column=table(kind).equals("users")?"phone_number":"mobile",verified=table(kind).equals("users")?"phone_verified_at":"mobile_verified_at";return db.queryForObject("SELECT "+column+","+verified+" IS NOT NULL FROM "+table(kind)+" WHERE id=?",(rs,n)->new Status(rs.getString(1),rs.getBoolean(2)),id);}
 @Transactional(propagation=org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
 public Started start(String kind,UUID accountId,String mobile){
  ready();table(kind);
  if(mobile==null||!mobile.matches("\\+[1-9][0-9]{7,14}"))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Enter a mobile number with country code");
  UUID id=UUID.randomUUID();String code=RecoveryOtp.generate(),fingerprint=digest(mobile);
  // Commit throttling and challenge before contacting the provider. Failed delivery still counts.
  tx.executeWithoutResult(state->{
   db.queryForObject("SELECT pg_advisory_xact_lock(hashtextextended(?,0))",Object.class,"contact-mobile:"+kind+":"+accountId);
   db.queryForObject("SELECT pg_advisory_xact_lock(hashtextextended(?,0))",Object.class,"contact-mobile-destination:"+fingerprint);
   Integer count=db.queryForObject("SELECT count(*) FROM account_contact_challenges WHERE account_kind=? AND account_id=? AND channel='SMS' AND created_at>now()-interval '1 hour'",Integer.class,kind,accountId);
   Boolean cooldown=db.queryForObject("SELECT EXISTS(SELECT 1 FROM account_contact_challenges WHERE account_kind=? AND account_id=? AND channel='SMS' AND created_at>now()-interval '60 seconds')",Boolean.class,kind,accountId);
   Integer destinationCount=db.queryForObject("SELECT count(*) FROM account_contact_challenges WHERE destination_fingerprint=? AND channel='SMS' AND created_at>now()-interval '1 hour'",Integer.class,fingerprint);
   if((count!=null&&count>=3)||Boolean.TRUE.equals(cooldown)||(destinationCount!=null&&destinationCount>=5))throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,"Please wait before requesting another code");
   Instant expiry=Instant.now().plusSeconds(expiryMinutes*60L);
   challenges.create(id,kind,accountId,"SMS","VERIFY_CONTACT",fingerprint,signed(id,kind,accountId,mobile,code),expiry);
   db.update("INSERT INTO account_pending_mobile_verifications(challenge_id,account_kind,account_id,mobile,expires_at) VALUES(?,?,?,?,?)",id,kind,accountId,mobile,java.sql.Timestamp.from(expiry));
  });
  try{sms.sendCode(mobile,code);db.update("UPDATE account_pending_mobile_verifications SET delivered_at=now() WHERE challenge_id=?",id);}
  catch(RuntimeException ex){db.update("UPDATE account_contact_challenges SET consumed_at=now() WHERE id=?",id);throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Could not send the mobile verification code. Try again later.");}
  return new Started(id,"A verification code has been sent to your mobile.");
 }
 @Transactional public void complete(String kind,UUID accountId,UUID challengeId,String code){
  ready();table(kind);
  if(challengeId==null||code==null||!code.matches("[0-9]{6}"))throw invalid();
  var rows=db.query("SELECT mobile FROM account_pending_mobile_verifications WHERE challenge_id=? AND account_kind=? AND account_id=? AND expires_at>now() AND delivered_at IS NOT NULL FOR UPDATE",(rs,n)->rs.getString(1),challengeId,kind,accountId);
  if(rows.size()!=1)throw invalid();String mobile=rows.getFirst();
  if(!challenges.verifyAndConsume(challengeId,kind,accountId,"VERIFY_CONTACT",digest(mobile),signed(challengeId,kind,accountId,mobile,code)))throw invalid();
  String columns=table(kind).equals("users")?"phone_number=?,phone_verified_at=now()":"mobile=?,mobile_verified_at=now()";
  try{if(db.update("UPDATE "+table(kind)+" SET "+columns+" WHERE id=?",mobile,accountId)!=1)throw invalid();}
  catch(org.springframework.dao.DuplicateKeyException ex){throw new ResponseStatusException(HttpStatus.CONFLICT,"This mobile number cannot be added to this account");}
  db.update("DELETE FROM account_pending_mobile_verifications WHERE challenge_id=?",challengeId);
 }
 private static ResponseStatusException invalid(){return new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid or expired verification code");}
}
