package com.giftplatform.growth;

import com.giftplatform.admin.EffectiveModulePermissionService;
import com.giftplatform.cohost.EventAccess;
import com.giftplatform.event.entity.Event;
import com.giftplatform.event.entity.EventStatus;
import com.giftplatform.event.repository.EventRepository;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

/**
 * Wishlist item types: a regular ITEM, an EXPERIENCE (honeymoon dinner, spa day…) that guests contribute toward like
 * any gift, or a CHARITY wish that links to the charity's own donation page. Payments toward charity wishes are
 * rejected by a database trigger, so GiftWise never holds charity money.
 */
@RestController
public class GiftKindsController {
    public static final String MODULE = "EXPERIENCE_GIFTS";
    static final Set<String> KINDS = Set.of("ITEM", "EXPERIENCE", "CHARITY");

    private final JdbcTemplate db;
    private final EventAccess access;
    private final EventRepository events;
    private final EffectiveModulePermissionService permissions;

    public GiftKindsController(JdbcTemplate db, EventAccess access, EventRepository events, EffectiveModulePermissionService permissions) {
        this.db = db; this.access = access; this.events = events; this.permissions = permissions;
    }

    public record Kind(String kind, String charityName, String charityUrl) {}
    public record Input(String kind, String charityName, String charityUrl) {}

    @GetMapping("/api/v1/events/{id}/gift-kinds")
    public ResponseEntity<Map<UUID, Kind>> list(@PathVariable UUID id) {
        access.requireOwner(id, MODULE);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(kinds(id));
    }

    @PutMapping("/api/v1/events/{id}/gifts/{giftId}/kind")
    public Kind set(@PathVariable UUID id, @PathVariable UUID giftId, @RequestBody Input in) {
        access.requireOwner(id, MODULE);
        if (!Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM gifts WHERE id=? AND event_id=?)", Boolean.class, giftId, id)))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        String kind = in == null || in.kind() == null ? "" : in.kind().strip().toUpperCase(Locale.ROOT);
        if (!KINDS.contains(kind)) throw bad("Choose gift, experience or charity");
        String name = null, url = null;
        if (kind.equals("CHARITY")) {
            name = in.charityName() == null ? "" : in.charityName().strip();
            url = in.charityUrl() == null ? "" : in.charityUrl().strip();
            if (name.isEmpty() || name.length() > 120) throw bad("Add the charity's name");
            if (url.length() > 500 || !url.matches("https://[^\\s<>\"]+")) throw bad("Add the charity's donation page link (starting with https://)");
            Boolean paid = db.queryForObject("""
                SELECT EXISTS(SELECT 1 FROM payment_orders WHERE gift_id=? AND status NOT IN ('FAILED','CANCELLED'))
                    OR EXISTS(SELECT 1 FROM guest_direct_transfer_claims WHERE gift_id=? AND status IN ('AWAITING_VERIFICATION','HOST_CONFIRMED'))""", Boolean.class, giftId, giftId);
            if (Boolean.TRUE.equals(paid)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Guests have already contributed to this gift, so it cannot become a charity wish");
        }
        db.update("""
            INSERT INTO gift_kinds(gift_id,kind,charity_name,charity_url) VALUES(?,?,?,?)
            ON CONFLICT(gift_id) DO UPDATE SET kind=EXCLUDED.kind,charity_name=EXCLUDED.charity_name,charity_url=EXCLUDED.charity_url,updated_at=now()""", giftId, kind, name, url);
        return new Kind(kind, name, url);
    }

    @GetMapping("/api/public/events/{token}/gift-kinds")
    public ResponseEntity<Map<UUID, Kind>> publicKinds(@PathVariable UUID token) {
        Event e = events.findByPublicToken(token).filter(x -> x.getStatus() == EventStatus.PUBLISHED)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!permissions.eventEnabled(e.getOwnerId(), e.getId(), MODULE)) return ResponseEntity.ok(Map.of());
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(kinds(e.getId()));
    }

    private Map<UUID, Kind> kinds(UUID eventId) {
        Map<UUID, Kind> out = new LinkedHashMap<>();
        db.query("SELECT k.* FROM gift_kinds k JOIN gifts g ON g.id=k.gift_id WHERE g.event_id=? AND k.kind<>'ITEM'",
            rs -> { out.put(rs.getObject("gift_id", UUID.class), new Kind(rs.getString("kind"), rs.getString("charity_name"), rs.getString("charity_url"))); }, eventId);
        return out;
    }
    private static ResponseStatusException bad(String m) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, m); }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> reason(ResponseStatusException ex) {
        int code = ex.getStatusCode().value();
        if ((code == 400 || code == 409) && ex.getReason() != null) return ResponseEntity.status(code).body(Map.of("error", ex.getReason()));
        return ResponseEntity.status(ex.getStatusCode()).build();
    }
}
