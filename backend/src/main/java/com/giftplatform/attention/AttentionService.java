package com.giftplatform.attention;

import com.giftplatform.admin.EffectiveModulePermissionService;
import com.giftplatform.notification.email.TransactionalEmailOutbox;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

/**
 * "Needs your attention": payments to confirm, wishes to approve, tasks due, new RSVPs and replies, payments
 * due to vendors and showcase decisions — for celebrations the user owns or helps with. Shown in the app and,
 * once a day at most, as a short digest email (only if the host has notifications on).
 */
@Service
public class AttentionService {
    public static final String MODULE = "HOST_NOTIFICATIONS";
    static final ZoneId INDIA = ZoneId.of("Asia/Kolkata");

    public record Item(String kind, UUID eventId, String eventTitle, int count, String label, String href, String tone) {}

    private final JdbcTemplate db;
    private final EffectiveModulePermissionService permissions;
    private final TransactionalEmailOutbox outbox;

    public AttentionService(JdbcTemplate db, EffectiveModulePermissionService permissions, TransactionalEmailOutbox outbox) {
        this.db = db; this.permissions = permissions; this.outbox = outbox;
    }

    public List<Item> items(UUID me) {
        List<Item> out = new ArrayList<>();
        LocalDate today = LocalDate.now(INDIA);
        // Owner-only items.
        db.query("""
            SELECT e.id, e.title,
              (SELECT count(*) FROM guest_direct_transfer_claims c WHERE c.event_id=e.id AND c.status='AWAITING_VERIFICATION') AS pay,
              (SELECT count(*) FROM event_guestbook g WHERE g.event_id=e.id AND g.status='PENDING') AS wishes,
              (SELECT count(*) FROM event_rsvps r WHERE r.event_id=e.id AND r.created_at>now()-interval '24 hours')
                + (SELECT count(*) FROM event_function_rsvps fr JOIN event_functions f ON f.id=fr.function_id WHERE f.event_id=e.id AND fr.updated_at>now()-interval '24 hours') AS rsvps,
              (SELECT count(*) FROM event_guest_contacts k WHERE k.event_id=e.id AND k.replied_at>now()-interval '24 hours') AS replies,
              (SELECT count(*) FROM event_budget_lines b WHERE b.event_id=e.id AND b.committed_paise>b.paid_paise AND b.due_date IS NOT NULL AND b.due_date<=?::date+3) AS dues,
              (SELECT status FROM event_showcase s WHERE s.event_id=e.id AND s.reviewed_at>now()-interval '7 days' AND s.status IN ('APPROVED','REJECTED')) AS showcase
            FROM events e WHERE e.owner_id=? AND e.status IN ('DRAFT','PUBLISHED')""", rs -> {
            UUID id = rs.getObject("id", UUID.class); String t = rs.getString("title"); String base = "/events/" + id;
            add(out, "PAYMENTS", id, t, rs.getInt("pay"), "%d gift%s waiting for you to confirm", base + "/transactions", "warning");
            add(out, "WISHES", id, t, rs.getInt("wishes"), "%d wish%s to approve", base + "/celebration", "info");
            add(out, "RSVPS", id, t, rs.getInt("rsvps"), "%d new RSVP%s in the last day", base + "/celebration", "good");
            add(out, "REPLIES", id, t, rs.getInt("replies"), "%d WhatsApp repl%s in the last day", base + "/whatsapp", "good");
            add(out, "VENDOR_DUES", id, t, rs.getInt("dues"), "%d vendor payment%s due within 3 days", base + "/budget", "warning");
            String sc = rs.getString("showcase");
            if (sc != null) out.add(new Item("SHOWCASE", id, t, 1, sc.equals("APPROVED") ? "Your celebration is live in the showcase" : "Your showcase submission needs a change", base + "/showcase", sc.equals("APPROVED") ? "good" : "info"));
        }, today, me);
        // Tasks assigned to me, or unassigned tasks on my own events.
        db.query("""
            SELECT e.id, e.title, (e.owner_id=?) AS owner, count(*) FILTER (WHERE t.due_date<?) AS late, count(*) FILTER (WHERE t.due_date=?) AS today
            FROM event_tasks t JOIN events e ON e.id=t.event_id
            WHERE t.done_at IS NULL AND t.due_date<=? AND (t.assignee_id=? OR (t.assignee_id IS NULL AND e.owner_id=?)) AND e.status IN ('DRAFT','PUBLISHED')
            GROUP BY e.id, e.title, e.owner_id""", rs -> {
            UUID id = rs.getObject("id", UUID.class);
            String href = (rs.getBoolean("owner") ? "/events/" : "/helping/") + id + "/checklist";
            add(out, "TASKS_LATE", id, rs.getString("title"), rs.getInt("late"), "%d overdue task%s", href, "warning");
            add(out, "TASKS_TODAY", id, rs.getString("title"), rs.getInt("today"), "%d task%s due today", href, "info");
        }, me, today, today, today, me, me);
        out.sort(Comparator.comparingInt((Item i) -> i.tone().equals("warning") ? 0 : i.tone().equals("info") ? 1 : 2));
        return out;
    }

