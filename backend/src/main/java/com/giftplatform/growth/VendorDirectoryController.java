package com.giftplatform.growth;

import com.giftplatform.admin.EffectiveModulePermissionService;
import com.giftplatform.security.ActorContext;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

/** Admin-curated vendor directory (decor, catering, photography…) that hosts browse and shortlist. No bookings or payments. */
@RestController
public class VendorDirectoryController {
    public static final String MODULE = "VENDOR_DIRECTORY";
    static final Set<String> CATEGORIES = Set.of("DECOR", "CATERING", "PHOTOGRAPHY", "MAKEUP", "MEHENDI", "MUSIC", "VENUE", "PLANNER", "INVITATIONS", "GIFTS", "OTHER");

    private final JdbcTemplate db;
    private final ActorContext actor;
    private final EffectiveModulePermissionService permissions;

    public VendorDirectoryController(JdbcTemplate db, ActorContext actor, EffectiveModulePermissionService permissions) {
        this.db = db; this.actor = actor; this.permissions = permissions;
    }

    public record Vendor(UUID id, String name, String category, String city, String description, String phone, String website,
                         String instagram, String priceHint, boolean verified, boolean active, boolean shortlisted) {}
    public record Input(String name, String category, String city, String description, String phone, String website, String instagram,
                        String priceHint, Boolean verified, Boolean active) {}
    public record Directory(List<Vendor> vendors, List<String> cities) {}

    // ---- Hosts ----

