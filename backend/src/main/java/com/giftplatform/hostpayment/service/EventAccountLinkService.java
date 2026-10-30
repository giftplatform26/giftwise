package com.giftplatform.hostpayment.service;

import com.giftplatform.security.ActorContext;
import com.giftplatform.hostpayment.entity.VerificationStatus;
import com.giftplatform.hostpayment.qr.UpiQrImageStorage;
import com.giftplatform.audit.service.AuditService;
import com.giftplatform.audit.entity.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@Service
@ConditionalOnProperty(name="gift.payment.profiles.enabled",havingValue="true")
public class EventAccountLinkService {
    private final JdbcTemplate db;private final ActorContext actor;private final UpiQrImageStorage qr;private final AuditService audit;
    public EventAccountLinkService(JdbcTemplate db,ActorContext actor,UpiQrImageStorage qr,AuditService audit){this.db=db;this.actor=actor;this.qr=qr;this.audit=audit;}
    private String table(String kind){return switch(kind){case "BANK"->"host_bank_accounts";case "UPI"->"host_upi_accounts";default->throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid account kind");};}
    private UUID owned(UUID eventId,boolean lock){
        UUID host=actor.requireAuthenticatedUserId();
        var found=db.query("SELECT id FROM events WHERE id=? AND owner_id=?"+(lock?" FOR UPDATE":""),(rs,n)->rs.getObject(1,UUID.class),eventId,host);
        if(found.isEmpty())throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Event not found");return host;
    }
    @Transactional public void link(UUID eventId,String kind,UUID id){
        UUID host=owned(eventId,true);String table=table(kind);
        Integer count=db.queryForObject("SELECT count(*) FROM "+table+" WHERE id=? AND host_id=? AND archived_at IS NULL AND (event_id IS NULL OR event_id=?)",Integer.class,id,host,eventId);
        if(count==null||count!=1)throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Account not found");
        int inserted=db.update("INSERT INTO event_payment_account_links(event_id,host_id,"+(kind.equals("BANK")?"bank_account_id":"upi_account_id")+") VALUES(?,?,?) ON CONFLICT DO NOTHING",eventId,host,id);
        if(inserted>0)log(host,eventId,id,kind,"LINKED");
    }
    @Transactional public void update(UUID eventId,String kind,UUID id,String action){
        UUID host=owned(eventId,true);String table=table(kind);
        // Account lock serializes link eligibility with account disable/archive/verification.
        var rows=db.query("SELECT enabled,verification_status,host_confirmed_at,event_id FROM "+table+" WHERE id=? AND host_id=? AND archived_at IS NULL FOR UPDATE",(rs,n)->new Object[]{rs.getBoolean(1),rs.getString(2),rs.getObject(3),rs.getObject(4,UUID.class)},id,host);
        if(rows.isEmpty())throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Account not found");
        Integer linked=db.queryForObject("SELECT count(*) FROM event_payment_account_links WHERE event_id=? AND host_id=? AND destination_kind=? AND destination_id=?",Integer.class,eventId,host,kind,id);
        if(linked==null||linked!=1)throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Account is not linked to this event");
        switch(action){
            case "preferred" -> {
                var account=rows.getFirst();
                Boolean enabled=db.queryForObject("SELECT enabled FROM event_payment_account_links WHERE event_id=? AND destination_kind=? AND destination_id=?",Boolean.class,eventId,kind,id);
                if(!Boolean.TRUE.equals(enabled)||!Boolean.TRUE.equals(account[0])||Set.of("FAILED","REVOKED").contains(account[1])||(!"VERIFIED".equals(account[1])&&account[2]==null)
                    ||(kind.equals("UPI")&&!qr.hasValidQr(host,id)))throw new ResponseStatusException(HttpStatus.CONFLICT,"Enable and confirm this linked account before selecting Preferred; UPI requires a valid QR");
                db.update("UPDATE event_payment_account_links SET preferred=FALSE WHERE event_id=? AND destination_kind=? AND preferred",eventId,kind);
                db.update("UPDATE event_payment_account_links SET preferred=TRUE WHERE event_id=? AND destination_kind=? AND destination_id=?",eventId,kind,id);
            }
            case "enable" -> {
                if("REVOKED".equals(rows.getFirst()[1]))throw new ResponseStatusException(HttpStatus.CONFLICT,"Revoked accounts cannot be enabled");
                if(!Boolean.TRUE.equals(rows.getFirst()[0])){
                    if(!eventId.equals(rows.getFirst()[3]))throw new ResponseStatusException(HttpStatus.CONFLICT,"Enable the host account before enabling it for this event");
                    db.update("UPDATE "+table+" SET enabled=TRUE,preferred=FALSE,version=version+1,updated_at=now() WHERE id=? AND host_id=?",id,host);
                }
                db.update("UPDATE event_payment_account_links SET enabled=TRUE WHERE event_id=? AND destination_kind=? AND destination_id=?",eventId,kind,id);
            }
            case "disable" -> db.update("UPDATE event_payment_account_links SET enabled=FALSE,preferred=FALSE WHERE event_id=? AND destination_kind=? AND destination_id=?",eventId,kind,id);
            case "unlink" -> {
                db.update("DELETE FROM event_payment_account_links WHERE event_id=? AND destination_kind=? AND destination_id=?",eventId,kind,id);
                // Event-only destinations cannot be reused elsewhere. Archive them so they can be re-added later.
                if(eventId.equals(rows.getFirst()[3]))db.update("UPDATE "+table+" SET enabled=FALSE,preferred=FALSE,archived_at=now(),version=version+1,updated_at=now() WHERE id=? AND host_id=?",id,host);
            }
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid link action");
        }
        log(host,eventId,id,kind,action.toUpperCase(Locale.ROOT));
    }
    private void log(UUID host,UUID event,UUID account,String kind,String action){audit.recordChange(host,AuditActorType.CUSTOMER,"EVENT_PAYMENT_ACCOUNT_LINK",event,AuditAction.UPDATE,null,Map.of("accountId",account.toString(),"kind",kind,"action",action),null);}
    @Transactional(readOnly=true) public List<HostPaymentProfileService.UpiView> upis(UUID eventId){
        UUID host=owned(eventId,false);
        return db.query("""
            SELECT a.id,a.upi_id_masked,a.verification_status,(a.enabled AND l.enabled),l.preferred,
                a.host_confirmed_at IS NOT NULL,a.account_label
            FROM host_upi_accounts a JOIN event_payment_account_links l
              ON l.destination_id=a.id AND l.destination_kind='UPI' AND l.host_id=a.host_id
            WHERE l.event_id=? AND l.host_id=? AND a.archived_at IS NULL ORDER BY a.created_at,a.id
            """,(rs,n)->new HostPaymentProfileService.UpiView(rs.getObject(1,UUID.class),rs.getString(2),VerificationStatus.valueOf(rs.getString(3)),rs.getBoolean(4),rs.getBoolean(5),rs.getBoolean(6),rs.getString(7),qr.hasValidQr(host,rs.getObject(1,UUID.class)),host,eventId),eventId,host);
    }
    @Transactional(readOnly=true) public List<HostPaymentProfileService.BankView> banks(UUID eventId){
        UUID host=owned(eventId,false);
        return db.query("""
            SELECT a.id,a.account_number_masked,a.ifsc_masked,a.verification_status,(a.enabled AND l.enabled),l.preferred,
                a.host_confirmed_at IS NOT NULL,a.account_label
            FROM host_bank_accounts a JOIN event_payment_account_links l
              ON l.destination_id=a.id AND l.destination_kind='BANK' AND l.host_id=a.host_id
            WHERE l.event_id=? AND l.host_id=? AND a.archived_at IS NULL ORDER BY a.created_at,a.id
            """,(rs,n)->new HostPaymentProfileService.BankView(rs.getObject(1,UUID.class),rs.getString(2),rs.getString(3),VerificationStatus.valueOf(rs.getString(4)),rs.getBoolean(5),rs.getBoolean(6),rs.getBoolean(7),rs.getString(8),host,eventId),eventId,host);
    }
}
