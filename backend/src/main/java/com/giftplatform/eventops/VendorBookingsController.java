package com.giftplatform.eventops;

import com.giftplatform.admin.EffectiveModulePermissionService;
import com.giftplatform.register.GiftPeople;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * Vendor quotes & bookings for one celebration: collect quotes from several vendors, compare them, accept one to book
 * the vendor, then track the payment schedule (advance, instalments). When the budget is on, a booked vendor keeps a
 * matching budget line up to date. Owner, or family the budget is shared with. A planning record; it never moves money.
 */
@RestController
@RequestMapping("/api/v1/events/{id}/vendors")
public class VendorBookingsController extends EventOpsSupport {
    public static final String MODULE = "VENDOR_BOOKINGS";
    static final List<String> CATEGORIES = List.of("Venue", "Food", "Decor", "Photos", "Attire", "Music", "Makeup", "Mehendi", "Travel", "Stay", "Invitations", "Gifts", "Other");
    static final Map<String, String> FROM_DIRECTORY = Map.ofEntries(Map.entry("DECOR", "Decor"), Map.entry("CATERING", "Food"), Map.entry("PHOTOGRAPHY", "Photos"),
        Map.entry("MAKEUP", "Makeup"), Map.entry("MEHENDI", "Mehendi"), Map.entry("MUSIC", "Music"), Map.entry("VENUE", "Venue"), Map.entry("PLANNER", "Other"),
        Map.entry("INVITATIONS", "Invitations"), Map.entry("GIFTS", "Gifts"), Map.entry("OTHER", "Other"));
    static final Set<String> BUDGET_CATEGORIES = Set.copyOf(EventBudgetController.CATEGORIES);

    private final JdbcTemplate db;
    private final BudgetAccess access;
    private final EffectiveModulePermissionService permissions;

    public VendorBookingsController(JdbcTemplate db, BudgetAccess access, EffectiveModulePermissionService permissions) {
        this.db = db; this.access = access; this.permissions = permissions;
    }

    public record Quote(UUID id, long amountPaise, String details, LocalDate validUntil, String status, boolean expired, OffsetDateTime createdAt) {}
    public record Payment(UUID id, String label, long amountPaise, LocalDate dueDate, LocalDate paidOn, String note, boolean overdue, boolean dueSoon) {}
    public record Vendor(UUID id, UUID directoryVendorId, String name, String category, String contactName, String phone, String email, String status,
                         long bookedPaise, long paidPaise, long balancePaise, long scheduledPaise, LocalDate nextDue, boolean overdue, String note,
                         boolean budgetLinked, long version, List<Quote> quotes, List<Payment> payments) {}
    public record Due(UUID vendorId, String vendor, UUID paymentId, String label, long amountPaise, LocalDate dueDate, boolean overdue) {}
    public record Totals(int vendors, int booked, long bookedPaise, long paidPaise, long balancePaise) {}
    public record Shortlisted(UUID id, String name, String category, String city, String phone, String priceHint) {}
    public record Board(List<Vendor> vendors, Totals totals, List<Due> upcoming, List<Shortlisted> shortlist, List<String> categories, String access, boolean budgetOn) {}
    public record Input(String name, String category, String contactName, String phone, String email, String note, Long bookedPaise, UUID directoryVendorId, Long version) {}
    public record QuoteInput(Long amountPaise, String details, LocalDate validUntil) {}
    public record PaymentInput(String label, Long amountPaise, LocalDate dueDate, LocalDate paidOn, String note) {}
    public record Paid(LocalDate paidOn) {}

