package com.giftplatform.admin;

import com.giftplatform.security.ActorContext;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

/**
 * Feature presets for the platform admin: switch on a whole set of modules in one action, for the platform,
 * one host, every host, or one event. Presets only ever turn things on; switching off stays per module.
 */
@RestController
@RequestMapping("/api/admin/console/presets")
public class ModulePresetsController {
    record Preset(String code, String name, String description, List<String> modules) {}
    static final List<String> ESSENTIALS = List.of("RSVP", "GUESTBOOK", "WHATSAPP_SHARING", "SOCIAL_SHARING", "GROUP_GIFTING", "CELEBRATION_PLANNING", "EVENT_ESSENTIALS");
    static final List<Preset> PRESETS = List.of(
        new Preset("ESSENTIALS", "Essentials", "RSVP, guestbook, sharing, group gifting, venue details and calendar.", ESSENTIALS),
        new Preset("WEDDING", "Wedding pack", "Everything for a multi-function wedding: schedule, co-hosts, gift register, live wall, check-in, run sheet, budget and more.",
            concat(ESSENTIALS, "GUEST_GIFTS", "GUEST_ATTENDANCE", "MEMORY_ALBUM", "EVENT_FUNCTIONS", "CATERING_HEADCOUNT", "CO_HOSTS", "SHAGUN_REGISTER", "LIVE_WALL",
                "GUEST_CHECKIN", "RUN_SHEET", "PLANNING_CHECKLIST", "EVENT_BUDGET", "LIVE_UPDATES", "RETURN_GIFTS", "MEMORY_BOOK", "STORY_CARDS", "WHATSAPP_MESSAGING",
                "EXPERIENCE_GIFTS", "SEATING_PLAN", "GUEST_PASSES", "GUEST_HOUSEHOLDS", "GUEST_TRAVEL", "SHARED_ALBUM", "GUEST_FEEDBACK", "VENDOR_BOOKINGS", "BUDGET_SHARING")),
        new Preset("PARTY", "Birthday & party pack", "Invitations, wishes, photos, story cards, a short checklist, live updates and return gifts.",
            concat(ESSENTIALS, "MEMORY_ALBUM", "STORY_CARDS", "PLANNING_CHECKLIST", "LIVE_UPDATES", "RETURN_GIFTS", "EXPERIENCE_GIFTS", "LIVE_WALL", "SHARED_ALBUM", "GUEST_FEEDBACK")),
        new Preset("GROWTH", "Growth pack", "Account-wide features that bring hosts back: gift ledger, reminders, referrals, yearly re-hosting, vendors, group gifts, welcome offer and showcase.",
            List.of("GIFT_LEDGER", "OCCASION_REMINDERS", "HOST_REFERRALS", "YEARLY_REHOST", "VENDOR_DIRECTORY", "GROUP_COLLECTIONS", "GUEST_FIRST_FREE", "CELEBRATION_SHOWCASE", "TRY_WITHOUT_ACCOUNT")),
        new Preset("PARTNER", "Planner & venue pack", "The wedding pack plus the partner workspace and client handover.", null),
        new Preset("EVERYTHING", "Everything", "Every module. Best for test accounts.", null));

    private final JdbcTemplate db;
    private final ActorContext actor;
    public ModulePresetsController(JdbcTemplate db, ActorContext actor) { this.db = db; this.actor = actor; }

    public record PresetView(String code, String name, String description, List<String> modules) {}
    public record Apply(String scope, UUID hostId, UUID eventId) {}
    public record Result(String preset, String scope, int changed, List<String> modules, List<String> skipped) {}
    public record AllHosts(boolean enabled) {}

    @GetMapping
    public List<PresetView> list() {
        admin();
        return PRESETS.stream().map(p -> new PresetView(p.code(), p.name(), p.description(), modules(p))).toList();
    }

