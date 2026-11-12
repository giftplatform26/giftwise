package com.giftplatform.acquisition;

import com.giftplatform.admin.EffectiveModulePermissionService;
import com.giftplatform.event.entity.Event;
import com.giftplatform.event.entity.EventStatus;
import com.giftplatform.event.repository.EventRepository;
import com.giftplatform.security.ActorContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * "Your first celebration is free": guests of an event where the host allows it can, after creating an account,
 * claim premium for a limited time — once per person, within 14 days of signing up. Granted through the existing
 * commercial entitlements as a zero-value FREE order, so every premium check honours it.
 */
@RestController
public class FirstFreeOfferController {
    public static final String MODULE = "GUEST_FIRST_FREE";

    private final JdbcTemplate db;
    private final EventRepository events;
    private final ActorContext actor;
    private final EffectiveModulePermissionService permissions;
    private final int days;

    public FirstFreeOfferController(JdbcTemplate db, EventRepository events, ActorContext actor, EffectiveModulePermissionService permissions,
                                    @Value("${gift.offers.first-free-days:90}") int days) {
        this.db = db; this.events = events; this.actor = actor; this.permissions = permissions; this.days = Math.max(1, Math.min(days, 365));
    }

    public record Offer(boolean available, int days) {}
    public record Claim(UUID eventToken) {}
    public record Result(boolean granted, String message, OffsetDateTime expiresAt) {}

    /** Whether to show the offer on this invitation or receipt. */
    @GetMapping("/api/public/events/{token}/first-free")
    public ResponseEntity<Offer> offer(@PathVariable UUID token) {
        boolean on = events.findByPublicToken(token).filter(e -> e.getStatus() == EventStatus.PUBLISHED)
            .map(e -> permissions.eventEnabled(e.getOwnerId(), e.getId(), MODULE)).orElse(false);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new Offer(on, days));
    }

    @PostMapping("/api/v1/me/offers/first-free")
    @Transactional
    public Result claim(@RequestBody Claim in) {
        UUID me = actor.requireAuthenticatedUserId();
        if (in == null || in.eventToken() == null) throw bad("This offer link is incomplete");
        Event source = events.findByPublicToken(in.eventToken()).filter(e -> e.getStatus() == EventStatus.PUBLISHED)
            .orElseThrow(() -> bad("This offer is no longer available"));
        if (!permissions.eventEnabled(source.getOwnerId(), source.getId(), MODULE)) throw bad("This offer is no longer available");
        if (me.equals(source.getOwnerId())) throw bad("This offer is for your guests");
        db.queryForObject("SELECT id FROM users WHERE id=? FOR UPDATE", UUID.class, me);
        if (Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM guest_offer_claims WHERE user_id=?)", Boolean.class, me)))
            return new Result(false, "You have already used this offer.", null);
        Boolean fresh = db.queryForObject("SELECT created_at > now() - interval '14 days' FROM users WHERE id=?", Boolean.class, me);
        if (!Boolean.TRUE.equals(fresh)) return new Result(false, "This welcome offer is for new accounts.", null);
        Boolean hosted = db.queryForObject("SELECT EXISTS(SELECT 1 FROM events WHERE owner_id=? AND status IN ('PUBLISHED','COMPLETED'))", Boolean.class, me);
        if (Boolean.TRUE.equals(hosted)) return new Result(false, "This offer is for your first celebration.", null);
        Long pricing = db.queryForObject("SELECT max(revision) FROM platform_monetization_history", Long.class);
        Long policy = db.queryForObject("SELECT revision FROM commercial_policy WHERE id=1", Long.class);
        if (pricing == null || policy == null) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
        UUID order = UUID.randomUUID();
        db.update("""
            INSERT INTO commercial_orders(id,buyer_kind,buyer_id,stream,resource_id,amount_paise,pricing_revision,policy_revision,duration_days,idempotency_key,status,paid_at)
            VALUES(?, 'HOST', ?, 'HOST_PLAN', ?, 0, ?, ?, ?, ?, 'FREE', now())""", order, me, me, pricing, policy, days, "first-free:" + me);
        OffsetDateTime expires = OffsetDateTime.now().plusDays(days);
        db.update("INSERT INTO commercial_entitlements(order_id,buyer_kind,buyer_id,stream,resource_id,starts_at,expires_at) VALUES(?, 'HOST', ?, 'HOST_PLAN', ?, now(), ?)",
            order, me, me, expires);
        db.update("INSERT INTO guest_offer_claims(user_id,source_event_id,order_id) VALUES(?,?,?)", me, source.getId(), order);
        AcquisitionEventsController.record(db, "OFFER_CLAIMED", me, source.getId());
        return new Result(true, "Premium is yours free for " + days + " days. Enjoy planning!", expires);
    }

    private static ResponseStatusException bad(String m) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, m); }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> reason(ResponseStatusException ex) {
        int code = ex.getStatusCode().value();
        if (code == 400 && ex.getReason() != null) return ResponseEntity.status(code).body(Map.of("error", ex.getReason()));
        return ResponseEntity.status(ex.getStatusCode()).build();
    }
}
