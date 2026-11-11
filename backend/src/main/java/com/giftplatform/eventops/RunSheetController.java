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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

/** Day-of run sheet: who does what, when and where. A rotatable read-only link can be shared with vendors. */
@RestController
public class RunSheetController extends EventOpsSupport {
    public static final String MODULE = "RUN_SHEET";

    private final JdbcTemplate db;
    private final EventAccess access;
    private final EventRepository events;
    private final EffectiveModulePermissionService permissions;

    public RunSheetController(JdbcTemplate db, EventAccess access, EventRepository events, EffectiveModulePermissionService permissions) {
        this.db = db; this.access = access; this.events = events; this.permissions = permissions;
    }

    public record Input(LocalDate runDate, LocalTime startsAt, String title, String ownerName, String location, String note, UUID functionId, Long version) {}
    public record Item(UUID id, LocalDate runDate, LocalTime startsAt, String title, String ownerName, String location, String note, UUID functionId,
                       String functionName, boolean done, long version) {}
    public record Fn(UUID id, String name, LocalDate date) {}
    public record Sheet(List<Item> items, boolean shared, UUID shareToken, List<Fn> functions, LocalDate eventDate) {}
    public record PublicSheet(String eventTitle, LocalDate eventDate, List<Item> items) {}
    public record Share(boolean shared) {}
    public record Done(boolean done) {}

