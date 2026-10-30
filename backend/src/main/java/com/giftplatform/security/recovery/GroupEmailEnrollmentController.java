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

/** Authenticated group contact enrollment; email is not trusted until its OTP is consumed. */
@RestController
@RequestMapping("/api/public/groups/{token}/contact-email")
public class GroupEmailEnrollmentController {
    private final JdbcTemplate db;
    private final RecoveryChallengeStore challenges;
    private final CredentialSessionVersionStore versions;
    private final MessageDelivery email;
    private final byte[] key;
    public record Start(String email, UUID groupId) {}
    public record Complete(UUID challengeId, String code, UUID groupId) {}
    public record Started(UUID challengeId, String message) {}
    public record Result(String message) {}
    private record Account(String kind, UUID id) {}

    public GroupEmailEnrollmentController(JdbcTemplate db, RecoveryChallengeStore challenges,
            CredentialSessionVersionStore versions, @Qualifier("otpEmailDelivery") MessageDelivery email,
            @Value("${gift.otp.hmac-key-base64:}") String encodedKey) {
        this.db=db; this.challenges=challenges; this.versions=versions; this.email=email;
        try { key=encodedKey.isBlank()?new byte[0]:Base64.getDecoder().decode(encodedKey); }
        catch(IllegalArgumentException ex) { throw new IllegalStateException("Invalid OTP HMAC key",ex); }
    }
    private void ready() {
        if(key.length<32) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Email verification is not configured");
    }
    private static ResponseStatusException denied() { return new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Sign in again to verify your contact"); }
    private static String fingerprint(String value) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); }
        catch(Exception ex) { throw new IllegalStateException(ex); }
    }
    private UUID event(UUID token) {
        List<UUID> ids=db.query("SELECT id FROM events WHERE public_token=? AND status='PUBLISHED'",(rs,n)->(UUID)rs.getObject(1),token);
        if(ids.size()!=1) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return ids.get(0);
    }
    private Account account(UUID token, UUID groupId, HttpServletRequest req) {
        UUID eventId=event(token);
        HttpSession session=req.getSession(false);
        if(session==null) throw denied();
        if(groupId==null) {
            Object raw=session.getAttribute("gift.group.admin."+eventId);
            Object page=session.getAttribute("gift.group.page."+eventId);
            Object activity=session.getAttribute("gift.group.activity."+eventId);
            if(!(raw instanceof UUID id) || !(page instanceof String pageId)
                    || !pageId.equals(req.getHeader("X-Group-Page-Id")) || !(activity instanceof Long last)
                    || System.currentTimeMillis()-last<0 || System.currentTimeMillis()-last>=600000L) throw denied();
            Object epoch=session.getAttribute("gift.group.admin.epoch."+eventId);
            if(!(epoch instanceof Long version) || version.longValue()!=versions.current("GROUP_ADMIN",id)) throw denied();
            Integer count=db.queryForObject("SELECT count(*) FROM gift_group_accounts WHERE id=? AND event_id=?",Integer.class,id,eventId);
            if(count==null||count!=1) throw denied();
            session.setAttribute("gift.group.activity."+eventId,System.currentTimeMillis());
            return new Account("GROUP_ADMIN",id);
        }
        Integer exists=db.queryForObject("SELECT count(*) FROM gift_groups WHERE id=? AND event_id=?",Integer.class,groupId,eventId);
        if(exists==null||exists!=1) throw denied();
        Object raw=session.getAttribute("gift.group.member."+groupId);
        if(!(raw instanceof UUID id)) throw denied();
        Object epoch=session.getAttribute("gift.group.member.epoch."+groupId);
        if(!(epoch instanceof Long version)||version.longValue()!=versions.current("GROUP_MEMBER",id)) throw denied();
        Integer active=db.queryForObject("SELECT count(*) FROM gift_group_members WHERE id=? AND group_id=? AND status='APPROVED'",Integer.class,id,groupId);
        if(active==null||active!=1) throw denied();
        return new Account("GROUP_MEMBER",id);
    }
    private String hash(UUID challenge, Account a, String destination, String code) {
        return RecoveryOtp.fingerprint(key,challenge,a.kind,a.id,"VERIFY_CONTACT",destination,code);
    }
    public record EmailStatus(String email,boolean verified){}
    @GetMapping("/status")
    public EmailStatus status(@PathVariable UUID token,@RequestParam(required=false) UUID groupId,HttpServletRequest request){
        Account a=account(token,groupId,request);
        String table=a.kind.equals("GROUP_ADMIN")?"gift_group_accounts":"gift_group_members";
        return db.queryForObject("SELECT email,email_verified_at IS NOT NULL FROM "+table+" WHERE id=?",(rs,n)->new EmailStatus(rs.getString(1),rs.getBoolean(2)),a.id);
    }
    @PostMapping("/start")
    @Transactional
    public Started start(@PathVariable UUID token,@RequestBody Start body,HttpServletRequest req) {
        ready();
        if(body==null||body.email()==null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Email required");
        Account a=account(token,body.groupId(),req);
        String destination=body.email().strip().toLowerCase(Locale.ROOT);
        if(destination.length()>254||!destination.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Enter a valid email address");
        Integer attempts=db.queryForObject("SELECT count(*) FROM account_contact_challenges WHERE account_kind=? AND account_id=? AND purpose='VERIFY_CONTACT' AND created_at>now()-interval '1 hour'",Integer.class,a.kind,a.id);
        if(attempts!=null&&attempts>=3) throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,"Try again later");
        UUID id=UUID.randomUUID();String code=RecoveryOtp.generate();
        challenges.create(id,a.kind,a.id,"EMAIL","VERIFY_CONTACT",fingerprint(destination),hash(id,a,destination,code),Instant.now().plusSeconds(600));
        db.update("INSERT INTO account_pending_email_verifications(challenge_id,account_kind,account_id,email,expires_at) VALUES(?,?,?,?,?)",id,a.kind,a.id,destination,java.sql.Timestamp.from(Instant.now().plusSeconds(600)));
        try { email.send(destination,"Your Gift Platform email verification code is "+code+". It expires in 10 minutes. Never share it."); }
        catch(RuntimeException ex) { throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Email delivery unavailable"); }
        return new Started(id,"Verification code sent to your new email address");
    }
    @PostMapping("/complete")
    @Transactional
    public Result complete(@PathVariable UUID token,@RequestBody Complete body,HttpServletRequest req) {
        ready();
        if(body==null||body.challengeId()==null||body.code()==null||!body.code().matches("[0-9]{6}"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid or expired code");
        Account a=account(token,body.groupId(),req);
        List<String> pending=db.query("SELECT email FROM account_pending_email_verifications WHERE challenge_id=? AND account_kind=? AND account_id=? AND expires_at>now() FOR UPDATE",(rs,n)->rs.getString(1),body.challengeId(),a.kind,a.id);
        if(pending.size()!=1) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid or expired code");
        String destination=pending.get(0);
        if(!challenges.verifyAndConsume(body.challengeId(),a.kind,a.id,"VERIFY_CONTACT",fingerprint(destination),hash(body.challengeId(),a,destination,body.code())))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid or expired code");
        String table=a.kind.equals("GROUP_ADMIN")?"gift_group_accounts":"gift_group_members";
        if(db.update("UPDATE "+table+" SET email=?,email_verified_at=now() WHERE id=?",destination,a.id)!=1)
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Account no longer available");
        db.update("DELETE FROM account_pending_email_verifications WHERE challenge_id=?",body.challengeId());
        return new Result("Email verified. You can now use it for account recovery.");
    }
}
