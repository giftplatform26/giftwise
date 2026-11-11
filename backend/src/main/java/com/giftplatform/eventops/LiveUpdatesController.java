package com.giftplatform.eventops;

import com.giftplatform.admin.EffectiveModulePermissionService;
import com.giftplatform.cohost.EventAccess;
import com.giftplatform.cohost.EventAccess.Capability;
import com.giftplatform.event.entity.Event;
import com.giftplatform.event.entity.EventStatus;
import com.giftplatform.event.repository.EventRepository;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.*;

/** Short announcements for guests ("Buses leave at 10 pm") and a livestream link, shown on the invitation and the live wall. */
@RestController
public class LiveUpdatesController extends EventOpsSupport {
    public static final String MODULE = "LIVE_UPDATES";

    private final JdbcTemplate db;
    private final EventAccess access;
    private final EventRepository events;
    private final EffectiveModulePermissionService permissions;

    public LiveUpdatesController(JdbcTemplate db, EventAccess access, EventRepository events, EffectiveModulePermissionService permissions) {
        this.db = db; this.access = access; this.events = events; this.permissions = permissions;
    }

    public record Update(UUID id, String message, OffsetDateTime postedAt, String postedBy) {}
    public record Stream(String url, String label) {}
    public record HostView(List<Update> updates, Stream stream) {}
    public record PublicView(List<Update> updates, Stream stream) {}
    public record Post(String message) {}

    @GetMapping("/api/v1/events/{id}/live-updates")
    public ResponseEntity<HostView> get(@PathVariable UUID id) {
        access.require(id, Capability.UPDATES, MODULE);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new HostView(updates(id, 50, null), stream(id)));
    }

    @PostMapping("/api/v1/events/{id}/live-updates")
    public Update post(@PathVariable UUID id, @RequestBody Post in) {
        var a = access.require(id, Capability.UPDATES, MODULE);
        String message = required(in == null ? null : in.message(), 200, "Message");
        Integer today = db.queryForObject("SELECT count(*) FROM event_live_updates WHERE event_id=? AND posted_at>now()-interval '24 hours'", Integer.class, id);
        if (today != null && today >= 40) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.TOO_MANY_REQUESTS, "That's a lot of updates for one day. Try again later.");
        UUID uid = UUID.randomUUID();
        db.update("INSERT INTO event_live_updates(id,event_id,message,posted_by) VALUES(?,?,?,?)", uid, id, message, a.userId());
        return updates(id, 1, uid).getFirst();
    }

    @PostMapping("/api/v1/events/{id}/live-updates/{updateId}/remove")
    public Map<String, Object> remove(@PathVariable UUID id, @PathVariable UUID updateId) {
        access.require(id, Capability.UPDATES, MODULE);
        if (db.update("UPDATE event_live_updates SET removed_at=now() WHERE id=? AND event_id=? AND removed_at IS NULL", updateId, id) == 0) throw notFound();
        return Map.of("removed", true);
    }

    @PutMapping("/api/v1/events/{id}/live-updates/stream")
    public Map<String, Object> setStream(@PathVariable UUID id, @RequestBody Stream in) {
        access.require(id, Capability.UPDATES, MODULE);
        String url = https(in == null ? null : in.url(), "Stream link");
        if (url == null) { db.update("DELETE FROM event_stream_links WHERE event_id=?", id); return Map.of("saved", true, "url", ""); }
        String label = text(in.label(), 80, "Label");
        db.update("INSERT INTO event_stream_links(event_id,url,label) VALUES(?,?,?) ON CONFLICT(event_id) DO UPDATE SET url=EXCLUDED.url,label=EXCLUDED.label", id, url, label);
        return Map.of("saved", true, "url", url);
    }

    /** Guests see the latest few updates from the last 24 hours, plus the stream link. */
    @GetMapping("/api/public/events/{token}/live-updates")
    public ResponseEntity<PublicView> publicView(@PathVariable UUID token) {
        Event e = events.findByPublicToken(token).filter(x -> x.getStatus() == EventStatus.PUBLISHED).orElseThrow(EventOpsSupport::notFound);
        if (!permissions.eventEnabled(e.getOwnerId(), e.getId(), MODULE)) return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new PublicView(List.of(), null));
        List<Update> recent = updates(e.getId(), 5, null).stream().filter(u -> u.postedAt().isAfter(OffsetDateTime.now().minusHours(24)))
            .map(u -> new Update(u.id(), u.message(), u.postedAt(), null)).toList();
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new PublicView(recent, stream(e.getId())));
    }

    /** Latest announcement within the last 6 hours, for the live wall ticker; null when none or the feature is off. */
    public String latestForWall(Event e) {
        if (!permissions.eventEnabled(e.getOwnerId(), e.getId(), MODULE)) return null;
        return db.query("SELECT message FROM event_live_updates WHERE event_id=? AND removed_at IS NULL AND posted_at>now()-interval '6 hours' ORDER BY posted_at DESC LIMIT 1",
            (rs, i) -> rs.getString(1), e.getId()).stream().findFirst().orElse(null);
    }

    private List<Update> updates(UUID eventId, int limit, UUID only) {
        String filter = only == null ? "" : " AND l.id=?";
        Object[] args = only == null ? new Object[]{eventId, limit} : new Object[]{eventId, only, limit};
        return db.query("""
            SELECT l.id, l.message, l.posted_at, COALESCE(NULLIF(u.full_name,''), u.username) FROM event_live_updates l LEFT JOIN users u ON u.id=l.posted_by
            WHERE l.event_id=? AND l.removed_at IS NULL""" + filter + " ORDER BY l.posted_at DESC LIMIT ?",
            (rs, i) -> new Update(rs.getObject(1, UUID.class), rs.getString(2), rs.getObject(3, OffsetDateTime.class), rs.getString(4)), args);
    }
    private Stream stream(UUID eventId) {
        return db.query("SELECT url,label FROM event_stream_links WHERE event_id=?", (rs, i) -> new Stream(rs.getString(1), rs.getString(2)), eventId).stream().findFirst().orElse(null);
    }
}
