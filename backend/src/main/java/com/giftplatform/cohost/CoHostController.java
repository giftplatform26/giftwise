package com.giftplatform.cohost;

import com.giftplatform.admin.EffectiveModulePermissionService;
import com.giftplatform.event.entity.Event;
import com.giftplatform.event.repository.EventRepository;
import com.giftplatform.security.ActorContext;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * Co-hosts and gift-desk helpers. The owner shares a one-time invite link (stored only as a hash, valid 7 days);
 * the person signs in to GiftWise and accepts it. Owners can change roles or remove people at any time.
 */
@RestController
public class CoHostController {
    static final Set<String> ROLES = Set.of("CO_HOST", "GIFT_DESK");
    private static final SecureRandom RANDOM = new SecureRandom();

    private final JdbcTemplate db;
    private final EventRepository events;
    private final ActorContext actor;
    private final EventAccess access;
    private final EffectiveModulePermissionService permissions;

    public CoHostController(JdbcTemplate db, EventRepository events, ActorContext actor, EventAccess access, EffectiveModulePermissionService permissions) {
        this.db = db; this.events = events; this.actor = actor; this.access = access; this.permissions = permissions;
    }

    public record Member(UUID userId, String name, String role, OffsetDateTime joinedAt) {}
    public record Invite(UUID id, String label, String role, OffsetDateTime createdAt, OffsetDateTime expiresAt, String status, String acceptedBy) {}
    public record Team(List<Member> members, List<Invite> invites) {}
    public record InviteInput(String label, String role) {}
    public record CreatedInvite(Invite invite, String token) {}
    public record RoleInput(String role) {}
    public record Preview(String eventTitle, LocalDate eventDate, String hostName, String role, String status) {}
    public record Helping(UUID eventId, String title, LocalDate eventDate, String hostName, String role, List<String> capabilities, Map<String, Boolean> features) {}

    // ---- Owner ----