    /** Scope: PLATFORM | HOST (hostId) | ALL_HOSTS | EVENT (eventId). Event scope covers only modules that have an event switch. */
    @PostMapping("/{code}/apply")
    @Transactional
    public Result apply(@PathVariable String code, @RequestBody Apply in) {
        UUID admin = admin();
        Preset preset = PRESETS.stream().filter(p -> p.code().equals(code)).findFirst().orElseThrow(() -> bad("Unknown preset"));
        String scope = in == null || in.scope() == null ? "" : in.scope().toUpperCase(Locale.ROOT);
        List<String> mods = modules(preset);
        List<String> skipped = new ArrayList<>();
        int changed = 0;
        switch (scope) {
            case "PLATFORM" -> {
                for (String m : mods) changed += db.update("UPDATE platform_module_controls SET enabled=TRUE,changed_by=?,changed_at=now() WHERE module_code=? AND NOT enabled", admin, m);
            }
            case "HOST" -> {
                UUID host = requireHost(in.hostId());
                for (String m : mods) changed += grantHost(host, m, admin);
            }
            case "ALL_HOSTS" -> {
                List<UUID> hosts = db.queryForList("SELECT id FROM users WHERE role='CUSTOMER'", UUID.class);
                for (UUID host : hosts) for (String m : mods) changed += grantHost(host, m, admin);
            }
            case "EVENT" -> {
                if (in.eventId() == null || !Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM events WHERE id=?)", Boolean.class, in.eventId())))
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found");
                for (String m : mods) {
                    if (EventModuleGrantsController.CODES.contains(m)) changed += db.update("""
                        INSERT INTO platform_event_module_grants(event_id,module_code,enabled,changed_by) VALUES(?,?,TRUE,?)
                        ON CONFLICT(event_id,module_code) DO UPDATE SET enabled=TRUE,changed_by=EXCLUDED.changed_by,changed_at=now() WHERE NOT platform_event_module_grants.enabled""", in.eventId(), m, admin);
                    else if (m.equals("MEMORY_ALBUM")) changed += db.update("""
                        INSERT INTO platform_event_album_settings(event_id,enabled,changed_by) VALUES(?,TRUE,?)
                        ON CONFLICT(event_id) DO UPDATE SET enabled=TRUE,changed_by=EXCLUDED.changed_by,changed_at=now() WHERE NOT platform_event_album_settings.enabled""", in.eventId(), admin);
                    else if (m.equals("AI_INVITATION")) changed += db.update("""
                        INSERT INTO platform_event_ai_design_settings(event_id,enabled,changed_by) VALUES(?,TRUE,?)
                        ON CONFLICT(event_id) DO UPDATE SET enabled=TRUE,changed_by=EXCLUDED.changed_by,changed_at=now() WHERE NOT platform_event_ai_design_settings.enabled""", in.eventId(), admin);
                    else skipped.add(m);
                }
            }
            default -> throw bad("Choose where to apply the preset");
        }
        db.update("INSERT INTO platform_admin_action_audit(id,actor_id,action,target_id,module_code) VALUES(?,?,?,?,?)",
            UUID.randomUUID(), admin, "PRESET_" + preset.code() + "_" + scope, scope.equals("HOST") ? in.hostId() : scope.equals("EVENT") ? in.eventId() : null, null);
        return new Result(preset.code(), scope, changed, mods, skipped);
    }

    /** Switch one module on or off for every host at once. */
    @PostMapping("/modules/{module}/all-hosts")
    @Transactional
    public Map<String, Object> allHosts(@PathVariable String module, @RequestBody AllHosts in) {
        UUID admin = admin();
        if (!allCodes().contains(module)) throw bad("Unknown module");
        boolean on = in != null && in.enabled();
        int n = db.update("""
            INSERT INTO platform_host_module_grants(host_id,module_code,enabled,changed_by) SELECT id,?,?,? FROM users WHERE role='CUSTOMER'
            ON CONFLICT(host_id,module_code) DO UPDATE SET enabled=EXCLUDED.enabled,changed_by=EXCLUDED.changed_by,changed_at=now()""", module, on, admin);
        db.update("INSERT INTO platform_admin_action_audit(id,actor_id,action,target_id,module_code) VALUES(?,?,?,?,?)", UUID.randomUUID(), admin, on ? "MODULE_ALL_HOSTS_ON" : "MODULE_ALL_HOSTS_OFF", null, module);
        return Map.of("module", module, "enabled", on, "hosts", n);
    }

    private int grantHost(UUID host, String module, UUID admin) {
        return db.update("""
            INSERT INTO platform_host_module_grants(host_id,module_code,enabled,changed_by) VALUES(?,?,TRUE,?)
            ON CONFLICT(host_id,module_code) DO UPDATE SET enabled=TRUE,changed_by=EXCLUDED.changed_by,changed_at=now() WHERE NOT platform_host_module_grants.enabled""", host, module, admin);
    }
    private UUID requireHost(UUID host) {
        if (host == null || !Integer.valueOf(1).equals(db.queryForObject("SELECT count(*) FROM users WHERE id=? AND role='CUSTOMER'", Integer.class, host)))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Host not found");
        return host;
    }
    private Set<String> allCodes() { return new LinkedHashSet<>(db.queryForList("SELECT module_code FROM platform_module_controls ORDER BY module_code", String.class)); }
    private List<String> modules(Preset p) {
        Set<String> known = allCodes();
        List<String> list = switch (p.code()) {
            case "PARTNER" -> concat(PRESETS.get(1).modules(), "PARTNER_ACCOUNTS");
            case "EVERYTHING" -> List.copyOf(known);
            default -> p.modules();
        };
        return list.stream().filter(known::contains).distinct().toList();
    }
    static List<String> concat(List<String> base, String... more) { List<String> l = new ArrayList<>(base); l.addAll(List.of(more)); return List.copyOf(l); }
    private UUID admin() { if (!actor.isPlatformAdmin()) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Platform admin only"); return actor.requireAuthenticatedUserId(); }
    private static ResponseStatusException bad(String m) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, m); }

    @ExceptionHandler(ResponseStatusException.class)
    public org.springframework.http.ResponseEntity<?> reason(ResponseStatusException ex) {
        int code = ex.getStatusCode().value();
        if ((code == 400 || code == 404) && ex.getReason() != null) return org.springframework.http.ResponseEntity.status(code).body(Map.of("error", ex.getReason()));
        return org.springframework.http.ResponseEntity.status(ex.getStatusCode()).build();
    }
}
