package com.giftplatform.hostpayment.verification;

import com.giftplatform.security.ActorContext;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/** Host-owned verification attempt history and fail-closed disabled-provider workflow.
 * Does not change destination verification status or execute any provider call.
 */
@RestController
@RequestMapping("/api/v1/host/payment-profiles/verification")
@ConditionalOnProperty(name="gift.payment.profiles.enabled", havingValue="true")
public class HostDestinationVerificationInboxController {
    private final JdbcTemplate jdbc;
    private final ActorContext actor;
    private final boolean liveEnabled;

    public HostDestinationVerificationInboxController(JdbcTemplate jdbc, ActorContext actor,
        @Value("${gift.payment.destination-verification.enabled:false}") boolean liveEnabled) {
        this.jdbc = jdbc;
        this.actor = actor;
        this.liveEnabled = liveEnabled;
    }

    public record Attempt(UUID id, UUID destinationId, String destinationKind,
                          String state, String provider, Instant createdAt, Instant updatedAt) {}
    public record RequestResult(UUID attemptId, String state, String message) {}

    @GetMapping("/{kind}/{destinationId}/attempts")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Attempt>> history(@PathVariable String kind,
        @PathVariable UUID destinationId, @RequestParam(defaultValue="20") int limit,
        @RequestParam(defaultValue="0") int offset) {
        String type = requireKind(kind);
        if (limit < 1 || limit > 100 || offset < 0 || offset > 100000)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid pagination");
        UUID hostId = actor.requireAuthenticatedUserId();
        requireOwned(hostId, destinationId, type);
        List<Attempt> rows = jdbc.query("""
            SELECT id, destination_id, destination_kind, state, provider, created_at, updated_at
              FROM host_destination_verification_attempts
             WHERE host_id=? AND destination_id=? AND destination_kind=?
             ORDER BY created_at DESC, id DESC LIMIT ? OFFSET ?
            """, (rs, i) -> new Attempt(rs.getObject(1, UUID.class),
                rs.getObject(2, UUID.class), rs.getString(3), rs.getString(4),
                rs.getString(5), rs.getTimestamp(6).toInstant(),
                rs.getTimestamp(7).toInstant()), hostId, destinationId, type, limit, offset);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(rows);
    }

    @PostMapping("/{kind}/{destinationId}/request")
    @Transactional
    public ResponseEntity<RequestResult> request(@PathVariable String kind,
        @PathVariable UUID destinationId) {
        String type = requireKind(kind);
        UUID hostId = actor.requireAuthenticatedUserId();
        // No live provider may be selected through this endpoint until the
        // authenticated, request-bound integration has been implemented.
        if (liveEnabled)
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "Live verification integration is not configured");
        requireOwned(hostId, destinationId, type);
        // Serialize requests per host/destination even across application instances.
        jdbc.queryForObject("SELECT pg_advisory_xact_lock(hashtext(?), hashtext(?))",
            Object.class, hostId.toString(), type + ":" + destinationId);
        // Avoid creating unlimited unavailable attempts during repeated clicks.
        List<UUID> recent = jdbc.query("""
            SELECT id FROM host_destination_verification_attempts
             WHERE host_id=? AND destination_id=? AND destination_kind=?
               AND state='UNAVAILABLE' AND created_at > CURRENT_TIMESTAMP - INTERVAL '60 seconds'
             ORDER BY created_at DESC LIMIT 1
            """, (rs, i) -> rs.getObject(1, UUID.class), hostId, destinationId, type);
        if (!recent.isEmpty())
            return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(new RequestResult(recent.get(0), "UNAVAILABLE", "Provider verification is not enabled"));
        UUID id = UUID.randomUUID();
        jdbc.update("""
            INSERT INTO host_destination_verification_attempts
              (id, host_id, destination_id, destination_kind, state)
            VALUES (?, ?, ?, ?, 'UNAVAILABLE')
            """, id, hostId, destinationId, type);
        return ResponseEntity.status(HttpStatus.CREATED).cacheControl(CacheControl.noStore())
            .body(new RequestResult(id, "UNAVAILABLE", "Provider verification is not enabled"));
    }

    private void requireOwned(UUID hostId, UUID destinationId, String kind) {
        String table = "UPI".equals(kind) ? "host_upi_accounts" : "host_bank_accounts";
        // Table name comes only from a fixed allowlist, never client input.
        Long count = jdbc.queryForObject("SELECT COUNT(*) FROM " + table +
            " WHERE id=? AND host_id=?", Long.class, destinationId, hostId);
        if (count == null || count == 0)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Destination not found");
    }

    private static String requireKind(String kind) {
        if ("upi".equalsIgnoreCase(kind)) return "UPI";
        if ("bank".equalsIgnoreCase(kind)) return "BANK";
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid destination kind");
    }
}