    @GetMapping("/api/v1/events/{id}/run-sheet")
    public ResponseEntity<Sheet> get(@PathVariable UUID id) {
        Event e = access.require(id, Capability.PLANNING, MODULE).event();
        var link = db.queryForList("SELECT share_token, shared FROM event_run_sheet_links WHERE event_id=?", id);
        List<Fn> fns = db.query("SELECT id,name,function_date FROM event_functions WHERE event_id=? ORDER BY sort_order",
            (rs, i) -> new Fn(rs.getObject(1, UUID.class), rs.getString(2), rs.getObject(3, LocalDate.class)), id);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new Sheet(items(id), !link.isEmpty() && (Boolean) link.getFirst().get("shared"),
            link.isEmpty() ? null : (UUID) link.getFirst().get("share_token"), fns, e.getEventDate()));
    }

    @PostMapping("/api/v1/events/{id}/run-sheet/items")
    public Item create(@PathVariable UUID id, @RequestBody Input in) {
        access.require(id, Capability.PLANNING, MODULE);
        Clean c = clean(id, in);
        Integer n = db.queryForObject("SELECT count(*) FROM event_run_items WHERE event_id=?", Integer.class, id);
        if (n != null && n >= 200) throw bad("A run sheet can have up to 200 items");
        UUID iid = UUID.randomUUID();
        db.update("INSERT INTO event_run_items(id,event_id,function_id,starts_at,run_date,title,owner_name,location,note) VALUES(?,?,?,?,?,?,?,?,?)",
            iid, id, in.functionId(), in.startsAt(), in.runDate(), c.title, c.owner, c.location, c.note);
        return one(id, iid);
    }

    @PutMapping("/api/v1/events/{id}/run-sheet/items/{itemId}")
    public Item update(@PathVariable UUID id, @PathVariable UUID itemId, @RequestBody Input in) {
        access.require(id, Capability.PLANNING, MODULE);
        Clean c = clean(id, in);
        if (in.version() == null) throw changed();
        int n = db.update("UPDATE event_run_items SET function_id=?,starts_at=?,run_date=?,title=?,owner_name=?,location=?,note=?,version=version+1 WHERE id=? AND event_id=? AND version=?",
            in.functionId(), in.startsAt(), in.runDate(), c.title, c.owner, c.location, c.note, itemId, id, in.version());
        if (n == 0) throw Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM event_run_items WHERE id=? AND event_id=?)", Boolean.class, itemId, id)) ? changed() : notFound();
        return one(id, itemId);
    }

    @PostMapping("/api/v1/events/{id}/run-sheet/items/{itemId}/done")
    public Item done(@PathVariable UUID id, @PathVariable UUID itemId, @RequestBody Done in) {
        access.require(id, Capability.PLANNING, MODULE);
        if (db.update("UPDATE event_run_items SET done_at=" + (in != null && in.done() ? "COALESCE(done_at,now())" : "NULL") + ",version=version+1 WHERE id=? AND event_id=?", itemId, id) == 0) throw notFound();
        return one(id, itemId);
    }

    @PostMapping("/api/v1/events/{id}/run-sheet/items/{itemId}/delete")
    public Map<String, Object> delete(@PathVariable UUID id, @PathVariable UUID itemId) {
        access.require(id, Capability.PLANNING, MODULE);
        if (db.update("DELETE FROM event_run_items WHERE id=? AND event_id=?", itemId, id) == 0) throw notFound();
        return Map.of("deleted", true);
    }

    @PostMapping("/api/v1/events/{id}/run-sheet/share")
    public Map<String, Object> share(@PathVariable UUID id, @RequestBody Share in) {
        access.require(id, Capability.PLANNING, MODULE);
        boolean on = in != null && in.shared();
        UUID token = db.queryForObject("INSERT INTO event_run_sheet_links(event_id,shared) VALUES(?,?) ON CONFLICT(event_id) DO UPDATE SET shared=EXCLUDED.shared RETURNING share_token",
            UUID.class, id, on);
        return Map.of("shared", on, "shareToken", token);
    }

    @PostMapping("/api/v1/events/{id}/run-sheet/share/rotate")
    public Map<String, Object> rotate(@PathVariable UUID id) {
        access.require(id, Capability.PLANNING, MODULE);
        UUID token = db.queryForObject("INSERT INTO event_run_sheet_links(event_id) VALUES(?) ON CONFLICT(event_id) DO UPDATE SET share_token=gen_random_uuid() RETURNING share_token", UUID.class, id);
        return Map.of("shareToken", token);
    }

    /** Read-only for vendors: times, titles, owners and locations. */
    @GetMapping("/api/public/run-sheet/{token}")
    public ResponseEntity<PublicSheet> shared(@PathVariable UUID token) {
        var link = db.queryForList("SELECT event_id FROM event_run_sheet_links WHERE share_token=? AND shared", token);
        if (link.isEmpty()) throw notFound();
        Event e = events.findById((UUID) link.getFirst().get("event_id")).filter(x -> x.getStatus() != EventStatus.CANCELLED).orElseThrow(EventOpsSupport::notFound);
        if (!permissions.eventEnabled(e.getOwnerId(), e.getId(), MODULE)) throw notFound();
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).header("X-Robots-Tag", "noindex").body(new PublicSheet(e.getTitle(), e.getEventDate(), items(e.getId())));
    }

    private record Clean(String title, String owner, String location, String note) {}
    private Clean clean(UUID eventId, Input in) {
        if (in == null || in.startsAt() == null) throw bad("Add a start time");
        if (in.functionId() != null && !Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM event_functions WHERE id=? AND event_id=?)", Boolean.class, in.functionId(), eventId)))
            throw bad("That function is not part of this celebration");
        return new Clean(required(in.title(), 140, "What happens"), text(in.ownerName(), 80, "Who"), text(in.location(), 120, "Where"), text(in.note(), 400, "Note"));
    }
    private List<Item> items(UUID eventId) {
        return db.query("""
            SELECT r.*, f.name AS function_name FROM event_run_items r LEFT JOIN event_functions f ON f.id=r.function_id
            WHERE r.event_id=? ORDER BY r.run_date NULLS FIRST, r.starts_at""", (rs, i) -> item(rs), eventId);
    }
    private Item one(UUID eventId, UUID itemId) {
        return db.query("SELECT r.*, f.name AS function_name FROM event_run_items r LEFT JOIN event_functions f ON f.id=r.function_id WHERE r.id=? AND r.event_id=?",
            (rs, i) -> item(rs), itemId, eventId).stream().findFirst().orElseThrow(EventOpsSupport::notFound);
    }
    private static Item item(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new Item(rs.getObject("id", UUID.class), rs.getObject("run_date", LocalDate.class), rs.getObject("starts_at", LocalTime.class), rs.getString("title"),
            rs.getString("owner_name"), rs.getString("location"), rs.getString("note"), rs.getObject("function_id", UUID.class), rs.getString("function_name"),
            rs.getObject("done_at") != null, rs.getLong("version"));
    }
}
