package com.giftplatform.monetization;

import com.giftplatform.payment.provider.RazorpayClient;
import com.giftplatform.payment.security.RazorpaySignatureVerifier;
import java.time.OffsetDateTime;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CommercialPurchaseService {
    @org.springframework.beans.factory.annotation.Autowired private com.giftplatform.payment.control.PaymentControlService paymentControls;
    private final JdbcTemplate db;
    private final MonetizationService pricing;
    private final CommercialAccessService access;
    private final RazorpayClient provider;
    private final RazorpaySignatureVerifier signatures;
    private final TransactionTemplate tx;
    private final String publicKey,secret;
    public CommercialPurchaseService(JdbcTemplate db,MonetizationService pricing,CommercialAccessService access,
            RazorpayClient provider,RazorpaySignatureVerifier signatures,TransactionTemplate tx,
            @Value("${gift.payment.razorpay.key-id:}") String publicKey,
            @Value("${gift.payment.razorpay.key-secret:}") String secret) {
        this.db=db;this.pricing=pricing;this.access=access;this.provider=provider;this.signatures=signatures;this.tx=tx;this.publicKey=publicKey;this.secret=secret;
    }
    public record Buyer(String kind,UUID id) {}
    public record Offer(String stream,UUID resourceId,String title,long amountPaise,long revision,int durationDays,long policyRevision,boolean entitled) {}
    public record Checkout(UUID id,String providerOrderId,String keyId,long amountPaise,String currency,String status,String stream,UUID resourceId) {}
    public record Request(String stream,UUID resourceId,String idempotencyKey,long acceptedAmountPaise) {}
    public record Confirmation(UUID orderId,String providerOrderId,String paymentId,String signature) {}
    private static final Set<String> PURCHASES=Set.of("HOST_PLAN","PREMIUM_TEMPLATE","VENDOR_LISTING","SPONSORED_GIFT","GROUP_GIFT");

    @Transactional
    public Offer offer(Buyer buyer,String code,UUID resource) {
        if(buyer==null || buyer.id()==null || code==null || !PURCHASES.contains(code) || resource==null)throw bad("Invalid product");
        var config=pricing.current();
        var stream=config.streams().stream().filter(s->s.code().equals(code)).findFirst().orElseThrow();
        if(!config.enabled()||!stream.enabled())throw new ResponseStatusException(HttpStatus.CONFLICT,"This revenue stream is switched off");
        var policy=db.queryForMap("SELECT * FROM commercial_policy WHERE id=1 FOR SHARE");
        String title;int days=0;long amount=stream.fixedPaise();
        if("GROUP_GIFT".equals(code)) {
            if(!"GROUP_ADMIN".equals(buyer.kind()))throw notFound();
            var groups=db.queryForList("SELECT name,target_paise,reserved_gift_id FROM gift_groups WHERE id=? AND admin_id=? AND collection_published_at IS NULL",resource,buyer.id());
            if(groups.size()!=1)throw notFound();var group=groups.getFirst();
            Number target=(Number)group.get("target_paise");
            if(target==null && group.get("reserved_gift_id")!=null)target=db.queryForObject("SELECT (target_amount*100)::bigint FROM gifts WHERE id=?",Long.class,group.get("reserved_gift_id"));
            if("PERCENTAGE".equals(stream.feeMode()) && target==null)throw bad("Set a group gift goal before calculating a percentage coordination fee");
            amount="FIXED".equals(stream.feeMode())?stream.fixedPaise():java.math.BigDecimal.valueOf(target.longValue()).multiply(java.math.BigDecimal.valueOf(stream.rateBps())).divide(java.math.BigDecimal.valueOf(10000),0,java.math.RoundingMode.HALF_UP).longValueExact();
            if(stream.capPaise()>0)amount=Math.min(amount,stream.capPaise());
            title="Group coordination · "+group.get("name");
        } else {
            if(!"HOST".equals(buyer.kind()))throw notFound();
            switch(code) {
                case "HOST_PLAN" -> {if(!resource.equals(buyer.id()))throw notFound();title="Premium host plan";days=((Number)policy.get("host_plan_days")).intValue();}
                case "PREMIUM_TEMPLATE" -> {
                    var assets=db.queryForList("SELECT title FROM invitation_gallery_assets WHERE id=? AND published AND tier='PREMIUM'",resource);
                    if(assets.size()!=1)throw notFound();title=String.valueOf(assets.getFirst().get("title"));
                }
                default -> {
                    var placements=db.queryForList("SELECT title FROM commercial_placements WHERE id=? AND owner_id=? AND stream=? AND moderation='APPROVED'",resource,buyer.id(),code);
                    if(placements.size()!=1)throw notFound();title=String.valueOf(placements.getFirst().get("title"));
                    days=((Number)policy.get(code.equals("VENDOR_LISTING")?"vendor_days":"sponsor_days")).intValue();
                }
            }
        }
        if(stream.capPaise()>0)amount=Math.min(amount,stream.capPaise());
        var pending=db.queryForList("SELECT amount_paise,pricing_revision,duration_days,policy_revision FROM commercial_orders WHERE buyer_kind=? AND buyer_id=? AND stream=? AND resource_id=? AND status='PENDING' ORDER BY created_at LIMIT 1",buyer.kind(),buyer.id(),code,resource);
        if(!pending.isEmpty()) {
            var original=pending.getFirst();
            return new Offer(code,resource,title,((Number)original.get("amount_paise")).longValue(),((Number)original.get("pricing_revision")).longValue(),((Number)original.get("duration_days")).intValue(),((Number)original.get("policy_revision")).longValue(),access.has(buyer.kind(),buyer.id(),code,resource));
        }
        if(amount<0 || amount>100_000_000L)throw bad("Price exceeds supported checkout amount");
        return new Offer(code,resource,title,amount,config.revision(),days,((Number)policy.get("revision")).longValue(),
                access.has(buyer.kind(),buyer.id(),code,resource)||("PREMIUM_TEMPLATE".equals(code)&&access.premium(buyer.id())));
    }
    @Transactional
    public Checkout create(Buyer buyer,Request request) {
        if(request==null||request.idempotencyKey()==null||request.idempotencyKey().isBlank()||request.idempotencyKey().length()>120)throw bad("Valid checkout key required");
        db.queryForObject("SELECT pg_advisory_xact_lock(hashtextextended(?,0))",Object.class,"commercial-resource:"+buyer.kind()+buyer.id()+request.stream()+request.resourceId());
        db.queryForObject("SELECT pg_advisory_xact_lock(hashtextextended(?,0))",Object.class,"commercial:"+buyer.kind()+buyer.id()+request.idempotencyKey());
        var prior=db.queryForList("SELECT * FROM commercial_orders WHERE buyer_kind=? AND buyer_id=? AND idempotency_key=? FOR UPDATE",buyer.kind(),buyer.id(),request.idempotencyKey());
        if(!prior.isEmpty()) {
            var row=prior.getFirst();
            if(!Objects.equals(row.get("stream"),request.stream())||!Objects.equals(row.get("resource_id"),request.resourceId())||((Number)row.get("amount_paise")).longValue()!=request.acceptedAmountPaise())
                throw new ResponseStatusException(HttpStatus.CONFLICT,"Checkout key already belongs to different purchase details");
            return checkout(row);
        }
        Offer product=offer(buyer,request.stream(),request.resourceId());
        if(product.amountPaise()!=request.acceptedAmountPaise())throw new ResponseStatusException(HttpStatus.CONFLICT,"Price changed. Review the current price before paying.");
        if(product.entitled()&&Set.of("PREMIUM_TEMPLATE","GROUP_GIFT").contains(product.stream()))throw new ResponseStatusException(HttpStatus.CONFLICT,"This purchase is already unlocked");
        var pending=db.queryForList("SELECT * FROM commercial_orders WHERE buyer_kind=? AND buyer_id=? AND stream=? AND resource_id=? AND status='PENDING' ORDER BY created_at LIMIT 1",buyer.kind(),buyer.id(),product.stream(),product.resourceId());
        if(!pending.isEmpty())return checkout(pending.getFirst());
        UUID id=UUID.randomUUID();
        String providerId=null,status="FREE";
        if(product.amountPaise()>0) {
            if(paymentControls!=null) {
                if("HOST".equals(buyer.kind()))paymentControls.requireHost(buyer.id(),"RAZORPAY");
                else paymentControls.require(db.queryForObject("SELECT event_id FROM gift_groups WHERE id=?",UUID.class,product.resourceId()),"RAZORPAY");
            }
            if(!publicKey.startsWith("rzp_test_"))throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Razorpay test checkout is not configured");
            var order=provider.createOrder(product.amountPaise(),id.toString());
            if(order==null||order.id()==null||!order.id().matches("order_[A-Za-z0-9]+")||order.amount()!=product.amountPaise()||!"INR".equals(order.currency())||!"created".equals(order.status()))throw new IllegalStateException("Invalid provider order response");
            providerId=order.id();status="PENDING";
        }
        db.update("""
            INSERT INTO commercial_orders(id,buyer_kind,buyer_id,stream,resource_id,amount_paise,pricing_revision,
            policy_revision,duration_days,idempotency_key,provider_order_id,status,paid_at)
            VALUES(?,?,?,?,?,?,?,?,?,?,?,?,CASE WHEN ?='FREE' THEN now() ELSE NULL END)
            """,id,buyer.kind(),buyer.id(),product.stream(),product.resourceId(),product.amountPaise(),product.revision(),product.policyRevision(),product.durationDays(),request.idempotencyKey(),providerId,status,status);
        var row=db.queryForMap("SELECT * FROM commercial_orders WHERE id=?",id);
        if("FREE".equals(status))grant(row);
        return checkout(row);
    }
    public Checkout confirm(Buyer buyer,Confirmation input) {
        if(input==null||input.orderId()==null)throw bad("Purchase required");
        var rows=db.queryForList("SELECT id FROM commercial_orders WHERE id=? AND buyer_kind=? AND buyer_id=?",input.orderId(),buyer.kind(),buyer.id());
        if(rows.size()!=1)throw notFound();
        if(!signatures.verifyCheckout(input.providerOrderId(),input.paymentId(),input.signature(),secret))throw bad("Invalid payment confirmation");
        return verifyCapture(input.orderId(),input.providerOrderId(),input.paymentId());
    }
    public Checkout verifyCapture(UUID id,String providerOrderId,String paymentId) {
        if(providerOrderId==null||!providerOrderId.matches("order_[A-Za-z0-9]+")||paymentId==null||!paymentId.matches("pay_[A-Za-z0-9]+"))throw bad("Invalid payment identity");
        var payment=provider.fetchPayment(paymentId);
        if(payment==null||!paymentId.equals(payment.id())||!providerOrderId.equals(payment.orderId())||!"captured".equals(payment.status())||!"INR".equals(payment.currency()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Verified captured purchase required");
        return tx.execute(state->{
            var rows=db.queryForList("SELECT * FROM commercial_orders WHERE id=? FOR UPDATE",id);
            if(rows.size()!=1)throw notFound();var row=rows.getFirst();
            if(!providerOrderId.equals(row.get("provider_order_id"))||payment.amount()!=((Number)row.get("amount_paise")).longValue())throw new ResponseStatusException(HttpStatus.CONFLICT,"Payment does not match this purchase");
            if("CAPTURED".equals(row.get("status"))) {
                if(!paymentId.equals(row.get("provider_payment_id")))throw new ResponseStatusException(HttpStatus.CONFLICT,"Purchase was already paid by another payment");
                return checkout(row);
            }
            if(!"PENDING".equals(row.get("status")))throw new ResponseStatusException(HttpStatus.CONFLICT,"Purchase is not awaiting payment");
            db.update("UPDATE commercial_orders SET status='CAPTURED',provider_payment_id=?,paid_at=now() WHERE id=?",paymentId,id);
            db.update("INSERT INTO commercial_revenue(order_id,amount_paise,currency) VALUES(?,?,'INR')",id,payment.amount());
            grant(db.queryForMap("SELECT * FROM commercial_orders WHERE id=?",id));
            return checkout(db.queryForMap("SELECT * FROM commercial_orders WHERE id=?",id));
        });
    }
    private void grant(Map<String,Object> order) {
        String kind=(String)order.get("buyer_kind"),code=(String)order.get("stream");
        UUID buyer=(UUID)order.get("buyer_id"),resource=(UUID)order.get("resource_id");
        db.queryForObject("SELECT pg_advisory_xact_lock(hashtextextended(?,0))",Object.class,"entitlement:"+kind+buyer+code+resource);
        OffsetDateTime start=db.queryForObject("SELECT now()",OffsetDateTime.class);
        int days=((Number)order.get("duration_days")).intValue();
        OffsetDateTime end=null;
        if(days>0) {
            OffsetDateTime previous=db.queryForObject("SELECT MAX(expires_at) FROM commercial_entitlements WHERE buyer_kind=? AND buyer_id=? AND stream=? AND resource_id=?",OffsetDateTime.class,kind,buyer,code,resource);
            if(previous!=null&&previous.isAfter(start))start=previous;
            end=start.plusDays(days);
        }
        db.update("INSERT INTO commercial_entitlements(order_id,buyer_kind,buyer_id,stream,resource_id,starts_at,expires_at) VALUES(?,?,?,?,?,?,?) ON CONFLICT(order_id) DO NOTHING",
                order.get("id"),kind,buyer,code,resource,start,end);
    }
    private Checkout checkout(Map<String,Object> row) {
        return new Checkout((UUID)row.get("id"),(String)row.get("provider_order_id"),publicKey,((Number)row.get("amount_paise")).longValue(),"INR",(String)row.get("status"),(String)row.get("stream"),(UUID)row.get("resource_id"));
    }
    private static ResponseStatusException bad(String message) {return new ResponseStatusException(HttpStatus.BAD_REQUEST,message);}
    private static ResponseStatusException notFound() {return new ResponseStatusException(HttpStatus.NOT_FOUND,"Purchase resource not found");}
}