    /** "3 gifts to confirm · 2 overdue tasks · 5 new RSVPs". */
    public static String summary(List<Item> items) {
        Map<String, Integer> totals = new LinkedHashMap<>();
        for (Item i : items) totals.merge(i.kind(), i.count(), Integer::sum);
        List<String> parts = new ArrayList<>();
        totals.forEach((k, n) -> parts.add(switch (k) {
            case "PAYMENTS" -> plural(n, "gift", "gifts") + " to confirm";
            case "WISHES" -> plural(n, "wish", "wishes") + " to approve";
            case "RSVPS" -> plural(n, "new RSVP", "new RSVPs");
            case "REPLIES" -> plural(n, "WhatsApp reply", "WhatsApp replies");
            case "VENDOR_DUES" -> plural(n, "vendor payment", "vendor payments") + " due soon";
            case "TASKS_LATE" -> plural(n, "overdue task", "overdue tasks");
            case "TASKS_TODAY" -> plural(n, "task", "tasks") + " due today";
            default -> plural(n, "update", "updates");
        }));
        return String.join(" · ", parts);
    }

    /** Daily digest at 8:30 IST. Skips hosts without the module, with notifications off, or already sent today. */
    @Scheduled(cron = "${gift.notifications.digest-cron:0 30 8 * * *}", zone = "Asia/Kolkata")
    public void digest() { sendDigests(); }

    public int sendDigests() {
        LocalDate today = LocalDate.now(INDIA);
        int sent = 0;
        List<Map<String, Object>> hosts = db.queryForList("""
            SELECT u.id, u.email, COALESCE(NULLIF(u.full_name,''), u.username, 'there') AS name FROM users u
            LEFT JOIN user_profile_preferences p ON p.user_id=u.id
            WHERE u.role='CUSTOMER' AND u.enabled AND COALESCE(p.event_notifications, TRUE)
              AND NOT EXISTS (SELECT 1 FROM host_digest_log l WHERE l.user_id=u.id AND l.sent_on=?)""", today);
        for (var h : hosts) {
            UUID id = (UUID) h.get("id");
            if (!permissions.enabled(id, MODULE)) continue;
            List<Item> items = items(id);
            if (items.isEmpty()) continue;
            if (db.update("INSERT INTO host_digest_log(user_id,sent_on,items) VALUES(?,?,?) ON CONFLICT DO NOTHING", id, today, items.size()) == 0) continue;
            String name = String.valueOf(h.get("name")).split(" ")[0];
            outbox.enqueue("digest:" + id + ":" + today, "attention-digest", (String) h.get("email"),
                Map.of("recipientName", name, "summary", summary(items), "dashboardUrl", "/dashboard"), "en");
            sent++;
        }
        return sent;
    }

    private static void add(List<Item> out, String kind, UUID id, String title, int n, String fmt, String href, String tone) {
        if (n <= 0) return;
        String suffix = fmt.contains("repl%s") ? (n == 1 ? "y" : "ies") : fmt.contains("wish%s") ? (n == 1 ? "" : "es") : (n == 1 ? "" : "s");
        out.add(new Item(kind, id, title, n, String.format(fmt, n, suffix), href, tone));
    }
    private static String plural(int n, String one, String many) { return n + " " + (n == 1 ? one : many); }
}
