package com.giftplatform.admin.bootstrap;

import com.giftplatform.user.entity.User;
import com.giftplatform.user.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;

/** One-shot, explicit, offline account creation. Never exposed as an HTTP endpoint. */
@Component
@Profile("admin-provision")
public class AdminProvisionRunner implements ApplicationRunner {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    public AdminProvisionRunner(UserRepository users, PasswordEncoder encoder) {
        this.users=users; this.encoder=encoder;
    }
    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!"YES".equals(System.getenv("ADMIN_PROVISION_CONFIRM"))) {
            throw new IllegalStateException("Set ADMIN_PROVISION_CONFIRM=YES explicitly");
        }
        String rawEmail=System.getenv("ADMIN_PROVISION_EMAIL");
        String password=System.getenv("ADMIN_PROVISION_PASSWORD");
        if (rawEmail == null || !rawEmail.trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Valid ADMIN_PROVISION_EMAIL required");
        }
        if (password==null || password.length()<16 || password.length()>128) {
            throw new IllegalArgumentException("ADMIN_PROVISION_PASSWORD must be 16-128 characters");
        }
        String email=rawEmail.trim().toLowerCase(Locale.ROOT);
        if (users.existsByEmailIgnoreCase(email)) {
            throw new IllegalStateException("Account already exists; refusing to elevate an existing account");
        }
        User admin=new User(email,encoder.encode(password));
        admin.provisionAsPlatformAdmin();
        users.saveAndFlush(admin);
        System.out.println("Created PLATFORM_ADMIN account: "+email);
    }
}
