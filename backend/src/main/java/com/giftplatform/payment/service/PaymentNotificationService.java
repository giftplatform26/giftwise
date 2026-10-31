package com.giftplatform.payment.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentNotificationService {
    private final JdbcTemplate jdbc;
    public PaymentNotificationService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    private record Detail(UUID eventId, String guestEmail, long amountPaise) {}

    @Transactional
    public void recordVerifiedContribution(RazorpayCaptureService.CaptureResult result) {
        UUID contributionId = result.contributionId();
        List<Detail> rows = jdbc.query("""
            SELECT c.event_id, c.guest_email, c.amount_paise-po.platform_fee_paise
            FROM contributions c
            JOIN payment_orders po ON po.id = c.payment_order_id AND po.status = 'CAPTURED'
            WHERE c.id = ?
            """, (rs, i) -> new Detail(rs.getObject(1, UUID.class), rs.getString(2),
                    rs.getLong(3)), contributionId);
        if (rows.size() != 1) throw new IllegalStateException("Verified contribution not found");
        Detail d = rows.get(0);
        if (result.giftPaise() < 0 || result.walletPaise() < 0 ||
                result.giftPaise() + result.walletPaise() != d.amountPaise()) {
            throw new IllegalStateException("Contribution allocation mismatch");
        }
        jdbc.update("""
            INSERT INTO payment_notifications
                (id, contribution_id, recipient_type, recipient_address, event_id,
                 gift_amount_paise, wallet_amount_paise)
            VALUES (?, ?, 'GUEST', ?, ?, ?, ?)
            ON CONFLICT (contribution_id, recipient_type) DO NOTHING
            """, UUID.randomUUID(), contributionId, d.guestEmail(), d.eventId(), result.giftPaise(), result.walletPaise());
        jdbc.update("""
            INSERT INTO payment_notifications
                (id, contribution_id, recipient_type, event_id,
                 gift_amount_paise, wallet_amount_paise)
            VALUES (?, ?, 'HOST', ?, ?, ?)
            ON CONFLICT (contribution_id, recipient_type) DO NOTHING
            """, UUID.randomUUID(), contributionId, d.eventId(), result.giftPaise(), result.walletPaise());
    }
}
