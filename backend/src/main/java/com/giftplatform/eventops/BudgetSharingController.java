package com.giftplatform.eventops;

import com.giftplatform.admin.EffectiveModulePermissionService;
import com.giftplatform.cohost.EventAccess;
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
 * Share the budget (and vendor bookings) with family. The owner creates a one-time link (stored only as a hash,
 * valid 7 days) with view-only or edit access; the family member signs in and accepts it. The owner can change
 * access or remove people at any time, even after switching the feature off.
 */
@RestController
public class BudgetSharingController extends EventOpsSupport {
    static final Set<String> LEVELS = Set.of("VIEW", "EDIT");
    private static final SecureRandom RANDOM = new SecureRandom();

    private final JdbcTemplate db;
    private final EventRepository events;
    private final ActorContext actor;
    private final EventAccess access;
    private final EffectiveModulePermissionService permissions;

    public BudgetSharingController(JdbcTemplate db, EventRepository events, ActorContext actor, EventAccess access, EffectiveModulePermissionService permissions) {
        this.db = db; this.events = events; this.actor = actor; this.access = access; this.permissions = permissions;
    }

    public record Member(UUID userId, String name, String access, OffsetDateTime joinedAt) {}
    public record Invite(UUID id, String label, String access, OffsetDateTime createdAt, OffsetDateTime expiresAt, String status, String acceptedBy) {}
    public record Sharing(List<Member> members, List<Invite> invites) {}
    public record InviteInput(String label, String access) {}
    public record CreatedInvite(Invite invite, String token) {}
    public record AccessInput(String access) {}
    public record Preview(String eventTitle, LocalDate eventDate, String hostName, String access, String status) {}
    public record Shared(UUID eventId, String title, LocalDate eventDate, String hostName, String access, boolean budget, boolean vendors) {}

    // ---- Owner ----

