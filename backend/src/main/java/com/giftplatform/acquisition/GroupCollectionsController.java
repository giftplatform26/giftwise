package com.giftplatform.acquisition;

import com.giftplatform.admin.EffectiveModulePermissionService;
import com.giftplatform.event.entity.Event;
import com.giftplatform.event.entity.EventStatus;
import com.giftplatform.event.entity.OccasionType;
import com.giftplatform.event.repository.EventRepository;
import com.giftplatform.event.service.EventManagementService;
import com.giftplatform.register.ShagunRegisterController;
import com.giftplatform.security.ActorContext;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

/**
 * Office and group gifts without a celebration (a colleague's farewell, a teacher's day gift). A collection is a
 * light event of kind COLLECTION with one gift and the organiser's receiving account, so it reuses direct
 * transfers, payment codes, receipts, host confirmation and wishes unchanged.
 */
@RestController
public class GroupCollectionsController {
    public static final String MODULE = "GROUP_COLLECTIONS";
    static final Map<String, OccasionType> OCCASIONS = Map.of("FAREWELL", OccasionType.FAREWELL, "BIRTHDAY", OccasionType.BIRTHDAY,
        "RETIREMENT", OccasionType.RETIREMENT, "WEDDING", OccasionType.WEDDING, "BABY_SHOWER", OccasionType.BABY_SHOWER, "OTHER", OccasionType.OTHER);

    private final JdbcTemplate db;
    private final EventRepository events;
    private final EventManagementService eventService;
    private final ActorContext actor;
    private final EffectiveModulePermissionService permissions;

    public GroupCollectionsController(JdbcTemplate db, EventRepository events, EventManagementService eventService, ActorContext actor, EffectiveModulePermissionService permissions) {
        this.db = db; this.events = events; this.eventService = eventService; this.actor = actor; this.permissions = permissions;
    }

    public record Account(UUID id, String kind, String label) {}
    public record Input(String recipientName, String occasion, String occasionLabel, String title, String message, Long targetPaise,
                        LocalDate closesOn, Boolean showProgress, String accountKind, UUID accountId) {}
    public record Mine(UUID eventId, UUID publicToken, String title, String recipientName, String occasionLabel, Long targetPaise,
                       long confirmedPaise, long pendingPaise, int contributors, LocalDate closesOn, String status) {}
    public record PublicView(String recipientName, String occasionLabel, String organiserName, LocalDate closesOn, boolean closed,
                             Long targetPaise, Long raisedPaise, int contributors, boolean showProgress) {}

    @GetMapping("/api/v1/me/collections/accounts")
    public List<Account> accounts() {
        UUID me = me();
        return db.query("""
            SELECT id,'UPI' AS kind, COALESCE(account_label, upi_id_masked) AS label FROM host_upi_accounts
            WHERE host_id=? AND enabled AND archived_at IS NULL AND verification_status NOT IN ('FAILED','REVOKED')
              AND (host_confirmed_at IS NOT NULL OR verification_status='VERIFIED')
              AND EXISTS (SELECT 1 FROM host_upi_qr_images q WHERE q.host_id=host_upi_accounts.host_id AND q.upi_account_id=host_upi_accounts.id
                          AND q.source IN ('UPLOADED','GENERATED') AND q.payload_sha256 IS NOT NULL)
            UNION ALL
            SELECT id,'BANK', COALESCE(account_label, account_number_masked) FROM host_bank_accounts
            WHERE host_id=? AND enabled AND archived_at IS NULL AND verification_status NOT IN ('FAILED','REVOKED')
              AND (host_confirmed_at IS NOT NULL OR verification_status='VERIFIED')""",
            (rs, i) -> new Account(rs.getObject(1, UUID.class), rs.getString(2), rs.getString(3)), me, me);
    }

