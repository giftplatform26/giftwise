package com.giftplatform.livewall;

import com.giftplatform.admin.EffectiveModulePermissionService;
import com.giftplatform.event.entity.Event;
import com.giftplatform.event.entity.EventStatus;
import com.giftplatform.event.repository.EventRepository;
import com.giftplatform.security.ActorContext;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.*;

/**
 * Live venue wall: a full-screen slideshow of approved guestbook wishes and photos for a TV or projector.
 * The wall has its own rotatable token; it never shows pending or hidden wishes and carries no host access.
 */
@RestController
public class LiveWallController {
    public static final String MODULE = "LIVE_WALL";
    static final Set<String> THEMES = Set.of("NIGHT", "IVORY", "FESTIVE");

    private final JdbcTemplate db;
    private final EventRepository events;
    private final ActorContext actor;
    private final EffectiveModulePermissionService permissions;
    private final com.giftplatform.qr.QrRenderer qr;
    private final com.giftplatform.cohost.EventAccess access;
    private final String guestBaseUrl;

    public LiveWallController(JdbcTemplate db, EventRepository events, ActorContext actor, EffectiveModulePermissionService permissions,
                              com.giftplatform.qr.QrRenderer qr, com.giftplatform.cohost.EventAccess access, @org.springframework.beans.factory.annotation.Value("${gift.guest-base-url:http://localhost:3000}") String guestBaseUrl) {
        this.db = db; this.events = events; this.actor = actor; this.permissions = permissions; this.qr = qr; this.access = access;
        this.guestBaseUrl = guestBaseUrl.replaceAll("/+$", "");
    }

    public record Settings(boolean active, String headline, String theme, boolean showMessages, boolean showPhotos, boolean showJoinQr,
                           int slideSeconds, UUID wallToken, boolean guestbookEnabled, boolean published, long version) {}
    public record Input(boolean active, String headline, String theme, boolean showMessages, boolean showPhotos, boolean showJoinQr,
                        int slideSeconds, long version) {}
    public record Item(UUID id, String guestName, String message, String photoUrl, OffsetDateTime createdAt) {}
    public record Wall(String eventTitle, String hostName, String headline, String theme, boolean showJoinQr, int slideSeconds,
                       String invitationPath, List<Item> items) {}

    // ---- Host ----

    @GetMapping("/api/v1/events/{id}/live-wall")
    public ResponseEntity<Settings> get(@PathVariable UUID id) {
        Event e = owned(id);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(read(e));
    }

    @PutMapping("/api/v1/events/{id}/live-wall")
    @Transactional
    public Settings save(@PathVariable UUID id, @RequestBody Input in) {
        Event e = owned(id);
        if (in == null) throw bad("Wall settings are required");
        String theme = in.theme() == null ? "" : in.theme().strip().toUpperCase(Locale.ROOT);
        if (!THEMES.contains(theme)) throw bad("Choose a wall theme");
        if (in.slideSeconds() < 4 || in.slideSeconds() > 60) throw bad("Each slide can stay on screen for 4 to 60 seconds");
        if (!in.showMessages() && !in.showPhotos()) throw bad("Show messages, photos or both");
        String headline = in.headline() == null || in.headline().isBlank() ? null : in.headline().strip();
        if (headline != null && (headline.length() > 120 || headline.chars().anyMatch(Character::isISOControl)))
            throw bad("Headline must be at most 120 characters");
        Long current = db.query("SELECT version FROM event_live_walls WHERE event_id=? FOR UPDATE", (rs, i) -> rs.getLong(1), id)
            .stream().findFirst().orElse(null);
        UUID who = actor.requireAuthenticatedUserId();
        if (current == null) {
            if (in.version() != 0) throw conflict();
            db.update("""
                INSERT INTO event_live_walls(event_id,active,headline,theme,show_messages,show_photos,show_join_qr,slide_seconds,updated_by,version)
                VALUES(?,?,?,?,?,?,?,?,?,1)""", id, in.active(), headline, theme, in.showMessages(), in.showPhotos(), in.showJoinQr(), in.slideSeconds(), who);
        } else {
            if (current != in.version()) throw conflict();
            db.update("""
                UPDATE event_live_walls SET active=?,headline=?,theme=?,show_messages=?,show_photos=?,show_join_qr=?,slide_seconds=?,
                  updated_by=?,version=version+1,updated_at=now() WHERE event_id=?""",
                in.active(), headline, theme, in.showMessages(), in.showPhotos(), in.showJoinQr(), in.slideSeconds(), who, id);
        }
        return read(e);
    }

    /** Issues a new wall link; the old link stops working immediately. */
    @PostMapping("/api/v1/events/{id}/live-wall/rotate")
    @Transactional
    public Settings rotate(@PathVariable UUID id) {
        Event e = owned(id);
        int n = db.update("UPDATE event_live_walls SET wall_token=gen_random_uuid(),version=version+1,updated_at=now() WHERE event_id=?", id);
        if (n == 0) throw bad("Save the wall settings first");
        return read(e);
    }

