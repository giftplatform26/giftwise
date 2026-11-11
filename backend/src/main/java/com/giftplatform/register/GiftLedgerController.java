package com.giftplatform.register;

import com.giftplatform.admin.EffectiveModulePermissionService;
import com.giftplatform.security.ActorContext;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

import static com.giftplatform.register.GiftPeople.*;

/**
 * Personal gift ledger: what this user gave at other people's celebrations (entered by them) and what they
 * received at their own (from their shagun registers and confirmed online gifts). Private to the user.
 */
@RestController
@RequestMapping("/api/v1/me/ledger")
public class GiftLedgerController {
    public static final String MODULE = "GIFT_LEDGER";

    private final JdbcTemplate db;
    private final ActorContext actor;
    private final EffectiveModulePermissionService permissions;

    public GiftLedgerController(JdbcTemplate db, ActorContext actor, EffectiveModulePermissionService permissions) {
        this.db = db; this.actor = actor; this.permissions = permissions;
    }

    public record Input(String recipientName, String recipientPhone, String occasion, LocalDate occasionDate, String kind,
                        Long amountPaise, String itemDescription, String note, Long version) {}
    public record Given(UUID id, String recipientName, String recipientPhone, String occasion, LocalDate occasionDate, String kind,
                        Long amountPaise, String itemDescription, String note, long version) {}
    public record Received(UUID id, String giverName, String kind, Long amountPaise, String itemDescription, String eventTitle,
                           LocalDate eventDate, OffsetDateTime createdAt) {}
    public record Person(String key, String name, long givenPaise, int givenCount, long receivedPaise, int receivedCount,
                         LocalDate lastGiven, LocalDate lastReceived) {}
    public record Ledger(List<Given> given, long givenPaise, long receivedPaise, List<Person> people) {}
    public record History(String name, List<Given> given, List<Received> received, long givenPaise, long receivedPaise) {}

    private static final String RECEIVED = """
        SELECT s.id, s.giver_name, s.giver_key, s.kind, s.amount_paise, s.item_description, e.title, e.event_date, s.created_at
        FROM event_shagun_entries s JOIN events e ON e.id=s.event_id WHERE e.owner_id=? AND s.deleted_at IS NULL
        UNION ALL
        SELECT o.id, o.giver_name, NULL, o.kind, o.amount_paise, NULL, e.title, e.event_date, o.created_at
        FROM (""" + ShagunRegisterController.CONFIRMED_ONLINE + ") o JOIN events e ON e.id=o.event_id WHERE e.owner_id=?";

