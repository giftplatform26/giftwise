package com.giftplatform.security.recovery;

import com.giftplatform.security.GiftUserPrincipal;
import com.giftplatform.security.delivery.MessageDelivery;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** Email-only recovery. Never trusts unverified legacy contacts or the SMS simulator. */
@RestController
@RequestMapping("/api/auth/recovery")
public class EmailRecoveryController {
    private final JdbcTemplate db;
    private final RecoveryChallengeStore challenges;
    private final CredentialSessionVersionStore versions;
    private final PasswordEncoder passwords;
    private final MessageDelivery email;
    private final byte[] key;
    private static final ConcurrentHashMap<String, Window> IP_LIMIT = new ConcurrentHashMap<>();
    private record Window(long start, int count) {}
    private record Account(String kind, UUID id, String email, boolean verified, String groupId) {}
    public record RequestCode(String accountKind, String identifier, String purpose) {}
    public record CodeIssued(UUID challengeId, String message) {}
    public record Reset(UUID challengeId, String accountKind, String identifier, String purpose, String code, String newCredential, String confirmCredential) {}
    public record VerifyEmail(UUID challengeId, String code) {}
    public record Result(String message) {}

    public EmailRecoveryController(JdbcTemplate db, RecoveryChallengeStore challenges,
            CredentialSessionVersionStore versions, PasswordEncoder passwords,
            @Qualifier("otpEmailDelivery") MessageDelivery email,
            @Value("${gift.otp.hmac-key-base64:}") String encodedKey) {
        this.db=db; this.challenges=challenges; this.versions=versions; this.passwords=passwords; this.email=email;
        byte[] decoded;
        try { decoded=encodedKey.isBlank()?new byte[0]:Base64.getDecoder().decode(encodedKey); }
        catch(IllegalArgumentException ex) { throw new IllegalStateException("Invalid OTP HMAC key",ex); }
        this.key=decoded;
    }
    private void ready() { if(key.length<32) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Recovery not configured"); }
    private static String normalized(String s) { return s==null?"":s.strip().toLowerCase(Locale.ROOT); }
    private static String digest(String s) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8))); }
        catch(Exception ex) { throw new IllegalStateException(ex); }
    }
    private static boolean permitted(String address) {
        long now=System.currentTimeMillis();
        if(IP_LIMIT.size()>20000) IP_LIMIT.entrySet().removeIf(e -> now-e.getValue().start>3600000L);
        Window w=IP_LIMIT.compute(address,(k,old)->old==null||now-old.start>3600000L?new Window(now,1):new Window(old.start,old.count+1));
        return w.count<=12;
    }
    private Account find(String kind, String identifier) {
        String value=normalized(identifier);
        if(value.length()>254||value.isEmpty()) return null;
        String sql=switch(kind) {
            case "HOST", "PLATFORM_ADMIN" -> "SELECT id,email,email_verified_at FROM users WHERE lower(email)=? AND role=? AND enabled=true";
            case "GROUP_ADMIN" -> "SELECT id,email,email_verified_at FROM gift_group_accounts WHERE lower(email)=?";
            case "GROUP_MEMBER" -> "SELECT id,email,email_verified_at FROM gift_group_members WHERE lower(email)=? AND status='APPROVED'";
            default -> null;
        };
        if(sql==null) return null;
        List<Account> accounts=db.query(sql, (rs,n)->new Account(kind,(UUID)rs.getObject("id"),rs.getString("email"),rs.getTimestamp("email_verified_at")!=null,null),
                kind.equals("HOST")||kind.equals("PLATFORM_ADMIN")?new Object[]{value,kind.equals("HOST")?"CUSTOMER":"PLATFORM_ADMIN"}:new Object[]{value});
        // Ambiguous email cannot safely identify a group account; fail closed.
        return accounts.size()==1?accounts.get(0):null;
    }
    private static String purpose(String p) { if(!"RESET_PIN".equals(p)&&!"RESET_PASSWORD".equals(p)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid recovery purpose"); return p; }
    private String sign(UUID challenge, Account a, String action, String code) {
        return RecoveryOtp.fingerprint(key,challenge,a.kind,a.id,action,normalized(a.email),code);
    }
    private UUID issue(Account a, String action) {
        UUID id=UUID.randomUUID(); String code=RecoveryOtp.generate();
        challenges.create(id,a.kind,a.id,"EMAIL",action,digest(normalized(a.email)),sign(id,a,action,code),Instant.now().plusSeconds(600));
        email.send(a.email,"Your Gift Platform security code is "+code+". It expires in 10 minutes. Never share it.");
        return id;
    }
    @PostMapping("/request")
    public CodeIssued request(@RequestBody RequestCode body, jakarta.servlet.http.HttpServletRequest req) {
        ready();
        if(body==null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        String action=purpose(body.purpose());
        UUID opaque=UUID.randomUUID();
        String remote=req.getRemoteAddr()==null?"unknown":req.getRemoteAddr();
        if(!permitted(remote)) throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,"Please try again later");
        Account a=find(body.accountKind(),body.identifier());
        if(a!=null&&a.verified&&(!action.equals("RESET_PIN")||a.kind.startsWith("GROUP_"))) {
            Integer count=db.queryForObject("SELECT count(*) FROM account_contact_challenges WHERE account_kind=? AND account_id=? AND purpose=? AND created_at>now()-interval '1 hour'",Integer.class,a.kind,a.id,action);
            if(count!=null&&count<3) {
                try { opaque=issue(a,action); }
                catch(RuntimeException ignored) { /* Avoid leaking whether account exists or transport is enabled. */ }
            }
        }
        return new CodeIssued(opaque,"If an eligible verified account exists, a security code will be sent.");
    }
    private static void validateNew(String purpose,String value,String confirmation) {
        if(value==null||!value.equals(confirmation)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Credentials do not match");
        if("RESET_PIN".equals(purpose)) {
            if(!value.matches("[0-9]{6}")) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"PIN must be six digits");
        } else if(value.length()<8||value.length()>128||!value.matches("(?s).*\\p{L}.*")||!value.matches("(?s).*\\d.*")||!value.matches("(?s).*[\\p{Punct}].*")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Password must have 8–128 characters including a letter, number and symbol");
        }
    }
    @PostMapping("/complete") @Transactional
    public Result complete(@RequestBody Reset body) {
        ready();
        if(body==null||body.challengeId()==null||body.code()==null||!body.code().matches("[0-9]{6}")) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid or expired code");
        String action=purpose(body.purpose());
        validateNew(action,body.newCredential(),body.confirmCredential());
        Account a=find(body.accountKind(),body.identifier());
        if(a==null||!a.verified||action.equals("RESET_PIN")&&!a.kind.startsWith("GROUP_")) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid or expired code");
        boolean valid=challenges.verifyAndConsume(body.challengeId(),a.kind,a.id,action,digest(normalized(a.email)),sign(body.challengeId(),a,action,body.code()));
        if(!valid) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid or expired code");
        String table=switch(a.kind){case "HOST","PLATFORM_ADMIN"->"users";case "GROUP_ADMIN"->"gift_group_accounts";default->"gift_group_members";};
        if(action.equals("RESET_PIN")) {
            // Keep existing PIN uniqueness guarantees within the event/group.
            String scopeColumn=a.kind.equals("GROUP_ADMIN")?"event_id":"group_id";
            UUID scope=db.queryForObject("SELECT "+scopeColumn+" FROM "+table+" WHERE id=?",UUID.class,a.id);
            db.queryForObject("SELECT id FROM "+(a.kind.equals("GROUP_ADMIN")?"events":"gift_groups")+" WHERE id=? FOR UPDATE",UUID.class,scope);
            for(Map<String,Object> other:db.queryForList("SELECT id,pin_hash FROM "+table+" WHERE "+scopeColumn+"=? AND id<>? AND pin_hash IS NOT NULL",scope,a.id)) {
                if(passwords.matches(body.newCredential(),(String)other.get("pin_hash"))) throw new ResponseStatusException(HttpStatus.CONFLICT,"Choose a different PIN");
            }
        }
        String field=action.equals("RESET_PIN")?"pin_hash":"password_hash";
        if(db.update("UPDATE "+table+" SET "+field+"=? WHERE id=?",passwords.encode(body.newCredential()),a.id)!=1) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Account unavailable");
        versions.revokeAll(a.kind,a.id);
        return new Result("Credential updated. Sign in again.");
    }
    public record EmailStatus(String email,boolean verified){}
    @GetMapping("/email-status")
    public EmailStatus emailStatus(Authentication auth){
        if(auth==null||!(auth.getPrincipal() instanceof GiftUserPrincipal p))throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        String kind=p.getAuthorities().stream().anyMatch(x->x.getAuthority().equals("ROLE_PLATFORM_ADMIN"))?"PLATFORM_ADMIN":"HOST";
        Account account=find(kind,p.getUsername());
        if(account==null||!account.id.equals(p.getUserId()))throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        return new EmailStatus(account.email,account.verified);
    }
    @PostMapping("/enroll-email")
    public CodeIssued enroll(Authentication auth, jakarta.servlet.http.HttpServletRequest req) {
        ready();
        if(auth==null||!(auth.getPrincipal() instanceof GiftUserPrincipal p)) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        if(!permitted(req.getRemoteAddr()==null?"unknown":req.getRemoteAddr())) throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS);
        String kind=p.getAuthorities().stream().anyMatch(x->x.getAuthority().equals("ROLE_PLATFORM_ADMIN"))?"PLATFORM_ADMIN":"HOST";
        Account a=find(kind,p.getUsername());
        if(a==null||!a.id.equals(p.getUserId())) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        Integer count=db.queryForObject("SELECT count(*) FROM account_contact_challenges WHERE account_kind=? AND account_id=? AND purpose='VERIFY_CONTACT' AND created_at>now()-interval '1 hour'",Integer.class,kind,a.id);
        if(count!=null&&count>=3) throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS);
        if(!email.isAvailable())throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Email verification delivery is not configured. Please contact support.");
        try{return new CodeIssued(issue(a,"VERIFY_CONTACT"),"A verification code has been sent to your registered email.");}
        catch(com.giftplatform.security.delivery.EmailDeliveryException ex){
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,ex.getMessage());
        }
    }
    @PostMapping("/verify-email") @Transactional
    public Result verifyEmail(@RequestBody VerifyEmail body,Authentication auth) {
        ready();
        if(auth==null||!(auth.getPrincipal() instanceof GiftUserPrincipal p)||body==null||body.challengeId()==null||body.code()==null||!body.code().matches("[0-9]{6}")) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        String kind=p.getAuthorities().stream().anyMatch(x->x.getAuthority().equals("ROLE_PLATFORM_ADMIN"))?"PLATFORM_ADMIN":"HOST";
        Account a=find(kind,p.getUsername());
        if(a==null||!a.id.equals(p.getUserId())||!challenges.verifyAndConsume(body.challengeId(),kind,a.id,"VERIFY_CONTACT",digest(normalized(a.email)),sign(body.challengeId(),a,"VERIFY_CONTACT",body.code()))) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid or expired code");
        db.update("UPDATE users SET email_verified_at=now() WHERE id=? AND lower(email)=?",a.id,normalized(a.email));
        return new Result("Email verified for account recovery.");
    }
}
