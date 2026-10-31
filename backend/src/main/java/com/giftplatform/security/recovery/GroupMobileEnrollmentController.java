
package com.giftplatform.security.recovery;

import com.giftplatform.security.delivery.MessageDelivery;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;

/**
 * Page-bound group mobile verification. Production uses the shared MSG91 service.
 * The legacy development simulator never asserts ownership.
 */
@RestController
@RequestMapping("/api/public/groups/{token}/contact-mobile")
public class GroupMobileEnrollmentController {
    @org.springframework.beans.factory.annotation.Autowired(required=false)
    private com.giftplatform.security.verification.MobileVerificationService mobileService;
    private final JdbcTemplate db;
    private final RecoveryChallengeStore challenges;
    private final CredentialSessionVersionStore versions;
    private final MessageDelivery sms;
    private final byte[] key;
    private final boolean realSmsEnabled;
    private final boolean simulatorEnabled;
    private final String profile;
    public record Start(String mobile, UUID groupId) {}
    public record Complete(UUID challengeId, String code, UUID groupId) {}
    public record Started(UUID challengeId, String message) {}
    public record Result(String message) {}
    private record Account(String kind, UUID id) {}
    public GroupMobileEnrollmentController(JdbcTemplate db, RecoveryChallengeStore challenges,
        CredentialSessionVersionStore versions, @Qualifier("otpSmsDelivery") MessageDelivery sms,
        @Value("${gift.otp.hmac-key-base64:}") String key64,
        @Value("${gift.otp.sms.real-verification-enabled:false}") boolean realSmsEnabled,
        @Value("${gift.otp.sms.simulator.enabled:false}") boolean simulatorEnabled,
        @Value("${spring.profiles.active:}") String profile) {
        this.db=db;this.challenges=challenges;this.versions=versions;this.sms=sms;
        this.realSmsEnabled=realSmsEnabled;this.simulatorEnabled=simulatorEnabled;this.profile=profile;
        try { this.key=key64.isBlank()?new byte[0]:Base64.getDecoder().decode(key64); }
        catch(IllegalArgumentException ex){throw new IllegalStateException("Invalid HMAC key",ex);}
    }
    private void ready() {
        if(key.length<32) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Mobile OTP not configured");
        if(!simulatorEnabled || !Arrays.asList(profile.split(",")).contains("local"))
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Mobile OTP development testing only");
        if(realSmsEnabled) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Real SMS provider not implemented");
    }
    private static ResponseStatusException denied() {return new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Sign in again");}
    private UUID event(UUID token) {
        List<UUID> ids=db.query("SELECT id FROM events WHERE public_token=? AND status='PUBLISHED'",(rs,n)->(UUID)rs.getObject(1),token);
        if(ids.size()!=1)throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return ids.get(0);
    }
    private Account account(UUID token, UUID groupId, HttpServletRequest req) {
        UUID eventId=event(token); HttpSession session=req.getSession(false);
        if(session==null)throw denied();
        if(groupId==null){
            Object raw=session.getAttribute("gift.group.admin."+eventId);
            Object page=session.getAttribute("gift.group.page."+eventId);
            Object last=session.getAttribute("gift.group.activity."+eventId);
            if(!(raw instanceof UUID id)||!(page instanceof String pid)||!pid.equals(req.getHeader("X-Group-Page-Id"))
                ||!(last instanceof Long when)||System.currentTimeMillis()-when<0||System.currentTimeMillis()-when>=600000)throw denied();
            Object epoch=session.getAttribute("gift.group.admin.epoch."+eventId);
            if(!(epoch instanceof Long ver)||ver.longValue()!=versions.current("GROUP_ADMIN",id))throw denied();
            Integer exists=db.queryForObject("SELECT count(*) FROM gift_group_accounts WHERE id=? AND event_id=?",Integer.class,id,eventId);
            if(exists==null||exists!=1)throw denied();
            session.setAttribute("gift.group.activity."+eventId,System.currentTimeMillis());
            return new Account("GROUP_ADMIN",id);
        }
        Integer group=db.queryForObject("SELECT count(*) FROM gift_groups WHERE id=? AND event_id=?",Integer.class,groupId,eventId);
        if(group==null||group!=1)throw denied();
        Object raw=session.getAttribute("gift.group.member."+groupId);
        if(!(raw instanceof UUID id))throw denied();
        Object epoch=session.getAttribute("gift.group.member.epoch."+groupId);
        if(!(epoch instanceof Long ver)||ver.longValue()!=versions.current("GROUP_MEMBER",id))throw denied();
        Integer approved=db.queryForObject("SELECT count(*) FROM gift_group_members WHERE id=? AND group_id=? AND status='APPROVED'",Integer.class,id,groupId);
        if(approved==null||approved!=1)throw denied();
        return new Account("GROUP_MEMBER",id);
    }
    @GetMapping("/status")
    public com.giftplatform.security.verification.MobileVerificationService.Status status(@PathVariable UUID token,@RequestParam(required=false) UUID groupId,HttpServletRequest request){
        if(mobileService==null)throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
        Account a=account(token,groupId,request);return mobileService.status(a.kind,a.id);
    }
    private String fingerprint(String value) {
        try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));}
        catch(Exception ex){throw new IllegalStateException(ex);}
    }
    private String signed(UUID challenge,Account a,String destination,String code) {
        return RecoveryOtp.fingerprint(key,challenge,a.kind,a.id,"VERIFY_CONTACT",destination,code);
    }
    @PostMapping("/start")
    @Transactional
    public Started start(@PathVariable UUID token,@RequestBody Start body,HttpServletRequest req) {
        if(mobileService!=null){
            if(body==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            Account a=account(token,body.groupId(),req);
            var result=mobileService.start(a.kind,a.id,body.mobile());
            return new Started(result.challengeId(),result.message());
        }
        ready();
        if(body==null||body.mobile()==null||!body.mobile().matches("\\+[1-9][0-9]{7,14}"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Use international mobile format, e.g. +919876543210");
        Account a=account(token,body.groupId(),req);
        Integer count=db.queryForObject("SELECT count(*) FROM account_contact_challenges WHERE account_kind=? AND account_id=? AND channel='SMS' AND created_at>now()-interval '1 hour'",Integer.class,a.kind,a.id);
        if(count!=null&&count>=3)throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,"Try again later");
        UUID id=UUID.randomUUID();String code=RecoveryOtp.generate();
        challenges.create(id,a.kind,a.id,"SMS","VERIFY_CONTACT",fingerprint(body.mobile()),signed(id,a,body.mobile(),code),Instant.now().plusSeconds(600));
        db.update("INSERT INTO account_pending_mobile_verifications(challenge_id,account_kind,account_id,mobile,expires_at) VALUES(?,?,?,?,?)",
            id,a.kind,a.id,body.mobile(),java.sql.Timestamp.from(Instant.now().plusSeconds(600)));
        try{sms.send(body.mobile(),"Gift Platform LOCAL TEST ONLY OTP: "+code);}
        catch(RuntimeException ex){throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"SMS simulator unavailable");}
        return new Started(id,"Local test code sent to development SMS inbox. This is not real mobile verification.");
    }
    @PostMapping("/complete")
    @Transactional
    public Result complete(@PathVariable UUID token,@RequestBody Complete body,HttpServletRequest req) {
        if(mobileService!=null){
            if(body==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            Account a=account(token,body.groupId(),req);
            mobileService.complete(a.kind,a.id,body.challengeId(),body.code());
            return new Result("Mobile number verified");
        }
        ready();
        if(body==null||body.challengeId()==null||body.code()==null||!body.code().matches("[0-9]{6}"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid code");
        Account a=account(token,body.groupId(),req);
        List<String> mobiles=db.query("SELECT mobile FROM account_pending_mobile_verifications WHERE challenge_id=? AND account_kind=? AND account_id=? AND expires_at>now() FOR UPDATE",
            (rs,n)->rs.getString(1),body.challengeId(),a.kind,a.id);
        if(mobiles.size()!=1)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid or expired code");
        String mobile=mobiles.get(0);
        if(!challenges.verifyAndConsume(body.challengeId(),a.kind,a.id,"VERIFY_CONTACT",fingerprint(mobile),signed(body.challengeId(),a,mobile,body.code())))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid or expired code");
        // Intentionally NEVER set mobile_verified_at: local simulator cannot establish ownership.
        db.update("DELETE FROM account_pending_mobile_verifications WHERE challenge_id=?",body.challengeId());
        return new Result("Local OTP test passed. Mobile remains unverified until a real SMS provider is connected.");
    }
}