    @GetMapping
    public ResponseEntity<Board> board(@PathVariable UUID id) {
        var who = access.require(id, false, MODULE);
        LocalDate today = today();
        List<Vendor> vendors = vendors(id, null);
        List<Due> upcoming = new ArrayList<>();
        for (Vendor v : vendors) if (!"CANCELLED".equals(v.status())) for (Payment p : v.payments())
            if (p.paidOn() == null && p.dueDate() != null && !p.dueDate().isAfter(today.plusDays(14)))
                upcoming.add(new Due(v.id(), v.name(), p.id(), p.label(), p.amountPaise(), p.dueDate(), p.overdue()));
        upcoming.sort(Comparator.comparing(Due::dueDate));
        List<Vendor> active = vendors.stream().filter(v -> "BOOKED".equals(v.status())).toList();
        Totals totals = new Totals(vendors.size(), active.size(), active.stream().mapToLong(Vendor::bookedPaise).sum(),
            active.stream().mapToLong(Vendor::paidPaise).sum(), active.stream().mapToLong(Vendor::balancePaise).sum());
        UUID owner = who.event().getOwnerId();
        List<Shortlisted> shortlist = who.owner() && permissions.enabled(owner, "VENDOR_DIRECTORY") ? db.query("""
            SELECT v.id, v.name, v.category, v.city, v.phone, v.price_hint FROM user_vendor_shortlist s JOIN platform_vendors v ON v.id=s.vendor_id
            WHERE s.user_id=? AND v.active AND NOT EXISTS(SELECT 1 FROM event_vendors e WHERE e.event_id=? AND e.directory_vendor_id=v.id) ORDER BY v.name LIMIT 100""",
            (rs, i) -> new Shortlisted(rs.getObject(1, UUID.class), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6)), owner, id) : List.of();
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new Board(vendors, totals, upcoming, shortlist, CATEGORIES, who.level(), budgetOn(owner, id)));
    }

    @PostMapping
    @Transactional
    public Vendor create(@PathVariable UUID id, @RequestBody Input in) {
        var who = access.require(id, true, MODULE);
        Input src = in;
        if (in != null && in.directoryVendorId() != null) {
            var rows = db.queryForList("""
                SELECT v.* FROM platform_vendors v WHERE v.id=? AND v.active AND EXISTS(SELECT 1 FROM user_vendor_shortlist s WHERE s.vendor_id=v.id AND s.user_id=?)""",
                in.directoryVendorId(), who.event().getOwnerId());
            if (rows.isEmpty()) throw bad("That vendor is not on the shortlist");
            var r = rows.getFirst();
            src = new Input((String) r.get("name"), FROM_DIRECTORY.getOrDefault((String) r.get("category"), "Other"), null, (String) r.get("phone"), null,
                in.note(), null, in.directoryVendorId(), null);
        }
        Clean c = clean(src);
        Integer n = db.queryForObject("SELECT count(*) FROM event_vendors WHERE event_id=?", Integer.class, id);
        if (n != null && n >= 200) throw bad("A celebration can track up to 200 vendors");
        UUID vid = UUID.randomUUID();
        db.update("INSERT INTO event_vendors(id,event_id,directory_vendor_id,name,category,contact_name,phone,email,note) VALUES(?,?,?,?,?,?,?,?,?)",
            vid, id, src.directoryVendorId(), c.name, c.category, c.contact, c.phone, c.email, c.note);
        return vendors(id, vid).getFirst();
    }

    @PutMapping("/{vendorId}")
    @Transactional
    public Vendor update(@PathVariable UUID id, @PathVariable UUID vendorId, @RequestBody Input in) {
        var who = access.require(id, true, MODULE);
        Clean c = clean(in);
        if (in.version() == null) throw changed();
        Long booked = in.bookedPaise() == null ? null : paise(in.bookedPaise(), "Booked amount");
        int n = db.update("UPDATE event_vendors SET name=?,category=?,contact_name=?,phone=?,email=?,note=?,booked_paise=COALESCE(?,booked_paise),version=version+1 WHERE id=? AND event_id=? AND version=?",
            c.name, c.category, c.contact, c.phone, c.email, c.note, booked, vendorId, id, in.version());
        if (n == 0) throw exists(id, vendorId) ? changed() : notFound();
        sync(who.event().getOwnerId(), id, vendorId);
        return vendors(id, vendorId).getFirst();
    }

    @PostMapping("/{vendorId}/delete")
    @Transactional
    public Map<String, Object> delete(@PathVariable UUID id, @PathVariable UUID vendorId) {
        access.require(id, true, MODULE);
        if (db.update("DELETE FROM event_vendors WHERE id=? AND event_id=?", vendorId, id) == 0) throw notFound();
        return Map.of("deleted", true, "note", "Any budget line it created stays in your budget.");
    }

    @PostMapping("/{vendorId}/cancel")
    @Transactional
    public Vendor cancel(@PathVariable UUID id, @PathVariable UUID vendorId) {
        var who = access.require(id, true, MODULE);
        if (db.update("UPDATE event_vendors SET status='CANCELLED',version=version+1 WHERE id=? AND event_id=?", vendorId, id) == 0) throw notFound();
        sync(who.event().getOwnerId(), id, vendorId);
        return vendors(id, vendorId).getFirst();
    }

    // ---- Quotes ----

    @PostMapping("/{vendorId}/quotes")
    @Transactional
    public Vendor addQuote(@PathVariable UUID id, @PathVariable UUID vendorId, @RequestBody QuoteInput in) {
        access.require(id, true, MODULE);
        if (in == null || in.amountPaise() == null) throw bad("Add the quoted amount");
        long amount = paise(in.amountPaise(), "Quote");
        if (!exists(id, vendorId)) throw notFound();
        Integer n = db.queryForObject("SELECT count(*) FROM event_vendor_quotes WHERE vendor_id=?", Integer.class, vendorId);
        if (n != null && n >= 20) throw bad("A vendor can have up to 20 quotes");
        db.update("INSERT INTO event_vendor_quotes(id,vendor_id,event_id,amount_paise,details,valid_until) VALUES(?,?,?,?,?,?)",
            UUID.randomUUID(), vendorId, id, amount, text(in.details(), 600, "What's included"), in.validUntil());
        db.update("UPDATE event_vendors SET status='QUOTED',version=version+1 WHERE id=? AND status='ENQUIRED'", vendorId);
        return vendors(id, vendorId).getFirst();
    }

    /** Accepting a quote books the vendor at that amount; their other quotes are marked not chosen. */
    @PostMapping("/{vendorId}/quotes/{quoteId}/accept")
    @Transactional
    public Vendor accept(@PathVariable UUID id, @PathVariable UUID vendorId, @PathVariable UUID quoteId) {
        var who = access.require(id, true, MODULE);
        Long amount = db.query("SELECT amount_paise FROM event_vendor_quotes WHERE id=? AND vendor_id=? AND event_id=?", (rs, i) -> rs.getLong(1), quoteId, vendorId, id)
            .stream().findFirst().orElseThrow(EventOpsSupport::notFound);
        db.update("UPDATE event_vendor_quotes SET status=CASE WHEN id=? THEN 'ACCEPTED' ELSE 'REJECTED' END WHERE vendor_id=?", quoteId, vendorId);
        db.update("UPDATE event_vendors SET status='BOOKED',booked_paise=?,version=version+1 WHERE id=?", amount, vendorId);
        sync(who.event().getOwnerId(), id, vendorId);
        return vendors(id, vendorId).getFirst();
    }

    @PostMapping("/{vendorId}/quotes/{quoteId}/reject")
    public Vendor reject(@PathVariable UUID id, @PathVariable UUID vendorId, @PathVariable UUID quoteId) {
        access.require(id, true, MODULE);
        if (db.update("UPDATE event_vendor_quotes SET status='REJECTED' WHERE id=? AND vendor_id=? AND event_id=? AND status='RECEIVED'", quoteId, vendorId, id) == 0) throw notFound();
        return vendors(id, vendorId).getFirst();
    }

    @PostMapping("/{vendorId}/quotes/{quoteId}/delete")
    public Vendor deleteQuote(@PathVariable UUID id, @PathVariable UUID vendorId, @PathVariable UUID quoteId) {
        access.require(id, true, MODULE);
        if (db.update("DELETE FROM event_vendor_quotes WHERE id=? AND vendor_id=? AND event_id=?", quoteId, vendorId, id) == 0) throw notFound();
        return vendors(id, vendorId).getFirst();
    }

    // ---- Payment schedule ----

    @PostMapping("/{vendorId}/payments")
    @Transactional
    public Vendor addPayment(@PathVariable UUID id, @PathVariable UUID vendorId, @RequestBody PaymentInput in) {
        var who = access.require(id, true, MODULE);
        CleanPay p = payment(in);
        if (!exists(id, vendorId)) throw notFound();
        Integer n = db.queryForObject("SELECT count(*) FROM event_vendor_payments WHERE vendor_id=?", Integer.class, vendorId);
        if (n != null && n >= 30) throw bad("A vendor can have up to 30 payments");
        db.update("INSERT INTO event_vendor_payments(id,vendor_id,event_id,label,amount_paise,due_date,paid_on,note) VALUES(?,?,?,?,?,?,?,?)",
            UUID.randomUUID(), vendorId, id, p.label, p.amount, in.dueDate(), in.paidOn(), p.note);
        sync(who.event().getOwnerId(), id, vendorId);
        return vendors(id, vendorId).getFirst();
    }

    @PutMapping("/{vendorId}/payments/{paymentId}")
    @Transactional
    public Vendor editPayment(@PathVariable UUID id, @PathVariable UUID vendorId, @PathVariable UUID paymentId, @RequestBody PaymentInput in) {
        var who = access.require(id, true, MODULE);
        CleanPay p = payment(in);
        if (db.update("UPDATE event_vendor_payments SET label=?,amount_paise=?,due_date=?,paid_on=?,note=? WHERE id=? AND vendor_id=? AND event_id=?",
            p.label, p.amount, in.dueDate(), in.paidOn(), p.note, paymentId, vendorId, id) == 0) throw notFound();
        sync(who.event().getOwnerId(), id, vendorId);
        return vendors(id, vendorId).getFirst();
    }

    @PostMapping("/{vendorId}/payments/{paymentId}/paid")
    @Transactional
    public Vendor markPaid(@PathVariable UUID id, @PathVariable UUID vendorId, @PathVariable UUID paymentId, @RequestBody(required = false) Paid in) {
        var who = access.require(id, true, MODULE);
        LocalDate on = in == null || in.paidOn() == null ? today() : in.paidOn();
        if (db.update("UPDATE event_vendor_payments SET paid_on=? WHERE id=? AND vendor_id=? AND event_id=?", on, paymentId, vendorId, id) == 0) throw notFound();
        sync(who.event().getOwnerId(), id, vendorId);
        return vendors(id, vendorId).getFirst();
    }

    @PostMapping("/{vendorId}/payments/{paymentId}/delete")
    @Transactional
    public Vendor deletePayment(@PathVariable UUID id, @PathVariable UUID vendorId, @PathVariable UUID paymentId) {
        var who = access.require(id, true, MODULE);
        if (db.update("DELETE FROM event_vendor_payments WHERE id=? AND vendor_id=? AND event_id=?", paymentId, vendorId, id) == 0) throw notFound();
        sync(who.event().getOwnerId(), id, vendorId);
        return vendors(id, vendorId).getFirst();
    }

    // ---- Budget sync ----

    /**
     * Keeps one budget line per booked vendor: booked amount → "booked", paid instalments → "paid", next unpaid
     * instalment → "next payment due". Only while the budget is switched on for the event. A cancelled vendor's
     * line keeps what was already paid. Lines the host deleted are recreated only if the vendor is booked again.
     */
    private void sync(UUID owner, UUID event, UUID vendorId) {
        if (!budgetOn(owner, event)) return;
        var v = db.queryForMap("SELECT * FROM event_vendors WHERE id=?", vendorId);
        long paid = db.queryForObject("SELECT COALESCE(SUM(amount_paise),0) FROM event_vendor_payments WHERE vendor_id=? AND paid_on IS NOT NULL", Long.class, vendorId);
        LocalDate due = db.query("SELECT min(due_date) FROM event_vendor_payments WHERE vendor_id=? AND paid_on IS NULL", (rs, i) -> rs.getObject(1, LocalDate.class), vendorId)
            .stream().filter(Objects::nonNull).findFirst().orElse(null);
        String status = (String) v.get("status");
        long booked = ((Number) v.get("booked_paise")).longValue();
        long committed = "CANCELLED".equals(status) ? paid : Math.max(booked, paid);
        UUID line = (UUID) v.get("budget_line_id");
        String category = BUDGET_CATEGORIES.contains((String) v.get("category")) ? (String) v.get("category") : "Other";
        String name = (String) v.get("name");
        if (line != null && db.update("""
            UPDATE event_budget_lines SET category=?,vendor_name=?,committed_paise=?,paid_paise=?,due_date=?,planned_paise=GREATEST(planned_paise,?),version=version+1
            WHERE id=? AND event_id=?""", category, name, committed, Math.min(paid, MAX_PAISE), due, "CANCELLED".equals(status) ? 0 : committed, line, event) > 0) return;
        if (!"BOOKED".equals(status)) return;
        Integer lines = db.queryForObject("SELECT count(*) FROM event_budget_lines WHERE event_id=?", Integer.class, event);
        if (lines != null && lines >= 300) return;
        UUID created = UUID.randomUUID();
        db.update("INSERT INTO event_budget_lines(id,event_id,category,item,vendor_name,planned_paise,committed_paise,paid_paise,due_date,note) VALUES(?,?,?,?,?,?,?,?,?,?)",
            created, event, category, name.length() > 120 ? name.substring(0, 120) : name, name, committed, committed, paid, due, "From Vendors & bookings");
        db.update("UPDATE event_vendors SET budget_line_id=? WHERE id=?", created, vendorId);
    }

    // ---- Helpers ----

    private boolean budgetOn(UUID owner, UUID event) { return permissions.eventEnabled(owner, event, EventBudgetController.MODULE); }
    private boolean exists(UUID event, UUID vendor) {
        return Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM event_vendors WHERE id=? AND event_id=?)", Boolean.class, vendor, event));
    }
    private static LocalDate today() { return LocalDate.now(ZoneId.of("Asia/Kolkata")); }

    private List<Vendor> vendors(UUID event, UUID only) {
        LocalDate today = today();
        Map<UUID, List<Quote>> quotes = new HashMap<>();
        db.query("SELECT * FROM event_vendor_quotes WHERE event_id=? AND (?::uuid IS NULL OR vendor_id=?) ORDER BY amount_paise", rs -> {
            LocalDate valid = rs.getObject("valid_until", LocalDate.class);
            quotes.computeIfAbsent(rs.getObject("vendor_id", UUID.class), k -> new ArrayList<>()).add(new Quote(rs.getObject("id", UUID.class), rs.getLong("amount_paise"),
                rs.getString("details"), valid, rs.getString("status"), valid != null && valid.isBefore(today) && "RECEIVED".equals(rs.getString("status")), rs.getObject("created_at", OffsetDateTime.class)));
        }, event, only, only);
        Map<UUID, List<Payment>> payments = new HashMap<>();
        db.query("SELECT * FROM event_vendor_payments WHERE event_id=? AND (?::uuid IS NULL OR vendor_id=?) ORDER BY due_date NULLS LAST, created_at", rs -> {
            LocalDate due = rs.getObject("due_date", LocalDate.class), paid = rs.getObject("paid_on", LocalDate.class);
            payments.computeIfAbsent(rs.getObject("vendor_id", UUID.class), k -> new ArrayList<>()).add(new Payment(rs.getObject("id", UUID.class), rs.getString("label"),
                rs.getLong("amount_paise"), due, paid, rs.getString("note"), paid == null && due != null && due.isBefore(today),
                paid == null && due != null && !due.isBefore(today) && !due.isAfter(today.plusDays(14))));
        }, event, only, only);
        List<Vendor> out = db.query("SELECT * FROM event_vendors WHERE event_id=? AND (?::uuid IS NULL OR id=?) ORDER BY CASE status WHEN 'BOOKED' THEN 0 WHEN 'QUOTED' THEN 1 WHEN 'ENQUIRED' THEN 2 ELSE 3 END, category, lower(name)", (rs, i) -> {
            UUID vid = rs.getObject("id", UUID.class);
            List<Payment> pay = payments.getOrDefault(vid, List.of());
            long booked = rs.getLong("booked_paise"), paid = pay.stream().filter(p -> p.paidOn() != null).mapToLong(Payment::amountPaise).sum();
            long scheduled = pay.stream().mapToLong(Payment::amountPaise).sum();
            LocalDate next = pay.stream().filter(p -> p.paidOn() == null && p.dueDate() != null).map(Payment::dueDate).min(Comparator.naturalOrder()).orElse(null);
            boolean cancelled = "CANCELLED".equals(rs.getString("status"));
            return new Vendor(vid, rs.getObject("directory_vendor_id", UUID.class), rs.getString("name"), rs.getString("category"), rs.getString("contact_name"),
                rs.getString("phone"), rs.getString("email"), rs.getString("status"), booked, paid, cancelled ? 0 : Math.max(0, booked - paid), scheduled, cancelled ? null : next,
                !cancelled && pay.stream().anyMatch(Payment::overdue), rs.getString("note"), rs.getObject("budget_line_id") != null, rs.getLong("version"),
                quotes.getOrDefault(vid, List.of()), pay);
        }, event, only, only);
        if (only != null && out.isEmpty()) throw notFound();
        return out;
    }

    private record Clean(String name, String category, String contact, String phone, String email, String note) {}
    private static Clean clean(Input in) {
        if (in == null) throw bad("Vendor details are required");
        String category = in.category() == null || in.category().isBlank() ? "Other" : in.category().strip();
        if (!CATEGORIES.contains(category)) throw bad("Choose a category from the list");
        String email = text(in.email(), 160, "Email");
        if (email != null && !email.matches("[^@\\s]{1,64}@[^@\\s]{1,120}\\.[^@\\s]{2,}")) throw bad("Enter a valid email address, or leave it empty");
        return new Clean(required(in.name(), 120, "Vendor name"), category, text(in.contactName(), 80, "Contact person"), GiftPeople.phone(in.phone()), email, text(in.note(), 500, "Note"));
    }
    private record CleanPay(String label, long amount, String note) {}
    private static CleanPay payment(PaymentInput in) {
        if (in == null || in.amountPaise() == null) throw bad("Add the amount");
        long amount = paise(in.amountPaise(), "Amount");
        if (amount == 0) throw bad("Amount must be more than ₹0");
        return new CleanPay(required(in.label(), 60, "Payment name"), amount, text(in.note(), 200, "Note"));
    }
}