    // ---- Public wall ----

    /** QR code guests scan from the screen to open the invitation and add their own wish. */
    @GetMapping(value = "/api/public/live/{wallToken}/join-qr.png", produces = org.springframework.http.MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> joinQr(@PathVariable UUID wallToken) {
        var w = liveWall(wallToken);
        if (!(Boolean) w.get("show_join_qr")) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        Event e = events.findById((UUID) w.get("event_id")).orElseThrow();
        return ResponseEntity.ok().cacheControl(CacheControl.maxAge(java.time.Duration.ofMinutes(10)))
            .body(qr.png(guestBaseUrl + "/e/" + e.getPublicToken() + "#wishes", 480));
    }

    @GetMapping("/api/public/live/{wallToken}")
    public ResponseEntity<Wall> wall(@PathVariable UUID wallToken) {
        var w = liveWall(wallToken);
        Event e = events.findById((UUID) w.get("event_id")).orElseThrow();
        boolean messages = (Boolean) w.get("show_messages"), photos = (Boolean) w.get("show_photos");
        String photoBase = "/backend/api/public/celebrations/" + e.getPublicToken() + "/wishes/";
        String filter = messages ? "" : " AND photo_path IS NOT NULL";
        List<Item> items = db.query("""
            SELECT id,guest_name,message,(photo_path IS NOT NULL) AS has_photo,created_at FROM event_guestbook
            WHERE event_id=? AND status='APPROVED'""" + filter + " ORDER BY created_at DESC LIMIT 60",
            (rs, i) -> {
                UUID wishId = rs.getObject("id", UUID.class);
                boolean hasPhoto = photos && rs.getBoolean("has_photo");
                return new Item(wishId, rs.getString("guest_name"), messages ? rs.getString("message") : null,
                    hasPhoto ? photoBase + wishId + "/photo" : null, rs.getObject("created_at", OffsetDateTime.class));
            }, e.getId());
        String host = e.getHostDisplayName() == null || e.getHostDisplayName().isBlank() ? null : e.getHostDisplayName();
        Wall body = new Wall(e.getTitle(), host, (String) w.get("headline"), (String) w.get("theme"), (Boolean) w.get("show_join_qr"),
            ((Number) w.get("slide_seconds")).intValue(), "/e/" + e.getPublicToken(), items);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).header("X-Robots-Tag", "noindex, nofollow").body(body);
    }

    // ---- Helpers ----

    /** Active wall on a published event whose host still has the live wall and guestbook; otherwise 404. */
    private Map<String, Object> liveWall(UUID wallToken) {
        var row = db.queryForList("SELECT * FROM event_live_walls WHERE wall_token=? AND active", wallToken);
        if (row.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        var w = row.getFirst();
        Event e = events.findById((UUID) w.get("event_id")).filter(x -> x.getStatus() == EventStatus.PUBLISHED)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!permissions.eventEnabled(e.getOwnerId(), e.getId(), MODULE) || !permissions.enabled(e.getOwnerId(), "GUESTBOOK"))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return w;
    }

    private Settings read(Event e) {
        boolean guestbook = permissions.enabled(e.getOwnerId(), "GUESTBOOK");
        boolean published = e.getStatus() == EventStatus.PUBLISHED;
        var rows = db.queryForList("SELECT * FROM event_live_walls WHERE event_id=?", e.getId());
        if (rows.isEmpty()) return new Settings(false, null, "NIGHT", true, true, true, 8, null, guestbook, published, 0);
        var r = rows.getFirst();
        return new Settings((Boolean) r.get("active"), (String) r.get("headline"), (String) r.get("theme"), (Boolean) r.get("show_messages"),
            (Boolean) r.get("show_photos"), (Boolean) r.get("show_join_qr"), ((Number) r.get("slide_seconds")).intValue(),
            (UUID) r.get("wall_token"), guestbook, published, ((Number) r.get("version")).longValue());
    }

    private Event owned(UUID id) {
        return access.require(id, com.giftplatform.cohost.EventAccess.Capability.LIVE_WALL, MODULE).event();
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> reason(ResponseStatusException ex) {
        int code = ex.getStatusCode().value();
        if ((code == 400 || code == 409) && ex.getReason() != null) return ResponseEntity.status(code).body(Map.of("error", ex.getReason()));
        return ResponseEntity.status(ex.getStatusCode()).build();
    }

    private static ResponseStatusException bad(String m) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, m); }
    private static ResponseStatusException conflict() {
        return new ResponseStatusException(HttpStatus.CONFLICT, "The wall settings changed in another tab. Reload and try again.");
    }
}
