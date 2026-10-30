package com.giftplatform.security.recovery;

import com.giftplatform.security.delivery.MessageDelivery;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/** Sends a username only to its previously verified email. Never changes credentials or sessions. */
@RestController
@RequestMapping("/api/auth/recovery/username")
public class UsernameRecoveryController {
    private static final org.slf4j.Logger log=org.slf4j.LoggerFactory.getLogger(UsernameRecoveryController.class);
    private final JdbcTemplate db;
    private final TransactionTemplate transactions;
    private final MessageDelivery email;
    public record Request(String accountKind,String identifier,UUID eventToken,UUID groupId) {}
    public record Result(String message) {}
    private record Account(UUID id,String email,String username) {}
    public UsernameRecoveryController(JdbcTemplate db,TransactionTemplate transactions,
            @Qualifier("usernameReminderDelivery") MessageDelivery email) {
        this.db=db;this.transactions=transactions;this.email=email;
    }
    private Account find(Request input,String address) {
        List<Account> rows;
        if(Set.of("HOST","PLATFORM_ADMIN").contains(input.accountKind())) {
            rows=db.query("""
                SELECT id,email,username FROM users
                WHERE lower(email)=? AND role=? AND enabled=TRUE AND email_verified_at IS NOT NULL
                LIMIT 2
                """,(rs,n)->new Account(rs.getObject(1,UUID.class),rs.getString(2),rs.getString(3)),
                address,input.accountKind().equals("HOST")?"CUSTOMER":"PLATFORM_ADMIN");
        } else if("GROUP_ADMIN".equals(input.accountKind())) {
            rows=db.query("""
                SELECT a.id,a.email,a.username FROM gift_group_accounts a
                JOIN events e ON e.id=a.event_id
                WHERE lower(a.email)=? AND a.email_verified_at IS NOT NULL
                  AND (?::uuid IS NULL OR e.public_token=?::uuid)
                LIMIT 2
                """,(rs,n)->new Account(rs.getObject(1,UUID.class),rs.getString(2),rs.getString(3)),
                address,input.eventToken(),input.eventToken());
        } else {
            rows=db.query("""
                SELECT a.id,a.email,a.username FROM gift_group_members a
                JOIN gift_groups g ON g.id=a.group_id JOIN events e ON e.id=g.event_id
                WHERE lower(a.email)=? AND a.email_verified_at IS NOT NULL AND a.status='APPROVED'
                  AND (?::uuid IS NULL OR e.public_token=?::uuid)
                  AND (?::uuid IS NULL OR g.id=?::uuid)
                LIMIT 2
                """,(rs,n)->new Account(rs.getObject(1,UUID.class),rs.getString(2),rs.getString(3)),
                address,input.eventToken(),input.eventToken(),input.groupId(),input.groupId());
        }
        return rows.size()==1?rows.getFirst():null;
    }
    private static String fingerprint(String value) {
        try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));}
        catch(java.security.NoSuchAlgorithmException ex){throw new IllegalStateException(ex);}
    }
    private boolean reserve(String ip,String kind,Account account) {
        return Boolean.TRUE.equals(transactions.execute(status->{
            db.queryForObject("SELECT pg_advisory_xact_lock(hashtextextended(?,0))",Object.class,"username-recovery-ip:"+ip);
            int attempts=db.queryForObject("SELECT count(*) FROM username_recovery_attempts WHERE ip_fingerprint=? AND requested_at>now()-interval '1 hour'",Integer.class,ip);
            if(attempts>=12)throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,"Please try again later");
            boolean send=account!=null;
            if(account!=null){
                db.queryForObject("SELECT pg_advisory_xact_lock(hashtextextended(?,0))",Object.class,"username-recovery-account:"+kind+":"+account.id());
                int count=db.queryForObject("SELECT count(*) FROM username_recovery_attempts WHERE account_kind=? AND account_id=? AND requested_at>now()-interval '1 hour'",Integer.class,kind,account.id());
                send=count<3;
            }
            db.update("INSERT INTO username_recovery_attempts(id,ip_fingerprint,account_kind,account_id) VALUES(?,?,?,?)",UUID.randomUUID(),ip,kind,account==null?null:account.id());
            return send;
        }));
    }
    @PostMapping("/request")
    public ResponseEntity<Result> request(@RequestBody Request input,HttpServletRequest request) {
        if(input==null||input.accountKind()==null||!Set.of("HOST","PLATFORM_ADMIN","GROUP_ADMIN","GROUP_MEMBER").contains(input.accountKind()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Choose a valid account type");
        String address=input.identifier()==null?"":input.identifier().strip().toLowerCase(Locale.ROOT);
        if(address.length()>254||!address.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Enter your verified email address");
        if(!email.isAvailable())throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Username reminder delivery is not configured. Please contact support.");
        Account account=find(input,address);
        String remote=request.getRemoteAddr()==null?"unknown":request.getRemoteAddr();
        if(reserve(fingerprint(remote),input.accountKind(),account)){
            String label=switch(input.accountKind()){
                case "HOST"->"event host";case "PLATFORM_ADMIN"->"platform administrator";
                case "GROUP_ADMIN"->"group creator";default->"group member";
            };
            String reminder=account.username()==null||account.username().isBlank()
                    ? "No separate username is set for your Gift Platform "+label+" account. Sign in using your email address: "+account.email()
                    : "Your Gift Platform "+label+" username is: "+account.username();
            String instructions=input.accountKind().startsWith("GROUP_")
                    ? ". Use it with your PIN or password on your group invitation."
                    : ". Use it with your password on the Gift Platform sign-in page.";
            try{email.send(account.email(),reminder+instructions+" If you did not request this reminder, you can ignore it. Your sign-in details have not changed.");}
            catch(RuntimeException ex){
                // Never log recipients, usernames, provider bodies or credentials.
                log.warn("Username reminder delivery failed ({}); check email provider configuration",ex.getClass().getSimpleName());
            }
        }
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new Result(
                "If a matching account has this verified email, a username reminder will be sent. Your password and PIN remain unchanged."));
    }
}
