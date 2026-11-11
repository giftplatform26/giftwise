package com.giftplatform.register;

import com.giftplatform.cohost.EventAccess;
import com.giftplatform.cohost.EventAccess.Capability;
import com.giftplatform.event.entity.Event;
import com.giftplatform.security.ActorContext;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.*;

import static com.giftplatform.register.GiftPeople.*;

/**
 * Shagun register: the host's record of gifts handed over in person (cash, envelopes, items), shown alongside
 * confirmed online gifts. Entries are the host's own notes; they never post to the payment ledger or wallet.
 */
@RestController
@RequestMapping("/api/v1/events/{id}/register")
public class ShagunRegisterController {
    public static final String MODULE = "SHAGUN_REGISTER";

    /** Confirmed online gifts only: verified provider captures and direct transfers the host confirmed. */
    static final String CONFIRMED_ONLINE = """
        SELECT c.id, c.event_id, c.guest_name AS giver_name, c.amount_paise, 'ONLINE'::text AS kind, c.created_at
        FROM contributions c JOIN payment_orders po ON po.id=c.payment_order_id
        WHERE po.provider='RAZORPAY' AND po.status='CAPTURED' AND EXISTS (
          SELECT 1 FROM payment_transactions pt WHERE pt.payment_order_id=po.id AND pt.status='CAPTURED' AND pt.verified_at IS NOT NULL)
        UNION ALL
        SELECT cl.id, cl.event_id, cl.guest_name, cl.amount_paise, cl.method::text, cl.created_at
        FROM guest_direct_transfer_claims cl WHERE cl.status='HOST_CONFIRMED'
        """;

    private final JdbcTemplate db;
    private final ActorContext actor;
    private final EventAccess access;
    private final com.giftplatform.admin.EffectiveModulePermissionService permissions;

    public ShagunRegisterController(JdbcTemplate db, ActorContext actor, EventAccess access, com.giftplatform.admin.EffectiveModulePermissionService permissions) {
        this.db = db; this.actor = actor; this.access = access; this.permissions = permissions;
    }

    public record Input(String giverName, String giverPhone, String side, String kind, Long amountPaise, String itemDescription,
                        String note, String clientKey, Long version) {}
    public record Entry(UUID id, String giverName, String giverPhone, String side, String kind, Long amountPaise, String itemDescription,
                        String note, OffsetDateTime createdAt, long version, OffsetDateTime returnGiftAt) {}
    public record OnlineGift(UUID id, String giverName, long amountPaise, String kind, OffsetDateTime createdAt) {}
    public record SideTotal(String side, long amountPaise, int entries) {}
    public record Totals(long inPersonPaise, long onlinePaise, int itemGifts, int givers) {}
    public record Register(List<Entry> entries, List<OnlineGift> online, Totals totals, List<SideTotal> sides,
                           List<String> sideSuggestions, List<String> nameSuggestions, boolean canManage, boolean returnGifts, int returnGiftsGiven) {}

