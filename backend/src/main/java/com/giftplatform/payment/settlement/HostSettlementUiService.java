package com.giftplatform.payment.settlement;

import com.giftplatform.audit.entity.AuditAction;
import com.giftplatform.audit.entity.AuditActorType;
import com.giftplatform.audit.service.AuditService;
import com.giftplatform.security.ActorContext;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** UI facade: no reservation, provider dispatch, or live transfer capability. */
@Service
public class HostSettlementUiService {
    private final ActorContext actors;
    private final JdbcTemplate jdbc;
    private final SettlementBalanceService balances;
    private final HostPayoutRequestService requests;
    private final PayoutSafetyFinalizationService safety;
    private final AuditService audit;

    public HostSettlementUiService(ActorContext actors, JdbcTemplate jdbc,
            SettlementBalanceService balances, HostPayoutRequestService requests,
            PayoutSafetyFinalizationService safety, AuditService audit) {
        this.actors=actors; this.jdbc=jdbc; this.balances=balances;
        this.requests=requests; this.safety=safety; this.audit=audit;
    }
    public record Wallet(UUID eventId, long availablePaise, long heldPaise,
                         long withdrawablePaise, boolean livePayoutsEnabled) {}
    public record Bank(UUID id, String maskedAccount, String maskedIfsc, boolean preferred) {}
    public record Payout(UUID id, UUID eventId, UUID destinationId, long amountPaise,
                         String state, String reason, boolean cancelled) {}

    private UUID owner(UUID eventId) {
        UUID host=actors.requireAuthenticatedUserId();
        Integer count=jdbc.queryForObject(
            "SELECT COUNT(*) FROM events WHERE id=? AND owner_id=?",Integer.class,eventId,host);
        if (count==null || count!=1) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Event not found");
        return host;
    }
    @Transactional(readOnly=true)
    public Wallet wallet(UUID eventId) {
        owner(eventId);
        var b=balances.read(eventId);
        return new Wallet(eventId,b.availablePaise(),b.heldPaise(),b.withdrawablePaise(),false);
    }
    @Transactional(readOnly=true)
    public List<Bank> banks(UUID eventId) {
        UUID host=owner(eventId);
        return jdbc.query("""
            SELECT id,account_number_masked,ifsc_masked,EXISTS (SELECT 1 FROM event_payment_account_links l WHERE l.bank_account_id=host_bank_accounts.id AND l.event_id=? AND l.preferred) AS preferred FROM host_bank_accounts
            WHERE host_id=? AND EXISTS (SELECT 1 FROM event_payment_account_links l WHERE l.bank_account_id=host_bank_accounts.id AND l.host_id=host_bank_accounts.host_id AND l.event_id=? AND l.enabled) AND archived_at IS NULL AND enabled=TRUE AND verification_status='VERIFIED'
            ORDER BY preferred DESC,created_at ASC,id ASC
            """,(rs,i)->new Bank(rs.getObject(1,UUID.class),rs.getString(2),
                rs.getString(3),rs.getBoolean(4)),eventId,host,eventId);
    }
    @Transactional(readOnly=true)
    public List<Payout> payouts(UUID eventId) {
        UUID host=owner(eventId);
        return jdbc.query("""
            SELECT id,event_id,destination_id,amount_paise,state,reason,cancelled_at
            FROM host_payout_requests WHERE event_id=? AND host_id=?
            ORDER BY created_at DESC,id DESC
            """,(rs,i)->new Payout(rs.getObject(1,UUID.class),rs.getObject(2,UUID.class),
                rs.getObject(3,UUID.class),rs.getLong(4),rs.getString(5),rs.getString(6),
                rs.getTimestamp(7)!=null),eventId,host);
    }
    @Transactional
    public HostPayoutRequestService.Result request(UUID eventId,UUID bankId,long amount,String key) {
        UUID host=owner(eventId);
        var result=requests.request(host,eventId,bankId,amount,key);
        // Only the first request is audited; duplicate idempotent requests remain read-only.
        // Auditing the replay is harmless but creates misleading duplicate change records.
        Integer previous=jdbc.queryForObject("""
            SELECT COUNT(*) FROM audit_logs WHERE entity_type='HOST_PAYOUT_REQUEST'
            AND entity_id=? AND action='CREATE'
            """,Integer.class,result.requestId());
        if (previous==null || previous==0) {
            audit.recordChange(host,AuditActorType.CUSTOMER,"HOST_PAYOUT_REQUEST",result.requestId(),
                AuditAction.CREATE,null,Map.of("state","BLOCKED","eventId",eventId.toString()),null);
        }
        return result;
    }
    @Transactional
    public PayoutSafetyFinalizationService.Cancellation cancel(UUID eventId,UUID requestId) {
        UUID host=owner(eventId);
        Integer linked=jdbc.queryForObject("""
            SELECT COUNT(*) FROM host_payout_requests
            WHERE id=? AND host_id=? AND event_id=?
            """,Integer.class,requestId,host,eventId);
        if (linked==null || linked!=1)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Payout request not found");
        var result=safety.cancel(host,requestId);
        if (!result.alreadyCancelled())
            audit.recordChange(host,AuditActorType.CUSTOMER,"HOST_PAYOUT_REQUEST",requestId,
                AuditAction.STATUS_CHANGE,null,Map.of("state","CANCELLED"),null);
        return result;
    }
}
