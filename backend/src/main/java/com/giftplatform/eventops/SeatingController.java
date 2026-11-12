package com.giftplatform.eventops;

import com.giftplatform.admin.EffectiveModulePermissionService;
import com.giftplatform.cohost.EventAccess;
import com.giftplatform.cohost.EventAccess.Capability;
import com.giftplatform.event.entity.Event;
import com.giftplatform.event.entity.EventStatus;
import com.giftplatform.event.repository.EventRepository;
import com.giftplatform.register.GiftPeople;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

/**
 * Seating planner: tables with capacities, families assigned to tables, a printable chart, and an optional
 * "find my table" lookup for guests on the invitation.
 */
@RestController
public class SeatingController extends EventOpsSupport {
    public static final String MODULE = "SEATING_PLAN";

    private final JdbcTemplate db;
    private final EventAccess access;
    private final EventRepository events;
    private final EffectiveModulePermissionService permissions;

    public SeatingController(JdbcTemplate db, EventAccess access, EventRepository events, EffectiveModulePermissionService permissions) {
        this.db = db; this.access = access; this.events = events; this.permissions = permissions;
    }

    public record Seated(String key, String name, int party) {}
    public record Table(UUID id, String name, int capacity, String shape, int seated, List<Seated> guests) {}
    public record Unseated(String key, String name, int party, String source) {}
    public record Plan(List<Table> tables, List<Unseated> unseated, int seatedPeople, int expectedPeople, boolean guestLookup) {}
    public record TableInput(String name, Integer capacity, String shape) {}
    public record Assign(String guestName, Integer partySize, UUID tableId) {}
    public record Key(String key) {}
    public record Settings(boolean guestLookup) {}
    public record Found(String name, String table) {}