    @PostMapping("/api/v1/me/collections")
    @Transactional
    public Map<String, Object> create(@RequestBody Input in) {
        UUID me = me();
        if (in == null) throw bad("Collection details are required");
        String recipient = text(in.recipientName(), 120, "Name");
        if (recipient == null) throw bad("Who is the gift for?");
        String occ = in.occasion() == null ? "" : in.occasion().strip().toUpperCase(Locale.ROOT);
        OccasionType type = OCCASIONS.get(occ);
        if (type == null) throw bad("Choose the occasion");
        String label = Optional.ofNullable(text(in.occasionLabel(), 60, "Occasion")).orElse(defaultLabel(occ));
        if (in.targetPaise() == null || in.targetPaise() < 10000 || in.targetPaise() > 100000000000L) throw bad("Set a target between ₹100 and ₹1,00,00,00,000");
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
        if (in.closesOn() != null && (in.closesOn().isBefore(today) || in.closesOn().isAfter(today.plusYears(1)))) throw bad("Choose a closing date within the next year");
        String kind = in.accountKind() == null ? "" : in.accountKind().toUpperCase(Locale.ROOT);
        if (in.accountId() == null || accounts().stream().noneMatch(a -> a.id().equals(in.accountId()) && a.kind().equals(kind)))
            throw bad("Choose one of your confirmed receiving accounts");
        String organiser = db.queryForObject("SELECT COALESCE(NULLIF(full_name,''), username, 'Organiser') FROM users WHERE id=?", String.class, me);
        String title = Optional.ofNullable(text(in.title(), 200, "Title")).orElse(label + " gift for " + recipient);
        String message = text(in.message(), 2000, "Message");

        Event e = new Event(title, type);
        e.assignOwner(me);
        e.setHostDisplayName(organiser);
        e.setEventContact(null, null, null, null, "India", organiser, null, null);
        e.update(title, message == null ? "" : message, type, in.closesOn());
        e.setGiftingEnabled(true);
        events.saveAndFlush(e);
        UUID id = e.getId();
        db.update("UPDATE events SET kind='COLLECTION' WHERE id=?", id);
        db.update("INSERT INTO event_collections(event_id,recipient_name,occasion_label,target_paise,closes_on,show_progress,organiser_name) VALUES(?,?,?,?,?,?,?)",
            id, recipient, label, in.targetPaise(), in.closesOn(), in.showProgress() == null || in.showProgress(), organiser);
        db.update("INSERT INTO gifts(id,event_id,name,description,target_amount,visibility) VALUES(?,?,?,?,?,'PUBLIC')",
            UUID.randomUUID(), id, "Gift for " + recipient, "Everyone chips in for one thoughtful gift.", BigDecimal.valueOf(in.targetPaise(), 2));
        db.update("INSERT INTO event_payment_account_links(event_id,host_id," + (kind.equals("UPI") ? "upi_account_id" : "bank_account_id") + ",enabled,preferred) VALUES(?,?,?,TRUE,TRUE)",
            id, me, in.accountId());
        // The collection itself is the feature: its event switch starts on, and the admin can switch an individual collection off.
        db.update("INSERT INTO platform_event_module_grants(event_id,module_code,enabled,changed_by) VALUES(?,?,TRUE,?)", id, MODULE, me);
        // Publishing reuses every existing check (verification, plan limits, receiving account); any failure rolls the collection back.
        eventService.changeStatus(id, EventStatus.PUBLISHED);
        AcquisitionEventsController.record(db, "COLLECTION_CREATED", me, id);
        UUID token = db.queryForObject("SELECT public_token FROM events WHERE id=?", UUID.class, id);
        return Map.of("eventId", id, "publicToken", token);
    }

