package com.giftplatform.acquisition;

import com.giftplatform.admin.EffectiveModulePermissionService;
import com.giftplatform.branding.SafeBrandImage;
import com.giftplatform.cohost.EventAccess;
import com.giftplatform.event.entity.Event;
import com.giftplatform.event.entity.EventStatus;
import com.giftplatform.event.repository.EventRepository;
import com.giftplatform.security.ActorContext;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * Planner and venue partners. A partner (a host granted PARTNER_ACCOUNTS) has a business profile, sets up
 * celebrations for clients and hands each one over with a one-time link: the client becomes the owner (so gifts
 * go to the client's own accounts) and the partner stays on as a PARTNER co-host. Guests see "Planned by …".
 */
@RestController
public class PartnerController {
    public static final String MODULE = "PARTNER_ACCOUNTS";
    static final Set<String> KINDS = Set.of("PLANNER", "VENUE", "DECORATOR", "CATERER", "PHOTOGRAPHER", "OTHER");
    private static final SecureRandom RANDOM = new SecureRandom();

    private final JdbcTemplate db;
    private final EventRepository events;
    private final ActorContext actor;
    private final EventAccess access;
    private final EffectiveModulePermissionService permissions;
    private final ShowcaseController showcase;

    public PartnerController(JdbcTemplate db, EventRepository events, ActorContext actor, EventAccess access, EffectiveModulePermissionService permissions,
                             ShowcaseController showcase) {
        this.db = db; this.events = events; this.actor = actor; this.access = access; this.permissions = permissions; this.showcase = showcase;
    }

    public record Profile(boolean exists, String slug, String businessName, String kind, String city, String phone, String website, String brandColor,
                          String bio, String logoDataUrl) {}
    public record Input(String slug, String businessName, String kind, String city, String phone, String website, String brandColor, String bio) {}
    public record Client(UUID eventId, String title, String occasion, LocalDate eventDate, String status, String clientLabel, String handover,
                         boolean owned, long rsvpYes) {}
    public record Handover(String token, OffsetDateTime expiresAt) {}
    public record HandoverInput(String clientLabel) {}
    public record Preview(String eventTitle, LocalDate eventDate, String partnerName, String status) {}
    public record PublicPartner(String businessName, String kind, String city, String phone, String website, String brandColor, String bio,
                                boolean hasLogo, List<ShowcaseController.Card> celebrations) {}
    public record Credit(String businessName, String slug, String kind, boolean hasLogo) {}

    // ---- Partner profile ----

    @GetMapping("/api/v1/me/partner")
    public ResponseEntity<Profile> profile() {
        UUID me = me();
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(read(me));
    }

    @PutMapping("/api/v1/me/partner")
    public Profile save(@RequestBody Input in) {
        UUID me = me();
        if (in == null) throw bad("Business details are required");
        String name = text(in.businessName(), 120, "Business name");
        if (name == null) throw bad("Add your business name");
        String slug = in.slug() == null ? "" : in.slug().strip().toLowerCase(Locale.ROOT);
        if (!slug.matches("[a-z0-9][a-z0-9-]{2,39}")) throw bad("Page address must be 3–40 lowercase letters, numbers or hyphens");
        String kind = in.kind() == null ? "" : in.kind().strip().toUpperCase(Locale.ROOT);
        if (!KINDS.contains(kind)) throw bad("Choose what kind of business you are");
        String phone = text(in.phone(), 20, "Phone");
        if (phone != null) { phone = phone.replaceAll("[\\s()\\-]", ""); if (!phone.matches("\\+[1-9][0-9]{7,14}")) throw bad("Phone must include the country code, e.g. +919876543210"); }
        String website = text(in.website(), 300, "Website");
        if (website != null && !website.matches("https://[^\\s<>\"]+")) throw bad("Website must start with https://");
        String color = in.brandColor() == null || in.brandColor().isBlank() ? "#683C55" : in.brandColor().strip();
        if (!color.matches("#[0-9A-Fa-f]{6}")) throw bad("Choose a colour like #683C55");
        Boolean taken = db.queryForObject("SELECT EXISTS(SELECT 1 FROM partner_profiles WHERE slug=? AND user_id<>?)", Boolean.class, slug, me);
        if (Boolean.TRUE.equals(taken)) throw new ResponseStatusException(HttpStatus.CONFLICT, "That page address is taken. Try another.");
        db.update("""
            INSERT INTO partner_profiles(user_id,slug,business_name,kind,city,phone,website,brand_color,bio) VALUES(?,?,?,?,?,?,?,?,?)
            ON CONFLICT(user_id) DO UPDATE SET slug=EXCLUDED.slug,business_name=EXCLUDED.business_name,kind=EXCLUDED.kind,city=EXCLUDED.city,
              phone=EXCLUDED.phone,website=EXCLUDED.website,brand_color=EXCLUDED.brand_color,bio=EXCLUDED.bio,updated_at=now()""",
            me, slug, name, kind, text(in.city(), 80, "City"), phone, website, color.toUpperCase(Locale.ROOT), text(in.bio(), 500, "About"));
        return read(me);
    }

    @PostMapping(value = "/api/v1/me/partner/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Profile logo(@RequestPart("file") MultipartFile file) throws java.io.IOException {
        UUID me = me();
        if (!read(me).exists()) throw bad("Save your business details first");
        byte[] png;
        try { png = SafeBrandImage.png(file.getBytes(), 320); } catch (IllegalArgumentException e) { throw bad(e.getMessage()); }
        if (png.length > 400_000) throw bad("This logo is too detailed. Try a simpler PNG or JPEG.");
        db.update("UPDATE partner_profiles SET logo_png=?,updated_at=now() WHERE user_id=?", png, me);
        return read(me);
    }

    @GetMapping("/api/v1/me/partner/clients")
    public ResponseEntity<List<Client>> clients() {
        UUID me = me();
        List<Client> out = db.query("""
            SELECT e.id, e.title, e.occasion_type, e.event_date, e.status, e.owner_id, l.client_label, l.handed_over_at, l.handover_expires_at,
                   (SELECT COALESCE(SUM(party_size),0) FROM event_rsvps r WHERE r.event_id=e.id AND r.attendance='YES') AS yes
            FROM events e LEFT JOIN event_partner_links l ON l.event_id=e.id
            WHERE e.status<>'CANCELLED' AND (l.partner_id=? OR (e.owner_id=? AND e.kind='CELEBRATION'))
            ORDER BY e.event_date NULLS LAST, e.created_at DESC LIMIT 300""", (rs, i) -> {
            boolean owned = me.equals(rs.getObject("owner_id", UUID.class));
            String handover = rs.getObject("handed_over_at") != null ? "HANDED_OVER"
                : rs.getObject("handover_expires_at") != null && rs.getObject("handover_expires_at", OffsetDateTime.class).isAfter(OffsetDateTime.now()) ? "INVITED" : "NOT_STARTED";
            return new Client(rs.getObject("id", UUID.class), rs.getString("title"), rs.getString("occasion_type"), rs.getObject("event_date", LocalDate.class),
                rs.getString("status"), rs.getString("client_label"), handover, owned, rs.getLong("yes"));
        }, me, me);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(out);
    }

    // ---- Handover to the client ----

    /** One-time link (stored hashed, 14 days) that makes the client the owner of this celebration. */
    @PostMapping("/api/v1/events/{id}/partner/handover")
    @Transactional
    public Handover startHandover(@PathVariable UUID id, @RequestBody HandoverInput in) {
        UUID me = me();
        Event e = access.requireOwner(id, MODULE);
        if (!read(me).exists()) throw bad("Create your partner profile first");
        if (e.getStatus() == EventStatus.CANCELLED) throw bad("This celebration is cancelled");
        requireTransferable(id);
        byte[] raw = new byte[24]; RANDOM.nextBytes(raw);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        OffsetDateTime expires = OffsetDateTime.now().plusDays(14);
        String label = text(in == null ? null : in.clientLabel(), 120, "Client name");
        db.update("""
            INSERT INTO event_partner_links(event_id,partner_id,client_label,handover_token_hash,handover_expires_at) VALUES(?,?,?,?,?)
            ON CONFLICT(event_id) DO UPDATE SET client_label=COALESCE(EXCLUDED.client_label,event_partner_links.client_label),
              handover_token_hash=EXCLUDED.handover_token_hash,handover_expires_at=EXCLUDED.handover_expires_at
            WHERE event_partner_links.partner_id=EXCLUDED.partner_id AND event_partner_links.handed_over_at IS NULL""", id, me, label, hash(token), expires);
        return new Handover(token, expires);
    }

    @GetMapping("/api/v1/me/partner-handover/{token}")
    public ResponseEntity<Preview> preview(@PathVariable String token) {
        actor.requireAuthenticatedUserId();
        var l = link(token);
        Event e = events.findById((UUID) l.get("event_id")).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        String partner = db.queryForObject("SELECT business_name FROM partner_profiles WHERE user_id=?", String.class, l.get("partner_id"));
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new Preview(e.getTitle(), e.getEventDate(), partner, status(l)));
    }

    @PostMapping("/api/v1/me/partner-handover/{token}/accept")
    @Transactional
    public Map<String, Object> accept(@PathVariable String token) {
        UUID me = actor.requireAuthenticatedUserId();
        var rows = db.queryForList("SELECT * FROM event_partner_links WHERE handover_token_hash=? FOR UPDATE", hash(clean(token)));
        if (rows.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "This link is not valid");
        var l = rows.getFirst();
        UUID eventId = (UUID) l.get("event_id"), partner = (UUID) l.get("partner_id");
        if (me.equals(l.get("client_id"))) return Map.of("eventId", eventId);
        String st = status(l);
        if (!st.equals("OPEN")) throw new ResponseStatusException(HttpStatus.CONFLICT, st.equals("USED") ? "This celebration has already been handed over." : "This link has expired. Ask your planner for a new one.");
        if (me.equals(partner)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Send this link to your client; it can't be accepted by you.");
        Event e = events.findById(eventId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!partner.equals(e.getOwnerId())) throw new ResponseStatusException(HttpStatus.CONFLICT, "This celebration has changed hands. Ask your planner for a new link.");
        requireTransferable(eventId);
        db.update("UPDATE events SET owner_id=? WHERE id=?", me, eventId);
        db.update("INSERT INTO event_co_hosts(event_id,user_id,role,invited_by) VALUES(?,?,'PARTNER',?) ON CONFLICT(event_id,user_id) DO UPDATE SET role='PARTNER'", eventId, partner, me);
        db.update("DELETE FROM event_co_hosts WHERE event_id=? AND user_id=?", eventId, me);
        // The client inherits the partner's host-level features for this celebration to keep working; the admin can change them any time.
        int copied = db.update("""
            INSERT INTO platform_host_module_grants(host_id,module_code,enabled)
            SELECT ?, module_code, TRUE FROM platform_host_module_grants WHERE host_id=? AND enabled AND module_code<>'PARTNER_ACCOUNTS'
            ON CONFLICT(host_id,module_code) DO NOTHING""", me, partner);
        db.update("UPDATE event_partner_links SET client_id=?,handed_over_at=now(),handover_token_hash=NULL WHERE event_id=?", me, eventId);
        db.update("INSERT INTO platform_admin_action_audit(id,actor_id,action,target_id,module_code) VALUES(?,?,?,?,?)", UUID.randomUUID(), me, "PARTNER_HANDOVER", eventId, MODULE);
        AcquisitionEventsController.record(db, "HANDOVER_ACCEPTED", me, eventId);
        return Map.of("eventId", eventId, "featuresGranted", copied);
    }

    // ---- Public ----

    @GetMapping("/api/public/partners/{slug}")
    public ResponseEntity<PublicPartner> publicProfile(@PathVariable String slug) {
        var rows = db.queryForList("SELECT * FROM partner_profiles WHERE slug=?", slug == null ? "" : slug.toLowerCase(Locale.ROOT));
        if (rows.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        var p = rows.getFirst();
        if (!permissions.enabled((UUID) p.get("user_id"), MODULE)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        List<ShowcaseController.Card> mine = Optional.ofNullable(showcase.showcase(48).getBody()).orElse(List.of()).stream()
            .filter(c -> slug.equalsIgnoreCase(c.partnerSlug())).toList();
        return ResponseEntity.ok().cacheControl(CacheControl.maxAge(Duration.ofMinutes(2))).body(new PublicPartner((String) p.get("business_name"), (String) p.get("kind"),
            (String) p.get("city"), (String) p.get("phone"), (String) p.get("website"), ((String) p.get("brand_color")).trim(), (String) p.get("bio"), p.get("logo_png") != null, mine));
    }

    @GetMapping(value = "/api/public/partners/{slug}/logo.png", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> publicLogo(@PathVariable String slug) {
        var rows = db.queryForList("SELECT user_id, logo_png FROM partner_profiles WHERE slug=?", slug == null ? "" : slug.toLowerCase(Locale.ROOT));
        if (rows.isEmpty() || rows.getFirst().get("logo_png") == null || !permissions.enabled((UUID) rows.getFirst().get("user_id"), MODULE)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return ResponseEntity.ok().cacheControl(CacheControl.maxAge(Duration.ofHours(1))).header("X-Content-Type-Options", "nosniff").body((byte[]) rows.getFirst().get("logo_png"));
    }

    /** "Planned by …" on the invitation, when this celebration is linked to a partner and the event switch is on. */
    @GetMapping("/api/public/events/{token}/partner")
    public ResponseEntity<Credit> credit(@PathVariable UUID token) {
        var rows = db.queryForList("""
            SELECT e.id, e.owner_id, p.business_name, p.slug, p.kind, (p.logo_png IS NOT NULL) AS has_logo, p.user_id AS partner
            FROM events e JOIN event_partner_links l ON l.event_id=e.id JOIN partner_profiles p ON p.user_id=l.partner_id
            WHERE e.public_token=? AND e.status='PUBLISHED'""", token);
        if (rows.isEmpty()) return ResponseEntity.noContent().build();
        var r = rows.getFirst();
        if (!permissions.eventEnabled((UUID) r.get("owner_id"), (UUID) r.get("id"), MODULE) || !permissions.enabled((UUID) r.get("partner"), MODULE))
            return ResponseEntity.noContent().build();
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new Credit((String) r.get("business_name"), (String) r.get("slug"), (String) r.get("kind"), (Boolean) r.get("has_logo")));
    }

    // ---- Helpers ----

    /** Payments and receiving accounts are tied to the owner, so ownership moves only before money is involved. */
    private void requireTransferable(UUID eventId) {
        Boolean money = db.queryForObject("""
            SELECT EXISTS(SELECT 1 FROM event_payment_account_links WHERE event_id=?) OR EXISTS(SELECT 1 FROM payment_orders WHERE event_id=?)
                OR EXISTS(SELECT 1 FROM guest_direct_transfer_claims WHERE event_id=?)
                OR EXISTS(SELECT 1 FROM host_upi_accounts WHERE event_id=?) OR EXISTS(SELECT 1 FROM host_bank_accounts WHERE event_id=?)""",
            Boolean.class, eventId, eventId, eventId, eventId, eventId);
        if (Boolean.TRUE.equals(money)) throw new ResponseStatusException(HttpStatus.CONFLICT,
            "Hand the celebration over before connecting receiving accounts or receiving gifts. Your client then adds their own UPI or bank account.");
    }
    private Profile read(UUID me) {
        var rows = db.queryForList("SELECT * FROM partner_profiles WHERE user_id=?", me);
        if (rows.isEmpty()) return new Profile(false, null, null, "PLANNER", null, null, null, "#683C55", null, null);
        var p = rows.getFirst();
        byte[] logo = (byte[]) p.get("logo_png");
        return new Profile(true, (String) p.get("slug"), (String) p.get("business_name"), (String) p.get("kind"), (String) p.get("city"), (String) p.get("phone"),
            (String) p.get("website"), ((String) p.get("brand_color")).trim(), (String) p.get("bio"), logo == null ? null : "data:image/png;base64," + Base64.getEncoder().encodeToString(logo));
    }
    private Map<String, Object> link(String token) {
        var rows = db.queryForList("SELECT * FROM event_partner_links WHERE handover_token_hash=?", hash(clean(token)));
        if (rows.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "This link is not valid");
        return rows.getFirst();
    }
    private static String status(Map<String, Object> l) {
        if (l.get("handed_over_at") != null) return "USED";
        Object exp = l.get("handover_expires_at");
        OffsetDateTime e = exp instanceof java.sql.Timestamp t ? t.toInstant().atOffset(java.time.ZoneOffset.UTC) : (OffsetDateTime) exp;
        return e == null || e.isBefore(OffsetDateTime.now()) ? "EXPIRED" : "OPEN";
    }
    private static String clean(String token) {
        if (token == null || !token.matches("[A-Za-z0-9_-]{32}")) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "This link is not valid");
        return token;
    }
    static String hash(String token) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8))); }
        catch (Exception e) { throw new IllegalStateException(e); }
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
        if ((code == 400 || code == 409) && ex.getReason() != null && !ex.getReason().equals("Event not found")) return ResponseEntity.status(code).body(Map.of("error", ex.getReason()));
        if (code == 404 && ex.getReason() != null && ex.getReason().startsWith("This link")) return ResponseEntity.status(code).body(Map.of("error", ex.getReason()));
        return ResponseEntity.status(ex.getStatusCode()).build();
    }
}
