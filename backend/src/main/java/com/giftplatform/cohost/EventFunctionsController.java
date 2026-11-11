package com.giftplatform.cohost;

import com.giftplatform.admin.EffectiveModulePermissionService;
import com.giftplatform.event.entity.Event;
import com.giftplatform.event.entity.EventStatus;
import com.giftplatform.event.repository.EventRepository;
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
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.*;

/** Multi-function celebrations: a schedule of functions (mehendi, sangeet, reception…), each with its own RSVP. */
@RestController
public class EventFunctionsController {
    public static final String MODULE = "EVENT_FUNCTIONS";
    private static final int MAX_FUNCTIONS = 15;

    private final JdbcTemplate db;
    private final EventRepository events;
    private final EventAccess access;
    private final EffectiveModulePermissionService permissions;

    public EventFunctionsController(JdbcTemplate db, EventRepository events, EventAccess access, EffectiveModulePermissionService permissions) {
        this.db = db; this.events = events; this.access = access; this.permissions = permissions;
    }

    public record Input(String name, LocalDate functionDate, LocalTime startTime, LocalTime endTime, String venueName, String venueAddress,
                        String mapUrl, String dressCode, String notes, Boolean rsvpEnabled, Long version) {}
    public record Function(UUID id, String name, LocalDate functionDate, LocalTime startTime, LocalTime endTime, String venueName,
                           String venueAddress, String mapUrl, String dressCode, String notes, boolean rsvpEnabled, int sortOrder, long version) {}
    public record Counts(long yes, long maybe, long no, long guests) {}
    public record Response(String guestName, String guestEmail, String attendance, int partySize) {}
    public record HostFunction(Function function, Counts counts, List<Response> responses) {}
    public record GuestAnswer(UUID functionId, String attendance, Integer partySize) {}
    public record GuestRsvp(String guestName, String guestEmail, List<GuestAnswer> answers) {}

    // ---- Host ----

    @GetMapping("/api/v1/events/{id}/functions")
    public ResponseEntity<List<HostFunction>> list(@PathVariable UUID id) {
        access.require(id, EventAccess.Capability.FUNCTIONS, MODULE);
        List<HostFunction> out = functions(id).stream().map(f -> {
            List<Response> responses = db.query("SELECT guest_name,guest_email,attendance,party_size FROM event_function_rsvps WHERE function_id=? ORDER BY updated_at DESC",
                (rs, i) -> new Response(rs.getString(1), rs.getString(2), rs.getString(3), rs.getInt(4)), f.id());
            long yes = responses.stream().filter(r -> r.attendance().equals("YES")).count();
            long maybe = responses.stream().filter(r -> r.attendance().equals("MAYBE")).count();
            long guests = responses.stream().filter(r -> r.attendance().equals("YES")).mapToLong(Response::partySize).sum();
            return new HostFunction(f, new Counts(yes, maybe, responses.size() - yes - maybe, guests), responses);
        }).toList();
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(out);
    }

    @PostMapping("/api/v1/events/{id}/functions")
    @Transactional
    public Function create(@PathVariable UUID id, @RequestBody Input in) {
        access.require(id, EventAccess.Capability.FUNCTIONS, MODULE);
        db.queryForList("SELECT id FROM events WHERE id=? FOR UPDATE", id);
        Integer count = db.queryForObject("SELECT count(*) FROM event_functions WHERE event_id=?", Integer.class, id);
        if (count != null && count >= MAX_FUNCTIONS) throw bad("A celebration can have up to " + MAX_FUNCTIONS + " functions");
        Clean c = clean(in);
        UUID fid = UUID.randomUUID();
        db.update("""
            INSERT INTO event_functions(id,event_id,name,function_date,start_time,end_time,venue_name,venue_address,map_url,dress_code,notes,rsvp_enabled,sort_order)
            VALUES(?,?,?,?,?,?,?,?,?,?,?,?,(SELECT COALESCE(MAX(sort_order),-1)+1 FROM event_functions WHERE event_id=?))""",
            fid, id, c.name, in.functionDate(), in.startTime(), in.endTime(), c.venue, c.address, c.map, c.dress, c.notes, c.rsvp, id);
        return one(id, fid);
    }

