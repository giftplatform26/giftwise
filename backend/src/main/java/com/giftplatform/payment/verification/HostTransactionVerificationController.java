package com.giftplatform.payment.verification;

import com.giftplatform.security.ActorContext;
import com.giftplatform.payment.service.RazorpayCaptureService;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.OffsetDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/v1/events/{eventId}/transactions")
public class HostTransactionVerificationController {
    private final JdbcTemplate db;private final ActorContext actor;private final RazorpayCaptureService capture;
    public HostTransactionVerificationController(JdbcTemplate db,ActorContext actor,RazorpayCaptureService capture){this.db=db;this.actor=actor;this.capture=capture;}
    private void owned(UUID eventId){
        Integer count=db.queryForObject("SELECT count(*) FROM events WHERE id=? AND owner_id=?",Integer.class,eventId,actor.requireAuthenticatedUserId());
        if(count==null||count!=1)throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Event not found");
    }
    public record Transaction(UUID id,String source,String provider,String providerOrderId,String providerPaymentId,
        String method,long amountPaise,String currency,String status,OffsetDateTime verifiedAt,OffsetDateTime createdAt,String explanation,String bankCode,String bankTransactionId,String rrn,String upiTransactionId,
        String guestName,String giftName,String paymentCode,String referenceTail,OffsetDateTime hostDecidedAt,String receiptNumber){}
    public ResponseEntity<List<Transaction>> list(UUID eventId,int limit,int offset){return list(eventId,limit,offset,false);}
    @GetMapping public ResponseEntity<List<Transaction>> list(@PathVariable UUID eventId,
        @RequestParam(defaultValue="20") int limit,@RequestParam(defaultValue="0") int offset,@RequestParam(defaultValue="false") boolean pending){
        owned(eventId);
        if(limit<1||limit>100||offset<0||offset>100000)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid pagination");
        var rows=db.query("""
            SELECT po.id,'CHECKOUT' AS source,po.provider,po.provider_order_id,t.provider_payment_id,
              COALESCE(t.payment_method,'CHECKOUT') AS method,po.amount_paise,po.currency,COALESCE(t.status,po.status) AS status,
              t.verified_at,po.created_at,t.bank_code,t.bank_transaction_id,t.rrn,t.upi_transaction_id,
              ct.guest_name,g.name AS gift_name,NULL::text AS payment_code,NULL::text AS reference_tail,NULL::timestamptz AS host_decided_at,NULL::text AS receipt_number
            FROM payment_orders po LEFT JOIN payment_transactions t ON t.payment_order_id=po.id
              LEFT JOIN contributions ct ON ct.payment_order_id=po.id LEFT JOIN gifts g ON g.id=ct.gift_id
            WHERE po.event_id=? AND NOT ?
            UNION ALL
            SELECT c.id,'DIRECT_TRANSFER','BANK_FEED_NOT_CONNECTED',NULL,NULL,c.method,c.amount_paise,'INR',c.status,NULL,c.created_at,NULL,NULL,NULL,NULL,
              c.guest_name,g.name,c.payment_code,c.reference_tail,c.host_decided_at,c.receipt_number
            FROM guest_direct_transfer_claims c LEFT JOIN gifts g ON g.id=c.gift_id WHERE c.event_id=? AND (NOT ? OR c.status='AWAITING_VERIFICATION')
            ORDER BY created_at DESC,id DESC LIMIT ? OFFSET ?
            """,(rs,n)->new Transaction(rs.getObject(1,UUID.class),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5),
                rs.getString(6),rs.getLong(7),rs.getString(8),rs.getString(9),rs.getObject(10,OffsetDateTime.class),rs.getObject(11,OffsetDateTime.class),
                rs.getString(2).equals("DIRECT_TRANSFER")?("HOST_CONFIRMED".equals(rs.getString(9))?"You confirmed this transfer arrived in your account.":"REJECTED".equals(rs.getString(9))?"You marked this transfer as not received.":"Check your bank or UPI app for this amount and payment code, then confirm."):
                rs.getObject(10)!=null?"Provider payment was checked by the server. It is separate from recipient account verification.":"Awaiting a signed checkout confirmation or provider webhook.",rs.getString(12),rs.getString(13),rs.getString(14),rs.getString(15),
                rs.getString(16),rs.getString(17),rs.getString(18),rs.getString(19),rs.getObject(20,OffsetDateTime.class),rs.getString(21)),eventId,pending,eventId,pending,limit,offset);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(rows);
    }
    public record Decision(String decision){}
    public record DecisionResult(UUID claimId,String status,OffsetDateTime decidedAt){}
    /** The host states whether a guest-reported direct transfer arrived. It is the host's confirmation, not provider verification. */
    @PostMapping("/claims/{claimId}/decision") @org.springframework.transaction.annotation.Transactional
    public DecisionResult decide(@PathVariable UUID eventId,@PathVariable UUID claimId,@RequestBody Decision input){
        owned(eventId);
        String next=input==null?null:switch(String.valueOf(input.decision())){case "RECEIVED"->"HOST_CONFIRMED";case "NOT_RECEIVED"->"REJECTED";case "UNDECIDED"->"AWAITING_VERIFICATION";default->null;};
        if(next==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Choose RECEIVED, NOT_RECEIVED or UNDECIDED");
        UUID host=actor.requireAuthenticatedUserId();
        var rows=db.queryForList("SELECT status FROM guest_direct_transfer_claims WHERE id=? AND event_id=? FOR UPDATE",claimId,eventId);
        if(rows.isEmpty())throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Transfer not found");
        String before=(String)rows.getFirst().get("status");
        if("EXPIRED".equals(before))throw new ResponseStatusException(HttpStatus.CONFLICT,"This transfer report has expired");
        boolean undecided="AWAITING_VERIFICATION".equals(next);
        db.update("UPDATE guest_direct_transfer_claims SET status=?,host_decided_at="+(undecided?"NULL":"now()")+",host_decided_by=? WHERE id=?",next,undecided?null:host,claimId);
        if(audit!=null)audit.recordChange(host,com.giftplatform.audit.entity.AuditActorType.CUSTOMER,"DIRECT_TRANSFER_CLAIM",claimId,
            com.giftplatform.audit.entity.AuditAction.STATUS_CHANGE,java.util.Map.of("status",before),java.util.Map.of("status",next),null);
        var decided=db.queryForObject("SELECT host_decided_at FROM guest_direct_transfer_claims WHERE id=?",OffsetDateTime.class,claimId);
        return new DecisionResult(claimId,next,decided);
    }
    @org.springframework.beans.factory.annotation.Autowired(required=false) private com.giftplatform.audit.service.AuditService audit;
    @PostMapping("/{orderId}/verify") public RazorpayCaptureService.CaptureResult verify(@PathVariable UUID eventId,@PathVariable UUID orderId){
        owned(eventId);
        var rows=db.query("""
            SELECT po.provider_order_id,t.provider_payment_id FROM payment_orders po
            JOIN payment_transactions t ON t.payment_order_id=po.id
            WHERE po.event_id=? AND po.id=? AND po.provider='RAZORPAY' ORDER BY t.created_at DESC LIMIT 1
            """,(rs,n)->new String[]{rs.getString(1),rs.getString(2)},eventId,orderId);
        if(rows.isEmpty())throw new ResponseStatusException(HttpStatus.CONFLICT,"No provider payment is recorded yet. Await the signed checkout confirmation or webhook.");
        return capture.recheckRecordedPayment(orderId,rows.getFirst()[0],rows.getFirst()[1]);
    }
}
