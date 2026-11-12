package com.giftplatform.eventops;

import com.giftplatform.admin.EffectiveModulePermissionService;
import com.giftplatform.cohost.EventAccess;
import com.giftplatform.event.entity.Event;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * Setup guide for the event overview: one step per tool that is switched on for this celebration, ticked once
 * the host has started using it. Tools that are off never appear, so hosts only see what they can use.
 */
@RestController
public class EventSetupController {
    private final JdbcTemplate db;
    private final EventAccess access;
    private final EffectiveModulePermissionService permissions;

    public EventSetupController(JdbcTemplate db, EventAccess access, EffectiveModulePermissionService permissions) {
        this.db = db; this.access = access; this.permissions = permissions;
    }

    public record Step(String code, String label, String hint, boolean done, String href) {}
    public record Setup(List<Step> steps, int done, int total) {}

    @GetMapping("/api/v1/events/{id}/setup")
    public ResponseEntity<Setup> setup(@PathVariable UUID id) {
        Event e = access.requireOwner(id, null);
        Map<String, Boolean> f = permissions.eventModules(e.getOwnerId(), id);
        String base = "/events/" + id;
        List<Step> steps = new ArrayList<>();
        if (Boolean.TRUE.equals(f.get("GIFTING_ENABLED")))
            steps.add(step("RECEIVING", "Connect a receiving account", "So guests can send gifts straight to you", exists("SELECT 1 FROM event_payment_account_links WHERE event_id=? AND enabled", id), base + "/receiving-accounts"));
        steps.add(step("PUBLISH", "Publish your invitation", "Share it once everything looks right", e.getStatus() != com.giftplatform.event.entity.EventStatus.DRAFT, base + "/preview"));
        add(steps, f, "EVENT_FUNCTIONS", "Add your functions", "Each with its own time, venue and RSVP", "SELECT 1 FROM event_functions WHERE event_id=?", id, base + "/functions");
        add(steps, f, "PLANNING_CHECKLIST", "Start your planning checklist", "One tap adds the usual tasks for your occasion", "SELECT 1 FROM event_tasks WHERE event_id=?", id, base + "/checklist");
        add(steps, f, "EVENT_BUDGET", "Set up your budget", "Track planned, booked and paid", "SELECT 1 FROM event_budget_lines WHERE event_id=?", id, base + "/budget");
        add(steps, f, "CO_HOSTS", "Invite a co-host", "Family can help with guests and the gift desk", "SELECT 1 FROM event_co_hosts WHERE event_id=?", id, base + "/co-hosts");
        add(steps, f, "WHATSAPP_MESSAGING", "Add your guest list", "Then send the invitation on WhatsApp", "SELECT 1 FROM event_guest_contacts WHERE event_id=?", id, base + "/whatsapp");
        add(steps, f, "SEATING_PLAN", "Plan the seating", "Add tables and seat families", "SELECT 1 FROM event_seating_tables WHERE event_id=?", id, base + "/seating");
        add(steps, f, "GUEST_PASSES", "Create guest passes", "A QR per family for quick check-in", "SELECT 1 FROM event_guest_passes WHERE event_id=?", id, base + "/passes");
        add(steps, f, "RUN_SHEET", "Draft your run sheet", "Who does what on the day", "SELECT 1 FROM event_run_items WHERE event_id=?", id, base + "/run-sheet");
        add(steps, f, "LIVE_WALL", "Set up the live wall", "Wishes and photos on the big screen", "SELECT 1 FROM event_live_walls WHERE event_id=?", id, base + "/live-wall");
        add(steps, f, "CELEBRATION_SHOWCASE", "Share in the showcase", "Inspire other hosts (optional)", "SELECT 1 FROM event_showcase WHERE event_id=? AND status<>'WITHDRAWN'", id, base + "/showcase");
        int done = (int) steps.stream().filter(Step::done).count();
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new Setup(steps, done, steps.size()));
    }

    private void add(List<Step> steps, Map<String, Boolean> f, String module, String label, String hint, String sql, UUID id, String href) {
        if (Boolean.TRUE.equals(f.get(module))) steps.add(step(module, label, hint, exists(sql, id), href));
    }
    private boolean exists(String sql, UUID id) { return Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(" + sql + ")", Boolean.class, id)); }
    private static Step step(String code, String label, String hint, boolean done, String href) { return new Step(code, label, hint, done, href); }
}
