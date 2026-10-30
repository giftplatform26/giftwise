
package com.giftplatform.user.service;

import com.giftplatform.user.dto.RegisterRequest;
import com.giftplatform.user.dto.RegistrationResponse;
import com.giftplatform.user.entity.User;
import com.giftplatform.user.repository.UserRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
public class RegistrationServiceImpl implements RegistrationService {

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
            throw new IllegalArgumentException("Invalid email");
        }

        String password = request.password();

        int passwordBytes = password
                .getBytes(StandardCharsets.UTF_8)
                .length;

        if (password.isBlank()
                || passwordBytes < 12
                || passwordBytes > 72) {

            throw new IllegalArgumentException(
                    "Password must be between 12 and 72 UTF-8 bytes"
            );
        }

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
                !username.matches("^[a-z][a-z0-9_]{3,29}$")) {
            throw new IllegalArgumentException("Invalid username");
        }

        if (repository.existsByUsernameIgnoreCase(username)) {
            throw new DuplicateUsernameException();
        }

        try {
            User saved = repository.saveAndFlush(user);
            if(request.emailVerificationToken()!=null&&!request.emailVerificationToken().isBlank()){
                if(signupEmails==null)throw new IllegalStateException("Signup verification service unavailable");
                if(signupEmails.consume(request.emailVerificationToken(),"HOST",null,email))
                    jdbc.update("UPDATE users SET email_verified_at=now() WHERE id=?",saved.getId());
            }

            return new RegistrationResponse(
                    saved.getId(),
                    saved.getUsername(),
                    saved.getEmail(),
                    saved.getRole().name()
            );
        } catch (DataIntegrityViolationException ex) {
            // The database unique index also protects
            // against concurrent duplicate registrations.
            throw new DuplicateEmailException();
        }
    }


}
