package com.giftplatform.monetization;

import com.giftplatform.security.ActorContext;
import java.util.*;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
public class CommercialController {
    private final CommercialPurchaseService purchases;private final CommercialSetupService setup;
    private final CommercialPublicService publicOffers;private final CommercialAccessService access;
    private final ActorContext actors;private final JdbcTemplate db;private final MonetizationService pricing;
    public CommercialController(CommercialPurchaseService purchases,CommercialSetupService setup,CommercialPublicService publicOffers,
            CommercialAccessService access,ActorContext actors,JdbcTemplate db,MonetizationService pricing){
        this.purchases=purchases;this.setup=setup;this.publicOffers=publicOffers;this.access=access;this.actors=actors;this.db=db;this.pricing=pricing;
    }
    private CommercialPurchaseService.Buyer buyer(){return new CommercialPurchaseService.Buyer("HOST",actors.requireAuthenticatedUserId());}
    @GetMapping("/api/v1/commerce") public ResponseEntity<Map<String,Object>> account(){
        UUID host=actors.requireAuthenticatedUserId();
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(Map.of("hostId",host,"premium",access.premium(host),"settings",pricing.current(),"policy",setup.policy(),
                "entitlements",db.queryForList("SELECT stream,resource_id,starts_at,expires_at FROM commercial_entitlements WHERE buyer_kind='HOST' AND buyer_id=? ORDER BY starts_at DESC",host),
                "orders",db.queryForList("SELECT id,stream,resource_id,amount_paise,status,paid_at,created_at FROM commercial_orders WHERE buyer_kind='HOST' AND buyer_id=? ORDER BY created_at DESC LIMIT 100",host)));
    }
    @GetMapping("/api/v1/commerce/offer") public CommercialPurchaseService.Offer offer(@RequestParam String stream,@RequestParam UUID resourceId){return purchases.offer(buyer(),stream,resourceId);}
    @PostMapping("/api/v1/commerce/checkout") public CommercialPurchaseService.Checkout checkout(@RequestBody CommercialPurchaseService.Request input){return purchases.create(buyer(),input);}
    @PostMapping("/api/v1/commerce/confirm") public CommercialPurchaseService.Checkout confirm(@RequestBody CommercialPurchaseService.Confirmation input){return purchases.confirm(buyer(),input);}
    @GetMapping("/api/v1/commerce/templates") public List<Map<String,Object>> templates(){
        UUID host=actors.requireAuthenticatedUserId();
        var rows=db.queryForList("SELECT id,title,category,occasion,tier,published FROM invitation_gallery_assets WHERE published ORDER BY title");
        for(var row:rows)row.put("accessible","FREE".equals(row.get("tier"))||access.template(host,(UUID)row.get("id")));
        return rows;
    }
    @GetMapping("/api/v1/commerce/placements") public List<Map<String,Object>> own(){return setup.ownPlacements();}
    @PostMapping("/api/v1/commerce/placements") public Map<String,Object> create(@RequestBody CommercialSetupService.Placement input){return setup.createPlacement(input);}
    @GetMapping("/api/v1/commerce/analytics/{eventId}") public Map<String,Object> analytics(@PathVariable UUID eventId){
        UUID host=actors.requireAuthenticatedUserId();
        if(!Integer.valueOf(1).equals(db.queryForObject("SELECT count(*) FROM events WHERE id=? AND owner_id=?",Integer.class,eventId,host)))throw new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND);
        if(pricing.enabled("HOST_PLAN")&&!access.premium(host))throw new org.springframework.web.server.ResponseStatusException(HttpStatus.PAYMENT_REQUIRED,"Premium host plan required");
        return Map.of("dailyContributions",db.queryForList("SELECT c.created_at::date AS day,count(*) AS count, SUM(c.amount_paise-po.platform_fee_paise) AS host_net_paise FROM contributions c JOIN payment_orders po ON po.id=c.payment_order_id WHERE c.event_id=? AND po.status='CAPTURED' AND EXISTS(SELECT 1 FROM payment_transactions pt WHERE pt.payment_order_id=po.id AND pt.status='CAPTURED' AND pt.verified_at IS NOT NULL AND pt.amount_paise=po.amount_paise AND pt.currency=po.currency) AND EXISTS(SELECT 1 FROM contribution_allocation_postings ap WHERE ap.contribution_id=c.id) GROUP BY c.created_at::date ORDER BY day",eventId),
                "affiliateClicks",db.queryForObject("SELECT count(*) FROM affiliate_clicks WHERE event_id=?",Long.class,eventId));
    }
    @GetMapping("/api/public/commerce/placements") public ResponseEntity<List<Map<String,Object>>> placements(@RequestParam String stream,@RequestParam(defaultValue="") String occasion){return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(publicOffers.placements(stream,occasion));}
    @GetMapping("/api/public/commerce/placements/{id}/visit") public ResponseEntity<Void> visit(@PathVariable UUID id){return redirect(publicOffers.visit(id));}
    @GetMapping("/api/public/commerce/affiliate/{token}/{giftId}") public ResponseEntity<Void> affiliate(@PathVariable UUID token,@PathVariable UUID giftId){return redirect(publicOffers.affiliate(token,giftId));}
    private ResponseEntity<Void> redirect(String url){return ResponseEntity.status(HttpStatus.FOUND).cacheControl(CacheControl.noStore()).header(HttpHeaders.LOCATION,url).header("Referrer-Policy","no-referrer").build();}
    @GetMapping("/api/admin/console/monetization/policy") public Map<String,Object> policy(){pricing.requireAdmin();return setup.policy();}
    @PutMapping("/api/admin/console/monetization/policy") public Map<String,Object> policy(@RequestBody CommercialSetupService.Policy input){return setup.policy(input);}
    @GetMapping("/api/admin/console/monetization/placements") public List<Map<String,Object>> adminPlacements(){return setup.placements();}
    @PostMapping("/api/admin/console/monetization/placements/{id}") public Map<String,Object> moderate(@PathVariable UUID id,@RequestBody CommercialSetupService.Moderation input){return setup.moderate(id,input);}
    @GetMapping("/api/admin/console/monetization/partners") public List<Map<String,Object>> partners(){return setup.partners();}
    @PutMapping("/api/admin/console/monetization/partners") public Map<String,Object> partner(@RequestBody CommercialSetupService.Partner input){return setup.partner(input);}
    @GetMapping("/api/admin/console/monetization/commissions") public List<Map<String,Object>> commissions(){return setup.commissions();}
    @PutMapping("/api/admin/console/monetization/commissions") public Map<String,Object> commission(@RequestBody CommercialSetupService.Commission input){return setup.commission(input);}
    @GetMapping("/api/admin/console/monetization/setup-history") public List<Map<String,Object>> history(){return setup.history();}
    @GetMapping("/api/admin/console/monetization/revenue") public Map<String,Object> revenue(){return setup.revenue();}
}
