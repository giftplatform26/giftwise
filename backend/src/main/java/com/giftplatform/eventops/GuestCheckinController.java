package com.giftplatform.eventops;

import com.giftplatform.cohost.EventAccess;
import com.giftplatform.cohost.EventAccess.Capability;
import com.giftplatform.register.GiftPeople;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.*;

/**
 * Entry check-in: search the expected guests (RSVPs, function answers, WhatsApp guest list), tap to mark arrival,
 * add walk-ins, and watch the headcount. Owner, co-hosts and gift-desk helpers can use it.
 */
@RestController
@RequestMapping("/api/v1/events/{id}/checkin")
public class GuestCheckinController extends EventOpsSupport {
    public static final String MODULE = "GUEST_CHECKIN";

    private final JdbcTemplate db;
    private final EventAccess access;

    public GuestCheckinController(JdbcTemplate db, EventAccess access) { this.db = db; this.access = access; }

    public record Guest(String key, String name, int expectedParty, String source, boolean arrived, Integer arrivedParty, OffsetDateTime arrivedAt) {}
    public record Arrival(String key, String name, int partySize, boolean walkIn, OffsetDateTime at, String by) {}
    public record Board(List<Guest> guests, int expectedPeople, int arrivedGuests, int arrivedPeople, int walkIns, List<Arrival> recent) {}
    public record Input(String guestName, Integer partySize, Boolean walkIn) {}
    public record Undo(String key) {}

    @GetMapping
    public ResponseEntity<Board> board(@PathVariable UUID id) {
        access.require(id, Capability.CHECKIN, MODULE);
        Map<String, Guest> expected = new LinkedHashMap<>();
        // Same expected list as seating and passes (households first, then the highest party size across lists).
        for (ExpectedGuests.Guest g : ExpectedGuests.of(db, id).values())
            expected.put(g.key(), new Guest(g.key(), g.name(), g.party(), g.source(), false, null, null));
        List<Arrival> arrivals = db.query("""
            SELECT c.guest_key, c.guest_name, c.party_size, c.walk_in, c.checked_in_at, COALESCE(NULLIF(u.full_name,''), u.username) AS by_name
            FROM event_checkins c LEFT JOIN users u ON u.id=c.checked_in_by WHERE c.event_id=? ORDER BY c.checked_in_at DESC""",
            (rs, i) -> new Arrival(rs.getString(1), rs.getString(2), rs.getInt(3), rs.getBoolean(4), rs.getObject(5, OffsetDateTime.class), rs.getString(6)), id);
        for (Arrival a : arrivals) {
            Guest g = expected.get(a.key());
            expected.put(a.key(), new Guest(a.key(), g == null ? a.name() : g.name(), g == null ? a.partySize() : g.expectedParty(),
                g == null ? "WALK_IN" : g.source(), true, a.partySize(), a.at()));
        }
        List<Guest> guests = expected.values().stream().sorted(Comparator.comparing(Guest::arrived).thenComparing(Guest::name, String.CASE_INSENSITIVE_ORDER)).toList();
        int expectedPeople = guests.stream().filter(g -> !g.source().equals("WALK_IN")).mapToInt(Guest::expectedParty).sum();
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new Board(guests, expectedPeople, arrivals.size(),
            arrivals.stream().mapToInt(Arrival::partySize).sum(), (int) arrivals.stream().filter(Arrival::walkIn).count(), arrivals.stream().limit(10).toList()));
    }

    @PostMapping
    public Arrival checkIn(@PathVariable UUID id, @RequestBody Input in) {
        var a = access.require(id, Capability.CHECKIN, MODULE);
        if (in == null) throw bad("Choose a guest");
        String name = required(in.guestName(), 120, "Guest name");
        String key = GiftPeople.key(name);
        if (key.isEmpty()) throw bad("Enter the guest's name");
        int party = in.partySize() == null ? 1 : in.partySize();
        if (party < 1 || party > 30) throw bad("Party size must be between 1 and 30");
        var existing = db.queryForList("SELECT checked_in_at FROM event_checkins WHERE event_id=? AND guest_key=?", id, key);
        int n = db.update("""
            INSERT INTO event_checkins(id,event_id,guest_name,guest_key,party_size,walk_in,checked_in_by) VALUES(?,?,?,?,?,?,?)
            ON CONFLICT (event_id,guest_key) DO UPDATE SET party_size=EXCLUDED.party_size""",
            UUID.randomUUID(), id, name, key, party, Boolean.TRUE.equals(in.walkIn()), a.userId());
        if (n == 0) throw notFound();
        return db.query("""
            SELECT c.guest_key, c.guest_name, c.party_size, c.walk_in, c.checked_in_at, COALESCE(NULLIF(u.full_name,''), u.username)
            FROM event_checkins c LEFT JOIN users u ON u.id=c.checked_in_by WHERE c.event_id=? AND c.guest_key=?""",
            (rs, i) -> new Arrival(rs.getString(1), rs.getString(2), rs.getInt(3), rs.getBoolean(4), rs.getObject(5, OffsetDateTime.class), rs.getString(6)), id, key).getFirst();
    }

    @PostMapping("/undo")
    public Map<String, Object> undo(@PathVariable UUID id, @RequestBody Undo in) {
        access.require(id, Capability.CHECKIN, MODULE);
        if (in == null || in.key() == null || db.update("DELETE FROM event_checkins WHERE event_id=? AND guest_key=?", id, in.key()) == 0) throw notFound();
        return Map.of("undone", true);
    }
}
