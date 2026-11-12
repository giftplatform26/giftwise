package com.giftplatform.eventops;

import com.giftplatform.admin.EffectiveModulePermissionService;
import com.giftplatform.cohost.EventAccess;
import com.giftplatform.cohost.EventAccess.Capability;
import com.giftplatform.event.entity.Event;
import com.giftplatform.event.entity.EventStatus;
import com.giftplatform.event.repository.EventRepository;
import com.giftplatform.register.GiftPeople;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * Personal guest passes: one QR (and a 6-letter code) per family. At the door, scanning the pass or typing the
 * code checks the family in with their party size, and shows their table if seating is on.
 */
@RestController
public class GuestPassesController extends EventOpsSupport {
    public static final String MODULE = "GUEST_PASSES";
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final JdbcTemplate db;
    private final EventAccess access;
    private final EventRepository events;
    private final EffectiveModulePermissionService permissions;
    private final com.giftplatform.qr.QrRenderer qr;
    private final String guestBaseUrl;

    public GuestPassesController(JdbcTemplate db, EventAccess access, EventRepository events, EffectiveModulePermissionService permissions,
                                 com.giftplatform.qr.QrRenderer qr, @org.springframework.beans.factory.annotation.Value("${gift.guest-base-url:http://localhost:3000}") String guestBaseUrl) {
        this.db = db; this.access = access; this.events = events; this.permissions = permissions; this.qr = qr; this.guestBaseUrl = guestBaseUrl.replaceAll("/+$", "");
    }

    /** QR of the pass link, scanned at the door. */
    @GetMapping(value = "/api/public/passes/{token}/qr.png", produces = org.springframework.http.MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> passQr(@PathVariable UUID token) {
        pass(token);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(qr.png(guestBaseUrl + "/pass/" + token, 420));
    }

    public record Pass(UUID id, String guestName, int partySize, UUID token, String shortCode, String phone, boolean revoked, boolean arrived) {}
    public record Passes(List<Pass> passes, int withoutPass) {}
    public record Generate(String guestName, Integer partySize) {}
    public record Scan(String code) {}
    public record ScanResult(String guestName, int partySize, String table, boolean alreadyArrived, OffsetDateTime arrivedAt) {}
    public record PublicPass(String eventTitle, LocalDate eventDate, String hostName, String guestName, int partySize, String shortCode, String table, String invitationPath) {}

    @GetMapping("/api/v1/events/{id}/passes")
    public ResponseEntity<Passes> list(@PathVariable UUID id) {
        access.require(id, Capability.GUESTS, MODULE);
        List<Pass> passes = db.query("""
            SELECT p.*, (SELECT phone FROM event_guest_contacts c WHERE c.event_id=p.event_id AND lower(c.name)=lower(p.guest_name) LIMIT 1) AS phone,
                   EXISTS(SELECT 1 FROM event_checkins k WHERE k.event_id=p.event_id AND k.guest_key=p.guest_key) AS arrived
            FROM event_guest_passes p WHERE p.event_id=? ORDER BY p.guest_name""", (rs, i) -> new Pass(rs.getObject("id", UUID.class), rs.getString("guest_name"),
            rs.getInt("party_size"), rs.getObject("pass_token", UUID.class), rs.getString("short_code"), rs.getString("phone"), rs.getObject("revoked_at") != null, rs.getBoolean("arrived")), id);
        Set<String> have = new HashSet<>(db.queryForList("SELECT guest_key FROM event_guest_passes WHERE event_id=?", String.class, id));
        int without = (int) ExpectedGuests.of(db, id).keySet().stream().filter(k -> !have.contains(k)).count();
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new Passes(passes, without));
    }

    /** Creates passes for every expected family that doesn't have one yet (or one named guest). */
    @PostMapping("/api/v1/events/{id}/passes/generate")
    @Transactional
    public Map<String, Object> generate(@PathVariable UUID id, @RequestBody(required = false) Generate in) {
        access.require(id, Capability.GUESTS, MODULE);
        db.queryForList("SELECT id FROM events WHERE id=? FOR UPDATE", id);
        List<ExpectedGuests.Guest> targets;
        if (in != null && in.guestName() != null && !in.guestName().isBlank()) {
            String name = required(in.guestName(), 120, "Guest name");
            int party = in.partySize() == null ? 1 : in.partySize();
            if (party < 1 || party > 30) throw bad("Party size must be between 1 and 30");
            targets = List.of(new ExpectedGuests.Guest(GiftPeople.key(name), name, party, "MANUAL"));
        } else targets = new ArrayList<>(ExpectedGuests.of(db, id).values());
        Integer count = db.queryForObject("SELECT count(*) FROM event_guest_passes WHERE event_id=?", Integer.class, id);
        if (count != null && count + targets.size() > 3000) throw bad("A celebration can have up to 3,000 passes");
        Set<String> codes = new HashSet<>(db.queryForList("SELECT short_code FROM event_guest_passes WHERE event_id=?", String.class, id));
        int created = 0;
        for (var g : targets) {
            if (g.key().isEmpty()) continue;
            String code;
            do { code = randomCode(); } while (!codes.add(code));
            created += db.update("""
                INSERT INTO event_guest_passes(id,event_id,guest_name,guest_key,party_size,short_code) VALUES(?,?,?,?,?,?)
                ON CONFLICT (event_id,guest_key) DO NOTHING""", UUID.randomUUID(), id, g.name(), g.key(), g.party(), code);
        }
        return Map.of("created", created);
    }

