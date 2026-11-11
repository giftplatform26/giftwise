package com.giftplatform.eventops;

import com.giftplatform.cohost.EventAccess;
import com.giftplatform.cohost.EventAccess.Capability;
import com.giftplatform.event.entity.Event;
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
 * Planning checklist: tasks counted back from the celebration date, owned by the host or a co-host.
 * A starter list per occasion gives hosts a head start; everything is editable.
 */
@RestController
@RequestMapping("/api/v1/events/{id}/tasks")
public class PlanningChecklistController extends EventOpsSupport {
    public static final String MODULE = "PLANNING_CHECKLIST";
    static final List<String> CATEGORIES = List.of("Venue", "Food", "Decor", "Guests", "Attire", "Photos", "Gifts", "Travel", "Rituals", "Other");

    /** Starter tasks: title, category, weeks before the date. */
    record Starter(String title, String category, int weeksBefore) {}
    static final Map<String, List<Starter>> STARTERS = Map.of(
        "WEDDING", List.of(new Starter("Fix the budget with both families", "Other", 26), new Starter("Book the venue", "Venue", 24),
            new Starter("Shortlist and book the caterer", "Food", 20), new Starter("Book photographer and videographer", "Photos", 20),
            new Starter("Finalise the guest list", "Guests", 12), new Starter("Send invitations", "Guests", 8),
            new Starter("Order outfits and jewellery", "Attire", 10), new Starter("Book decorator and mehendi artist", "Decor", 10),
            new Starter("Arrange stay for outstation guests", "Travel", 6), new Starter("Confirm menu and headcount with caterer", "Food", 2),
            new Starter("Buy return gifts", "Gifts", 3), new Starter("Share day-of schedule with vendors", "Rituals", 1)),
        "BIRTHDAY", List.of(new Starter("Pick the date and venue", "Venue", 6), new Starter("Make the guest list", "Guests", 5),
            new Starter("Send invitations", "Guests", 4), new Starter("Order the cake", "Food", 2), new Starter("Plan games or entertainment", "Other", 2),
            new Starter("Buy decorations", "Decor", 1), new Starter("Buy return gifts", "Gifts", 1), new Starter("Confirm headcount", "Guests", 0)),
        "DEFAULT", List.of(new Starter("Fix the date and budget", "Other", 8), new Starter("Book the venue", "Venue", 6),
            new Starter("Make the guest list", "Guests", 5), new Starter("Send invitations", "Guests", 4), new Starter("Arrange food", "Food", 3),
            new Starter("Plan decorations", "Decor", 2), new Starter("Confirm headcount", "Guests", 1), new Starter("Send thank-you notes", "Other", -1)));

    private final JdbcTemplate db;
    private final EventAccess access;

    public PlanningChecklistController(JdbcTemplate db, EventAccess access) { this.db = db; this.access = access; }

    public record Input(String title, String category, LocalDate dueDate, UUID assigneeId, String note, Long version) {}
    public record Task(UUID id, String title, String category, LocalDate dueDate, UUID assigneeId, String assigneeName, String note,
                       boolean done, OffsetDateTime doneAt, boolean overdue, long version) {}
    public record Member(UUID userId, String name, String role) {}
    public record Progress(int total, int done, int overdue, int dueThisWeek) {}
    public record Checklist(LocalDate eventDate, List<Task> tasks, List<Member> team, Progress progress, List<String> categories, boolean canAddStarter) {}

