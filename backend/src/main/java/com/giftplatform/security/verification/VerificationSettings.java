package com.giftplatform.security.verification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/** Global contact verification policy. Bypass never manufactures ownership evidence. */
@Service
public class VerificationSettings {
 private final JdbcTemplate db;
 private final boolean mobileDefault,emailDefault;
 public record Settings(boolean mobileOtpEnabled,boolean emailVerificationEnabled){}
 public VerificationSettings(JdbcTemplate db,
     @Value("${gift.verification.mobile.enabled:false}") boolean mobileDefault,
     @Value("${gift.verification.email.enabled:false}") boolean emailDefault){this.db=db;this.mobileDefault=mobileDefault;this.emailDefault=emailDefault;}
 public Settings current(){
  var rows=db.query("SELECT mobile_otp_enabled,email_verification_enabled FROM platform_verification_settings WHERE id=1",(rs,n)->new Settings(rs.getBoolean(1),rs.getBoolean(2)));
  return rows.isEmpty()?new Settings(mobileDefault,emailDefault):rows.getFirst();
 }
 public boolean emailEnabled(){return current().emailVerificationEnabled();}
 public boolean mobileEnabled(){return current().mobileOtpEnabled();}
 public void requireEmail(){if(!emailEnabled())throw new ResponseStatusException(HttpStatus.CONFLICT,"Email verification is disabled by the platform administrator");}
 public void requireMobile(){if(!mobileEnabled())throw new ResponseStatusException(HttpStatus.CONFLICT,"Mobile verification is disabled by the platform administrator");}
}