    @GetMapping
    public ResponseEntity<Register> list(@PathVariable UUID id) {
        var acc = owned(id, Capability.REGISTER_ADD);
        boolean canManage = acc.can(Capability.REGISTER_MANAGE);
        boolean returnGifts = permissions.eventEnabled(acc.event().getOwnerId(), id, "RETURN_GIFTS");
        List<Entry> entries = db.query("SELECT * FROM event_shagun_entries WHERE event_id=? AND deleted_at IS NULL ORDER BY created_at DESC",
            (rs, i) -> new Entry(rs.getObject("id", UUID.class), rs.getString("giver_name"), rs.getString("giver_phone"), rs.getString("side"),
                rs.getString("kind"), (Long) rs.getObject("amount_paise"), rs.getString("item_description"), rs.getString("note"),
                rs.getObject("created_at", OffsetDateTime.class), rs.getLong("version"), rs.getObject("return_gift_at", OffsetDateTime.class)), id);
        List<OnlineGift> online = db.query("SELECT * FROM (" + CONFIRMED_ONLINE + ") o WHERE o.event_id=? ORDER BY created_at DESC",
            (rs, i) -> new OnlineGift(rs.getObject("id", UUID.class), rs.getString("giver_name"), rs.getLong("amount_paise"),
                rs.getString("kind"), rs.getObject("created_at", OffsetDateTime.class)), id);
        long inPerson = entries.stream().mapToLong(e -> e.amountPaise() == null ? 0 : e.amountPaise()).sum();
        long onlineSum = online.stream().mapToLong(OnlineGift::amountPaise).sum();
        int items = (int) entries.stream().filter(e -> e.itemDescription() != null).count();
        Set<String> givers = new HashSet<>();
        entries.forEach(e -> givers.add(key(e.giverName())));
        online.forEach(o -> givers.add(key(o.giverName())));
        Map<String, long[]> bySide = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (Entry e : entries) {
            long[] t = bySide.computeIfAbsent(e.side() == null ? "" : e.side(), k -> new long[2]);
            t[0] += e.amountPaise() == null ? 0 : e.amountPaise(); t[1]++;
        }
        List<SideTotal> sides = bySide.entrySet().stream().map(s -> new SideTotal(s.getKey().isEmpty() ? null : s.getKey(), s.getValue()[0], (int) s.getValue()[1])).toList();
        List<String> sideSuggestions = bySide.keySet().stream().filter(s -> !s.isEmpty()).toList();
        // Names the host already knows: RSVPs and guestbook signers, to speed up entry at the venue.
        List<String> names = db.queryForList("""
            SELECT DISTINCT guest_name FROM (SELECT guest_name FROM event_rsvps WHERE event_id=?
              UNION SELECT guest_name FROM event_guestbook WHERE event_id=?) n ORDER BY guest_name LIMIT 500""", String.class, id, id);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
            .body(new Register(entries, online, new Totals(inPerson, onlineSum, items, givers.size()), sides, sideSuggestions, names, canManage, returnGifts,
                (int) entries.stream().filter(e -> e.returnGiftAt() != null).count()));
    }

    @PostMapping
    @Transactional
    public Entry create(@PathVariable UUID id, @RequestBody Input in) {
        owned(id, Capability.REGISTER_ADD);
        Clean c = clean(in);
        String clientKey = in.clientKey() == null ? "" : in.clientKey().strip();
        if (!clientKey.matches("[A-Za-z0-9-]{8,64}")) throw bad("Reload the page and try again");
        // Same entry submitted twice (double tap or flaky venue Wi-Fi) returns the original instead of duplicating.
        UUID entryId = db.queryForObject("""
            INSERT INTO event_shagun_entries(id,event_id,giver_name,giver_key,giver_phone,side,kind,amount_paise,item_description,note,client_key,recorded_by)
            VALUES(?,?,?,?,?,?,?,?,?,?,?,?) ON CONFLICT (event_id,client_key) DO UPDATE SET client_key=EXCLUDED.client_key RETURNING id""",
            UUID.class, UUID.randomUUID(), id, c.name, key(c.name), c.phone, c.side, c.kind, c.amount, c.item, c.note, clientKey,
            actor.requireAuthenticatedUserId());
        return entry(id, entryId);
    }

    @PutMapping("/{entryId}")
    @Transactional
    public Entry update(@PathVariable UUID id, @PathVariable UUID entryId, @RequestBody Input in) {
        owned(id, Capability.REGISTER_MANAGE);
        Clean c = clean(in);
        if (in.version() == null) throw conflict();
        int n = db.update("""
            UPDATE event_shagun_entries SET giver_name=?,giver_key=?,giver_phone=?,side=?,kind=?,amount_paise=?,item_description=?,note=?,
              version=version+1,updated_at=now() WHERE id=? AND event_id=? AND deleted_at IS NULL AND version=?""",
            c.name, key(c.name), c.phone, c.side, c.kind, c.amount, c.item, c.note, entryId, id, in.version());
        if (n == 0) throw exists(id, entryId) ? conflict() : new ResponseStatusException(HttpStatus.NOT_FOUND);
        return entry(id, entryId);
    }

    public record ReturnGift(boolean given) {}

    /** Tick off a return gift handed to this giver (gift desk can do it too). Needs RETURN_GIFTS for the event. */
    @PostMapping("/{entryId}/return-gift")
    public Entry returnGift(@PathVariable UUID id, @PathVariable UUID entryId, @RequestBody ReturnGift in) {
        var acc = owned(id, Capability.REGISTER_ADD);
        permissions.requireEvent(acc.event().getOwnerId(), id, "RETURN_GIFTS");
        String set = in != null && in.given() ? "COALESCE(return_gift_at,now())" : "NULL";
        if (db.update("UPDATE event_shagun_entries SET return_gift_at=" + set + " WHERE id=? AND event_id=? AND deleted_at IS NULL", entryId, id) == 0)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return entry(id, entryId);
    }

