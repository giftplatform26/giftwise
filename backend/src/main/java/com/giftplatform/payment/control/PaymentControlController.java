package com.giftplatform.payment.control;

import java.util.*;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class PaymentControlController {
    @org.springframework.beans.factory.annotation.Autowired private com.giftplatform.security.ActorContext actors;
    @org.springframework.beans.factory.annotation.Autowired private RoutingCommissionService commission;
    private final PaymentControlService controls;private final JdbcTemplate db;
    public PaymentControlController(PaymentControlService controls,JdbcTemplate db){this.controls=controls;this.db=db;}
    @GetMapping("/api/admin/console/payments/controls")
    public ResponseEntity<PaymentControlService.View> read(@RequestParam(defaultValue="PLATFORM") String scope,@RequestParam(required=false) UUID scopeId){return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(controls.read(scope,scopeId));}
    @PutMapping("/api/admin/console/payments/controls/{channel}")
    public PaymentControlService.View change(@PathVariable String channel,@RequestParam(defaultValue="PLATFORM") String scope,@RequestParam(required=false) UUID scopeId,@RequestBody PaymentControlService.Change input){return controls.change(scope,scopeId,channel,input);}
    @GetMapping("/api/admin/console/payments/history")
    public List<Map<String,Object>> history(@RequestParam(defaultValue="50") int limit,@RequestParam(defaultValue="9223372036854775807") long beforeRevision){return controls.history(limit,beforeRevision);}
    @GetMapping("/api/admin/console/payments/routing-commission")
    public ResponseEntity<RoutingCommissionService.Settings> commission(){return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(commission.current());}
    @PutMapping("/api/admin/console/payments/routing-commission")
    public RoutingCommissionService.Settings commission(@RequestBody RoutingCommissionService.Update input){return commission.update(input);}
    @GetMapping("/api/admin/console/payments/routing-commission/history")
    public List<Map<String,Object>> commissionHistory(@RequestParam(defaultValue="50") int limit,@RequestParam(defaultValue="9223372036854775807") long beforeRevision){return commission.history(limit,beforeRevision);}
    @GetMapping("/api/v1/events/{eventId}/payment-breakdown")
    public ResponseEntity<List<Map<String,Object>>> breakdown(@PathVariable UUID eventId,@RequestParam(defaultValue="50") int limit,@RequestParam(defaultValue="0") int offset){
        if(limit<1||limit>100||offset<0||offset>100000)throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        if(!Integer.valueOf(1).equals(db.queryForObject("SELECT count(*) FROM events WHERE id=? AND owner_id=?",Integer.class,eventId,actors.requireAuthenticatedUserId())))throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        var rows=db.queryForList("""
            SELECT po.id,po.amount_paise AS guest_paid_paise,po.platform_fee_paise,
            COALESCE(po.host_receives_paise,po.amount_paise) AS host_allocated_paise,
            pt.provider_fee_paise AS razorpay_fee_including_tax_paise,pt.provider_tax_paise AS razorpay_tax_component_paise,
            po.status,pt.verified_at,po.created_at
            FROM payment_orders po LEFT JOIN payment_transactions pt ON pt.payment_order_id=po.id
              AND pt.status='CAPTURED' AND pt.verified_at IS NOT NULL
            WHERE po.event_id=? ORDER BY po.created_at DESC,po.id DESC LIMIT ? OFFSET ?
            """,eventId,limit,offset);
        for(var row:rows){row.put("host_credited_paise",null);row.put("route_fee_paise",null);row.put("settlement_status","NOT_CONFIRMED");}
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(rows);
    }
    @GetMapping("/api/public/payments/controls/{token}")
    public ResponseEntity<Map<String,Object>> effective(@PathVariable UUID token){
        var events=db.query("SELECT id FROM events WHERE public_token=? AND status='PUBLISHED'",(rs,n)->rs.getObject(1,UUID.class),token);
        if(events.size()!=1)throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(Map.of("channels",controls.event(events.getFirst()),"routingIntegrationReady",false,
                "routingNotice","Route/VPA activation is disabled. Normal Razorpay checkout follows the Razorpay payment control."));
    }
}
