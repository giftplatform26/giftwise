package com.giftplatform.acquisition;

import com.giftplatform.admin.EffectiveModulePermissionService;
import com.giftplatform.cohost.EventAccess;
import com.giftplatform.event.entity.Event;
import com.giftplatform.security.ActorContext;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Array;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * Showcase of real celebrations: the host submits an event with a public title and up to six approved wishes;
 * a platform admin approves it; only then does it appear on /showcase. Withdrawing or switching the module
 * off removes it immediately. Never shows guest contact details, amounts or private photos.
 */
@RestController
public class ShowcaseController {
    public static final String MODULE = "CELEBRATION_SHOWCASE";

    private final JdbcTemplate db;
    private final EventAccess access;
    private final ActorContext actor;
    private final EffectiveModulePermissionService permissions;

    public ShowcaseController(JdbcTemplate db, EventAccess access, ActorContext actor, EffectiveModulePermissionService permissions) {
        this.db = db; this.access = access; this.actor = actor; this.permissions = permissions;
    }

    public record Wish(UUID id, String guestName, String message, boolean photo) {}
    public record HostView(String status, String displayTitle, String blurb, String city, List<UUID> featuredWishIds, String reviewNote, List<Wish> approvedWishes, boolean published) {}
    public record Input(String displayTitle, String blurb, String city, List<UUID> featuredWishIds) {}
    public record Card(UUID eventId, String displayTitle, String blurb, String city, String occasion, String monthYear, List<Wish> wishes, String photoUrl, String partnerName, String partnerSlug) {}
    public record AdminRow(UUID eventId, String eventTitle, String hostName, String displayTitle, String blurb, String city, String status, OffsetDateTime submittedAt, List<Wish> wishes) {}
    public record Review(String decision, String note) {}

    // ---- Host ----

