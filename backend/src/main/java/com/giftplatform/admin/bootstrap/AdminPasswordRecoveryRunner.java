package com.giftplatform.admin.bootstrap;

import com.giftplatform.user.entity.User;
import com.giftplatform.user.entity.UserRole;
import com.giftplatform.user.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;

/** Explicit offline emergency recovery; never reachable over HTTP. */
@Component
@Profile("admin-password-recovery")
public class AdminPasswordRecoveryRunner implements ApplicationRunner {
 private final UserRepository users;
 private final PasswordEncoder passwords;
 public AdminPasswordRecoveryRunner(UserRepository users,PasswordEncoder passwords){this.users=users;this.passwords=passwords;}
 @Override @Transactional
 public void run(ApplicationArguments args){
  if(!"YES".equals(System.getenv("ADMIN_RECOVERY_CONFIRM")))throw new IllegalStateException("Explicit ADMIN_RECOVERY_CONFIRM=YES required");
  String email=System.getenv("ADMIN_RECOVERY_EMAIL"),password=System.getenv("ADMIN_RECOVERY_NEW_PASSWORD");
  if(email==null||password==null||password.length()<16||password.length()>72)throw new IllegalArgumentException("Valid email and password of 16–72 characters required");
  User admin=users.findByEmailIgnoreCase(email.strip().toLowerCase(Locale.ROOT)).orElseThrow(()->new IllegalStateException("Account not found"));
  if(admin.getRole()!=UserRole.PLATFORM_ADMIN||!admin.isEnabled())throw new IllegalStateException("Account is not an enabled platform administrator");
  admin.setPasswordHash(passwords.encode(password));
  users.saveAndFlush(admin);
  System.out.println("Password updated for existing administrator. Restart the application and invalidate existing sessions before use.");
 }
}