    @GetMapping
    public ResponseEntity<Checklist> list(@PathVariable UUID id) {
        Event e = access.require(id, Capability.PLANNING, MODULE).event();
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
        List<Task> tasks = db.query("""
            SELECT t.*, COALESCE(NULLIF(u.full_name,''), u.username) AS assignee_name FROM event_tasks t LEFT JOIN users u ON u.id=t.assignee_id
            WHERE t.event_id=? ORDER BY (t.done_at IS NOT NULL), t.due_date NULLS LAST, t.created_at""", (rs, i) -> {
            LocalDate due = rs.getObject("due_date", LocalDate.class);
            boolean done = rs.getObject("done_at") != null;
            return new Task(rs.getObject("id", UUID.class), rs.getString("title"), rs.getString("category"), due, rs.getObject("assignee_id", UUID.class),
                rs.getString("assignee_name"), rs.getString("note"), done, rs.getObject("done_at", OffsetDateTime.class), !done && due != null && due.isBefore(today),
                rs.getLong("version"));
        }, id);
        int done = (int) tasks.stream().filter(Task::done).count();
        int overdue = (int) tasks.stream().filter(Task::overdue).count();
        int week = (int) tasks.stream().filter(t -> !t.done() && t.dueDate() != null && !t.dueDate().isBefore(today) && !t.dueDate().isAfter(today.plusDays(7))).count();
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new Checklist(e.getEventDate(), tasks, team(e),
            new Progress(tasks.size(), done, overdue, week), CATEGORIES, tasks.isEmpty()));
    }

    @PostMapping
    public Task create(@PathVariable UUID id, @RequestBody Input in) {
        var a = access.require(id, Capability.PLANNING, MODULE);
        Clean c = clean(a.event(), in);
        Integer count = db.queryForObject("SELECT count(*) FROM event_tasks WHERE event_id=?", Integer.class, id);
        if (count != null && count >= 300) throw bad("A checklist can have up to 300 tasks");
        UUID tid = UUID.randomUUID();
        db.update("INSERT INTO event_tasks(id,event_id,title,category,due_date,assignee_id,note,created_by) VALUES(?,?,?,?,?,?,?,?)",
            tid, id, c.title, c.category, in.dueDate(), in.assigneeId(), c.note, a.userId());
        return one(id, tid);
    }

    @PutMapping("/{taskId}")
    public Task update(@PathVariable UUID id, @PathVariable UUID taskId, @RequestBody Input in) {
        var a = access.require(id, Capability.PLANNING, MODULE);
        Clean c = clean(a.event(), in);
        if (in.version() == null) throw changed();
        int n = db.update("UPDATE event_tasks SET title=?,category=?,due_date=?,assignee_id=?,note=?,version=version+1 WHERE id=? AND event_id=? AND version=?",
            c.title, c.category, in.dueDate(), in.assigneeId(), c.note, taskId, id, in.version());
        if (n == 0) throw exists(id, taskId) ? changed() : notFound();
        return one(id, taskId);
    }

    public record Done(boolean done) {}
    @PostMapping("/{taskId}/done")
    public Task done(@PathVariable UUID id, @PathVariable UUID taskId, @RequestBody Done in) {
        var a = access.require(id, Capability.PLANNING, MODULE);
        int n = in != null && in.done()
            ? db.update("UPDATE event_tasks SET done_at=COALESCE(done_at,now()),done_by=COALESCE(done_by,?),version=version+1 WHERE id=? AND event_id=?", a.userId(), taskId, id)
            : db.update("UPDATE event_tasks SET done_at=NULL,done_by=NULL,version=version+1 WHERE id=? AND event_id=?", taskId, id);
        if (n == 0) throw notFound();
        return one(id, taskId);
    }

    @PostMapping("/{taskId}/delete")
    public Map<String, Object> delete(@PathVariable UUID id, @PathVariable UUID taskId) {
        access.require(id, Capability.PLANNING, MODULE);
        if (db.update("DELETE FROM event_tasks WHERE id=? AND event_id=?", taskId, id) == 0) throw notFound();
        return Map.of("deleted", true);
    }

    /** Adds the occasion's starter tasks, dated back from the celebration. Only for an empty checklist. */
    @PostMapping("/starter")
    @Transactional
    public Map<String, Object> starter(@PathVariable UUID id) {
        var a = access.require(id, Capability.PLANNING, MODULE);
        db.queryForList("SELECT id FROM events WHERE id=? FOR UPDATE", id);
        Integer count = db.queryForObject("SELECT count(*) FROM event_tasks WHERE event_id=?", Integer.class, id);
        if (count != null && count > 0) throw conflict("Your checklist already has tasks");
        LocalDate date = a.event().getEventDate();
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
        List<Starter> list = STARTERS.getOrDefault(a.event().getOccasionType().name(), STARTERS.get("DEFAULT"));
        for (Starter s : list) {
            LocalDate due = date == null ? null : date.minusWeeks(s.weeksBefore());
            if (due != null && due.isBefore(today) && s.weeksBefore() > 0) due = today.plusDays(3);
            db.update("INSERT INTO event_tasks(id,event_id,title,category,due_date,created_by) VALUES(?,?,?,?,?,?)", UUID.randomUUID(), id, s.title(), s.category(), due, a.userId());
        }
        return Map.of("added", list.size());
    }

    private record Clean(String title, String category, String note) {}
    private Clean clean(Event e, Input in) {
        if (in == null) throw bad("Task details are required");
        String category = text(in.category(), 40, "Category");
        if (in.assigneeId() != null && team(e).stream().noneMatch(m -> m.userId().equals(in.assigneeId())))
            throw bad("Assign the task to yourself or one of your co-hosts");
        if (in.dueDate() != null && (in.dueDate().getYear() < 2000 || in.dueDate().getYear() > 2100)) throw bad("Check the due date");
        return new Clean(required(in.title(), 160, "Task"), category, text(in.note(), 500, "Note"));
    }
    private List<Member> team(Event e) {
        List<Member> team = new ArrayList<>(db.query("SELECT id, COALESCE(NULLIF(full_name,''), username, 'You') FROM users WHERE id=?",
            (rs, i) -> new Member(rs.getObject(1, UUID.class), rs.getString(2), "OWNER"), e.getOwnerId()));
        team.addAll(db.query("""
            SELECT c.user_id, COALESCE(NULLIF(u.full_name,''), u.username, 'Co-host'), c.role FROM event_co_hosts c JOIN users u ON u.id=c.user_id
            WHERE c.event_id=? ORDER BY c.created_at""", (rs, i) -> new Member(rs.getObject(1, UUID.class), rs.getString(2), rs.getString(3)), e.getId()));
        return team;
    }
    private boolean exists(UUID event, UUID task) {
        return Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM event_tasks WHERE id=? AND event_id=?)", Boolean.class, task, event));
    }
    private Task one(UUID event, UUID task) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
        return db.query("""
            SELECT t.*, COALESCE(NULLIF(u.full_name,''), u.username) AS assignee_name FROM event_tasks t LEFT JOIN users u ON u.id=t.assignee_id
            WHERE t.id=? AND t.event_id=?""", (rs, i) -> {
            LocalDate due = rs.getObject("due_date", LocalDate.class);
            boolean done = rs.getObject("done_at") != null;
            return new Task(rs.getObject("id", UUID.class), rs.getString("title"), rs.getString("category"), due, rs.getObject("assignee_id", UUID.class),
                rs.getString("assignee_name"), rs.getString("note"), done, rs.getObject("done_at", OffsetDateTime.class), !done && due != null && due.isBefore(today), rs.getLong("version"));
        }, task, event).stream().findFirst().orElseThrow(EventOpsSupport::notFound);
    }
}
