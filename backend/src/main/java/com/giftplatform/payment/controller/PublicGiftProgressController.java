package com.giftplatform.payment.controller;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@RestController
@RequestMapping("/api/public/gifts")
public class PublicGiftProgressController {
    private final JdbcTemplate jdbc;
    public PublicGiftProgressController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public record GiftProgress(UUID giftId, long contributedPaise, long contributionCount) {}

    @GetMapping("/progress/{token}")
    public List<GiftProgress> progress(@PathVariable UUID token) {
        var eventIds = jdbc.query("SELECT id FROM events WHERE public_token = ? AND status = 'PUBLISHED'",
                (rs, row) -> rs.getObject("id", UUID.class), token);
        if (eventIds.size() != 1) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation unavailable");
        // Public progress combines captured allocations with guest-reported manual submissions.
        // Manual records are presentation-only and never enter the verified financial ledger.
        return jdbc.query("""
            SELECT combined.gift_id,
                   LEAST(MAX(g.target_amount)*100, SUM(combined.contributed))::bigint AS contributed,
                   SUM(combined.contributors) AS contributors
            FROM (
              SELECT ca.gift_id, COALESCE(SUM(ca.amount_paise),0) AS contributed,
                     COUNT(DISTINCT ca.contribution_id) AS contributors
              FROM contribution_allocations ca
              JOIN contributions c ON c.id=ca.contribution_id
              JOIN payment_orders po ON po.id=c.payment_order_id
              JOIN gifts g ON g.id=ca.gift_id AND g.event_id=ca.event_id
              WHERE ca.event_id=? AND ca.allocation_type='GIFT' AND po.status='CAPTURED'
                AND g.status='ACTIVE' AND g.visibility='PUBLIC'
              GROUP BY ca.gift_id
              UNION ALL
              SELECT a.gift_id, COALESCE(SUM(a.gift_paise),0), COUNT(*)
              FROM guest_manual_display_allocations a
              JOIN guest_direct_transfer_claims cl ON cl.id=a.claim_id
              JOIN gifts g ON g.id=a.gift_id AND g.event_id=a.event_id
              WHERE a.event_id=? AND a.gift_id IS NOT NULL
                AND cl.status IN ('AWAITING_VERIFICATION','HOST_CONFIRMED')
                AND g.status='ACTIVE' AND g.visibility='PUBLIC'
              GROUP BY a.gift_id
            ) combined
            JOIN gifts g ON g.id=combined.gift_id
            GROUP BY combined.gift_id
            """, (rs, row) -> new GiftProgress(
                rs.getObject("gift_id", UUID.class),
                rs.getLong("contributed"),
                rs.getLong("contributors")), eventIds.get(0),eventIds.get(0));
    }
}
