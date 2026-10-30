package com.giftplatform.event.service;

import com.giftplatform.event.entity.Event;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class EventPublishReadiness {
    private final JdbcTemplate jdbc;
    public EventPublishReadiness(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public boolean canPublish(Event event) {
        return Boolean.TRUE.equals(jdbc.queryForObject("""
            SELECT EXISTS (
                SELECT 1 FROM host_bank_accounts b
                JOIN event_payment_account_links l ON l.bank_account_id=b.id AND l.host_id=b.host_id
                WHERE b.host_id=? AND l.event_id=? AND l.enabled AND l.preferred
                  AND b.enabled AND b.archived_at IS NULL
                  AND b.verification_status NOT IN ('FAILED','REVOKED')
                  AND (b.host_confirmed_at IS NOT NULL OR b.verification_status='VERIFIED')
                UNION ALL
                SELECT 1 FROM host_upi_accounts u
                JOIN event_payment_account_links l ON l.upi_account_id=u.id AND l.host_id=u.host_id
                WHERE u.host_id=? AND l.event_id=? AND l.enabled AND l.preferred
                  AND u.enabled AND u.archived_at IS NULL
                  AND u.verification_status NOT IN ('FAILED','REVOKED')
                  AND (u.host_confirmed_at IS NOT NULL OR u.verification_status='VERIFIED')
                  AND EXISTS (SELECT 1 FROM host_upi_qr_images q
                      WHERE q.host_id=u.host_id AND q.upi_account_id=u.id
                        AND q.source IN ('UPLOADED','GENERATED') AND q.payload_sha256 IS NOT NULL)
            )
            """, Boolean.class,event.getOwnerId(),event.getId(),event.getOwnerId(),event.getId()));
    }
}