    @PostMapping("/{entryId}/delete")
    public Map<String, Object> delete(@PathVariable UUID id, @PathVariable UUID entryId) {
        owned(id, Capability.REGISTER_MANAGE);
        if (db.update("UPDATE event_shagun_entries SET deleted_at=now(),version=version+1 WHERE id=? AND event_id=? AND deleted_at IS NULL", entryId, id) == 0)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return Map.of("deleted", true);
    }

    @PostMapping("/{entryId}/restore")
    public Entry restore(@PathVariable UUID id, @PathVariable UUID entryId) {
        owned(id, Capability.REGISTER_MANAGE);
        if (db.update("UPDATE event_shagun_entries SET deleted_at=NULL,version=version+1 WHERE id=? AND event_id=? AND deleted_at IS NOT NULL", entryId, id) == 0)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return entry(id, entryId);
    }

    @GetMapping(value = "/export.csv", produces = "text/csv")
    public ResponseEntity<byte[]> export(@PathVariable UUID id) {
        Event e = owned(id, Capability.REGISTER_MANAGE).event();
        StringBuilder out = new StringBuilder("﻿Date,Giver,Phone,Side,Type,Amount (INR),Gift item,Note\n");
        db.query("""
            SELECT created_at,giver_name,giver_phone,side,kind,amount_paise,item_description,note FROM event_shagun_entries
            WHERE event_id=? AND deleted_at IS NULL
            UNION ALL SELECT created_at,giver_name,NULL,NULL,kind,amount_paise,NULL,'Confirmed online gift' FROM (""" + CONFIRMED_ONLINE + ") o WHERE o.event_id=? ORDER BY 1",
            rs -> {
                Long paise = (Long) rs.getObject("amount_paise");
                out.append(String.join(",", csv(rs.getObject("created_at", OffsetDateTime.class).toLocalDate()), csv(rs.getString("giver_name")),
                    csv(rs.getString("giver_phone")), csv(rs.getString("side")), csv(rs.getString("kind")),
                    csv(paise == null ? "" : String.format(Locale.ROOT, "%.2f", paise / 100.0)), csv(rs.getString("item_description")),
                    csv(rs.getString("note")))).append('\n');
            }, id, id);
        String file = "gift-register-" + e.getTitle().replaceAll("[^A-Za-z0-9]+", "-").replaceAll("(^-|-$)", "").toLowerCase(Locale.ROOT) + ".csv";
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
            .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(file.length() > 80 ? "gift-register.csv" : file).build().toString())
            .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8)).body(out.toString().getBytes(StandardCharsets.UTF_8));
    }

    // ---- Helpers ----

    private record Clean(String name, String phone, String side, String kind, Long amount, String item, String note) {}
    private Clean clean(Input in) {
        if (in == null) throw bad("Entry details are required");
        String item = text(in.itemDescription(), 200, "Gift item");
        return new Clean(name(in.giverName(), "Giver's name"), phone(in.giverPhone()), text(in.side(), 60, "Side"), kind(in.kind()),
            amount(in.amountPaise(), item), item, text(in.note(), 300, "Note"));
    }
    private boolean exists(UUID event, UUID entry) {
        return Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM event_shagun_entries WHERE id=? AND event_id=? AND deleted_at IS NULL)", Boolean.class, entry, event));
    }
    private Entry entry(UUID event, UUID entryId) {
        return db.query("SELECT * FROM event_shagun_entries WHERE id=? AND event_id=?", (rs, i) -> new Entry(rs.getObject("id", UUID.class),
                rs.getString("giver_name"), rs.getString("giver_phone"), rs.getString("side"), rs.getString("kind"), (Long) rs.getObject("amount_paise"),
                rs.getString("item_description"), rs.getString("note"), rs.getObject("created_at", OffsetDateTime.class), rs.getLong("version"),
                rs.getObject("return_gift_at", OffsetDateTime.class)), entryId, event)
            .stream().findFirst().orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
    private EventAccess.Access owned(UUID id, Capability capability) { return access.require(id, capability, MODULE); }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> reason(ResponseStatusException ex) {
        int code = ex.getStatusCode().value();
        if ((code == 400 || code == 409) && ex.getReason() != null) return ResponseEntity.status(code).body(Map.of("error", ex.getReason()));
        return ResponseEntity.status(ex.getStatusCode()).build();
    }
}
