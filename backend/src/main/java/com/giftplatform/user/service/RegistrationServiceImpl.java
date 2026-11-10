
package com.giftplatform.user.service;

import com.giftplatform.user.dto.RegisterRequest;
import com.giftplatform.user.dto.RegistrationResponse;
import com.giftplatform.user.entity.User;
import com.giftplatform.user.repository.UserRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;

@Service
public class RegistrationServiceImpl implements RegistrationService {

    @org.springframework.beans.factory.annotation.Autowired(required=false) private com.giftplatform.notification.email.TransactionalEmailOutbox emails;
    @org.springframework.beans.factory.annotation.Autowired(required=false) private com.giftplatform.audit.service.AuditService audit;
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final com.giftplatform.security.recovery.EffectiveRecoveryPolicyService policies;
    @org.springframework.beans.factory.annotation.Autowired private com.giftplatform.security.recovery.SignupEmailVerificationService signupEmails;
    @org.springframework.beans.factory.annotation.Autowired private org.springframework.jdbc.core.JdbcTemplate jdbc;

    @org.springframework.beans.factory.annotation.Autowired
    public RegistrationServiceImpl(
            UserRepository repository,
            PasswordEncoder passwordEncoder,
            com.giftplatform.security.recovery.EffectiveRecoveryPolicyService policies
    ) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.policies = policies;
    }

    public RegistrationServiceImpl(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository=repository; this.passwordEncoder=passwordEncoder; this.policies=null;
    }
    @Override
    @Transactional
    public RegistrationResponse register(RegisterRequest request) {
        if (request == null
                || request.email() == null
                || request.password() == null) {
            throw new IllegalArgumentException(
                    "Email and password are required"
            );
        }

        if (policies == null) throw new IllegalStateException("Registration policy service is required");
        policies.effective(null,null); // Policy must exist; contact collection is not verification.
        String email = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (email.isBlank()
                || email.length() > 254
                || !email.matches(
                "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"
        )) {
            throw new IllegalArgumentException("Please enter a valid email address.");
        }

        String password = request.password();

        com.giftplatform.security.CredentialPolicy.validatePassword(password);
        if (!Boolean.TRUE.equals(request.termsAccepted())) throw new IllegalArgumentException("Please agree to the Terms & Conditions and Privacy Policy.");

        if (repository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateEmailException();
        }

        User user = new User(
                request.username(),
                email,
                passwordEncoder.encode(password)
        );
        if(request.fullName()!=null)user.setFullName(request.fullName());
        if(request.mobile()==null || !request.mobile().matches("^\\+[1-9][0-9]{7,14}$"))
            throw new IllegalArgumentException("Mobile number is required in international format");
        user.setUnverifiedPhoneNumber(request.mobile());
        String username = request.username();

        if (username == null ||
                !username.matches(com.giftplatform.security.CredentialPolicy.USERNAME_PATTERN)) {
            throw new IllegalArgumentException(com.giftplatform.security.CredentialPolicy.USERNAME_MESSAGE);
        }

        if (repository.existsByUsernameIgnoreCase(username)) {
            throw new DuplicateUsernameException();
        }

        try {
            User saved = repository.saveAndFlush(user);
            if (jdbc != null) jdbc.update("INSERT INTO user_legal_consents(user_id,terms_version,privacy_version) VALUES (?,?,?)", saved.getId(), "2026-11-05", "2026-11-05");
            if(request.emailVerificationToken()!=null&&!request.emailVerificationToken().isBlank()){
                if(signupEmails==null)throw new IllegalStateException("Signup verification service unavailable");
                if(signupEmails.consume(request.emailVerificationToken(),"HOST",null,email))
                    jdbc.update("UPDATE users SET email_verified_at=now() WHERE id=?",saved.getId());
            }

            if(emails!=null)emails.registration(saved.getId(),saved.getEmail());
            if(audit!=null)audit.recordChange(saved.getId(),com.giftplatform.audit.entity.AuditActorType.CUSTOMER,"USER",saved.getId(),com.giftplatform.audit.entity.AuditAction.CREATE,java.util.Map.of(),java.util.Map.of("created",true),null);
            return new RegistrationResponse(
                    saved.getId(),
                    saved.getUsername(),
                    saved.getEmail(),
                    saved.getRole().name()
            );
        } catch (DataIntegrityViolationException ex) {
            // Classify database races without exposing SQL or mislabelling other failures.
            String detail = ex.getMostSpecificCause().getMessage();
            if (detail != null && detail.contains("ux_users_username_normalized")) throw new DuplicateUsernameException();
            if (detail != null && detail.contains("ux_users_email_normalized")) throw new DuplicateEmailException();
            throw ex;
        }
    }


}
