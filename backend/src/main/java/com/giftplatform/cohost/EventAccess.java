package com.giftplatform.cohost;

import com.giftplatform.admin.EffectiveModulePermissionService;
import com.giftplatform.event.entity.Event;
import com.giftplatform.event.repository.EventRepository;
import com.giftplatform.security.ActorContext;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Who may use an event-day tool: the owner always; co-hosts and gift-desk helpers only for the capabilities
 * of their role, and only while the owner has CO_HOSTS (platform + host + event). Feature switches are
 * always evaluated against the owner. Anyone else gets 404 so event IDs reveal nothing.
 */
@Service
public class EventAccess {
    public enum Capability { REGISTER_ADD, REGISTER_MANAGE, GUESTS, LIVE_WALL, FUNCTIONS, PLANNING, CHECKIN, UPDATES, SEATING }
    public static final String MODULE = "CO_HOSTS";
    static final Map<String, Set<Capability>> ROLES = Map.of(
        "CO_HOST", Set.of(Capability.REGISTER_ADD, Capability.REGISTER_MANAGE, Capability.GUESTS, Capability.LIVE_WALL, Capability.FUNCTIONS,
            Capability.PLANNING, Capability.CHECKIN, Capability.UPDATES, Capability.SEATING),
        "GIFT_DESK", Set.of(Capability.REGISTER_ADD, Capability.CHECKIN),
        // A planner or venue partner keeps co-host abilities after handing an event over to the client.
        "PARTNER", Set.of(Capability.REGISTER_ADD, Capability.REGISTER_MANAGE, Capability.GUESTS, Capability.LIVE_WALL, Capability.FUNCTIONS,
            Capability.PLANNING, Capability.CHECKIN, Capability.UPDATES, Capability.SEATING));

    public record Access(Event event, UUID userId, boolean owner, String role) {
        public boolean can(Capability c) { return owner || (role != null && ROLES.getOrDefault(role, Set.of()).contains(c)); }
    }

    private final JdbcTemplate db;
    private final EventRepository events;
    private final ActorContext actor;
    private final EffectiveModulePermissionService permissions;

    public EventAccess(JdbcTemplate db, EventRepository events, ActorContext actor, EffectiveModulePermissionService permissions) {
        this.db = db; this.events = events; this.actor = actor; this.permissions = permissions;
    }

    /** Requires the capability and the feature module (checked for the owner, at platform, host and event level). */
    public Access require(UUID eventId, Capability capability, String featureModule) {
        UUID user = actor.requireAuthenticatedUserId();
        Event e = events.findById(eventId).orElseThrow(EventAccess::notFound);
        Access access;
        if (user.equals(e.getOwnerId())) access = new Access(e, user, true, null);
        else {
            String role = db.query("SELECT role FROM event_co_hosts WHERE event_id=? AND user_id=?", (rs, i) -> rs.getString(1), eventId, user)
                .stream().findFirst().orElse(null);
            if (role == null || !permissions.eventEnabled(e.getOwnerId(), eventId, MODULE)) throw notFound();
            access = new Access(e, user, false, role);
        }
        if (!access.can(capability)) throw access.owner() ? notFound() : new ResponseStatusException(HttpStatus.FORBIDDEN, "Your role does not include this");
        if (featureModule != null) permissions.requireEvent(e.getOwnerId(), eventId, featureModule);
        return access;
    }

    /** Owner-only, with the feature module enabled for this event. */
    public Event requireOwner(UUID eventId, String featureModule) {
        UUID user = actor.requireAuthenticatedUserId();
        Event e = events.findById(eventId).filter(x -> user.equals(x.getOwnerId())).orElseThrow(EventAccess::notFound);
        if (featureModule != null) permissions.requireEvent(user, eventId, featureModule);
        return e;
    }

    static ResponseStatusException notFound() { return new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"); }
}