    @GetMapping("/api/v1/me/collections")
    public ResponseEntity<List<Mine>> mine() {
        UUID me = me();
        List<Mine> out = db.query("""
            SELECT e.id, e.public_token, e.title, e.status, c.*,
              COALESCE((SELECT SUM(o.amount_paise) FROM (""" + ShagunRegisterController.CONFIRMED_ONLINE + """
              ) o WHERE o.event_id=e.id),0)::bigint AS confirmed,
              COALESCE((SELECT SUM(amount_paise) FROM guest_direct_transfer_claims WHERE event_id=e.id AND status='AWAITING_VERIFICATION'),0)::bigint AS pending,
              (SELECT count(*) FROM guest_direct_transfer_claims WHERE event_id=e.id AND status IN ('AWAITING_VERIFICATION','HOST_CONFIRMED'))
               + (SELECT count(*) FROM contributions WHERE event_id=e.id) AS contributors
            FROM events e JOIN event_collections c ON c.event_id=e.id WHERE e.owner_id=? AND e.kind='COLLECTION' ORDER BY e.created_at DESC""",
            (rs, i) -> new Mine(rs.getObject("id", UUID.class), rs.getObject("public_token", UUID.class), rs.getString("title"), rs.getString("recipient_name"),
                rs.getString("occasion_label"), (Long) rs.getObject("target_paise"), rs.getLong("confirmed"), rs.getLong("pending"), rs.getInt("contributors"),
                rs.getObject("closes_on", LocalDate.class), rs.getString("status")), me);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(out);
    }

    /** Guest page header for a collection. Raised amount counts confirmed gifts only, and only if the organiser shows progress. */
    @GetMapping("/api/public/events/{token}/collection")
    public ResponseEntity<PublicView> publicView(@PathVariable UUID token) {
        var rows = db.queryForList("""
            SELECT e.id, e.owner_id, c.* FROM events e JOIN event_collections c ON c.event_id=e.id
            WHERE e.public_token=? AND e.kind='COLLECTION' AND e.status='PUBLISHED'""", token);
        if (rows.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        var r = rows.getFirst();
        UUID id = (UUID) r.get("id");
        if (!permissions.eventEnabled((UUID) r.get("owner_id"), id, MODULE)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        boolean show = (Boolean) r.get("show_progress");
        Long raised = show ? db.queryForObject("SELECT COALESCE(SUM(amount_paise),0)::bigint FROM (" + ShagunRegisterController.CONFIRMED_ONLINE + ") o WHERE o.event_id=?", Long.class, id) : null;
        Integer contributors = db.queryForObject("""
            SELECT (SELECT count(*) FROM guest_direct_transfer_claims WHERE event_id=? AND status IN ('AWAITING_VERIFICATION','HOST_CONFIRMED'))
                 + (SELECT count(*) FROM contributions WHERE event_id=?)""", Integer.class, id, id);
        LocalDate closes = r.get("closes_on") == null ? null : ((java.sql.Date) r.get("closes_on")).toLocalDate();
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new PublicView((String) r.get("recipient_name"), (String) r.get("occasion_label"),
            (String) r.get("organiser_name"), closes, closes != null && closes.isBefore(LocalDate.now(ZoneId.of("Asia/Kolkata"))),
            show ? (Long) r.get("target_paise") : null, raised, contributors == null ? 0 : contributors, show));
    }

    static String defaultLabel(String occ) {
        return switch (occ) { case "FAREWELL" -> "Farewell"; case "BIRTHDAY" -> "Birthday"; case "RETIREMENT" -> "Retirement"; case "WEDDING" -> "Wedding";
            case "BABY_SHOWER" -> "New baby"; default -> "Group"; };
    }
    private UUID me() { UUID me = actor.requireAuthenticatedUserId(); permissions.require(me, MODULE); return me; }
    private static String text(String v, int max, String label) {
        if (v == null || v.isBlank()) return null;
        String s = v.strip();
        if (s.length() > max) throw bad(label + " must be at most " + max + " characters");
        if (s.chars().anyMatch(c -> Character.isISOControl(c) && c != '\n')) throw bad(label + " contains unsupported characters");
        return s;
    }
    private static ResponseStatusException bad(String m) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, m); }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> reason(ResponseStatusException ex) {
        int code = ex.getStatusCode().value();
        if ((code == 400 || code == 409 || code == 402) && ex.getReason() != null) return ResponseEntity.status(code).body(Map.of("error", ex.getReason()));
        return ResponseEntity.status(ex.getStatusCode()).build();
    }
}