    @PutMapping("/api/v1/events/{id}/functions/{functionId}")
    public Function update(@PathVariable UUID id, @PathVariable UUID functionId, @RequestBody Input in) {
        access.require(id, EventAccess.Capability.FUNCTIONS, MODULE);
        Clean c = clean(in);
        if (in.version() == null) throw conflict();
        int n = db.update("""
            UPDATE event_functions SET name=?,function_date=?,start_time=?,end_time=?,venue_name=?,venue_address=?,map_url=?,dress_code=?,notes=?,
              rsvp_enabled=?,version=version+1,updated_at=now() WHERE id=? AND event_id=? AND version=?""",
            c.name, in.functionDate(), in.startTime(), in.endTime(), c.venue, c.address, c.map, c.dress, c.notes, c.rsvp, functionId, id, in.version());
        if (n == 0) throw exists(id, functionId) ? conflict() : new ResponseStatusException(HttpStatus.NOT_FOUND);
        return one(id, functionId);
    }

    @PostMapping("/api/v1/events/{id}/functions/{functionId}/delete")
    public Map<String, Object> delete(@PathVariable UUID id, @PathVariable UUID functionId) {
        access.require(id, EventAccess.Capability.FUNCTIONS, MODULE);
        if (db.update("DELETE FROM event_functions WHERE id=? AND event_id=?", functionId, id) == 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return Map.of("deleted", true);
    }

    /** Saves the order shown to guests. Must list every function of the event exactly once. */
    @PostMapping("/api/v1/events/{id}/functions/order")
    @Transactional
    public List<Function> reorder(@PathVariable UUID id, @RequestBody List<UUID> order) {
        access.require(id, EventAccess.Capability.FUNCTIONS, MODULE);
        Set<UUID> existing = new HashSet<>(db.queryForList("SELECT id FROM event_functions WHERE event_id=? FOR UPDATE", UUID.class, id));
        if (order == null || order.size() != existing.size() || !existing.equals(new HashSet<>(order))) throw conflict();
        for (int i = 0; i < order.size(); i++) db.update("UPDATE event_functions SET sort_order=? WHERE id=?", i, order.get(i));
        return functions(id);
    }

    // ---- Guests ----

    @GetMapping("/api/public/celebrations/{token}/functions")
    public ResponseEntity<List<Function>> publicList(@PathVariable UUID token) {
        Event e = published(token);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(functions(e.getId()));
    }

    @PostMapping("/api/public/celebrations/{token}/functions/rsvp")
    @Transactional
    public Map<String, Object> rsvp(@PathVariable UUID token, @RequestBody GuestRsvp in, HttpServletRequest http) {
        Event e = published(token);
        if (in == null || in.answers() == null || in.answers().isEmpty()) throw bad("Choose an answer for at least one function");
        String name = in.guestName() == null ? "" : in.guestName().strip();
        if (name.isEmpty() || name.length() > 120 || name.chars().anyMatch(Character::isISOControl)) throw bad("Enter your name");
        String email = in.guestEmail() == null ? "" : in.guestEmail().strip().toLowerCase(Locale.ROOT);
        if (email.length() > 254 || !email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) throw bad("Enter a valid email so you can change your answer later");
        limit(e.getId(), http);
        Set<UUID> open = new HashSet<>(db.queryForList("SELECT id FROM event_functions WHERE event_id=? AND rsvp_enabled", UUID.class, e.getId()));
        Set<UUID> seen = new HashSet<>();
        for (GuestAnswer a : in.answers()) {
            if (a == null || a.functionId() == null || !open.contains(a.functionId()) || !seen.add(a.functionId())) throw bad("One of the functions is no longer taking RSVPs. Refresh and try again.");
            String att = a.attendance() == null ? "" : a.attendance().toUpperCase(Locale.ROOT);
            if (!Set.of("YES", "NO", "MAYBE").contains(att)) throw bad("Choose yes, maybe or no");
            int party = att.equals("NO") ? 0 : a.partySize() == null ? 1 : a.partySize();
            if (party < (att.equals("NO") ? 0 : 1) || party > 10) throw bad("Party size must be between 1 and 10");
            db.update("""
                INSERT INTO event_function_rsvps(id,function_id,guest_name,guest_email,attendance,party_size) VALUES(?,?,?,?,?,?)
                ON CONFLICT(function_id,guest_email) DO UPDATE SET guest_name=EXCLUDED.guest_name,attendance=EXCLUDED.attendance,
                  party_size=EXCLUDED.party_size,updated_at=now()""", UUID.randomUUID(), a.functionId(), name, email, att, party);
        }
        return Map.of("message", "Thank you! Your answers are saved. Use the same email to change them.");
    }

    // ---- Helpers ----

    private record Clean(String name, String venue, String address, String map, String dress, String notes, boolean rsvp) {}
    private Clean clean(Input in) {
        if (in == null) throw bad("Function details are required");
        String name = text(in.name(), 80, "Function name");
        if (name == null) throw bad("Name the function, e.g. Mehendi or Reception");
        if (in.startTime() != null && in.endTime() != null && in.functionDate() != null && !in.endTime().isAfter(in.startTime()))
            throw bad("End time must be after the start time");
        if (in.endTime() != null && in.startTime() == null) throw bad("Add a start time");
        String map = text(in.mapUrl(), 500, "Map link");
        if (map != null && !map.matches("https://[^\\s<>\"]+")) throw bad("Map link must start with https://");
        return new Clean(name, text(in.venueName(), 120, "Venue"), text(in.venueAddress(), 300, "Address"), map,
            text(in.dressCode(), 120, "Dress code"), text(in.notes(), 600, "Notes"), in.rsvpEnabled() == null || in.rsvpEnabled());
    }
    private List<Function> functions(UUID eventId) {
        return db.query("SELECT * FROM event_functions WHERE event_id=? ORDER BY sort_order, function_date NULLS LAST, start_time NULLS LAST", (rs, i) -> fn(rs), eventId);
    }
    private Function one(UUID eventId, UUID id) {
        return db.query("SELECT * FROM event_functions WHERE id=? AND event_id=?", (rs, i) -> fn(rs), id, eventId).stream().findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
    private static Function fn(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new Function(rs.getObject("id", UUID.class), rs.getString("name"), rs.getObject("function_date", LocalDate.class),
            rs.getObject("start_time", LocalTime.class), rs.getObject("end_time", LocalTime.class), rs.getString("venue_name"), rs.getString("venue_address"),
            rs.getString("map_url"), rs.getString("dress_code"), rs.getString("notes"), rs.getBoolean("rsvp_enabled"), rs.getInt("sort_order"), rs.getLong("version"));
    }
    private boolean exists(UUID eventId, UUID id) {
        return Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM event_functions WHERE id=? AND event_id=?)", Boolean.class, id, eventId));
    }
    private Event published(UUID token) {
        Event e = events.findByPublicToken(token).filter(x -> x.getStatus() == EventStatus.PUBLISHED).orElseThrow(EventAccess::notFound);
        if (!permissions.eventEnabled(e.getOwnerId(), e.getId(), MODULE)) throw EventAccess.notFound();
        return e;
    }
    private void limit(UUID eventId, HttpServletRequest request) {
        String fp;
        try { fp = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(request.getRemoteAddr().getBytes(StandardCharsets.UTF_8))); }
        catch (Exception ex) { throw new IllegalStateException(ex); }
        int updated = db.update("""
            INSERT INTO event_guest_submission_limits(event_id,fingerprint,submission_date,submission_type,count) VALUES (?,?,?,'FN_RSVP',1)
            ON CONFLICT(event_id,fingerprint,submission_date,submission_type) DO UPDATE SET count=event_guest_submission_limits.count+1
            WHERE event_guest_submission_limits.count < 30""", eventId, fp, LocalDate.now(ZoneOffset.UTC));
        if (updated == 0) throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Please try again later");
    }
    private static String text(String value, int max, String label) {
        if (value == null || value.isBlank()) return null;
        String v = value.strip();
        if (v.length() > max) throw bad(label + " must be at most " + max + " characters");
        if (v.chars().anyMatch(c -> Character.isISOControl(c) && c != '\n')) throw bad(label + " contains unsupported characters");
        return v;
    }
    private static ResponseStatusException bad(String m) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, m); }
    private static ResponseStatusException conflict() { return new ResponseStatusException(HttpStatus.CONFLICT, "The schedule changed in another tab. Reload and try again."); }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> reason(ResponseStatusException ex) {
        int code = ex.getStatusCode().value();
        if ((code == 400 || code == 409 || code == 429) && ex.getReason() != null) return ResponseEntity.status(code).body(Map.of("error", ex.getReason()));
        return ResponseEntity.status(ex.getStatusCode()).build();
    }
}
