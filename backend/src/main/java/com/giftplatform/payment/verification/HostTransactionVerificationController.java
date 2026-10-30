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
        String method,long amountPaise,String currency,String status,OffsetDateTime verifiedAt,OffsetDateTime createdAt,String explanation,String bankCode,String bankTransactionId,String rrn,String upiTransactionId){}
    @GetMapping public ResponseEntity<List<Transaction>> list(@PathVariable UUID eventId,
        @RequestParam(defaultValue="20") int limit,@RequestParam(defaultValue="0") int offset){
        owned(eventId);
        if(limit<1||limit>100||offset<0||offset>100000)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid pagination");
        var rows=db.query("""
            SELECT po.id,'CHECKOUT' AS source,po.provider,po.provider_order_id,t.provider_payment_id,
              COALESCE(t.payment_method,'CHECKOUT') AS method,po.amount_paise,po.currency,COALESCE(t.status,po.status) AS status,
              t.verified_at,po.created_at,t.bank_code,t.bank_transaction_id,t.rrn,t.upi_transaction_id
            FROM payment_orders po LEFT JOIN payment_transactions t ON t.payment_order_id=po.id WHERE po.event_id=?
            UNION ALL
            SELECT c.id,'DIRECT_TRANSFER','BANK_FEED_NOT_CONNECTED',NULL,NULL,c.method,c.amount_paise,'INR',c.status,NULL,c.created_at,NULL,NULL,NULL,NULL
            FROM guest_direct_transfer_claims c WHERE c.event_id=?
            ORDER BY created_at DESC,id DESC LIMIT ? OFFSET ?
            """,(rs,n)->new Transaction(rs.getObject(1,UUID.class),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5),
                rs.getString(6),rs.getLong(7),rs.getString(8),rs.getString(9),rs.getObject(10,OffsetDateTime.class),rs.getObject(11,OffsetDateTime.class),
                rs.getString(2).equals("DIRECT_TRANSFER")?"Guest report only. Independent verification requires a connected bank or collection-provider feed.":
                rs.getObject(10)!=null?"Provider payment was checked by the server. It is separate from recipient account verification.":"Awaiting a signed checkout confirmation or provider webhook.",rs.getString(12),rs.getString(13),rs.getString(14),rs.getString(15)),eventId,eventId,limit,offset);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(rows);
    }
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
