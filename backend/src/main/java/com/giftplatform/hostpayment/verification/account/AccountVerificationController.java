package com.giftplatform.hostpayment.verification.account;

import com.giftplatform.security.ActorContext;
import com.giftplatform.hostpayment.security.PaymentDataProtector;
import com.giftplatform.audit.service.AuditService;
import com.giftplatform.audit.entity.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.OffsetDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/v1/host/payment-profiles/account-verification")
@ConditionalOnProperty(name="gift.payment.profiles.enabled",havingValue="true")
public class AccountVerificationController {
    private final JdbcTemplate db; private final ActorContext actor; private final PaymentDataProtector protector;
    private final TransactionTemplate tx; private final AccountVerificationClient provider; private final AuditService audit;
    public AccountVerificationController(JdbcTemplate db,ActorContext actor,PaymentDataProtector protector,
        TransactionTemplate tx,AccountVerificationClient provider,AuditService audit){
        this.db=db;this.actor=actor;this.protector=protector;this.tx=tx;this.provider=provider;this.audit=audit;
    }
    public record Settings(String provider,String environment,boolean available,String message){}
    public record Check(UUID id,String environment,String outcome,String providerStatus,String providerReference,
                        boolean nameMatched,OffsetDateTime createdAt,OffsetDateTime completedAt){}
    public record Request(String accountHolderName,boolean consent){}
    private record Destination(long version,Map<String,String> payload,String upi,String name){}
    @GetMapping("/settings") public ResponseEntity<Settings> settings(){
        actor.requireAuthenticatedUserId();
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new Settings("CASHFREE",provider.environment(),provider.available(),
            provider.available()?"Account verification is ready":"Configure Cashfree Secure ID credentials to enable account verification"));
    }
    private static String table(String kind){return switch(kind){case "BANK"->"host_bank_accounts";case "UPI"->"host_upi_accounts";default->throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid account kind");};}
    private void owned(UUID host,UUID id,String table){
        Integer count=db.queryForObject("SELECT count(*) FROM "+table+" WHERE id=? AND host_id=? AND archived_at IS NULL",Integer.class,id,host);
        if(count==null||count!=1)throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Account not found");
    }
    private List<Check> checks(UUID host,UUID id,String kind){return db.query("""
        SELECT id,environment,outcome,provider_status,provider_reference,name_matched,created_at,completed_at
        FROM host_account_verification_checks WHERE host_id=? AND destination_id=? AND destination_kind=?
        ORDER BY created_at DESC,id DESC LIMIT 20
        """,(rs,n)->new Check(rs.getObject(1,UUID.class),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5),rs.getBoolean(6),
            rs.getObject(7,OffsetDateTime.class),rs.getObject(8,OffsetDateTime.class)),host,id,kind);}
    @GetMapping("/{kind}/{id}") public ResponseEntity<List<Check>> history(@PathVariable String kind,@PathVariable UUID id){
        String type=kind.toUpperCase(Locale.ROOT),table=table(type);UUID host=actor.requireAuthenticatedUserId();owned(host,id,table);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(checks(host,id,type));
    }
    @PostMapping("/{kind}/{id}") public ResponseEntity<Check> verify(@PathVariable String kind,@PathVariable UUID id,@RequestBody Request input){
        UUID host=actor.requireAuthenticatedUserId();String type=kind.toUpperCase(Locale.ROOT),table=table(type);owned(host,id,table);
        if(input==null||!input.consent())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Consent is required for provider account verification");
        if(!provider.available())throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Account verification provider credentials are not configured");
        UUID checkId=UUID.randomUUID();String reference="acct_"+checkId.toString().replace("-","");
        Destination destination=tx.execute(s->{
            db.queryForObject("SELECT id FROM users WHERE id=? FOR UPDATE",UUID.class,host);
            db.update("UPDATE host_account_verification_checks SET outcome='UNAVAILABLE',completed_at=now() WHERE host_id=? AND outcome='PENDING' AND created_at<now()-interval '2 minutes'",host);
            Integer recent=db.queryForObject("SELECT count(*) FROM host_account_verification_checks WHERE host_id=? AND created_at>now()-interval '1 minute'",Integer.class,host);
            if(recent!=null&&recent>=5)throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,"Wait a minute before requesting more verification checks");
            Integer pending=db.queryForObject("SELECT count(*) FROM host_account_verification_checks WHERE host_id=? AND destination_id=? AND destination_kind=? AND outcome='PENDING'",Integer.class,host,id,type);
            if(pending!=null&&pending>0)throw new ResponseStatusException(HttpStatus.CONFLICT,"Account verification is already in progress");
            String columns=type.equals("BANK")?"account_number_encrypted,ifsc_encrypted,account_holder_encrypted":"upi_id_encrypted,NULL,NULL";
            var rows=db.query("SELECT version,"+columns+" FROM "+table+" WHERE id=? AND host_id=? AND enabled AND archived_at IS NULL AND verification_status<>'REVOKED'",(rs,n)->{
                if(type.equals("BANK")){
                    String name=protector.decrypt(host,id,"account_holder",rs.getString(4));
                    return new Destination(rs.getLong(1),Map.of("bank_account",protector.decrypt(host,id,"account_number",rs.getString(2)),"ifsc",protector.decrypt(host,id,"ifsc",rs.getString(3)),"name",name),null,name);
                }
                String name=input.accountHolderName();
                if(name==null||name.isBlank()||name.length()>120||name.chars().anyMatch(Character::isISOControl))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Enter the account holder name for UPI verification");
                String upi=protector.decrypt(host,id,"upi_id",rs.getString(2));
                return new Destination(rs.getLong(1),Map.of("verification_id",reference,"vpa",upi,"name",name.strip()),upi,name.strip());
            },id,host);
            if(rows.size()!=1)throw new ResponseStatusException(HttpStatus.CONFLICT,"Account is not eligible for verification");
            db.update("INSERT INTO host_account_verification_checks(id,host_id,destination_id,destination_kind,environment,outcome) VALUES(?,?,?,?,?,'PENDING')",checkId,host,id,type,provider.environment());
            return rows.getFirst();
        });
        AccountVerificationClient.Decision decision;
        try{decision=AccountVerificationClient.interpret(provider.verify(type,destination.payload()),type,reference,destination.upi(),destination.name(),provider.environment().equals("SANDBOX"));}
        catch(RuntimeException ex){decision=new AccountVerificationClient.Decision("UNAVAILABLE","UNAVAILABLE",null,false);}
        final var result=decision;
        tx.executeWithoutResult(s->{
            // Serialize against disable/archive and reject results for a changed account.
            var versions=db.query("SELECT version FROM "+table+" WHERE id=? AND host_id=? AND enabled AND archived_at IS NULL AND verification_status<>'REVOKED' FOR UPDATE",(rs,n)->rs.getLong(1),id,host);
            String outcome=versions.size()!=1||versions.getFirst()!=destination.version()?"STALE":result.outcome();
            if(outcome.equals("VERIFIED")){
                db.update("UPDATE "+table+" SET verification_status='VERIFIED',version=version+1,updated_at=now() WHERE id=? AND host_id=?",id,host);
                audit.recordChange(host,AuditActorType.CUSTOMER,type.equals("BANK")?"HOST_BANK_ACCOUNT":"HOST_UPI_ACCOUNT",id,AuditAction.STATUS_CHANGE,null,Map.of("status","VERIFIED","provider","CASHFREE","checkId",checkId.toString()),null);
            }
            if(provider.environment().equals("PRODUCTION") && (outcome.equals("REJECTED")||outcome.equals("NAME_MISMATCH"))){
                db.update("UPDATE "+table+" SET verification_status='FAILED',preferred=FALSE,version=version+1,updated_at=now() WHERE id=? AND host_id=?",id,host);
                audit.recordChange(host,AuditActorType.CUSTOMER,type.equals("BANK")?"HOST_BANK_ACCOUNT":"HOST_UPI_ACCOUNT",id,AuditAction.STATUS_CHANGE,null,Map.of("status","FAILED","provider","CASHFREE","checkId",checkId.toString()),null);
            }
            db.update("UPDATE host_account_verification_checks SET outcome=?,provider_status=?,provider_reference=?,name_matched=?,completed_at=now() WHERE id=?",outcome,result.providerStatus(),result.providerReference(),result.nameMatched(),checkId);
        });
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(checks(host,id,type).stream().filter(c->c.id().equals(checkId)).findFirst().orElseThrow());
    }
}
