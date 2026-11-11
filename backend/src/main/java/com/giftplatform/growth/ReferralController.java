package com.giftplatform.growth;

import com.giftplatform.admin.EffectiveModulePermissionService;
import com.giftplatform.security.ActorContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * Host referrals on the V105 foundations. A friend who signs up with your link is ATTRIBUTED; once they publish
 * their first celebration the referral is QUALIFIED; the platform admin grants the reward (REWARDED).
 */
@RestController
public class ReferralController {
    public static final String MODULE = "HOST_REFERRALS";
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final JdbcTemplate db;
    private final ActorContext actor;
    private final EffectiveModulePermissionService permissions;
    private final String baseUrl, rewardText;

    public ReferralController(JdbcTemplate db, ActorContext actor, EffectiveModulePermissionService permissions,
                              @Value("${gift.guest-base-url:http://localhost:3000}") String baseUrl,
                              @Value("${gift.referrals.reward-text:A free premium celebration for you when your friend publishes their first invitation}") String rewardText) {
        this.db = db; this.actor = actor; this.permissions = permissions; this.baseUrl = baseUrl.replaceAll("/+$", ""); this.rewardText = rewardText;
    }

    public record Friend(String name, String status, OffsetDateTime joinedAt) {}
    public record Mine(String code, String link, String reward, List<Friend> friends, int qualified, int rewarded) {}
    public record Claim(String code) {}
    public record AdminRow(UUID id, String referrer, String referrerEmail, String friend, String status, OffsetDateTime joinedAt, OffsetDateTime rewardedAt) {}

    @GetMapping("/api/v1/me/referrals")
    @Transactional
    public ResponseEntity<Mine> mine() {
        UUID me = actor.requireAuthenticatedUserId();
        permissions.require(me, MODULE);
        String code = db.query("SELECT code FROM referral_codes WHERE referrer_id=? AND tenant_id IS NULL ORDER BY created_at LIMIT 1", (rs, i) -> rs.getString(1), me)
            .stream().findFirst().orElse(null);
        if (code == null) {
            for (int attempt = 0; attempt < 5 && code == null; attempt++) {
                String candidate = "GW" + random(6);
                if (db.update("INSERT INTO referral_codes(id,referrer_id,code,enabled) VALUES(?,?,?,TRUE) ON CONFLICT (code) DO NOTHING", UUID.randomUUID(), me, candidate) == 1) code = candidate;
            }
            if (code == null) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
        }
        qualify(me);
        List<Friend> friends = db.query("""
            SELECT COALESCE(NULLIF(split_part(u.full_name,' ',1),''), 'A friend') AS name, r.status, r.created_at FROM user_referrals r
            JOIN users u ON u.id=r.referred_user_id WHERE r.referrer_id=? ORDER BY r.created_at DESC LIMIT 200""",
            (rs, i) -> new Friend(rs.getString(1), rs.getString(2), rs.getObject(3, OffsetDateTime.class)), me);
        int qualified = (int) friends.stream().filter(f -> !f.status().equals("ATTRIBUTED") && !f.status().equals("CANCELLED")).count();
        int rewarded = (int) friends.stream().filter(f -> f.status().equals("REWARDED")).count();
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
            .body(new Mine(code, baseUrl + "/register?ref=" + code, rewardText, friends, qualified, rewarded));
    }

    /** Called once by the client after a new account signs up with a referral link. */
    @PostMapping("/api/v1/me/referrals/claim")
    @Transactional
    public Map<String, Object> claim(@RequestBody Claim in) {
        UUID me = actor.requireAuthenticatedUserId();
        String code = in == null || in.code() == null ? "" : in.code().strip().toUpperCase(Locale.ROOT);
        if (!code.matches("[A-Z0-9_]{3,40}")) return Map.of("attributed", false);
        var ref = db.queryForList("SELECT id,referrer_id FROM referral_codes WHERE code=? AND enabled", code);
        if (ref.isEmpty()) return Map.of("attributed", false);
        UUID referrer = (UUID) ref.getFirst().get("referrer_id");
        Boolean fresh = db.queryForObject("SELECT created_at>now()-interval '14 days' FROM users WHERE id=?", Boolean.class, me);
        if (referrer.equals(me) || !Boolean.TRUE.equals(fresh) || !permissions.enabled(referrer, MODULE)) return Map.of("attributed", false);
        int n = db.update("INSERT INTO user_referrals(id,referral_code_id,referrer_id,referred_user_id) VALUES(?,?,?,?) ON CONFLICT (referred_user_id) DO NOTHING",
            UUID.randomUUID(), ref.getFirst().get("id"), referrer, me);
        return Map.of("attributed", n == 1);
    }

    @GetMapping("/api/admin/console/referrals")
    public List<AdminRow> admin() {
        requireAdmin();
        db.update("""
            UPDATE user_referrals r SET status='QUALIFIED' WHERE status='ATTRIBUTED'
            AND EXISTS (SELECT 1 FROM events e WHERE e.owner_id=r.referred_user_id AND e.status IN ('PUBLISHED','COMPLETED'))""");
        return db.query("""
            SELECT r.id, COALESCE(NULLIF(a.full_name,''),a.username,a.email) AS referrer, a.email AS referrer_email,
                   COALESCE(NULLIF(b.full_name,''),b.username,'New member') AS friend, r.status, r.created_at, r.rewarded_at
            FROM user_referrals r JOIN users a ON a.id=r.referrer_id JOIN users b ON b.id=r.referred_user_id
            ORDER BY CASE r.status WHEN 'QUALIFIED' THEN 0 WHEN 'ATTRIBUTED' THEN 1 ELSE 2 END, r.created_at DESC LIMIT 500""",
            (rs, i) -> new AdminRow(rs.getObject(1, UUID.class), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5),
                rs.getObject(6, OffsetDateTime.class), rs.getObject(7, OffsetDateTime.class)));
    }

    @PostMapping("/api/admin/console/referrals/{id}/reward")
    @Transactional
    public Map<String, Object> reward(@PathVariable UUID id) {
        UUID admin = requireAdmin();
        if (db.update("UPDATE user_referrals SET status='REWARDED',rewarded_at=now(),rewarded_by=? WHERE id=? AND status='QUALIFIED'", admin, id) == 0)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only qualified referrals can be rewarded");
        db.update("INSERT INTO platform_admin_action_audit(id,actor_id,action,target_id,module_code) VALUES(?,?,?,?,?)", UUID.randomUUID(), admin, "REFERRAL_REWARDED", id, MODULE);
        return Map.of("status", "REWARDED");
    }

    private void qualify(UUID referrer) {
        db.update("""
            UPDATE user_referrals r SET status='QUALIFIED' WHERE r.referrer_id=? AND status='ATTRIBUTED'
            AND EXISTS (SELECT 1 FROM events e WHERE e.owner_id=r.referred_user_id AND e.status IN ('PUBLISHED','COMPLETED'))""", referrer);
    }
    private UUID requireAdmin() {
        if (!actor.isPlatformAdmin()) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        return actor.requireAuthenticatedUserId();
    }
    private static String random(int n) {
        StringBuilder b = new StringBuilder(n);
        for (int i = 0; i < n; i++) b.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        return b.toString();
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> reason(ResponseStatusException ex) {
        int code = ex.getStatusCode().value();
        if (code == 409 && ex.getReason() != null) return ResponseEntity.status(code).body(Map.of("error", ex.getReason()));
        return ResponseEntity.status(ex.getStatusCode()).build();
    }
}
