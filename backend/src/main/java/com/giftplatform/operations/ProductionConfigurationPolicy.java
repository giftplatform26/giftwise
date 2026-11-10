package com.giftplatform.operations;

import org.springframework.core.env.Environment;
import java.net.URI;
import java.nio.file.Path;
import java.util.*;

/** Configuration checks only. Passing this policy is not a production readiness certificate. */
public final class ProductionConfigurationPolicy {
 private ProductionConfigurationPolicy(){}
 public static List<String> violations(Environment env){
  List<String> failures=new ArrayList<>();
  if(Arrays.stream(env.getActiveProfiles()).anyMatch(p->Set.of("local","test","admin-provision","admin-password-recovery").contains(p)))failures.add("Production cannot be combined with local/test/admin bootstrap profiles");
  if(!env.getProperty("server.servlet.session.cookie.secure",Boolean.class,false))failures.add("Secure session cookies required");
  if(!env.getProperty("server.servlet.session.cookie.http-only",Boolean.class,false))failures.add("HttpOnly session cookies required");
  if(!Set.of("lax","strict").contains(env.getProperty("server.servlet.session.cookie.same-site","").toLowerCase(Locale.ROOT)))failures.add("SameSite Lax or Strict session cookies required");
  String origin=env.getProperty("gift.production.public-origin","");
  try{URI uri=URI.create(origin);String host=uri.getHost();if(!"https".equals(uri.getScheme())||host==null||Set.of("localhost","127.0.0.1","::1").contains(host)||uri.getUserInfo()!=null||uri.getQuery()!=null||uri.getFragment()!=null||(uri.getPath()!=null&&!uri.getPath().isEmpty()&&!uri.getPath().equals("/")))throw new IllegalArgumentException();}catch(IllegalArgumentException ex){failures.add("A canonical external HTTPS public origin is required");}
  if(env.getProperty("gift.otp.sms.simulator.enabled",Boolean.class,false)||"simulator".equalsIgnoreCase(env.getProperty("gift.otp.sms.provider","")))failures.add("SMS simulator forbidden in production");
  if(env.getProperty("gift.production.instance-count",Integer.class,1)!=1)failures.add("Current session/rate-window topology supports only one declared backend instance; distributed rollout requires additional implementation");
  if(!"never".equals(env.getProperty("server.error.include-message","")))failures.add("Public server error messages must be disabled");
  for(String name:List.of("gift.otp.secret-base64","gift.otp.hmac-key-base64"))checkKey(env,name,failures);
  if(env.getProperty("gift.payment.profiles.enabled",Boolean.class,false)){
   checkKey(env,"gift.payment.encryption-key-base64",failures);checkKey(env,"gift.payment.fingerprint-key-base64",failures);
   if(Objects.equals(env.getProperty("gift.payment.encryption-key-base64"),env.getProperty("gift.payment.fingerprint-key-base64")))failures.add("Payment encryption and fingerprint keys must differ");
  }
  String media=env.getProperty("gift.guestbook.photos.dir","");try{if(media.isBlank()||!Path.of(media).isAbsolute()||Path.of(media).normalize().equals(Path.of("/")))failures.add("An explicit absolute private media directory is required");}catch(RuntimeException ex){failures.add("Invalid private media directory");}
  String ddl=env.getProperty("spring.jpa.hibernate.ddl-auto","");if(!Set.of("validate","none").contains(ddl))failures.add("Schema changes must use Flyway; Hibernate DDL must be validate or none");
  if(!env.getProperty("spring.flyway.enabled",Boolean.class,true))failures.add("Flyway must remain enabled");
  if(env.getProperty("spring.flyway.clean-disabled",Boolean.class,false)==false)failures.add("Flyway clean must be disabled");
  if(env.getProperty("gift.invitation.ai.enabled",Boolean.class,false)&&env.getProperty("gift.invitation.ai.api-key","").isBlank())failures.add("Enabled AI requires a provider credential");
  return List.copyOf(failures);
 }
 private static void checkKey(Environment env,String name,List<String> errors){try{byte[] value=Base64.getDecoder().decode(env.getProperty(name,""));Set<Byte> unique=new HashSet<>();for(byte b:value)unique.add(b);if(value.length!=32||unique.size()<8)errors.add(name+" requires a non-placeholder 256-bit key");}catch(IllegalArgumentException ex){errors.add(name+" requires valid base64");}}
}
