package com.giftplatform.acquisition;

import com.giftplatform.event.repository.EventRepository;
import com.giftplatform.security.ActorContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

/**
 * Measures whether the acquisition features work, using business events (no cookies or personal tracking):
 * try started → claimed → first celebration published; Status card shares; offers, handovers, collections,
 * showcase approvals, referrals and pass scans. Results appear in Admin → Clicks & outcomes.
 */
@RestController
public class AcquisitionEventsController {
    static final Set<String> PUBLIC_KINDS = Set.of("TRY_STARTED", "STORY_CARD_SHARED");

    private final JdbcTemplate db;
    private final ActorContext actor;
    private final EventRepository events;

    public AcquisitionEventsController(JdbcTemplate db, ActorContext actor, EventRepository events) { this.db = db; this.actor = actor; this.events = events; }

    public record Report(String kind, UUID eventToken) {}
    public record Results(int days, Map<String, Long> events, long registrations, long published, long tryClaimedThenPublished, long offerClaimedThenPublished,
                          long referralsAttributed, long referralsQualified, long whatsappSent) {}

    /** Anonymous events from public pages, limited to 30 per device, kind and day. */
    @PostMapping("/api/public/acquisition-events")
    public ResponseEntity<Void> report(@RequestBody Report in, HttpServletRequest http) {
        String kind = in == null || in.kind() == null ? "" : in.kind();
        if (!PUBLIC_KINDS.contains(kind)) return ResponseEntity.badRequest().build();
        String fp;
        try { fp = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(http.getRemoteAddr().getBytes(StandardCharsets.UTF_8))); }
        catch (Exception e) { throw new IllegalStateException(e); }
        int ok = db.update("""
            INSERT INTO acquisition_event_limits(fingerprint,kind,day,count) VALUES(?,?,?,1)
            ON CONFLICT(fingerprint,kind,day) DO UPDATE SET count=acquisition_event_limits.count+1 WHERE acquisition_event_limits.count<30""",
            fp, kind, LocalDate.now(ZoneOffset.UTC));
        if (ok == 0) return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        UUID event = in.eventToken() == null ? null : events.findByPublicToken(in.eventToken()).map(e -> e.getId()).orElse(null);
        record(db, kind, null, event);
        return ResponseEntity.noContent().build();
    }

    /** Signed-in events (the visitor's try draft arriving in their account). */
    @PostMapping("/api/v1/me/acquisition-events")
    public ResponseEntity<Void> reportMine(@RequestBody Report in) {
        UUID me = actor.requireAuthenticatedUserId();
        if (in == null || !"TRY_CLAIMED".equals(in.kind())) return ResponseEntity.badRequest().build();
        Boolean already = db.queryForObject("SELECT EXISTS(SELECT 1 FROM acquisition_events WHERE kind='TRY_CLAIMED' AND user_id=?)", Boolean.class, me);
        if (!Boolean.TRUE.equals(already)) record(db, "TRY_CLAIMED", me, null);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/admin/console/growth-results")
    public Results results(@RequestParam(defaultValue = "30") int days) {
        if (!actor.isPlatformAdmin()) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        int d = Math.max(1, Math.min(days, 365));
        Map<String, Long> counts = new LinkedHashMap<>();
        for (String k : List.of("TRY_STARTED", "TRY_CLAIMED", "STORY_CARD_SHARED", "OFFER_CLAIMED", "HANDOVER_ACCEPTED", "COLLECTION_CREATED", "SHOWCASE_APPROVED", "PASS_SCANNED")) counts.put(k, 0L);
        db.query("SELECT kind, count(*) FROM acquisition_events WHERE created_at > now() - make_interval(days => ?) GROUP BY kind",
            rs -> { counts.put(rs.getString(1), rs.getLong(2)); }, d);
        long registrations = q("SELECT count(*) FROM users WHERE role='CUSTOMER' AND created_at > now() - make_interval(days => ?)", d);
        long published = q("SELECT count(*) FROM events WHERE status IN ('PUBLISHED','COMPLETED') AND created_at > now() - make_interval(days => ?)", d);
        long tryPublished = q("""
            SELECT count(DISTINCT a.user_id) FROM acquisition_events a JOIN events e ON e.owner_id=a.user_id AND e.status IN ('PUBLISHED','COMPLETED')
            WHERE a.kind='TRY_CLAIMED' AND a.created_at > now() - make_interval(days => ?)""", d);
        long offerPublished = q("""
            SELECT count(DISTINCT c.user_id) FROM guest_offer_claims c JOIN events e ON e.owner_id=c.user_id AND e.status IN ('PUBLISHED','COMPLETED')
            WHERE c.claimed_at > now() - make_interval(days => ?)""", d);
        long attributed = q("SELECT count(*) FROM user_referrals WHERE created_at > now() - make_interval(days => ?)", d);
        long qualified = q("SELECT count(*) FROM user_referrals WHERE status IN ('QUALIFIED','REWARDED') AND created_at > now() - make_interval(days => ?)", d);
        long wa = q("SELECT count(*) FROM whatsapp_messages WHERE status IN ('SENT','DELIVERED','READ') AND created_at > now() - make_interval(days => ?)", d);
        return new Results(d, counts, registrations, published, tryPublished, offerPublished, attributed, qualified, wa);
    }

    static void record(JdbcTemplate db, String kind, UUID user, UUID event) {
        db.update("INSERT INTO acquisition_events(id,kind,user_id,event_id) VALUES(?,?,?,?)", UUID.randomUUID(), kind, user, event);
    }
    private long q(String sql, int days) { Long v = db.queryForObject(sql, Long.class, days); return v == null ? 0 : v; }
}
