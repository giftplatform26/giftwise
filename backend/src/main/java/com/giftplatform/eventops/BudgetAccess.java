package com.giftplatform.eventops;

import com.giftplatform.admin.EffectiveModulePermissionService;
import com.giftplatform.event.entity.Event;
import com.giftplatform.event.repository.EventRepository;
import com.giftplatform.security.ActorContext;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

/**
 * Who may open the budget and vendor bookings: the owner always; family members the owner shared the budget with,
 * only while BUDGET_SHARING is on for the event (VIEW = read only, EDIT = can change). Co-hosts get nothing here:
 * money plans stay private unless shared on purpose. Anyone else gets 404.
 */
@Service
public class BudgetAccess {
    public static final String SHARING = "BUDGET_SHARING";
    public record Who(Event event, UUID userId, boolean owner, String access) {
        public boolean canEdit() { return owner || "EDIT".equals(access); }
        public String level() { return owner ? "OWNER" : access; }
    }

    private final JdbcTemplate db;
    private final EventRepository events;
    private final ActorContext actor;
    private final EffectiveModulePermissionService permissions;

    public BudgetAccess(JdbcTemplate db, EventRepository events, ActorContext actor, EffectiveModulePermissionService permissions) {
        this.db = db; this.events = events; this.actor = actor; this.permissions = permissions;
    }

    public Who require(UUID eventId, boolean edit, String module) {
        UUID user = actor.requireAuthenticatedUserId();
        Event e = events.findById(eventId).orElseThrow(BudgetAccess::notFound);
        Who who;
        if (user.equals(e.getOwnerId())) who = new Who(e, user, true, null);
        else {
            String access = db.query("SELECT access FROM event_budget_members WHERE event_id=? AND user_id=?", (rs, i) -> rs.getString(1), eventId, user)
                .stream().findFirst().orElse(null);
            if (access == null || !permissions.eventEnabled(e.getOwnerId(), eventId, SHARING)) throw notFound();
            who = new Who(e, user, false, access);
        }
        if (module != null) permissions.requireEvent(e.getOwnerId(), eventId, module);
        if (edit && !who.canEdit()) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can view this budget but not change it");
        return who;
    }

    static ResponseStatusException notFound() { return new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"); }
}