    @GetMapping("/api/v1/events/{id}/showcase")
    public ResponseEntity<HostView> get(@PathVariable UUID id) {
        Event e = access.requireOwner(id, MODULE);
        List<Wish> approved = db.query("SELECT id,guest_name,message,(photo_path IS NOT NULL) FROM event_guestbook WHERE event_id=? AND status='APPROVED' ORDER BY created_at DESC LIMIT 200",
            (rs, i) -> new Wish(rs.getObject(1, UUID.class), rs.getString(2), rs.getString(3), rs.getBoolean(4)), id);
        var row = db.queryForList("SELECT * FROM event_showcase WHERE event_id=?", id);
        boolean published = e.getStatus() != com.giftplatform.event.entity.EventStatus.DRAFT && e.getStatus() != com.giftplatform.event.entity.EventStatus.CANCELLED;
        if (row.isEmpty()) return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new HostView("NONE", e.getTitle(), null, e.getCity(), List.of(), null, approved, published));
        var r = row.getFirst();
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new HostView((String) r.get("status"), (String) r.get("display_title"), (String) r.get("blurb"),
            (String) r.get("city"), uuids(r.get("featured_wish_ids")), (String) r.get("review_note"), approved, published));
    }

    @PutMapping("/api/v1/events/{id}/showcase")
    @Transactional
    public HostView submit(@PathVariable UUID id, @RequestBody Input in) {
        Event e = access.requireOwner(id, MODULE);
        if (e.getStatus() == com.giftplatform.event.entity.EventStatus.DRAFT || e.getStatus() == com.giftplatform.event.entity.EventStatus.CANCELLED)
            throw bad("Publish your invitation before sharing it in the showcase");
        if (in == null) throw bad("Showcase details are required");
        String title = text(in.displayTitle(), 120, "Title");
        if (title == null) throw bad("Add a title, e.g. Meera & Arjun's wedding");
        List<UUID> wishes = in.featuredWishIds() == null ? List.of() : in.featuredWishIds().stream().distinct().toList();
        if (wishes.size() > 6) throw bad("Choose up to 6 wishes");
        if (!wishes.isEmpty()) {
            Integer ok = db.queryForObject("SELECT count(*) FROM event_guestbook WHERE event_id=? AND status='APPROVED' AND id = ANY(?)", Integer.class, id, wishes.toArray(UUID[]::new));
            if (ok == null || ok != wishes.size()) throw bad("Only approved wishes can be featured");
        }
        // Any change goes back for review, so nothing unreviewed is ever public.
        db.update("""
            INSERT INTO event_showcase(event_id,display_title,blurb,city,featured_wish_ids,status,submitted_at) VALUES(?,?,?,?,?,'PENDING',now())
            ON CONFLICT(event_id) DO UPDATE SET display_title=EXCLUDED.display_title,blurb=EXCLUDED.blurb,city=EXCLUDED.city,
              featured_wish_ids=EXCLUDED.featured_wish_ids,status='PENDING',review_note=NULL,reviewed_by=NULL,reviewed_at=NULL,submitted_at=now()""",
            id, title, text(in.blurb(), 300, "Description"), text(in.city(), 80, "City"), wishes.toArray(UUID[]::new));
        return get(id).getBody();
    }

    @PostMapping("/api/v1/events/{id}/showcase/withdraw")
    public Map<String, Object> withdraw(@PathVariable UUID id) {
        access.requireOwner(id, null);
        if (db.update("UPDATE event_showcase SET status='WITHDRAWN' WHERE event_id=? AND status<>'WITHDRAWN'", id) == 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return Map.of("status", "WITHDRAWN");
    }

    // ---- Platform admin ----

    @GetMapping("/api/admin/console/showcase")
    public List<AdminRow> review(@RequestParam(defaultValue = "PENDING") String status) {
        requireAdmin();
        if (!Set.of("PENDING", "APPROVED", "REJECTED", "WITHDRAWN").contains(status)) throw bad("Unknown status");
        return db.query("""
            SELECT s.*, e.title, COALESCE(NULLIF(u.full_name,''), u.username, u.email) AS host FROM event_showcase s JOIN events e ON e.id=s.event_id JOIN users u ON u.id=e.owner_id
            WHERE s.status=? ORDER BY s.submitted_at DESC LIMIT 200""", (rs, i) -> new AdminRow(rs.getObject("event_id", UUID.class), rs.getString("title"), rs.getString("host"),
            rs.getString("display_title"), rs.getString("blurb"), rs.getString("city"), rs.getString("status"), rs.getObject("submitted_at", OffsetDateTime.class),
            wishes(rs.getObject("event_id", UUID.class), uuids(rs.getArray("featured_wish_ids")))), status);
    }

    @PostMapping("/api/admin/console/showcase/{eventId}/review")
    @Transactional
    public Map<String, Object> decide(@PathVariable UUID eventId, @RequestBody Review in) {
        UUID admin = requireAdmin();
        String decision = in == null || in.decision() == null ? "" : in.decision().toUpperCase(Locale.ROOT);
        if (!Set.of("APPROVED", "REJECTED").contains(decision)) throw bad("Approve or reject");
        if (db.update("UPDATE event_showcase SET status=?,review_note=?,reviewed_by=?,reviewed_at=now() WHERE event_id=? AND status='PENDING'",
            decision, text(in.note(), 300, "Note"), admin, eventId) == 0) throw new ResponseStatusException(HttpStatus.CONFLICT, "Only pending submissions can be reviewed");
        db.update("INSERT INTO platform_admin_action_audit(id,actor_id,action,target_id,module_code) VALUES(?,?,?,?,?)", UUID.randomUUID(), admin, "SHOWCASE_" + decision, eventId, MODULE);
        if (decision.equals("APPROVED")) AcquisitionEventsController.record(db, "SHOWCASE_APPROVED", null, eventId);
        return Map.of("status", decision);
    }

    // ---- Public ----

    @GetMapping("/api/public/showcase")
    public ResponseEntity<List<Card>> showcase(@RequestParam(defaultValue = "24") int limit) {
        if (!AcquisitionPublicController.platformOn(db, MODULE)) return ResponseEntity.ok(List.of());
        int n = Math.max(1, Math.min(limit, 48));
        List<Card> out = new ArrayList<>();
        db.query("""
            SELECT s.*, e.id AS eid, e.owner_id, e.occasion_type, e.event_date, e.public_token, e.status AS estatus, p.business_name, p.slug
            FROM event_showcase s JOIN events e ON e.id=s.event_id
            LEFT JOIN event_partner_links l ON l.event_id=e.id LEFT JOIN partner_profiles p ON p.user_id=l.partner_id
            WHERE s.status='APPROVED' AND e.status IN ('PUBLISHED','COMPLETED') ORDER BY s.reviewed_at DESC LIMIT 200""", rs -> {
            if (out.size() >= n) return;
            UUID eventId = rs.getObject("eid", UUID.class), owner = rs.getObject("owner_id", UUID.class);
            if (!permissions.eventEnabled(owner, eventId, MODULE)) return;
            List<Wish> wishes = wishes(eventId, uuids(rs.getArray("featured_wish_ids")));
            boolean live = "PUBLISHED".equals(rs.getString("estatus")) && permissions.enabled(owner, "GUESTBOOK");
            String photo = live ? wishes.stream().filter(Wish::photo).findFirst()
                .map(w -> { try { return "/backend/api/public/celebrations/" + rs.getObject("public_token", UUID.class) + "/wishes/" + w.id() + "/photo"; } catch (java.sql.SQLException ex) { return null; } }).orElse(null) : null;
            LocalDate date = rs.getObject("event_date", LocalDate.class);
            out.add(new Card(eventId, rs.getString("display_title"), rs.getString("blurb"), rs.getString("city"), rs.getString("occasion_type"),
                date == null ? null : date.getMonth().getDisplayName(java.time.format.TextStyle.FULL, Locale.ENGLISH) + " " + date.getYear(),
                wishes.stream().map(w -> new Wish(w.id(), firstName(w.guestName()), w.message(), w.photo())).toList(), photo, rs.getString("business_name"), rs.getString("slug")));
        });
        return ResponseEntity.ok().cacheControl(CacheControl.maxAge(Duration.ofMinutes(2))).body(out);
    }

    // ---- Helpers ----

    private List<Wish> wishes(UUID eventId, List<UUID> ids) {
        if (ids.isEmpty()) return List.of();
        return db.query("SELECT id,guest_name,message,(photo_path IS NOT NULL) FROM event_guestbook WHERE event_id=? AND status='APPROVED' AND id = ANY(?)",
            (rs, i) -> new Wish(rs.getObject(1, UUID.class), rs.getString(2), rs.getString(3), rs.getBoolean(4)), eventId, ids.toArray(UUID[]::new));
    }
    /** Showcase shows only first names. */
    static String firstName(String name) { String n = name == null ? "" : name.strip(); int s = n.indexOf(' '); return s > 0 ? n.substring(0, s) : n; }
    private static List<UUID> uuids(Object array) {
        try {
            if (array == null) return List.of();
            Object[] values = array instanceof Array a ? (Object[]) a.getArray() : (Object[]) array;
            return Arrays.stream(values).map(v -> v instanceof UUID u ? u : UUID.fromString(v.toString())).toList();
        } catch (java.sql.SQLException e) { return List.of(); }
    }
    private UUID requireAdmin() { if (!actor.isPlatformAdmin()) throw new ResponseStatusException(HttpStatus.FORBIDDEN); return actor.requireAuthenticatedUserId(); }
    private static String text(String v, int max, String label) {
        if (v == null || v.isBlank()) return null;
        String s = v.strip();
        if (s.length() > max) throw bad(label + " must be at most " + max + " characters");
        if (s.chars().anyMatch(Character::isISOControl)) throw bad(label + " contains unsupported characters");
        return s;
    }
    private static ResponseStatusException bad(String m) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, m); }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> reason(ResponseStatusException ex) {
        int code = ex.getStatusCode().value();
        if ((code == 400 || code == 409) && ex.getReason() != null) return ResponseEntity.status(code).body(Map.of("error", ex.getReason()));
        return ResponseEntity.status(ex.getStatusCode()).build();
    }
}
