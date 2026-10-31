package com.giftplatform.security.recovery;

import com.giftplatform.admin.EffectiveModulePermissionService;
import com.giftplatform.security.delivery.MessageDelivery;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.Instant;
import java.util.*;

/** Pre-account email ownership proof; never creates a login session. */
@Service
public class SignupEmailVerificationService {
    @Autowired(required=false) private com.giftplatform.security.verification.VerificationSettings verification;
    private static final UUID HOST_SCOPE=new UUID(0,0);
    private final JdbcTemplate db;private final TransactionTemplate tx;private final MessageDelivery delivery;
    private final EffectiveModulePermissionService permissions;private final byte[] key;
    public record Started(UUID challengeId,String message){}
    public record Verified(String verificationToken,String message,Instant expiresAt){}
    public SignupEmailVerificationService(JdbcTemplate db,TransactionTemplate tx,
            @Qualifier("otpEmailDelivery") MessageDelivery delivery,EffectiveModulePermissionService permissions,
            @Value("${gift.otp.hmac-key-base64:${gift.otp.secret-base64:}}") String key64){
        this.db=db;this.tx=tx;this.delivery=delivery;this.permissions=permissions;
        try{key=key64.isBlank()?new byte[0]:Base64.getDecoder().decode(key64);}catch(IllegalArgumentException ex){throw new IllegalStateException("Invalid email verification key");}
    }
    private ResponseStatusException bad(){return new ResponseStatusException(HttpStatus.BAD_REQUEST,"The code or verification has expired. Verify your email again before signing up.");}
    private static String address(String email){String value=email==null?"":email.strip().toLowerCase(Locale.ROOT);if(value.length()>254||!value.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+"))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Enter a valid email address");return value;}
    private static String digest(String value){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));}catch(NoSuchAlgorithmException ex){throw new IllegalStateException(ex);}}
    private String hash(UUID id,String kind,UUID scope,String email,String code){return RecoveryOtp.fingerprint(key,id,kind,scope,"VERIFY_SIGNUP_EMAIL",email,code);}
    private UUID scope(String kind,UUID token,UUID group){
        if("HOST".equals(kind))return HOST_SCOPE;
        if(!Set.of("GROUP_ADMIN","GROUP_MEMBER").contains(kind)||token==null)throw bad();
        var events=db.query("SELECT id,owner_id FROM events WHERE public_token=? AND status='PUBLISHED'",(rs,n)->new UUID[]{rs.getObject(1,UUID.class),rs.getObject(2,UUID.class)},token);
        if(events.size()!=1)throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Invitation unavailable");
        permissions.require(events.getFirst()[1],"GROUP_GIFTING");
        if(kind.equals("GROUP_ADMIN"))return events.getFirst()[0];
        Integer count=db.queryForObject("SELECT count(*) FROM gift_groups WHERE id=? AND event_id=? AND contribution_access_mode<>'GUESTS_ONLY' AND join_policy<>'INVITE_ONLY'",Integer.class,group,events.getFirst()[0]);
        if(count==null||count!=1)throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Group registration unavailable");
        return group;
    }
    public Started start(String kind,String email,UUID token,UUID group,String ip){
        if(kind==null||!Set.of("HOST","GROUP_ADMIN","GROUP_MEMBER").contains(kind))throw bad();
        if(key.length<32||!delivery.isAvailable())throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Email verification is temporarily unavailable. Please try again later.");
        String destination=address(email),ipHash=digest(ip==null?"unknown":ip);UUID scope=scope(kind,token,group),id=UUID.randomUUID();String code=RecoveryOtp.generate();
        tx.executeWithoutResult(status->{
            db.queryForObject("SELECT pg_advisory_xact_lock(hashtextextended(?,0))",Object.class,"signup-email-ip:"+ipHash);
            db.queryForObject("SELECT pg_advisory_xact_lock(hashtextextended(?,0))",Object.class,"signup-email-address:"+destination);
            int byIp=db.queryForObject("SELECT count(*) FROM signup_email_verifications WHERE ip_fingerprint=? AND created_at>now()-interval '1 hour'",Integer.class,ipHash);
            int byEmail=db.queryForObject("SELECT count(*) FROM signup_email_verifications WHERE email=? AND created_at>now()-interval '1 hour'",Integer.class,destination);
            if(byIp>=12||byEmail>=3)throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,"Too many verification requests. Please try again later.");
            db.update("INSERT INTO signup_email_verifications(id,account_kind,scope_id,email,ip_fingerprint,secret_hash,expires_at) VALUES(?,?,?,?,?,?,?)",id,kind,scope,destination,ipHash,hash(id,kind,scope,destination,code),java.sql.Timestamp.from(Instant.now().plusSeconds(600)));
        });
        try{delivery.send(destination,"Your Gift Platform signup email verification code is "+code+". It expires in 10 minutes. Never share it.");}
        catch(com.giftplatform.security.delivery.EmailDeliveryException ex){throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,ex.getMessage());}
        return new Started(id,"Check your email for a six-digit code.");
    }
    public Verified verify(UUID id,String code){
        if(key.length<32)throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Email verification is not configured");
        if(id==null||code==null||!code.matches("[0-9]{6}"))throw bad();
        Verified proof=tx.execute(status->{
            var rows=db.queryForList("SELECT account_kind,scope_id,email,secret_hash,expires_at FROM signup_email_verifications WHERE id=? AND expires_at>now() AND verified_at IS NULL AND attempt_count<5 FOR UPDATE",id);
            if(rows.size()!=1)return null;
            var row=rows.getFirst();String expected=hash(id,(String)row.get("account_kind"),(UUID)row.get("scope_id"),(String)row.get("email"),code);
            db.update("UPDATE signup_email_verifications SET attempt_count=attempt_count+1 WHERE id=?",id);
            if(!RecoveryOtp.matches((String)row.get("secret_hash"),expected))return null;
            byte[] bytes=new byte[32];new SecureRandom().nextBytes(bytes);String value=Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            db.update("UPDATE signup_email_verifications SET verified_at=now(),proof_hash=? WHERE id=?",digest(value),id);return new Verified(value,"Email verified. You can finish creating your account.",((java.sql.Timestamp)row.get("expires_at")).toInstant());
        });
        if(proof==null)throw bad();return proof;
    }
    /** Must join account creation's transaction so a failed signup does not spend the proof. */
    @Transactional(propagation=Propagation.MANDATORY)
    public boolean consume(String token,String kind,UUID scope,String email){
        if(verification!=null&&!verification.emailEnabled())return false;
        if(token==null||token.isBlank())return false;
        if(!token.matches("[A-Za-z0-9_-]{43}"))throw bad();
        int changed=db.update("UPDATE signup_email_verifications SET consumed_at=now() WHERE proof_hash=? AND account_kind=? AND scope_id=? AND email=? AND verified_at IS NOT NULL AND consumed_at IS NULL AND expires_at>now()",digest(token),kind,scope==null?HOST_SCOPE:scope,address(email));
        if(changed!=1)throw bad();return true;
    }
}
