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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Travel & stay for outstation guests: who is arriving when and how, who needs a pickup or drop, and which hotel and
 * room each family is in. Each family gets a private link to fill in their own travel details. Owner and co-hosts
 * with the GUESTS capability manage it.
 */
@RestController
public class TravelController extends EventOpsSupport {
    public static final String MODULE = "GUEST_TRAVEL";
    static final Set<String> MODES = Set.of("FLIGHT", "TRAIN", "BUS", "CAR", "OTHER");
    static final int MAX_GUESTS = 2000, MAX_STAYS = 50;

    private final JdbcTemplate db;
    private final EventAccess access;
    private final EventRepository events;
    private final EffectiveModulePermissionService permissions;

    public TravelController(JdbcTemplate db, EventAccess access, EventRepository events, EffectiveModulePermissionService permissions) {
        this.db = db; this.access = access; this.events = events; this.permissions = permissions;
    }

    public record Leg(LocalDateTime at, String mode, String ref, String point) {}
    public record Guest(UUID id, String guestName, int partySize, String phone, UUID formToken, Leg arrival, Leg departure, boolean pickupNeeded, boolean dropNeeded,
                        UUID stayId, String stayName, String roomLabel, int rooms, String guestNote, String hostNote, boolean submitted, long version) {}
    public record Stay(UUID id, String name, String address, String phone, String mapUrl, LocalDate checkIn, LocalDate checkOut, Integer roomsTotal,
                       int roomsUsed, int families, int people, String note) {}
    public record Movement(UUID travelId, String guestName, int partySize, LocalDateTime at, String mode, String ref, String point, boolean transport, String stayName) {}
    public record Day(LocalDate date, List<Movement> arrivals, List<Movement> departures) {}
    public record Totals(int families, int people, int submitted, int pickups, int drops, int withStay, int withoutArrival) {}
    public record Board(List<Guest> guests, List<Stay> stays, List<Day> days, Totals totals, List<String> suggestions) {}
    public record GuestInput(String guestName, Integer partySize, String phone) {}
    public record LegInput(LocalDateTime at, String mode, String ref, String point) {}
    public record HostEdit(String guestName, Integer partySize, String phone, LegInput arrival, LegInput departure, Boolean pickupNeeded, Boolean dropNeeded,
                           UUID stayId, String roomLabel, Integer rooms, String hostNote, Long version) {}
    public record StayInput(String name, String address, String phone, String mapUrl, LocalDate checkIn, LocalDate checkOut, Integer roomsTotal, String note) {}
    public record GuestForm(Integer partySize, LegInput arrival, LegInput departure, Boolean pickupNeeded, Boolean dropNeeded, String guestNote) {}
    public record PublicStay(String name, String address, String phone, String mapUrl, LocalDate checkIn, LocalDate checkOut, String room) {}
    public record PublicTravel(String eventTitle, LocalDate eventDate, String hostName, String guestName, int partySize, Leg arrival, Leg departure,
                               boolean pickupNeeded, boolean dropNeeded, String guestNote, boolean submitted, PublicStay stay, String invitationPath) {}

    // ---- Host ----

