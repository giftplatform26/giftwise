package com.giftplatform.user.service;

import com.giftplatform.audit.entity.AuditAction;
import com.giftplatform.audit.entity.AuditActorType;
import com.giftplatform.audit.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Self-service deletion for host (CUSTOMER) accounts.
 *
 * <p>The account is anonymised and disabled instead of physically deleted: contributions,
 * payment orders, audits and settlement records reference the user and are retained for
 * financial and legal obligations. Personal contact details, login identifiers, profile
 * picture and preferences are removed, draft celebrations are cancelled and payment
 * destinations are disabled so no guest can be directed to them.</p>
 */
@Service
public class AccountDeletionService {
    public static final String CONFIRMATION = "DELETE";

    private final JdbcTemplate db;
    private final PasswordEncoder passwords;
    @Autowired(required = false) private AuditService audit;
    @Autowired(required = false) private com.giftplatform.security.recovery.CredentialSessionVersionStore sessions;

    public AccountDeletionService(JdbcTemplate db, PasswordEncoder passwords) {
        this.db = db;
        this.passwords = passwords;
    }

    @Transactional
    public void delete(UUID userId, String password, String confirmation) {
        if (!CONFIRMATION.equals(confirmation))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Type DELETE to confirm that you want to delete your account.");
        if (password == null || password.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter your password to confirm.");

        List<Map<String, Object>> rows = db.queryForList(
            "SELECT password_hash, role, enabled FROM users WHERE id=? FOR UPDATE", userId);
        if (rows.isEmpty() || !Boolean.TRUE.equals(rows.get(0).get("enabled")))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        Map<String, Object> user = rows.get(0);
        if (!"CUSTOMER".equals(user.get("role")))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Administrator accounts cannot be deleted here.");
        if (!passwords.matches(password, (String) user.get("password_hash")))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Your password is incorrect.");

        Integer published = db.queryForObject(
            "SELECT count(*) FROM events WHERE owner_id=? AND status='PUBLISHED'", Integer.class, userId);
        if (published != null && published > 0)
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "You have " + published + (published == 1 ? " live celebration" : " live celebrations")
                    + ". Mark them as completed or cancel them before deleting your account.");

        int cancelledDrafts = db.update(
            "UPDATE events SET status='CANCELLED', version=version+1 WHERE owner_id=? AND status='DRAFT'", userId);
        db.update("UPDATE host_upi_accounts SET preferred=FALSE, enabled=FALSE, archived_at=COALESCE(archived_at, now()), updated_at=now() WHERE host_id=?", userId);
        db.update("UPDATE host_bank_accounts SET preferred=FALSE, enabled=FALSE, archived_at=COALESCE(archived_at, now()), updated_at=now() WHERE host_id=?", userId);
        db.update("DELETE FROM user_profile_preferences WHERE user_id=?", userId);
        db.update("""
            UPDATE users SET
              email=?, username=NULL, full_name=NULL,
              phone_number=NULL, phone_verified_at=NULL, email_verified_at=NULL,
              password_hash=?, enabled=FALSE, deleted_at=now(), updated_at=now()
            WHERE id=?""",
            "deleted+" + userId + "@deleted.invalid",
            passwords.encode(UUID.randomUUID().toString()), // unguessable; bcrypt accepts at most 72 bytes
            userId);

        // Sign the account out on every device, not only the current browser.
        if (sessions != null) sessions.revokeAll("HOST", userId);
        if (audit != null)
            audit.recordChange(userId, AuditActorType.CUSTOMER, "USER", userId, AuditAction.UPDATE,
                Map.of(), Map.of("accountDeleted", true, "draftCelebrationsCancelled", cancelledDrafts), null);
    }
}
