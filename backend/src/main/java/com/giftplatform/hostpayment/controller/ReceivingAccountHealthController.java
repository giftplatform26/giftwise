package com.giftplatform.hostpayment.controller;

import com.giftplatform.hostpayment.security.PaymentDataProtector;
import com.giftplatform.security.ActorContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

/** Owner-only, read-only diagnostics; never returns decrypted account details. */
@RestController
@ConditionalOnProperty(name="gift.payment.profiles.enabled",havingValue="true")
public class ReceivingAccountHealthController {
    private final JdbcTemplate db;private final ActorContext actor;private final PaymentDataProtector protector;private final boolean transfers;
    public ReceivingAccountHealthController(JdbcTemplate db,ActorContext actor,PaymentDataProtector protector,@org.springframework.beans.factory.annotation.Value("${gift.payment.direct-transfers.enabled:false}") boolean transfers){this.db=db;this.actor=actor;this.protector=protector;this.transfers=transfers;}
    public record Health(UUID id,String method,String label,String status,String message,boolean guestVisible){}
    @GetMapping("/api/v1/events/{eventId}/receiving-health")
    public ResponseEntity<List<Health>> health(@PathVariable UUID eventId){
        UUID host=actor.requireAuthenticatedUserId();
        Integer owned=db.queryForObject("SELECT count(*) FROM events WHERE id=? AND owner_id=?",Integer.class,eventId,host);
        if(owned==null||owned!=1)throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Event not found");
        boolean published=Boolean.TRUE.equals(db.queryForObject("SELECT status='PUBLISHED' FROM events WHERE id=?",Boolean.class,eventId));
        var result=db.query("""
            SELECT a.id,'BANK' AS method,a.account_label,(a.enabled AND l.enabled) AS enabled,l.preferred,
              a.verification_status,a.host_confirmed_at IS NOT NULL AS confirmed,
              a.account_number_encrypted AS first_value,a.ifsc_encrypted AS second_value,a.account_holder_encrypted AS third_value,TRUE AS qr_ready
            FROM host_bank_accounts a JOIN event_payment_account_links l ON l.destination_id=a.id AND l.destination_kind='BANK' AND l.host_id=a.host_id
            WHERE l.event_id=? AND a.host_id=? AND a.archived_at IS NULL
            UNION ALL
            SELECT a.id,'UPI',a.account_label,(a.enabled AND l.enabled),l.preferred,
              a.verification_status,a.host_confirmed_at IS NOT NULL,a.upi_id_encrypted,NULL,NULL,
              EXISTS(SELECT 1 FROM host_upi_qr_images q WHERE q.host_id=a.host_id AND q.upi_account_id=a.id AND q.source IN ('UPLOADED','GENERATED') AND q.payload_sha256 IS NOT NULL)
            FROM host_upi_accounts a JOIN event_payment_account_links l ON l.destination_id=a.id AND l.destination_kind='UPI' AND l.host_id=a.host_id
            WHERE l.event_id=? AND a.host_id=? AND a.archived_at IS NULL
            """,(rs,n)->{
                UUID id=rs.getObject("id",UUID.class);String method=rs.getString("method");String label=rs.getString("account_label");
                try{
                    protector.decrypt(host,id,method.equals("BANK")?"account_number":"upi_id",rs.getString("first_value"));
                    if(method.equals("BANK")){
                        protector.decrypt(host,id,"ifsc",rs.getString("second_value"));
                        protector.decrypt(host,id,"account_holder",rs.getString("third_value"));
                    }
                }catch(IllegalArgumentException ex){return new Health(id,method,label,"DETAILS_UNAVAILABLE","Saved details cannot be read. Remove this account from the event and add it again, or ask support to restore its original encryption key. Then confirm, enable and select it as preferred.",false);}
                String verification=rs.getString("verification_status");
                if(!rs.getBoolean("enabled"))return new Health(id,method,label,"DISABLED","Enable this account and its event link to make it available.",false);
                if(Set.of("FAILED","REVOKED").contains(verification))return new Health(id,method,label,"VERIFICATION_REQUIRED","Resolve the account verification issue before offering it to guests.",false);
                if(!rs.getBoolean("confirmed")&&!"VERIFIED".equals(verification))return new Health(id,method,label,"CONFIRMATION_REQUIRED","Check and confirm your account details before selecting Preferred.",false);
                if(!rs.getBoolean("qr_ready"))return new Health(id,method,label,"QR_REQUIRED","Upload or generate a matching UPI QR, then select Preferred.",false);
                if(!rs.getBoolean("preferred"))return new Health(id,method,label,"NOT_PREFERRED","Select this linked account as Preferred to offer it to guests.",false);
                if(!transfers)return new Health(id,method,label,"TRANSFERS_DISABLED","Direct transfers are currently disabled by the platform.",false);
                if(!published)return new Health(id,method,label,"INVITATION_PRIVATE","Publish the invitation to make this account available to guests.",false);
                return new Health(id,method,label,"READY","Account details are ready for guests when direct transfers are enabled and the invitation is published. This does not verify any payment.",true);
            },eventId,host,eventId,host);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(result);
    }
}
