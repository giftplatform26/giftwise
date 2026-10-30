package com.giftplatform.payment.settlement;

import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/** Read-only destination suggestions. Never initiates payment or changes preference. */
@Service
public class VerifiedBankFallbackService {
    private final JdbcTemplate jdbc;
    public VerifiedBankFallbackService(JdbcTemplate jdbc) { this.jdbc=jdbc; }
    public record Candidate(UUID bankId,boolean preferred) {}
    public List<Candidate> eligible(UUID hostId) {
        if(hostId==null) throw new IllegalArgumentException("Host required");
        return jdbc.query("""
            SELECT id,preferred FROM host_bank_accounts
            WHERE host_id=? AND event_id IS NULL AND archived_at IS NULL AND enabled=TRUE AND verification_status='VERIFIED'
            ORDER BY preferred DESC,created_at ASC,id ASC
            """,(rs,i)->new Candidate(rs.getObject(1,UUID.class),rs.getBoolean(2)),hostId);
    }
}