    @GetMapping
    public ResponseEntity<Ledger> ledger() {
        UUID me = me();
        List<Given> given = given(me, null);
        List<Received> received = received(me);
        Map<String, Person> people = new LinkedHashMap<>();
        for (Given g : given) merge(people, g.recipientName(), g.amountPaise(), true, g.occasionDate());
        for (Received r : received) merge(people, r.giverName(), r.amountPaise(), false,
            r.eventDate() != null ? r.eventDate() : r.createdAt().toLocalDate());
        List<Person> sorted = people.values().stream()
            .sorted(Comparator.comparingLong((Person p) -> p.givenPaise() + p.receivedPaise()).reversed().thenComparing(Person::name)).toList();
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new Ledger(given,
            given.stream().mapToLong(g -> g.amountPaise() == null ? 0 : g.amountPaise()).sum(),
            received.stream().mapToLong(r -> r.amountPaise() == null ? 0 : r.amountPaise()).sum(), sorted));
    }

    /** Everything exchanged with one person, matched by name (case, spacing and punctuation ignored). */
    @GetMapping("/person")
    public ResponseEntity<History> person(@RequestParam String name) {
        UUID me = me();
        String k = key(name);
        if (k.isEmpty()) throw bad("Enter a name");
        List<Given> given = given(me, k);
        List<Received> received = received(me).stream().filter(r -> key(r.giverName()).equals(k)).toList();
        String display = !given.isEmpty() ? given.getFirst().recipientName() : !received.isEmpty() ? received.getFirst().giverName() : name.strip();
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new History(display, given, received,
            given.stream().mapToLong(g -> g.amountPaise() == null ? 0 : g.amountPaise()).sum(),
            received.stream().mapToLong(r -> r.amountPaise() == null ? 0 : r.amountPaise()).sum()));
    }

    @PostMapping("/given")
    public Given add(@RequestBody Input in) {
        UUID me = me();
        Clean c = clean(in);
        UUID id = UUID.randomUUID();
        db.update("""
            INSERT INTO user_gifts_given(id,user_id,recipient_name,recipient_key,recipient_phone,occasion,occasion_date,kind,amount_paise,item_description,note)
            VALUES(?,?,?,?,?,?,?,?,?,?,?)""", id, me, c.name, key(c.name), c.phone, c.occasion, c.date, c.kind, c.amount, c.item, c.note);
        return one(me, id);
    }

    @PutMapping("/given/{entryId}")
    @Transactional
    public Given update(@PathVariable UUID entryId, @RequestBody Input in) {
        UUID me = me();
        Clean c = clean(in);
        if (in.version() == null) throw conflict();
        int n = db.update("""
            UPDATE user_gifts_given SET recipient_name=?,recipient_key=?,recipient_phone=?,occasion=?,occasion_date=?,kind=?,amount_paise=?,
              item_description=?,note=?,version=version+1,updated_at=now() WHERE id=? AND user_id=? AND deleted_at IS NULL AND version=?""",
            c.name, key(c.name), c.phone, c.occasion, c.date, c.kind, c.amount, c.item, c.note, entryId, me, in.version());
        if (n == 0) {
            boolean exists = Boolean.TRUE.equals(db.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM user_gifts_given WHERE id=? AND user_id=? AND deleted_at IS NULL)", Boolean.class, entryId, me));
            throw exists ? conflict() : new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return one(me, entryId);
    }

    /**
     * A guest saves a gift receipt into their own ledger. The receipt link is the proof; one ledger entry per receipt,
     * so pressing the button twice is harmless.
     */
    @PostMapping("/from-receipt/{receiptToken}")
    @Transactional
    public Given fromReceipt(@PathVariable UUID receiptToken) {
        UUID me = me();
        var rows = db.queryForList("""
            SELECT c.amount_paise, c.created_at, e.title, e.event_date, COALESCE(NULLIF(e.host_display_name,''), e.title) AS host
            FROM guest_direct_transfer_claims c JOIN events e ON e.id=c.event_id WHERE c.receipt_token=?""", receiptToken);
        if (rows.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Receipt not found");
        var r = rows.getFirst();
        String host = (String) r.get("host"), title = (String) r.get("title");
        LocalDate date = r.get("event_date") != null ? ((java.sql.Date) r.get("event_date")).toLocalDate()
            : ((java.sql.Timestamp) r.get("created_at")).toLocalDateTime().toLocalDate();
        String recipient = host.length() > 120 ? host.substring(0, 120) : host;
        String occasion = title.length() > 80 ? title.substring(0, 80) : title;
        UUID id = db.queryForObject("""
            INSERT INTO user_gifts_given(id,user_id,recipient_name,recipient_key,occasion,occasion_date,kind,amount_paise,note,source_ref)
            VALUES(?,?,?,?,?,?,'UPI',?,?,?) ON CONFLICT (user_id,source_ref) WHERE source_ref IS NOT NULL DO UPDATE SET deleted_at=NULL RETURNING id""",
            UUID.class, UUID.randomUUID(), me, recipient, key(recipient), occasion, date, ((Number) r.get("amount_paise")).longValue(),
            "Saved from your GiftWise receipt", "receipt:" + receiptToken);
        return one(me, id);
    }

    @PostMapping("/given/{entryId}/delete")
    public Map<String, Object> delete(@PathVariable UUID entryId) {
        UUID me = me();
        if (db.update("UPDATE user_gifts_given SET deleted_at=now(),version=version+1 WHERE id=? AND user_id=? AND deleted_at IS NULL", entryId, me) == 0)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return Map.of("deleted", true);
    }

    @PostMapping("/given/{entryId}/restore")
    public Given restore(@PathVariable UUID entryId) {
        UUID me = me();
        if (db.update("UPDATE user_gifts_given SET deleted_at=NULL,version=version+1 WHERE id=? AND user_id=? AND deleted_at IS NOT NULL", entryId, me) == 0)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return one(me, entryId);
    }

    // ---- Helpers ----

    private record Clean(String name, String phone, String occasion, LocalDate date, String kind, Long amount, String item, String note) {}
    private Clean clean(Input in) {
        if (in == null) throw bad("Gift details are required");
        if (in.occasionDate() != null && (in.occasionDate().isAfter(LocalDate.now().plusYears(2)) || in.occasionDate().getYear() < 1950))
            throw bad("Check the occasion date");
        String item = text(in.itemDescription(), 200, "Gift item");
        return new Clean(name(in.recipientName(), "Recipient's name"), phone(in.recipientPhone()), text(in.occasion(), 80, "Occasion"),
            in.occasionDate(), kind(in.kind()), amount(in.amountPaise(), item), item, text(in.note(), 300, "Note"));
    }
    private static void merge(Map<String, Person> people, String name, Long paise, boolean given, LocalDate when) {
        String k = key(name);
        if (k.isEmpty()) return;
        long amount = paise == null ? 0 : paise;
        Person p = people.get(k);
        if (p == null) p = new Person(k, name, 0, 0, 0, 0, null, null);
        people.put(k, given
            ? new Person(k, p.name(), p.givenPaise() + amount, p.givenCount() + 1, p.receivedPaise(), p.receivedCount(), later(p.lastGiven(), when), p.lastReceived())
            : new Person(k, p.name(), p.givenPaise(), p.givenCount(), p.receivedPaise() + amount, p.receivedCount() + 1, p.lastGiven(), later(p.lastReceived(), when)));
    }
    private static LocalDate later(LocalDate a, LocalDate b) { return a == null ? b : b == null ? a : a.isAfter(b) ? a : b; }

    private List<Given> given(UUID me, String recipientKey) {
        String where = recipientKey == null ? "" : " AND recipient_key=?";
        Object[] args = recipientKey == null ? new Object[]{me} : new Object[]{me, recipientKey};
        return db.query("SELECT * FROM user_gifts_given WHERE user_id=? AND deleted_at IS NULL" + where +
            " ORDER BY occasion_date DESC NULLS LAST, created_at DESC LIMIT 2000", (rs, i) -> given(rs), args);
    }
    private List<Received> received(UUID me) {
        return db.query("SELECT * FROM (" + RECEIVED + ") r ORDER BY created_at DESC LIMIT 5000", (rs, i) -> new Received(
            rs.getObject("id", UUID.class), rs.getString("giver_name"), rs.getString("kind"), (Long) rs.getObject("amount_paise"),
            rs.getString("item_description"), rs.getString("title"), rs.getObject("event_date", LocalDate.class),
            rs.getObject("created_at", OffsetDateTime.class)), me, me);
    }
    private Given one(UUID me, UUID id) {
        return db.query("SELECT * FROM user_gifts_given WHERE id=? AND user_id=?", (rs, i) -> given(rs), id, me)
            .stream().findFirst().orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
    private static Given given(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new Given(rs.getObject("id", UUID.class), rs.getString("recipient_name"), rs.getString("recipient_phone"), rs.getString("occasion"),
            rs.getObject("occasion_date", LocalDate.class), rs.getString("kind"), (Long) rs.getObject("amount_paise"),
            rs.getString("item_description"), rs.getString("note"), rs.getLong("version"));
    }
    private UUID me() {
        UUID me = actor.requireAuthenticatedUserId();
        permissions.require(me, MODULE);
        return me;
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> reason(ResponseStatusException ex) {
        int code = ex.getStatusCode().value();
        if ((code == 400 || code == 409) && ex.getReason() != null) return ResponseEntity.status(code).body(Map.of("error", ex.getReason()));
        return ResponseEntity.status(ex.getStatusCode()).build();
    }
}
