package com.giftplatform.eventops;

import com.giftplatform.cohost.EventAccess;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

/**
 * Celebration budget: planned vs committed (quoted/booked) vs paid per line, and dues coming up.
 * Owner only — money plans are private even from co-hosts. A planning aid; it never moves money.
 */
@RestController
@RequestMapping("/api/v1/events/{id}/budget")
public class EventBudgetController extends EventOpsSupport {
    public static final String MODULE = "EVENT_BUDGET";
    static final List<String> CATEGORIES = List.of("Venue", "Food", "Decor", "Photos", "Attire", "Music", "Gifts", "Travel", "Stay", "Invitations", "Other");

    private final JdbcTemplate db;
    private final EventAccess access;

    public EventBudgetController(JdbcTemplate db, EventAccess access) { this.db = db; this.access = access; }

    public record Input(String category, String item, String vendorName, Long plannedPaise, Long committedPaise, Long paidPaise, LocalDate dueDate, String note, Long version) {}
    public record Line(UUID id, String category, String item, String vendorName, long plannedPaise, long committedPaise, long paidPaise, long balancePaise,
                       LocalDate dueDate, boolean dueSoon, boolean overdue, String note, long version) {}
    public record CategoryTotal(String category, long plannedPaise, long committedPaise, long paidPaise) {}
    public record Summary(Long totalBudgetPaise, long plannedPaise, long committedPaise, long paidPaise, long balanceDuePaise, Long remainingPaise) {}
    public record Budget(Summary summary, List<Line> lines, List<CategoryTotal> categories, List<Line> upcoming, List<String> categoryChoices) {}
    public record Total(Long totalPaise) {}

    @GetMapping
    public ResponseEntity<Budget> get(@PathVariable UUID id) {
        access.requireOwner(id, MODULE);
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
        List<Line> lines = db.query("SELECT * FROM event_budget_lines WHERE event_id=? ORDER BY category, created_at", (rs, i) -> line(rs, today), id);
        Long total = db.query("SELECT total_paise FROM event_budget_settings WHERE event_id=?", (rs, i) -> (Long) rs.getObject(1), id).stream().findFirst().orElse(null);
        long planned = lines.stream().mapToLong(Line::plannedPaise).sum(), committed = lines.stream().mapToLong(Line::committedPaise).sum();
        long paid = lines.stream().mapToLong(Line::paidPaise).sum(), balance = lines.stream().mapToLong(Line::balancePaise).sum();
        Map<String, long[]> byCat = new LinkedHashMap<>();
        for (Line l : lines) { long[] t = byCat.computeIfAbsent(l.category(), k -> new long[3]); t[0] += l.plannedPaise(); t[1] += l.committedPaise(); t[2] += l.paidPaise(); }
        List<CategoryTotal> cats = byCat.entrySet().stream().map(x -> new CategoryTotal(x.getKey(), x.getValue()[0], x.getValue()[1], x.getValue()[2])).toList();
        List<Line> upcoming = lines.stream().filter(l -> l.balancePaise() > 0 && l.dueDate() != null && !l.dueDate().isAfter(today.plusDays(14)))
            .sorted(Comparator.comparing(Line::dueDate)).toList();
        Long remaining = total == null ? null : total - Math.max(planned, committed);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new Budget(new Summary(total, planned, committed, paid, balance, remaining), lines, cats, upcoming, CATEGORIES));
    }

    @PutMapping("/total")
    public Map<String, Object> total(@PathVariable UUID id, @RequestBody Total in) {
        access.requireOwner(id, MODULE);
        Long v = in == null ? null : in.totalPaise();
        if (v != null) paise(v, "Total budget");
        db.update("INSERT INTO event_budget_settings(event_id,total_paise) VALUES(?,?) ON CONFLICT(event_id) DO UPDATE SET total_paise=EXCLUDED.total_paise", id, v);
        return Map.of("saved", true);
    }

    @PostMapping("/lines")
    public Line create(@PathVariable UUID id, @RequestBody Input in) {
        access.requireOwner(id, MODULE);
        Clean c = clean(in);
        Integer n = db.queryForObject("SELECT count(*) FROM event_budget_lines WHERE event_id=?", Integer.class, id);
        if (n != null && n >= 300) throw bad("A budget can have up to 300 lines");
        UUID lid = UUID.randomUUID();
        db.update("INSERT INTO event_budget_lines(id,event_id,category,item,vendor_name,planned_paise,committed_paise,paid_paise,due_date,note) VALUES(?,?,?,?,?,?,?,?,?,?)",
            lid, id, c.category, c.item, c.vendor, c.planned, c.committed, c.paid, in.dueDate(), c.note);
        return one(id, lid);
    }

    @PutMapping("/lines/{lineId}")
    public Line update(@PathVariable UUID id, @PathVariable UUID lineId, @RequestBody Input in) {
        access.requireOwner(id, MODULE);
        Clean c = clean(in);
        if (in.version() == null) throw changed();
        int n = db.update("""
            UPDATE event_budget_lines SET category=?,item=?,vendor_name=?,planned_paise=?,committed_paise=?,paid_paise=?,due_date=?,note=?,version=version+1
            WHERE id=? AND event_id=? AND version=?""", c.category, c.item, c.vendor, c.planned, c.committed, c.paid, in.dueDate(), c.note, lineId, id, in.version());
        if (n == 0) throw Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM event_budget_lines WHERE id=? AND event_id=?)", Boolean.class, lineId, id)) ? changed() : notFound();
        return one(id, lineId);
    }

    @PostMapping("/lines/{lineId}/delete")
    public Map<String, Object> delete(@PathVariable UUID id, @PathVariable UUID lineId) {
        access.requireOwner(id, MODULE);
        if (db.update("DELETE FROM event_budget_lines WHERE id=? AND event_id=?", lineId, id) == 0) throw notFound();
        return Map.of("deleted", true);
    }

    private record Clean(String category, String item, String vendor, long planned, long committed, long paid, String note) {}
    private Clean clean(Input in) {
        if (in == null) throw bad("Budget line details are required");
        String category = required(in.category(), 40, "Category");
        long planned = paise(in.plannedPaise(), "Planned"), committed = paise(in.committedPaise(), "Booked amount"), paid = paise(in.paidPaise(), "Paid");
        if (committed > 0 && paid > committed) throw bad("Paid can't be more than the booked amount");
        return new Clean(category, required(in.item(), 120, "Item"), text(in.vendorName(), 120, "Vendor"), planned, committed, paid, text(in.note(), 300, "Note"));
    }
    private Line one(UUID event, UUID line) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
        return db.query("SELECT * FROM event_budget_lines WHERE id=? AND event_id=?", (rs, i) -> line(rs, today), line, event).stream().findFirst().orElseThrow(EventOpsSupport::notFound);
    }
    private static Line line(java.sql.ResultSet rs, LocalDate today) throws java.sql.SQLException {
        long committed = rs.getLong("committed_paise"), paid = rs.getLong("paid_paise");
        long balance = Math.max(0, committed - paid);
        LocalDate due = rs.getObject("due_date", LocalDate.class);
        return new Line(rs.getObject("id", UUID.class), rs.getString("category"), rs.getString("item"), rs.getString("vendor_name"), rs.getLong("planned_paise"),
            committed, paid, balance, due, balance > 0 && due != null && !due.isBefore(today) && !due.isAfter(today.plusDays(14)),
            balance > 0 && due != null && due.isBefore(today), rs.getString("note"), rs.getLong("version"));
    }
}