    @GetMapping("/api/v1/events/{id}/seating")
    public ResponseEntity<Plan> plan(@PathVariable UUID id) {
        access.require(id, Capability.SEATING, MODULE);
        Map<UUID, List<Seated>> byTable = new HashMap<>();
        Set<String> seatedKeys = new HashSet<>();
        db.query("SELECT table_id, guest_key, guest_name, party_size FROM event_seat_assignments WHERE event_id=? ORDER BY guest_name", rs -> {
            byTable.computeIfAbsent(rs.getObject(1, UUID.class), k -> new ArrayList<>()).add(new Seated(rs.getString(2), rs.getString(3), rs.getInt(4)));
            seatedKeys.add(rs.getString(2));
        }, id);
        List<Table> tables = db.query("SELECT id,name,capacity,shape FROM event_seating_tables WHERE event_id=? ORDER BY sort_order, created_at", (rs, i) -> {
            UUID tid = rs.getObject(1, UUID.class);
            List<Seated> guests = byTable.getOrDefault(tid, List.of());
            return new Table(tid, rs.getString(2), rs.getInt(3), rs.getString(4), guests.stream().mapToInt(Seated::party).sum(), guests);
        }, id);
        Map<String, ExpectedGuests.Guest> expected = ExpectedGuests.of(db, id);
        List<Unseated> unseated = expected.values().stream().filter(g -> !seatedKeys.contains(g.key()))
            .map(g -> new Unseated(g.key(), g.name(), g.party(), g.source())).sorted(Comparator.comparing(Unseated::name, String.CASE_INSENSITIVE_ORDER)).toList();
        boolean lookup = Boolean.TRUE.equals(db.query("SELECT guest_lookup FROM event_seating_settings WHERE event_id=?", (rs, i) -> rs.getBoolean(1), id).stream().findFirst().orElse(false));
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new Plan(tables, unseated, tables.stream().mapToInt(Table::seated).sum(),
            expected.values().stream().mapToInt(ExpectedGuests.Guest::party).sum(), lookup));
    }

    @PostMapping("/api/v1/events/{id}/seating/tables")
    public Map<String, Object> addTable(@PathVariable UUID id, @RequestBody TableInput in) {
        access.require(id, Capability.SEATING, MODULE);
        Clean c = clean(in);
        Integer n = db.queryForObject("SELECT count(*) FROM event_seating_tables WHERE event_id=?", Integer.class, id);
        if (n != null && n >= 200) throw bad("A plan can have up to 200 tables");
        UUID tid = UUID.randomUUID();
        db.update("INSERT INTO event_seating_tables(id,event_id,name,capacity,shape,sort_order) VALUES(?,?,?,?,?,(SELECT COALESCE(MAX(sort_order),-1)+1 FROM event_seating_tables WHERE event_id=?))",
            tid, id, c.name, c.capacity, c.shape, id);
        return Map.of("id", tid);
    }

    @PutMapping("/api/v1/events/{id}/seating/tables/{tableId}")
    public Map<String, Object> editTable(@PathVariable UUID id, @PathVariable UUID tableId, @RequestBody TableInput in) {
        access.require(id, Capability.SEATING, MODULE);
        Clean c = clean(in);
        Integer seated = db.queryForObject("SELECT COALESCE(SUM(party_size),0)::int FROM event_seat_assignments WHERE table_id=?", Integer.class, tableId);
        if (seated != null && seated > c.capacity) throw conflict("This table already seats " + seated + ". Move some guests first.");
        if (db.update("UPDATE event_seating_tables SET name=?,capacity=?,shape=? WHERE id=? AND event_id=?", c.name, c.capacity, c.shape, tableId, id) == 0) throw notFound();
        return Map.of("saved", true);
    }

    @PostMapping("/api/v1/events/{id}/seating/tables/{tableId}/delete")
    public Map<String, Object> deleteTable(@PathVariable UUID id, @PathVariable UUID tableId) {
        access.require(id, Capability.SEATING, MODULE);
        if (db.update("DELETE FROM event_seating_tables WHERE id=? AND event_id=?", tableId, id) == 0) throw notFound();
        return Map.of("deleted", true);
    }

    /** Seats a family at a table (moving them if already seated). Refuses to overfill a table. */
    @PostMapping("/api/v1/events/{id}/seating/assign")
    @Transactional
    public Map<String, Object> assign(@PathVariable UUID id, @RequestBody Assign in) {
        access.require(id, Capability.SEATING, MODULE);
        if (in == null || in.tableId() == null) throw bad("Choose a table");
        String name = required(in.guestName(), 120, "Guest name");
        String key = GiftPeople.key(name);
        if (key.isEmpty()) throw bad("Enter the guest's name");
        int party = in.partySize() == null ? 1 : in.partySize();
        if (party < 1 || party > 30) throw bad("Party size must be between 1 and 30");
        var table = db.queryForList("SELECT name, capacity FROM event_seating_tables WHERE id=? AND event_id=? FOR UPDATE", in.tableId(), id);
        if (table.isEmpty()) throw notFound();
        int capacity = ((Number) table.getFirst().get("capacity")).intValue();
        Integer used = db.queryForObject("SELECT COALESCE(SUM(party_size),0)::int FROM event_seat_assignments WHERE table_id=? AND guest_key<>?", Integer.class, in.tableId(), key);
        if (used != null && used + party > capacity)
            throw conflict(table.getFirst().get("name") + " has " + (capacity - used) + " seat" + (capacity - used == 1 ? "" : "s") + " left");
        db.update("""
            INSERT INTO event_seat_assignments(id,event_id,table_id,guest_name,guest_key,party_size) VALUES(?,?,?,?,?,?)
            ON CONFLICT(event_id,guest_key) DO UPDATE SET table_id=EXCLUDED.table_id,guest_name=EXCLUDED.guest_name,party_size=EXCLUDED.party_size""",
            UUID.randomUUID(), id, in.tableId(), name, key, party);
        return Map.of("seated", true);
    }

    @PostMapping("/api/v1/events/{id}/seating/unassign")
    public Map<String, Object> unassign(@PathVariable UUID id, @RequestBody Key in) {
        access.require(id, Capability.SEATING, MODULE);
        if (in == null || in.key() == null || db.update("DELETE FROM event_seat_assignments WHERE event_id=? AND guest_key=?", id, in.key()) == 0) throw notFound();
        return Map.of("unseated", true);
    }

    @PutMapping("/api/v1/events/{id}/seating/settings")
    public Map<String, Object> settings(@PathVariable UUID id, @RequestBody Settings in) {
        access.require(id, Capability.SEATING, MODULE);
        boolean on = in != null && in.guestLookup();
        db.update("INSERT INTO event_seating_settings(event_id,guest_lookup) VALUES(?,?) ON CONFLICT(event_id) DO UPDATE SET guest_lookup=EXCLUDED.guest_lookup", id, on);
        return Map.of("guestLookup", on);
    }

    /** Guests type at least 3 letters of their name; up to 5 matches with their table. Rate limited per device. */
    @GetMapping("/api/public/celebrations/{token}/find-table")
    public ResponseEntity<List<Found>> find(@PathVariable UUID token, @RequestParam(defaultValue = "") String name, HttpServletRequest http) {
        Event e = events.findByPublicToken(token).filter(x -> x.getStatus() == EventStatus.PUBLISHED).orElseThrow(EventOpsSupport::notFound);
        if (!permissions.eventEnabled(e.getOwnerId(), e.getId(), MODULE)) throw notFound();
        boolean on = Boolean.TRUE.equals(db.query("SELECT guest_lookup FROM event_seating_settings WHERE event_id=?", (rs, i) -> rs.getBoolean(1), e.getId()).stream().findFirst().orElse(false));
        if (!on) throw notFound();
        String q = GiftPeople.key(name);
        if (q.length() < 3) throw bad("Type at least 3 letters of your name");
        limit(e.getId(), http);
        List<Found> out = db.query("""
            SELECT a.guest_name, t.name FROM event_seat_assignments a JOIN event_seating_tables t ON t.id=a.table_id
            WHERE a.event_id=? AND (a.guest_key LIKE ? OR a.guest_key LIKE ?) ORDER BY a.guest_name LIMIT 5""",
            (rs, i) -> new Found(rs.getString(1), rs.getString(2)), e.getId(), q + "%", "% " + q + "%");
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(out);
    }

    private record Clean(String name, int capacity, String shape) {}
    private Clean clean(TableInput in) {
        if (in == null) throw bad("Table details are required");
        String name = required(in.name(), 60, "Table name");
        int capacity = in.capacity() == null ? 8 : in.capacity();
        if (capacity < 1 || capacity > 200) throw bad("A table seats between 1 and 200 people");
        String shape = in.shape() == null ? "ROUND" : in.shape().strip().toUpperCase(Locale.ROOT);
        if (!Set.of("ROUND", "LONG").contains(shape)) throw bad("Choose round or long");
        return new Clean(name, capacity, shape);
    }
    private void limit(UUID eventId, HttpServletRequest request) {
        String fp;
        try { fp = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(request.getRemoteAddr().getBytes(StandardCharsets.UTF_8))); }
        catch (Exception ex) { throw new IllegalStateException(ex); }
        int updated = db.update("""
            INSERT INTO event_guest_submission_limits(event_id,fingerprint,submission_date,submission_type,count) VALUES (?,?,?,'FIND_TABLE',1)
            ON CONFLICT(event_id,fingerprint,submission_date,submission_type) DO UPDATE SET count=event_guest_submission_limits.count+1
            WHERE event_guest_submission_limits.count < 60""", eventId, fp, LocalDate.now(ZoneOffset.UTC));
        if (updated == 0) throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Please try again later");
    }
}