    @GetMapping("/api/v1/events/{id}/co-hosts")
    public ResponseEntity<Team> team(@PathVariable UUID id) {
        access.requireOwner(id, EventAccess.MODULE);
        List<Member> members = db.query("""
            SELECT c.user_id, COALESCE(NULLIF(u.full_name,''), u.username, 'GiftWise member') AS name, c.role, c.created_at
            FROM event_co_hosts c JOIN users u ON u.id=c.user_id WHERE c.event_id=? ORDER BY c.created_at""",
            (rs, i) -> new Member(rs.getObject(1, UUID.class), rs.getString(2), rs.getString(3), rs.getObject(4, OffsetDateTime.class)), id);
        List<Invite> invites = db.query("""
            SELECT i.*, COALESCE(NULLIF(u.full_name,''), u.username) AS accepted_name FROM event_co_host_invites i
            LEFT JOIN users u ON u.id=i.accepted_by WHERE i.event_id=? ORDER BY i.created_at DESC LIMIT 50""", (rs, i) -> invite(rs), id);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new Team(members, invites));
    }

    @PostMapping("/api/v1/events/{id}/co-hosts/invites")
    @Transactional
    public CreatedInvite invite(@PathVariable UUID id, @RequestBody InviteInput in) {
        Event e = access.requireOwner(id, EventAccess.MODULE);
        String role = role(in == null ? null : in.role());
        String label = in.label() == null ? "" : in.label().strip();
        if (label.isEmpty() || label.length() > 80 || label.chars().anyMatch(Character::isISOControl)) throw bad("Add a name for this invite (up to 80 characters)");
        Integer open = db.queryForObject("SELECT count(*) FROM event_co_host_invites WHERE event_id=? AND accepted_at IS NULL AND revoked_at IS NULL AND expires_at>now()", Integer.class, id);
        if (open != null && open >= 20) throw bad("You have 20 open invites. Revoke some before creating more.");
        Integer team = db.queryForObject("SELECT count(*) FROM event_co_hosts WHERE event_id=?", Integer.class, id);
        if (team != null && team >= 30) throw bad("A celebration can have up to 30 co-hosts and helpers");
        byte[] raw = new byte[24]; RANDOM.nextBytes(raw);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        UUID inviteId = UUID.randomUUID();
        db.update("INSERT INTO event_co_host_invites(id,event_id,token_hash,role,label,invited_by,expires_at) VALUES(?,?,?,?,?,?,now()+interval '7 days')",
            inviteId, e.getId(), hash(token), role, label, actor.requireAuthenticatedUserId());
        Invite created = db.query("SELECT i.*, NULL AS accepted_name FROM event_co_host_invites i WHERE id=?", (rs, i) -> invite(rs), inviteId).getFirst();
        return new CreatedInvite(created, token);
    }

    @PostMapping("/api/v1/events/{id}/co-hosts/invites/{inviteId}/revoke")
    public Map<String, Object> revoke(@PathVariable UUID id, @PathVariable UUID inviteId) {
        access.requireOwner(id, null);
        if (db.update("UPDATE event_co_host_invites SET revoked_at=now() WHERE id=? AND event_id=? AND accepted_at IS NULL AND revoked_at IS NULL", inviteId, id) == 0)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return Map.of("revoked", true);
    }

    @PostMapping("/api/v1/events/{id}/co-hosts/{userId}/role")
    public Map<String, Object> changeRole(@PathVariable UUID id, @PathVariable UUID userId, @RequestBody RoleInput in) {
        access.requireOwner(id, EventAccess.MODULE);
        String role = role(in == null ? null : in.role());
        if (db.update("UPDATE event_co_hosts SET role=? WHERE event_id=? AND user_id=?", role, id, userId) == 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return Map.of("role", role);
    }

    /** Removal works even if the module was switched off, so owners can always take access away. */
    @PostMapping("/api/v1/events/{id}/co-hosts/{userId}/remove")
    public Map<String, Object> remove(@PathVariable UUID id, @PathVariable UUID userId) {
        access.requireOwner(id, null);
        if (db.update("DELETE FROM event_co_hosts WHERE event_id=? AND user_id=?", id, userId) == 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return Map.of("removed", true);
    }

    // ---- Invitee ----

    @GetMapping("/api/v1/me/co-host-invites/{token}")
    public ResponseEntity<Preview> preview(@PathVariable String token) {
        var row = inviteRow(token);
        Event e = events.findById((UUID) row.get("event_id")).orElseThrow(EventAccess::notFound);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
            .body(new Preview(e.getTitle(), e.getEventDate(), e.getHostDisplayName(), (String) row.get("role"), status(row)));
    }

    @PostMapping("/api/v1/me/co-host-invites/{token}/accept")
    @Transactional
    public Map<String, Object> accept(@PathVariable String token) {
        UUID me = actor.requireAuthenticatedUserId();
        var row = db.queryForList("SELECT * FROM event_co_host_invites WHERE token_hash=? FOR UPDATE", hash(clean(token)));
        if (row.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "This invite link is not valid");
        var invite = row.getFirst();
        UUID eventId = (UUID) invite.get("event_id");
        Event e = events.findById(eventId).orElseThrow(EventAccess::notFound);
        String status = status(invite);
        if (me.equals(invite.get("accepted_by"))) return Map.of("eventId", eventId, "role", invite.get("role"));
        if (!"OPEN".equals(status)) throw new ResponseStatusException(HttpStatus.CONFLICT, switch (status) {
            case "ACCEPTED" -> "This invite has already been used. Ask the host for a new link.";
            case "REVOKED" -> "The host cancelled this invite.";
            default -> "This invite has expired. Ask the host for a new link.";
        });
        if (me.equals(e.getOwnerId())) throw new ResponseStatusException(HttpStatus.CONFLICT, "You already own this celebration.");
        if (!permissions.eventEnabled(e.getOwnerId(), eventId, EventAccess.MODULE))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Co-hosting is not available for this celebration right now.");
        db.update("INSERT INTO event_co_hosts(event_id,user_id,role,invited_by) VALUES(?,?,?,?) ON CONFLICT(event_id,user_id) DO UPDATE SET role=EXCLUDED.role",
            eventId, me, invite.get("role"), invite.get("invited_by"));
        db.update("UPDATE event_co_host_invites SET accepted_by=?,accepted_at=now() WHERE id=?", me, invite.get("id"));
        return Map.of("eventId", eventId, "role", invite.get("role"));
    }

    @GetMapping("/api/v1/me/co-hosting")
    public ResponseEntity<List<Helping>> helping() {
        UUID me = actor.requireAuthenticatedUserId();
        List<Helping> out = new ArrayList<>();
        db.query("""
            SELECT e.id, e.title, e.event_date, e.host_display_name, e.owner_id, c.role FROM event_co_hosts c JOIN events e ON e.id=c.event_id
            WHERE c.user_id=? AND e.status<>'CANCELLED' ORDER BY e.event_date NULLS LAST, e.title""", rs -> {
            UUID eventId = rs.getObject("id", UUID.class), owner = rs.getObject("owner_id", UUID.class);
            if (!permissions.eventEnabled(owner, eventId, EventAccess.MODULE)) return;
            String role = rs.getString("role");
            Map<String, Boolean> features = Map.of(
                "SHAGUN_REGISTER", permissions.eventEnabled(owner, eventId, "SHAGUN_REGISTER"),
                "LIVE_WALL", permissions.eventEnabled(owner, eventId, "LIVE_WALL"),
                "EVENT_FUNCTIONS", permissions.eventEnabled(owner, eventId, "EVENT_FUNCTIONS"),
                "GUESTBOOK", permissions.enabled(owner, "GUESTBOOK"), "RSVP", permissions.enabled(owner, "RSVP"));
            out.add(new Helping(eventId, rs.getString("title"), rs.getObject("event_date", LocalDate.class), rs.getString("host_display_name"), role,
                EventAccess.ROLES.get(role).stream().map(Enum::name).sorted().toList(), features));
        }, me);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(out);
    }

    @PostMapping("/api/v1/me/co-hosting/{eventId}/leave")
    public Map<String, Object> leave(@PathVariable UUID eventId) {
        if (db.update("DELETE FROM event_co_hosts WHERE event_id=? AND user_id=?", eventId, actor.requireAuthenticatedUserId()) == 0)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return Map.of("left", true);
    }

    // ---- Helpers ----

    private Map<String, Object> inviteRow(String token) {
        actor.requireAuthenticatedUserId();
        var row = db.queryForList("SELECT * FROM event_co_host_invites WHERE token_hash=?", hash(clean(token)));
        if (row.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "This invite link is not valid");
        return row.getFirst();
    }
    private static String clean(String token) {
        if (token == null || !token.matches("[A-Za-z0-9_-]{32}")) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "This invite link is not valid");
        return token;
    }
    private static String status(Map<String, Object> r) {
        if (r.get("accepted_at") != null) return "ACCEPTED";
        if (r.get("revoked_at") != null) return "REVOKED";
        OffsetDateTime expires = r.get("expires_at") instanceof java.sql.Timestamp t ? t.toInstant().atOffset(java.time.ZoneOffset.UTC) : (OffsetDateTime) r.get("expires_at");
        return expires.isBefore(OffsetDateTime.now()) ? "EXPIRED" : "OPEN";
    }
    private static Invite invite(java.sql.ResultSet rs) throws java.sql.SQLException {
        OffsetDateTime expires = rs.getObject("expires_at", OffsetDateTime.class);
        String status = rs.getObject("accepted_at") != null ? "ACCEPTED" : rs.getObject("revoked_at") != null ? "REVOKED"
            : expires.isBefore(OffsetDateTime.now()) ? "EXPIRED" : "OPEN";
        return new Invite(rs.getObject("id", UUID.class), rs.getString("label"), rs.getString("role"), rs.getObject("created_at", OffsetDateTime.class),
            expires, status, rs.getString("accepted_name"));
    }
    private static String role(String value) {
        String r = value == null ? "" : value.strip().toUpperCase(Locale.ROOT);
        if (!ROLES.contains(r)) throw bad("Choose co-host or gift-desk helper");
        return r;
    }
    static String hash(String token) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8))); }
        catch (Exception e) { throw new IllegalStateException(e); }
    }
    private static ResponseStatusException bad(String m) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, m); }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> reason(ResponseStatusException ex) {
        int code = ex.getStatusCode().value();
        if ((code == 400 || code == 409 || code == 404) && ex.getReason() != null && !ex.getReason().equals("Event not found"))
            return ResponseEntity.status(code).body(Map.of("error", ex.getReason()));
        return ResponseEntity.status(ex.getStatusCode()).build();
    }
}