    @GetMapping("/api/v1/events/{id}/budget/sharing")
    public ResponseEntity<Sharing> sharing(@PathVariable UUID id) {
        access.requireOwner(id, BudgetAccess.SHARING);
        List<Member> members = db.query("""
            SELECT m.user_id, COALESCE(NULLIF(u.full_name,''), u.username, 'GiftWise member') AS name, m.access, m.created_at
            FROM event_budget_members m JOIN users u ON u.id=m.user_id WHERE m.event_id=? ORDER BY m.created_at""",
            (rs, i) -> new Member(rs.getObject(1, UUID.class), rs.getString(2), rs.getString(3), rs.getObject(4, OffsetDateTime.class)), id);
        List<Invite> invites = db.query("""
            SELECT i.*, COALESCE(NULLIF(u.full_name,''), u.username) AS accepted_name FROM event_budget_invites i
            LEFT JOIN users u ON u.id=i.accepted_by WHERE i.event_id=? ORDER BY i.created_at DESC LIMIT 50""", (rs, i) -> invite(rs), id);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new Sharing(members, invites));
    }

    @PostMapping("/api/v1/events/{id}/budget/sharing/invites")
    @Transactional
    public CreatedInvite invite(@PathVariable UUID id, @RequestBody InviteInput in) {
        Event e = access.requireOwner(id, BudgetAccess.SHARING);
        String level = level(in == null ? null : in.access());
        String label = required(in.label(), 80, "Who this link is for");
        Integer open = db.queryForObject("SELECT count(*) FROM event_budget_invites WHERE event_id=? AND accepted_at IS NULL AND revoked_at IS NULL AND expires_at>now()", Integer.class, id);
        if (open != null && open >= 10) throw bad("You have 10 open links. Cancel some before creating more.");
        Integer members = db.queryForObject("SELECT count(*) FROM event_budget_members WHERE event_id=?", Integer.class, id);
        if (members != null && members >= 15) throw bad("A budget can be shared with up to 15 people");
        byte[] raw = new byte[24]; RANDOM.nextBytes(raw);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        UUID inviteId = UUID.randomUUID();
        db.update("INSERT INTO event_budget_invites(id,event_id,token_hash,access,label,invited_by,expires_at) VALUES(?,?,?,?,?,?,now()+interval '7 days')",
            inviteId, e.getId(), hash(token), level, label, actor.requireAuthenticatedUserId());
        return new CreatedInvite(db.query("SELECT i.*, NULL AS accepted_name FROM event_budget_invites i WHERE id=?", (rs, i) -> invite(rs), inviteId).getFirst(), token);
    }

    @PostMapping("/api/v1/events/{id}/budget/sharing/invites/{inviteId}/revoke")
    public Map<String, Object> revoke(@PathVariable UUID id, @PathVariable UUID inviteId) {
        access.requireOwner(id, null);
        if (db.update("UPDATE event_budget_invites SET revoked_at=now() WHERE id=? AND event_id=? AND accepted_at IS NULL AND revoked_at IS NULL", inviteId, id) == 0) throw notFound();
        return Map.of("revoked", true);
    }

    @PostMapping("/api/v1/events/{id}/budget/sharing/members/{userId}/access")
    public Map<String, Object> change(@PathVariable UUID id, @PathVariable UUID userId, @RequestBody AccessInput in) {
        access.requireOwner(id, BudgetAccess.SHARING);
        String level = level(in == null ? null : in.access());
        if (db.update("UPDATE event_budget_members SET access=? WHERE event_id=? AND user_id=?", level, id, userId) == 0) throw notFound();
        return Map.of("access", level);
    }

    /** Works even with the feature switched off, so owners can always take access away. */
    @PostMapping("/api/v1/events/{id}/budget/sharing/members/{userId}/remove")
    public Map<String, Object> remove(@PathVariable UUID id, @PathVariable UUID userId) {
        access.requireOwner(id, null);
        if (db.update("DELETE FROM event_budget_members WHERE event_id=? AND user_id=?", id, userId) == 0) throw notFound();
        return Map.of("removed", true);
    }

    // ---- Family member ----

    @GetMapping("/api/v1/me/budget-invites/{token}")
    public ResponseEntity<Preview> preview(@PathVariable String token) {
        actor.requireAuthenticatedUserId();
        var row = db.queryForList("SELECT * FROM event_budget_invites WHERE token_hash=?", hash(clean(token)));
        if (row.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "This link is not valid");
        Event e = events.findById((UUID) row.getFirst().get("event_id")).orElseThrow(EventOpsSupport::notFound);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new Preview(e.getTitle(), e.getEventDate(), e.getHostDisplayName(),
            (String) row.getFirst().get("access"), status(row.getFirst())));
    }

    @PostMapping("/api/v1/me/budget-invites/{token}/accept")
    @Transactional
    public Map<String, Object> accept(@PathVariable String token) {
        UUID me = actor.requireAuthenticatedUserId();
        var rows = db.queryForList("SELECT * FROM event_budget_invites WHERE token_hash=? FOR UPDATE", hash(clean(token)));
        if (rows.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "This link is not valid");
        var invite = rows.getFirst();
        UUID eventId = (UUID) invite.get("event_id");
        Event e = events.findById(eventId).orElseThrow(EventOpsSupport::notFound);
        if (me.equals(invite.get("accepted_by"))) return Map.of("eventId", eventId, "access", invite.get("access"));
        String status = status(invite);
        if (!"OPEN".equals(status)) throw conflict(switch (status) {
            case "ACCEPTED" -> "This link has already been used. Ask for a new one.";
            case "REVOKED" -> "The host cancelled this link.";
            default -> "This link has expired. Ask for a new one.";
        });
        if (me.equals(e.getOwnerId())) throw conflict("You already own this celebration.");
        if (!permissions.eventEnabled(e.getOwnerId(), eventId, BudgetAccess.SHARING)) throw conflict("Budget sharing is not available for this celebration right now.");
        db.update("INSERT INTO event_budget_members(event_id,user_id,access,added_by) VALUES(?,?,?,?) ON CONFLICT(event_id,user_id) DO UPDATE SET access=EXCLUDED.access",
            eventId, me, invite.get("access"), invite.get("invited_by"));
        db.update("UPDATE event_budget_invites SET accepted_by=?,accepted_at=now() WHERE id=?", me, invite.get("id"));
        return Map.of("eventId", eventId, "access", invite.get("access"));
    }

    @GetMapping("/api/v1/me/shared-budgets")
    public ResponseEntity<List<Shared>> mine() {
        UUID me = actor.requireAuthenticatedUserId();
        List<Shared> out = new ArrayList<>();
        db.query("""
            SELECT e.id, e.title, e.event_date, e.host_display_name, e.owner_id, m.access FROM event_budget_members m JOIN events e ON e.id=m.event_id
            WHERE m.user_id=? AND e.status<>'CANCELLED' ORDER BY e.event_date NULLS LAST, e.title""", rs -> {
            UUID eventId = rs.getObject("id", UUID.class), owner = rs.getObject("owner_id", UUID.class);
            if (!permissions.eventEnabled(owner, eventId, BudgetAccess.SHARING)) return;
            out.add(new Shared(eventId, rs.getString("title"), rs.getObject("event_date", LocalDate.class), rs.getString("host_display_name"), rs.getString("access"),
                permissions.eventEnabled(owner, eventId, EventBudgetController.MODULE), permissions.eventEnabled(owner, eventId, VendorBookingsController.MODULE)));
        }, me);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(out);
    }

    @PostMapping("/api/v1/me/shared-budgets/{eventId}/leave")
    public Map<String, Object> leave(@PathVariable UUID eventId) {
        if (db.update("DELETE FROM event_budget_members WHERE event_id=? AND user_id=?", eventId, actor.requireAuthenticatedUserId()) == 0) throw notFound();
        return Map.of("left", true);
    }

    // ---- Helpers ----

    private static String level(String v) {
        String s = v == null ? "" : v.strip().toUpperCase(Locale.ROOT);
        if (!LEVELS.contains(s)) throw bad("Choose view only or can edit");
        return s;
    }
    private static String clean(String token) {
        if (token == null || !token.matches("[A-Za-z0-9_-]{32}")) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "This link is not valid");
        return token;
    }
    static String hash(String token) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8))); }
        catch (Exception ex) { throw new IllegalStateException(ex); }
    }
    private static String status(Map<String, Object> row) {
        if (row.get("accepted_at") != null) return "ACCEPTED";
        if (row.get("revoked_at") != null) return "REVOKED";
        Object exp = row.get("expires_at");
        OffsetDateTime expires = exp instanceof java.sql.Timestamp ts ? ts.toInstant().atOffset(java.time.ZoneOffset.UTC) : (OffsetDateTime) exp;
        return expires.isBefore(OffsetDateTime.now()) ? "EXPIRED" : "OPEN";
    }
    private static Invite invite(java.sql.ResultSet rs) throws java.sql.SQLException {
        OffsetDateTime expires = rs.getObject("expires_at", OffsetDateTime.class);
        String status = rs.getObject("accepted_at") != null ? "ACCEPTED" : rs.getObject("revoked_at") != null ? "REVOKED" : expires.isBefore(OffsetDateTime.now()) ? "EXPIRED" : "OPEN";
        return new Invite(rs.getObject("id", UUID.class), rs.getString("label"), rs.getString("access"), rs.getObject("created_at", OffsetDateTime.class), expires, status, rs.getString("accepted_name"));
    }
}