    @GetMapping("/api/v1/events/{id}/travel")
    public ResponseEntity<Board> board(@PathVariable UUID id) {
        var a = access.require(id, Capability.GUESTS, MODULE);
        List<Guest> guests = guests(id, null);
        Map<UUID, int[]> used = new HashMap<>();
        for (Guest g : guests) if (g.stayId() != null) { int[] u = used.computeIfAbsent(g.stayId(), k -> new int[3]); u[0] += g.rooms(); u[1]++; u[2] += g.partySize(); }
        List<Stay> stays = db.query("SELECT * FROM event_stay_options WHERE event_id=? ORDER BY created_at", (rs, i) -> {
            UUID sid = rs.getObject("id", UUID.class);
            int[] u = used.getOrDefault(sid, new int[3]);
            return new Stay(sid, rs.getString("name"), rs.getString("address"), rs.getString("phone"), rs.getString("map_url"), rs.getObject("check_in", LocalDate.class),
                rs.getObject("check_out", LocalDate.class), (Integer) rs.getObject("rooms_total"), u[0], u[1], u[2], rs.getString("note"));
        }, id);
        TreeMap<LocalDate, Day> days = new TreeMap<>();
        for (Guest g : guests) {
            if (g.arrival().at() != null) days.computeIfAbsent(g.arrival().at().toLocalDate(), d -> new Day(d, new ArrayList<>(), new ArrayList<>())).arrivals()
                .add(new Movement(g.id(), g.guestName(), g.partySize(), g.arrival().at(), g.arrival().mode(), g.arrival().ref(), g.arrival().point(), g.pickupNeeded(), g.stayName()));
            if (g.departure().at() != null) days.computeIfAbsent(g.departure().at().toLocalDate(), d -> new Day(d, new ArrayList<>(), new ArrayList<>())).departures()
                .add(new Movement(g.id(), g.guestName(), g.partySize(), g.departure().at(), g.departure().mode(), g.departure().ref(), g.departure().point(), g.dropNeeded(), g.stayName()));
        }
        days.values().forEach(d -> { d.arrivals().sort(Comparator.comparing(Movement::at)); d.departures().sort(Comparator.comparing(Movement::at)); });
        Totals totals = new Totals(guests.size(), guests.stream().mapToInt(Guest::partySize).sum(), (int) guests.stream().filter(Guest::submitted).count(),
            (int) guests.stream().filter(Guest::pickupNeeded).count(), (int) guests.stream().filter(Guest::dropNeeded).count(),
            (int) guests.stream().filter(g -> g.stayId() != null).count(), (int) guests.stream().filter(g -> g.arrival().at() == null).count());
        List<String> suggestions = permissions.eventEnabled(a.event().getOwnerId(), id, HouseholdsController.MODULE)
            ? db.queryForList("""
                SELECT h.name FROM event_households h WHERE h.event_id=? AND h.outstation AND h.rsvp<>'NO'
                AND NOT EXISTS(SELECT 1 FROM event_guest_travel t WHERE t.event_id=h.event_id AND t.guest_key=h.name_key) ORDER BY lower(h.name) LIMIT 200""", String.class, id)
            : List.of();
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new Board(guests, stays, List.copyOf(days.values()), totals, suggestions));
    }

    @PostMapping("/api/v1/events/{id}/travel/guests")
    @Transactional
    public Guest addGuest(@PathVariable UUID id, @RequestBody GuestInput in) {
        access.require(id, Capability.GUESTS, MODULE);
        if (in == null) throw bad("Guest details are required");
        String name = required(in.guestName(), 120, "Guest name"), key = GiftPeople.key(name);
        if (key.isEmpty()) throw bad("Enter the guest's name");
        int party = party(in.partySize());
        lockEvent(id);
        room(id, 1);
        if (Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM event_guest_travel WHERE event_id=? AND guest_key=?)", Boolean.class, id, key)))
            throw conflict(name + " is already on the travel list");
        UUID tid = UUID.randomUUID();
        db.update("INSERT INTO event_guest_travel(id,event_id,guest_name,guest_key,party_size,phone) VALUES(?,?,?,?,?,?)", tid, id, name, key, party, GiftPeople.phone(in.phone()));
        return guests(id, tid).getFirst();
    }

    /** Adds every outstation household from the guest list (not marked "not coming") that isn't on the travel list yet. */
    @PostMapping("/api/v1/events/{id}/travel/guests/from-households")
    @Transactional
    public Map<String, Object> fromHouseholds(@PathVariable UUID id) {
        var a = access.require(id, Capability.GUESTS, MODULE);
        if (!permissions.eventEnabled(a.event().getOwnerId(), id, HouseholdsController.MODULE)) throw bad("Turn on the guest list to add outstation families from it");
        lockEvent(id);
        var targets = db.queryForList("""
            SELECT h.name, h.name_key, h.phone, CASE WHEN EXISTS(SELECT 1 FROM event_household_members m WHERE m.household_id=h.id)
              THEN GREATEST(1,(SELECT count(*) FROM event_household_members m WHERE m.household_id=h.id AND m.attending))::int ELSE h.expected_size END AS party
            FROM event_households h WHERE h.event_id=? AND h.outstation AND h.rsvp<>'NO'
            AND NOT EXISTS(SELECT 1 FROM event_guest_travel t WHERE t.event_id=h.event_id AND t.guest_key=h.name_key) ORDER BY lower(h.name)""", id);
        room(id, targets.size());
        for (var t : targets)
            db.update("INSERT INTO event_guest_travel(id,event_id,guest_name,guest_key,party_size,phone) VALUES(?,?,?,?,?,?) ON CONFLICT(event_id,guest_key) DO NOTHING",
                UUID.randomUUID(), id, t.get("name"), t.get("name_key"), t.get("party"), t.get("phone"));
        return Map.of("created", targets.size());
    }

    @PutMapping("/api/v1/events/{id}/travel/guests/{travelId}")
    public Guest editGuest(@PathVariable UUID id, @PathVariable UUID travelId, @RequestBody HostEdit in) {
        access.require(id, Capability.GUESTS, MODULE);
        if (in == null) throw bad("Travel details are required");
        if (in.version() == null) throw changed();
        String name = required(in.guestName(), 120, "Guest name"), key = GiftPeople.key(name);
        if (key.isEmpty()) throw bad("Enter the guest's name");
        if (Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM event_guest_travel WHERE event_id=? AND guest_key=? AND id<>?)", Boolean.class, id, key, travelId)))
            throw conflict(name + " is already on the travel list");
        Leg arrival = leg(in.arrival(), "Arrival"), departure = leg(in.departure(), "Departure");
        order(arrival, departure);
        if (in.stayId() != null && !Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM event_stay_options WHERE id=? AND event_id=?)", Boolean.class, in.stayId(), id)))
            throw bad("Choose a stay from your list");
        int rooms = in.rooms() == null ? 1 : in.rooms();
        if (rooms < 0 || rooms > 20) throw bad("Rooms must be between 0 and 20");
        int n = db.update("""
            UPDATE event_guest_travel SET guest_name=?,guest_key=?,party_size=?,phone=?,arrival_at=?,arrival_mode=?,arrival_ref=?,arrival_point=?,
              departure_at=?,departure_mode=?,departure_ref=?,departure_point=?,pickup_needed=?,drop_needed=?,stay_id=?,room_label=?,rooms=?,host_note=?,version=version+1
            WHERE id=? AND event_id=? AND version=?""", name, key, party(in.partySize()), GiftPeople.phone(in.phone()),
            arrival.at(), arrival.mode(), arrival.ref(), arrival.point(), departure.at(), departure.mode(), departure.ref(), departure.point(),
            Boolean.TRUE.equals(in.pickupNeeded()), Boolean.TRUE.equals(in.dropNeeded()), in.stayId(), text(in.roomLabel(), 40, "Room"), rooms,
            text(in.hostNote(), 300, "Note"), travelId, id, in.version());
        if (n == 0) throw Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM event_guest_travel WHERE id=? AND event_id=?)", Boolean.class, travelId, id)) ? changed() : notFound();
        return guests(id, travelId).getFirst();
    }

    @PostMapping("/api/v1/events/{id}/travel/guests/{travelId}/delete")
    public Map<String, Object> deleteGuest(@PathVariable UUID id, @PathVariable UUID travelId) {
        access.require(id, Capability.GUESTS, MODULE);
        if (db.update("DELETE FROM event_guest_travel WHERE id=? AND event_id=?", travelId, id) == 0) throw notFound();
        return Map.of("deleted", true);
    }

    /** A new private link for this family; the old one stops working (e.g. it was forwarded to the wrong person). */
    @PostMapping("/api/v1/events/{id}/travel/guests/{travelId}/new-link")
    public Guest newLink(@PathVariable UUID id, @PathVariable UUID travelId) {
        access.require(id, Capability.GUESTS, MODULE);
        if (db.update("UPDATE event_guest_travel SET form_token=gen_random_uuid() WHERE id=? AND event_id=?", travelId, id) == 0) throw notFound();
        return guests(id, travelId).getFirst();
    }

    @PostMapping("/api/v1/events/{id}/travel/stays")
    @Transactional
    public Map<String, Object> addStay(@PathVariable UUID id, @RequestBody StayInput in) {
        access.require(id, Capability.GUESTS, MODULE);
        CleanStay c = stay(in);
        lockEvent(id);
        Integer n = db.queryForObject("SELECT count(*) FROM event_stay_options WHERE event_id=?", Integer.class, id);
        if (n != null && n >= MAX_STAYS) throw bad("A celebration can have up to 50 places to stay");
        UUID sid = UUID.randomUUID();
        db.update("INSERT INTO event_stay_options(id,event_id,name,address,phone,map_url,check_in,check_out,rooms_total,note) VALUES(?,?,?,?,?,?,?,?,?,?)",
            sid, id, c.name, c.address, c.phone, c.map, in.checkIn(), in.checkOut(), in.roomsTotal(), c.note);
        return Map.of("id", sid);
    }

    @PutMapping("/api/v1/events/{id}/travel/stays/{stayId}")
    public Map<String, Object> editStay(@PathVariable UUID id, @PathVariable UUID stayId, @RequestBody StayInput in) {
        access.require(id, Capability.GUESTS, MODULE);
        CleanStay c = stay(in);
        if (db.update("UPDATE event_stay_options SET name=?,address=?,phone=?,map_url=?,check_in=?,check_out=?,rooms_total=?,note=? WHERE id=? AND event_id=?",
            c.name, c.address, c.phone, c.map, in.checkIn(), in.checkOut(), in.roomsTotal(), c.note, stayId, id) == 0) throw notFound();
        return Map.of("saved", true);
    }

    @PostMapping("/api/v1/events/{id}/travel/stays/{stayId}/delete")
    public Map<String, Object> deleteStay(@PathVariable UUID id, @PathVariable UUID stayId) {
        access.require(id, Capability.GUESTS, MODULE);
        if (db.update("DELETE FROM event_stay_options WHERE id=? AND event_id=?", stayId, id) == 0) throw notFound();
        return Map.of("deleted", true);
    }

    /** Arrivals and departures as a spreadsheet for the transport team and hotel desk. */
    @GetMapping(value = "/api/v1/events/{id}/travel/manifest.csv", produces = "text/csv")
    public ResponseEntity<byte[]> manifest(@PathVariable UUID id) {
        access.require(id, Capability.GUESTS, MODULE);
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        StringBuilder b = new StringBuilder("Guest,People,Phone,Arrival,Arrival by,Arrival ref,Arriving at,Pickup,Departure,Departure by,Departure ref,Leaving from,Drop,Stay,Room,Rooms,Guest note,Host note\n");
        for (Guest g : guests(id, null)) {
            b.append(String.join(",", HouseholdsController.Csv.cell(g.guestName()), String.valueOf(g.partySize()), HouseholdsController.Csv.cell(g.phone()),
                g.arrival().at() == null ? "" : g.arrival().at().format(f), nz(g.arrival().mode()), HouseholdsController.Csv.cell(g.arrival().ref()), HouseholdsController.Csv.cell(g.arrival().point()),
                g.pickupNeeded() ? "Yes" : "No", g.departure().at() == null ? "" : g.departure().at().format(f), nz(g.departure().mode()), HouseholdsController.Csv.cell(g.departure().ref()),
                HouseholdsController.Csv.cell(g.departure().point()), g.dropNeeded() ? "Yes" : "No", HouseholdsController.Csv.cell(g.stayName()), HouseholdsController.Csv.cell(g.roomLabel()),
                String.valueOf(g.rooms()), HouseholdsController.Csv.cell(g.guestNote()), HouseholdsController.Csv.cell(g.hostNote()))).append('\n');
        }
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"travel-manifest.csv\"").body(("﻿" + b).getBytes(StandardCharsets.UTF_8));
    }

    // ---- Guest (private link, no account) ----

    @GetMapping("/api/public/travel/{token}")
    public ResponseEntity<PublicTravel> publicView(@PathVariable UUID token) {
        var row = publicRow(token);
        Event e = (Event) row.get("_event");
        PublicStay stay = row.get("stay_id") == null ? null : db.query("SELECT * FROM event_stay_options WHERE id=?", (rs, i) -> new PublicStay(rs.getString("name"),
            rs.getString("address"), rs.getString("phone"), rs.getString("map_url"), rs.getObject("check_in", LocalDate.class), rs.getObject("check_out", LocalDate.class),
            (String) row.get("room_label")), row.get("stay_id")).stream().findFirst().orElse(null);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).header("X-Robots-Tag", "noindex").body(new PublicTravel(e.getTitle(), e.getEventDate(), e.getHostDisplayName(),
            (String) row.get("guest_name"), ((Number) row.get("party_size")).intValue(), legOf(row, "arrival"), legOf(row, "departure"), Boolean.TRUE.equals(row.get("pickup_needed")),
            Boolean.TRUE.equals(row.get("drop_needed")), (String) row.get("guest_note"), row.get("submitted_at") != null, stay, "/e/" + e.getPublicToken()));
    }

    @PutMapping("/api/public/travel/{token}")
    public ResponseEntity<PublicTravel> publicSubmit(@PathVariable UUID token, @RequestBody GuestForm in, HttpServletRequest http) {
        var row = publicRow(token);
        if (in == null) throw bad("Travel details are required");
        Leg arrival = leg(in.arrival(), "Arrival"), departure = leg(in.departure(), "Departure");
        order(arrival, departure);
        int party = in.partySize() == null ? ((Number) row.get("party_size")).intValue() : party(in.partySize());
        limit(db, (UUID) row.get("event_id"), http, "TRAVEL", 30);
        db.update("""
            UPDATE event_guest_travel SET party_size=?,arrival_at=?,arrival_mode=?,arrival_ref=?,arrival_point=?,departure_at=?,departure_mode=?,departure_ref=?,departure_point=?,
              pickup_needed=?,drop_needed=?,guest_note=?,submitted_at=now(),version=version+1 WHERE form_token=?""", party,
            arrival.at(), arrival.mode(), arrival.ref(), arrival.point(), departure.at(), departure.mode(), departure.ref(), departure.point(),
            Boolean.TRUE.equals(in.pickupNeeded()), Boolean.TRUE.equals(in.dropNeeded()), text(in.guestNote(), 300, "Note"), token);
        return publicView(token);
    }

    // ---- Helpers ----

    private Map<String, Object> publicRow(UUID token) {
        var rows = db.queryForList("SELECT * FROM event_guest_travel WHERE form_token=?", token);
        if (rows.isEmpty()) throw notFound();
        Map<String, Object> row = new HashMap<>(rows.getFirst());
        Event e = events.findById((UUID) row.get("event_id")).filter(x -> x.getStatus() == EventStatus.PUBLISHED).orElseThrow(EventOpsSupport::notFound);
        if (!permissions.eventEnabled(e.getOwnerId(), e.getId(), MODULE)) throw notFound();
        row.put("_event", e);
        return row;
    }

    private List<Guest> guests(UUID event, UUID only) {
        List<Guest> out = db.query("""
            SELECT t.*, s.name AS stay_name FROM event_guest_travel t LEFT JOIN event_stay_options s ON s.id=t.stay_id
            WHERE t.event_id=? AND (?::uuid IS NULL OR t.id=?) ORDER BY t.arrival_at NULLS LAST, lower(t.guest_name)""", (rs, i) -> new Guest(rs.getObject("id", UUID.class),
            rs.getString("guest_name"), rs.getInt("party_size"), rs.getString("phone"), rs.getObject("form_token", UUID.class), leg(rs, "arrival"), leg(rs, "departure"),
            rs.getBoolean("pickup_needed"), rs.getBoolean("drop_needed"), rs.getObject("stay_id", UUID.class), rs.getString("stay_name"), rs.getString("room_label"),
            rs.getInt("rooms"), rs.getString("guest_note"), rs.getString("host_note"), rs.getObject("submitted_at") != null, rs.getLong("version")), event, only, only);
        if (only != null && out.isEmpty()) throw notFound();
        return out;
    }
    private static Leg leg(ResultSet rs, String p) throws SQLException {
        return new Leg(rs.getObject(p + "_at", LocalDateTime.class), rs.getString(p + "_mode"), rs.getString(p + "_ref"), rs.getString(p + "_point"));
    }
    private static Leg legOf(Map<String, Object> row, String p) {
        Object at = row.get(p + "_at");
        LocalDateTime when = at instanceof java.sql.Timestamp ts ? ts.toLocalDateTime() : (LocalDateTime) at;
        return new Leg(when, (String) row.get(p + "_mode"), (String) row.get(p + "_ref"), (String) row.get(p + "_point"));
    }
    private static Leg leg(LegInput in, String label) {
        if (in == null) return new Leg(null, null, null, null);
        String mode = in.mode() == null || in.mode().isBlank() ? null : in.mode().strip().toUpperCase(Locale.ROOT);
        if (mode != null && !MODES.contains(mode)) throw bad(label + ": choose flight, train, bus, car or other");
        if (in.at() != null && (in.at().getYear() < 2000 || in.at().getYear() > 2100)) throw bad(label + ": check the date");
        return new Leg(in.at() == null ? null : in.at().withSecond(0).withNano(0), mode, text(in.ref(), 40, label + " flight/train number"), text(in.point(), 120, label + " place"));
    }
    private static void order(Leg arrival, Leg departure) {
        if (arrival.at() != null && departure.at() != null && departure.at().isBefore(arrival.at())) throw bad("Departure can't be before arrival");
    }
    private static int party(Integer v) {
        int p = v == null ? 1 : v;
        if (p < 1 || p > 30) throw bad("People must be between 1 and 30");
        return p;
    }
    private void room(UUID event, int adding) {
        Integer n = db.queryForObject("SELECT count(*) FROM event_guest_travel WHERE event_id=?", Integer.class, event);
        if (n != null && n + adding > MAX_GUESTS) throw bad("The travel list can have up to 2,000 families");
    }
    private void lockEvent(UUID id) { db.queryForList("SELECT id FROM events WHERE id=? FOR UPDATE", id); }
    private static String nz(String v) { return v == null ? "" : v; }

    private record CleanStay(String name, String address, String phone, String map, String note) {}
    private static CleanStay stay(StayInput in) {
        if (in == null) throw bad("Stay details are required");
        if (in.checkIn() != null && in.checkOut() != null && in.checkOut().isBefore(in.checkIn())) throw bad("Check-out can't be before check-in");
        if (in.roomsTotal() != null && (in.roomsTotal() < 0 || in.roomsTotal() > 2000)) throw bad("Rooms must be between 0 and 2,000");
        return new CleanStay(required(in.name(), 120, "Hotel or stay name"), text(in.address(), 300, "Address"), GiftPeople.phone(in.phone()), https(in.mapUrl(), "Map link"), text(in.note(), 300, "Note"));
    }
}