    @PostMapping("/api/v1/events/{id}/passes/{passId}/revoke")
    public Map<String, Object> revoke(@PathVariable UUID id, @PathVariable UUID passId) {
        access.require(id, Capability.GUESTS, MODULE);
        if (db.update("UPDATE event_guest_passes SET revoked_at=now() WHERE id=? AND event_id=? AND revoked_at IS NULL", passId, id) == 0) throw notFound();
        return Map.of("revoked", true);
    }

    /** Door scan: a pass link/token or the 6-letter code. Checks the family in (idempotent) and returns their table. */
    @PostMapping("/api/v1/events/{id}/checkin/pass")
    @Transactional
    public ScanResult scan(@PathVariable UUID id, @RequestBody Scan in) {
        var a = access.require(id, Capability.CHECKIN, MODULE);
        String raw = in == null || in.code() == null ? "" : in.code().strip();
        var m = java.util.regex.Pattern.compile("([0-9a-fA-F-]{36})").matcher(raw);
        var rows = m.find()
            ? db.queryForList("SELECT * FROM event_guest_passes WHERE event_id=? AND pass_token=?::uuid", id, m.group(1))
            : db.queryForList("SELECT * FROM event_guest_passes WHERE event_id=? AND short_code=?", id, raw.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", ""));
        if (rows.isEmpty()) throw bad("No pass found for this celebration. Check the code, or search by name.");
        var p = rows.getFirst();
        if (p.get("revoked_at") != null) throw conflict("This pass was cancelled by the host.");
        String key = (String) p.get("guest_key");
        var existing = db.query("SELECT checked_in_at FROM event_checkins WHERE event_id=? AND guest_key=?", (rs, i) -> rs.getObject(1, OffsetDateTime.class), id, key);
        if (existing.isEmpty()) {
            if (!permissions.eventEnabled(a.event().getOwnerId(), id, "GUEST_CHECKIN")) throw notFound();
            db.update("INSERT INTO event_checkins(id,event_id,guest_name,guest_key,party_size,checked_in_by) VALUES(?,?,?,?,?,?) ON CONFLICT (event_id,guest_key) DO NOTHING",
                UUID.randomUUID(), id, p.get("guest_name"), key, p.get("party_size"), a.userId());
            db.update("INSERT INTO acquisition_events(id,kind,user_id,event_id) VALUES(?, 'PASS_SCANNED', ?, ?)", UUID.randomUUID(), a.userId(), id);
        }
        return new ScanResult((String) p.get("guest_name"), ((Number) p.get("party_size")).intValue(), table(id, key), !existing.isEmpty(), existing.isEmpty() ? null : existing.getFirst());
    }

    /** The guest's own pass page. */
    @GetMapping("/api/public/passes/{token}")
    public ResponseEntity<PublicPass> pass(@PathVariable UUID token) {
        var rows = db.queryForList("SELECT * FROM event_guest_passes WHERE pass_token=? AND revoked_at IS NULL", token);
        if (rows.isEmpty()) throw notFound();
        var p = rows.getFirst();
        Event e = events.findById((UUID) p.get("event_id")).filter(x -> x.getStatus() == EventStatus.PUBLISHED).orElseThrow(EventOpsSupport::notFound);
        if (!permissions.eventEnabled(e.getOwnerId(), e.getId(), MODULE)) throw notFound();
        String table = permissions.eventEnabled(e.getOwnerId(), e.getId(), SeatingController.MODULE) ? table(e.getId(), (String) p.get("guest_key")) : null;
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).header("X-Robots-Tag", "noindex").body(new PublicPass(e.getTitle(), e.getEventDate(), e.getHostDisplayName(),
            (String) p.get("guest_name"), ((Number) p.get("party_size")).intValue(), ((String) p.get("short_code")).trim(), table, "/e/" + e.getPublicToken()));
    }

    private String table(UUID eventId, String key) {
        return db.query("SELECT t.name FROM event_seat_assignments a JOIN event_seating_tables t ON t.id=a.table_id WHERE a.event_id=? AND a.guest_key=?",
            (rs, i) -> rs.getString(1), eventId, key).stream().findFirst().orElse(null);
    }
    private static String randomCode() {
        StringBuilder b = new StringBuilder(6);
        for (int i = 0; i < 6; i++) b.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        return b.toString();
    }
}
