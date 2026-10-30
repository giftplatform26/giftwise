package com.giftplatform.payment.service;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class ContributionAllocationService {

    private final JdbcTemplate jdbc;

    public ContributionAllocationService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public record AllocationResult(
            UUID contributionId,
            UUID entryGroupId,
            long giftPaise,
            long walletPaise,
            boolean alreadyProcessed
    ) {}

    private record ContributionData(
            UUID eventId,
            UUID giftId,
            UUID paymentOrderId,
            long amountPaise,
            String status
    ) {}

    @Transactional
    public AllocationResult allocate(UUID contributionId) {
        if (contributionId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Contribution required");
        }

        // Serialize processing of this contribution.
        List<ContributionData> rows = jdbc.query("""
            SELECT event_id, gift_id, payment_order_id,
                   amount_paise, allocation_status
            FROM contributions
            WHERE id = ?
            FOR UPDATE
            """,
            (rs, n) -> new ContributionData(
                    rs.getObject("event_id", UUID.class),
                    rs.getObject("gift_id", UUID.class),
                    rs.getObject("payment_order_id", UUID.class),
                    rs.getLong("amount_paise"),
                    rs.getString("allocation_status")),
            contributionId);

        if (rows.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Contribution not found");
        }

        ContributionData c = rows.get(0);

        // Idempotent retry: return the original split.
        List<UUID> previous = jdbc.query("""
            SELECT entry_group_id
            FROM contribution_allocation_postings
            WHERE contribution_id = ?
            """,
            (rs, n) -> rs.getObject(1, UUID.class),
            contributionId);

        if (!previous.isEmpty()) {
            long gift = allocated(contributionId, "GIFT");
            long wallet = allocated(contributionId, "EVENT_WALLET");
            return new AllocationResult(
                    contributionId, previous.get(0),
                    gift, wallet, true);
        }

        if (!"PENDING".equals(c.status())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Contribution already handled outside allocation service");
        }

        // The order and the trusted payment record must agree.
        Integer verified = jdbc.queryForObject("""
            SELECT COUNT(*)
            FROM payment_orders po
            JOIN payment_transactions pt
              ON pt.payment_order_id = po.id
            WHERE po.id = ?
              AND po.event_id = ?
              AND po.gift_id IS NOT DISTINCT FROM ?
              AND po.amount_paise = ?
              AND po.currency = 'INR'
              AND po.status = 'CAPTURED'
              AND pt.status = 'CAPTURED'
              AND pt.verified_at IS NOT NULL
              AND pt.amount_paise = po.amount_paise
              AND pt.currency = po.currency
            """,
            Integer.class,
            c.paymentOrderId(), c.eventId(),
            c.giftId(), c.amountPaise());

        if (verified == null || verified != 1) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Verified captured payment required");
        }

        long giftPaise = 0L;

        if (c.giftId() != null) {
            // Serialize all allocations to this gift.
            List<Long> targets = jdbc.query("""
                SELECT target_amount
                FROM gifts
                WHERE id = ? AND event_id = ?
                FOR UPDATE
                """,
                (rs, n) -> rs.getBigDecimal(1)
                        .movePointRight(2).longValueExact(),
                c.giftId(), c.eventId());

            if (targets.isEmpty()) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "Gift not found");
            }

            Long funded = jdbc.queryForObject("""
                SELECT COALESCE(SUM(amount_paise), 0)
                FROM (
                    SELECT ca.amount_paise
                    FROM contribution_allocations ca
                    WHERE ca.event_id = ?
                      AND ca.gift_id = ?
                      AND ca.allocation_type = 'GIFT'

                    UNION ALL

                    SELECT legacy.amount_paise
                    FROM contributions legacy
                    WHERE legacy.event_id = ?
                      AND legacy.gift_id = ?
                      AND legacy.allocation_status = 'GIFT_ALLOCATED'
                      AND NOT EXISTS (
                          SELECT 1
                          FROM contribution_allocations ca
                          WHERE ca.contribution_id = legacy.id
                      )
                ) amounts
                """,
                Long.class,
                c.eventId(), c.giftId(),
                c.eventId(), c.giftId());

            long remaining = Math.max(
                    0L, Math.subtractExact(
                            targets.get(0), funded == null ? 0L : funded));

            giftPaise = Math.min(c.amountPaise(), remaining);
        }

        long walletPaise = c.amountPaise() - giftPaise;
        UUID group = UUID.randomUUID();

        // Unique contribution marker and all financial writes
        // commit or roll back in the same transaction.
        jdbc.update("""
            INSERT INTO contribution_allocation_postings
                (contribution_id, entry_group_id)
            VALUES (?, ?)
            """, contributionId, group);

        if (giftPaise > 0) {
            jdbc.update("""
                INSERT INTO contribution_allocations
                    (id, contribution_id, event_id, gift_id,
                     allocation_type, amount_paise)
                VALUES (?, ?, ?, ?, 'GIFT', ?)
                """,
                UUID.randomUUID(), contributionId,
                c.eventId(), c.giftId(), giftPaise);
        }

        if (walletPaise > 0) {
            jdbc.update("""
                INSERT INTO contribution_allocations
                    (id, contribution_id, event_id, gift_id,
                     allocation_type, amount_paise)
                VALUES (?, ?, ?, NULL, 'EVENT_WALLET', ?)
                """,
                UUID.randomUUID(), contributionId,
                c.eventId(), walletPaise);
        }

        // Signed double-entry posting: total must equal zero.
        ledger(c.eventId(), contributionId, group,
                "PAYMENT_CLEARING", -c.amountPaise());

        if (giftPaise > 0) {
            ledger(c.eventId(), contributionId, group,
                    "GIFT_RESERVED", giftPaise);
        }

        if (walletPaise > 0) {
            ledger(c.eventId(), contributionId, group,
                    "HOST_PENDING", walletPaise);
        }

        String status = giftPaise > 0 && walletPaise > 0
                ? "REALLOCATED"
                : giftPaise > 0
                    ? "GIFT_ALLOCATED"
                    : "WALLET_ALLOCATED";

        jdbc.update("""
            UPDATE contributions
            SET allocation_status = ?
            WHERE id = ?
            """, status, contributionId);

        return new AllocationResult(
                contributionId, group, giftPaise, walletPaise, false);
    }

    private long allocated(UUID id, String type) {
        Long amount = jdbc.queryForObject("""
            SELECT COALESCE(SUM(amount_paise), 0)
            FROM contribution_allocations
            WHERE contribution_id = ? AND allocation_type = ?
            """, Long.class, id, type);
        return amount == null ? 0L : amount;
    }

    private void ledger(
            UUID eventId,
            UUID contributionId,
            UUID group,
            String account,
            long amount) {

        if (amount == 0L) return;

        jdbc.update("""
            INSERT INTO wallet_ledger_entries
                (id, event_id, contribution_id,
                 entry_group_id, account_code,
                 amount_paise, currency, entry_type)
            VALUES (?, ?, ?, ?, ?, ?, 'INR',
                    'CONTRIBUTION_ALLOCATION')
            """,
            UUID.randomUUID(), eventId, contributionId,
            group, account, amount);
    }
}