    @GetMapping("/api/v1/me/vendors")
    public ResponseEntity<Directory> browse(@RequestParam(defaultValue = "") String city, @RequestParam(defaultValue = "") String category,
                                            @RequestParam(defaultValue = "false") boolean shortlist) {
        UUID me = me();
        if (city.length() > 80 || (!category.isEmpty() && !CATEGORIES.contains(category))) throw bad("Unknown filter");
        List<Vendor> vendors = db.query("""
            SELECT v.*, (s.user_id IS NOT NULL) AS shortlisted FROM platform_vendors v
            LEFT JOIN user_vendor_shortlist s ON s.vendor_id=v.id AND s.user_id=?
            WHERE v.active AND (?='' OR lower(v.city)=lower(?)) AND (?='' OR v.category=?) AND (NOT ? OR s.user_id IS NOT NULL)
            ORDER BY v.verified DESC, v.name LIMIT 300""", (rs, i) -> vendor(rs, rs.getBoolean("shortlisted")), me, city.strip(), city.strip(), category, category, shortlist);
        List<String> cities = db.queryForList("SELECT DISTINCT initcap(lower(city)) FROM platform_vendors WHERE active ORDER BY 1 LIMIT 200", String.class);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new Directory(vendors, cities));
    }

    @PostMapping("/api/v1/me/vendors/{id}/shortlist")
    public Map<String, Object> shortlist(@PathVariable UUID id) {
        UUID me = me();
        int n = db.update("INSERT INTO user_vendor_shortlist(user_id,vendor_id) SELECT ?,id FROM platform_vendors WHERE id=? AND active ON CONFLICT DO NOTHING", me, id);
        if (n == 0 && !Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM user_vendor_shortlist WHERE user_id=? AND vendor_id=?)", Boolean.class, me, id)))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return Map.of("shortlisted", true);
    }

    @PostMapping("/api/v1/me/vendors/{id}/unshortlist")
    public Map<String, Object> unshortlist(@PathVariable UUID id) {
        db.update("DELETE FROM user_vendor_shortlist WHERE user_id=? AND vendor_id=?", me(), id);
        return Map.of("shortlisted", false);
    }

    // ---- Platform admin ----

    @GetMapping("/api/admin/console/vendors")
    public List<Vendor> all() {
        admin();
        return db.query("SELECT v.*, FALSE AS shortlisted FROM platform_vendors v ORDER BY v.active DESC, lower(v.city), v.name LIMIT 1000", (rs, i) -> vendor(rs, false));
    }

    @PostMapping("/api/admin/console/vendors")
    public Vendor create(@RequestBody Input in) {
        UUID admin = admin();
        Clean c = clean(in);
        UUID id = UUID.randomUUID();
        db.update("INSERT INTO platform_vendors(id,name,category,city,description,phone,website,instagram,price_hint,verified,active) VALUES(?,?,?,?,?,?,?,?,?,?,?)",
            id, c.name, c.category, c.city, c.description, c.phone, c.website, c.instagram, c.price, c.verified, c.active);
        audit(admin, "VENDOR_CREATED", id);
        return one(id);
    }

    @PutMapping("/api/admin/console/vendors/{id}")
    public Vendor update(@PathVariable UUID id, @RequestBody Input in) {
        UUID admin = admin();
        Clean c = clean(in);
        if (db.update("UPDATE platform_vendors SET name=?,category=?,city=?,description=?,phone=?,website=?,instagram=?,price_hint=?,verified=?,active=?,updated_at=now() WHERE id=?",
            c.name, c.category, c.city, c.description, c.phone, c.website, c.instagram, c.price, c.verified, c.active, id) == 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        audit(admin, "VENDOR_UPDATED", id);
        return one(id);
    }

    // ---- Helpers ----

    private record Clean(String name, String category, String city, String description, String phone, String website, String instagram, String price, boolean verified, boolean active) {}
    private Clean clean(Input in) {
        if (in == null) throw bad("Vendor details are required");
        String name = text(in.name(), 120, "Name"), city = text(in.city(), 80, "City");
        if (name == null || city == null) throw bad("Add the vendor's name and city");
        String category = in.category() == null ? "" : in.category().strip().toUpperCase(Locale.ROOT);
        if (!CATEGORIES.contains(category)) throw bad("Choose a category");
        String phone = text(in.phone(), 20, "Phone");
        if (phone != null) { phone = phone.replaceAll("[\\s()\\-]", ""); if (!phone.matches("\\+[1-9][0-9]{7,14}")) throw bad("Phone must include the country code, e.g. +919876543210"); }
        String website = text(in.website(), 300, "Website");
        if (website != null && !website.matches("https://[^\\s<>\"]+")) throw bad("Website must start with https://");
        String insta = text(in.instagram(), 60, "Instagram");
        if (insta != null) { insta = insta.replaceFirst("^@", ""); if (!insta.matches("[A-Za-z0-9._]{1,30}")) throw bad("Enter the Instagram handle without spaces"); }
        return new Clean(name, category, city, text(in.description(), 600, "Description"), phone, website, insta, text(in.priceHint(), 60, "Price hint"),
            Boolean.TRUE.equals(in.verified()), in.active() == null || in.active());
    }
    private Vendor one(UUID id) { return db.query("SELECT v.*, FALSE AS shortlisted FROM platform_vendors v WHERE id=?", (rs, i) -> vendor(rs, false), id).getFirst(); }
    private static Vendor vendor(java.sql.ResultSet rs, boolean shortlisted) throws java.sql.SQLException {
        return new Vendor(rs.getObject("id", UUID.class), rs.getString("name"), rs.getString("category"), rs.getString("city"), rs.getString("description"),
            rs.getString("phone"), rs.getString("website"), rs.getString("instagram"), rs.getString("price_hint"), rs.getBoolean("verified"), rs.getBoolean("active"), shortlisted);
    }
    private static String text(String v, int max, String label) {
        if (v == null || v.isBlank()) return null;
        String s = v.strip();
        if (s.length() > max) throw bad(label + " must be at most " + max + " characters");
        if (s.chars().anyMatch(c -> Character.isISOControl(c) && c != '\n')) throw bad(label + " contains unsupported characters");
        return s;
    }
    private UUID me() { UUID me = actor.requireAuthenticatedUserId(); permissions.require(me, MODULE); return me; }
    private UUID admin() { if (!actor.isPlatformAdmin()) throw new ResponseStatusException(HttpStatus.FORBIDDEN); return actor.requireAuthenticatedUserId(); }
    private void audit(UUID admin, String action, UUID target) {
        db.update("INSERT INTO platform_admin_action_audit(id,actor_id,action,target_id,module_code) VALUES(?,?,?,?,?)", UUID.randomUUID(), admin, action, target, MODULE);
    }
    private static ResponseStatusException bad(String m) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, m); }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> reason(ResponseStatusException ex) {
        int code = ex.getStatusCode().value();
        if ((code == 400 || code == 409) && ex.getReason() != null) return ResponseEntity.status(code).body(Map.of("error", ex.getReason()));
        return ResponseEntity.status(ex.getStatusCode()).build();
    }
}
