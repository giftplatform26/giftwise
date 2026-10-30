package com.giftplatform.payment.service;

import com.giftplatform.gift.entity.Gift;
import com.giftplatform.gift.repository.GiftRepository;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class GiftFundingService {

    private final GiftRepository gifts;
    private final JdbcTemplate jdbc;

    public GiftFundingService(
            GiftRepository gifts,
            JdbcTemplate jdbc) {
        this.gifts = gifts;
        this.jdbc = jdbc;
    }

    @Transactional(readOnly = true)
    public FundingSummary getSummary(UUID eventId, UUID giftId) {
        Gift gift = gifts.findById(giftId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Gift not found"));

        if (!gift.getEvent().getId().equals(eventId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Gift not found");
        }

        long targetPaise;
        try {
            targetPaise = gift.getTargetAmount()
                    .movePointRight(2)
                    .longValueExact();
        } catch (ArithmeticException ex) {
            throw new IllegalStateException(
                    "Invalid gift target amount", ex);
        }

        Long funded = jdbc.queryForObject(
                """
                SELECT COALESCE(SUM(amount_paise), 0)
                FROM (
                    SELECT ca.amount_paise
                    FROM contribution_allocations ca
                    WHERE ca.event_id = ?
                      AND ca.gift_id = ?
                      AND ca.allocation_type = 'GIFT'

                    UNION ALL

                    SELECT c.amount_paise
                    FROM contributions c
                    WHERE c.event_id = ?
                      AND c.gift_id = ?
                      AND c.allocation_status = 'GIFT_ALLOCATED'
                      AND NOT EXISTS (
                          SELECT 1
                          FROM contribution_allocations ca
                          WHERE ca.contribution_id = c.id
                      )
                ) funded_amounts
                """,
                Long.class,
                eventId,
                giftId,
                eventId,
                giftId
        );

        long fundedPaise = funded == null ? 0 : funded;
        long remainingPaise = Math.max(
                0L, targetPaise - fundedPaise);

        return new FundingSummary(
                eventId,
                giftId,
                targetPaise,
                fundedPaise,
                remainingPaise
        );
    }

    public record FundingSummary(
            UUID eventId,
            UUID giftId,
            long targetPaise,
            long fundedPaise,
            long remainingPaise
    ) {}
}
