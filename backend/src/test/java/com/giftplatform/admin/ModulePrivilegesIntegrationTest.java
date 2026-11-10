package com.giftplatform.admin;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import java.util.UUID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest(properties={"gift.otp.hmac-key-base64=${gift.otp.secret-base64}","gift.verification.email.enabled=true"})
@AutoConfigureMockMvc
@Testcontainers
class ModulePrivilegesIntegrationTest {
 @Container static PostgreSQLContainer postgres=new PostgreSQLContainer("postgres:17-alpine");
 @DynamicPropertySource static void database(DynamicPropertyRegistry r){
  r.add("spring.datasource.url",postgres::getJdbcUrl);r.add("spring.datasource.username",postgres::getUsername);r.add("spring.datasource.password",postgres::getPassword);
 }
 @Autowired MockMvc mvc;
 @Autowired JdbcTemplate db;
 @Autowired com.giftplatform.security.recovery.SignupEmailVerificationService signupEmails;
 @Autowired org.springframework.transaction.support.TransactionTemplate transactions;
 @org.springframework.test.context.bean.override.mockito.MockitoBean(name="usernameReminderDelivery") com.giftplatform.security.delivery.MessageDelivery usernameEmail;
 UUID host,event,token,creator,group;
 @org.springframework.test.context.bean.override.mockito.MockitoBean(name="otpEmailDelivery") com.giftplatform.security.delivery.MessageDelivery verificationEmail;
 @BeforeEach void setup(){
  org.mockito.Mockito.when(verificationEmail.isAvailable()).thenReturn(true);
  org.mockito.Mockito.when(usernameEmail.isAvailable()).thenReturn(true);
  host=UUID.randomUUID();event=UUID.randomUUID();token=UUID.randomUUID();creator=UUID.randomUUID();group=UUID.randomUUID();
  db.update("INSERT INTO users(id,email,password_hash) VALUES (?,?,?)",host,host+"@example.test","hash");
  db.update("INSERT INTO events(id,title,occasion_type,status,owner_id,host_display_name,public_token) VALUES (?,?,?,?,?,?,?)",event,"Privilege test","BIRTHDAY","PUBLISHED",host,"Host",token);
  db.update("INSERT INTO gift_group_accounts(id,event_id,username,password_hash,full_name) VALUES (?,?,?,?,?)",creator,event,"creator","hash","Creator");
  db.update("INSERT INTO gift_groups(id,event_id,admin_id,name,creator_name,creator_mobile,status) VALUES (?,?,?,?,?,?,?)",group,event,creator,"Group","Creator","+919876543210","PUBLISHED");
  db.update("UPDATE gift_groups SET collection_published_at=now() WHERE id=?",group);
  db.update("UPDATE gift_group_accounts SET email=?,email_verified_at=now() WHERE id=?",creator+"@example.test",creator);
  db.update("INSERT INTO gift_group_payment_destinations(id,group_id,kind,label,account_ciphertext,account_fingerprint,ifsc_ciphertext,holder_ciphertext) VALUES(?,?,'BANK','Fixture destination','encrypted',?,'encrypted','encrypted')",UUID.randomUUID(),group,"b".repeat(64));
  grants(true,true);
 }
 void grants(boolean platform,boolean hostGrant){
  for(String code:new String[]{"GROUP_GIFTING","WHATSAPP_SHARING"}){
   db.update("UPDATE platform_module_controls SET enabled=? WHERE module_code=?",platform,code);
   db.update("INSERT INTO platform_host_module_grants(host_id,module_code,enabled) VALUES (?,?,?) ON CONFLICT(host_id,module_code) DO UPDATE SET enabled=EXCLUDED.enabled",host,code,hostGrant);
  }
 }
 String groups(){return "/api/public/groups/"+token;}
 RequestPostProcessor csrf(){return request->{var t=CookieCsrfTokenRepository.withHttpOnlyFalse().generateToken(request);request.setCookies(new Cookie("XSRF-TOKEN",t.getToken()));request.addHeader("X-XSRF-TOKEN",t.getToken());return request;};}
 @ParameterizedTest @CsvSource({"false,false","false,true","true,false","true,true"})
 void publicFlagsAndGroupApisRequireBothPlatformAndHostGrant(boolean platform,boolean hostGrant) throws Exception {
  grants(platform,hostGrant);boolean enabled=platform&&hostGrant;
  mvc.perform(get("/api/public/events/"+token)).andExpect(status().isOk())
   .andExpect(jsonPath("$.hostName").value("Host")).andExpect(jsonPath("$.invitationTheme").value("MODERN_MINIMAL")).andExpect(jsonPath("$.contactPhone").doesNotExist()).andExpect(jsonPath("$.features.GROUP_GIFTING").value(enabled)).andExpect(jsonPath("$.features.WHATSAPP_SHARING").value(enabled));
  mvc.perform(get("/api/public/events/"+token+"/features")).andExpect(status().isOk()).andExpect(header().string("Cache-Control",containsString("no-store")))
   .andExpect(jsonPath("$.GROUP_GIFTING").value(enabled)).andExpect(jsonPath("$.WHATSAPP_SHARING").value(enabled));
  mvc.perform(get(groups()+"/guest/"+group+"/invitation")).andExpect(status().is(enabled?200:404));
  mvc.perform(get(groups()+"/dashboard")).andExpect(status().is(enabled?401:404));
  mvc.perform(get(groups()+"/member-view/"+group)).andExpect(status().is(enabled?401:404));
  mvc.perform(get(groups()+"/payments/"+group+"/destinations")).andExpect(status().is(enabled?401:404));
  mvc.perform(post(groups()+"/register").with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{}"))
   .andExpect(status().is(enabled?400:404));
 }
 @ParameterizedTest @CsvSource({"false,false","false,true","true,false","true,true"})
 void socialSharingRequiresPlatformAndHostGrant(boolean platform,boolean hostGrant) throws Exception {
  db.update("UPDATE platform_module_controls SET enabled=? WHERE module_code='SOCIAL_SHARING'",platform);
  db.update("INSERT INTO platform_host_module_grants(host_id,module_code,enabled) VALUES(?,'SOCIAL_SHARING',?)",host,hostGrant);
  mvc.perform(get("/api/public/events/"+token+"/features")).andExpect(status().isOk())
   .andExpect(jsonPath("$.SOCIAL_SHARING").value(platform&&hostGrant));
 }
 @Test void creatorContributesWithOwnIdentityAndCannotBeImpersonated() throws Exception {
  UUID destination=UUID.randomUUID(),key=UUID.randomUUID();String page=UUID.randomUUID().toString();
  db.update("""
   INSERT INTO gift_group_payment_destinations(id,group_id,kind,label,account_ciphertext,account_fingerprint,ifsc_ciphertext,holder_ciphertext)
   VALUES(?,?,'BANK','Group collection account','encrypted',?,'encrypted','encrypted')
   """,destination,group,"a".repeat(64));
  db.update("UPDATE gift_groups SET manual_contributions_enabled=TRUE,guest_bank_destination_id=?,contribution_access_mode='MEMBERS_ONLY' WHERE id=?",destination,group);
  var session=new MockHttpSession();
  session.setAttribute("gift.group.admin."+event,creator);session.setAttribute("gift.group.admin.epoch."+event,0L);
  session.setAttribute("gift.group.activity."+event,System.currentTimeMillis());session.setAttribute("gift.group.page."+event,page);
  String route=groups()+"/guest/"+group;
  String body="{\"destinationId\":\""+destination+"\",\"guestName\":\"Forged name\",\"amountPaise\":50000,\"transactionReference\":\"CREATOR12345\",\"submissionKey\":\""+key+"\"}";
  mvc.perform(post(route+"/creator-report").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isUnauthorized());
  mvc.perform(post(route+"/creator-report").session(session).header("X-Group-Page-Id",UUID.randomUUID()).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isUnauthorized());
  for(int attempt=0;attempt<2;attempt++)mvc.perform(post(route+"/creator-report").session(session).header("X-Group-Page-Id",page).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body))
   .andExpect(status().isOk()).andExpect(jsonPath("$.state").value("REPORTED_UNVERIFIED"));
  var report=db.queryForMap("SELECT guest_name,creator_admin_id,member_id FROM gift_group_guest_reports WHERE group_id=?",group);
  org.junit.jupiter.api.Assertions.assertEquals("Creator",report.get("guest_name"));
  org.junit.jupiter.api.Assertions.assertEquals(creator,report.get("creator_admin_id"));
  org.junit.jupiter.api.Assertions.assertNull(report.get("member_id"));
  mvc.perform(get(route+"/creator-history").session(session).header("X-Group-Page-Id",page)).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
  mvc.perform(get(route+"/creator-history")).andExpect(status().isUnauthorized());
  UUID otherGroup=UUID.randomUUID(),otherCreator=UUID.randomUUID();
  db.update("INSERT INTO gift_group_accounts(id,event_id,username,password_hash) VALUES(?,?,?,'hash')",otherCreator,event,"othercreator");
  db.update("INSERT INTO gift_groups(id,event_id,admin_id,name,creator_name,creator_mobile,status) VALUES(?,?,?,'Other','Other','+919876543211','PUBLISHED')",otherGroup,event,otherCreator);
  mvc.perform(get(groups()+"/guest/"+otherGroup+"/creator-history").session(session).header("X-Group-Page-Id",page)).andExpect(status().isForbidden());
 }
 @Test void pinLoginRequiresMatchingUsernameForCreatorAndMemberOverHttp() throws Exception {
  db.update("UPDATE account_registration_policies SET allow_group_pin=TRUE WHERE scope='PLATFORM'");
  String hash=new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder(4).encode("123456");
  db.update("UPDATE gift_group_accounts SET pin_hash=? WHERE id=?",hash,creator);
  UUID member=UUID.randomUUID();
  db.update("INSERT INTO gift_group_members(id,group_id,display_name,username,password_hash,pin_hash,status) VALUES(?,?,'Alice','alice','unused',?,'APPROVED')",member,group,hash);
  String page=UUID.randomUUID().toString();
  mvc.perform(post(groups()+"/login").header("X-Group-Page-Id",page).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"pin\":\"123456\"}"))
   .andExpect(status().isBadRequest());
  mvc.perform(post(groups()+"/login").header("X-Group-Page-Id",page).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"username\":\"unknown\",\"pin\":\"123456\"}"))
   .andExpect(status().isUnauthorized());
  mvc.perform(post(groups()+"/login").header("X-Group-Page-Id",page).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"username\":\"creator\",\"pin\":\"123456\"}"))
   .andExpect(status().isOk()).andExpect(jsonPath("$.message").value("Signed in"));
  String login=groups()+"/member-login/"+group;
  mvc.perform(post(login).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"pin\":\"123456\"}"))
   .andExpect(status().isBadRequest());
  mvc.perform(post(login).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"username\":\"unknown\",\"pin\":\"123456\"}"))
   .andExpect(status().isUnauthorized());
  mvc.perform(post(login).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"username\":\"ALICE\",\"pin\":\"654321\"}"))
   .andExpect(status().isUnauthorized());
  mvc.perform(post(login).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"username\":\"ALICE\",\"pin\":\"123456\"}"))
   .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("APPROVED"));
  // Older clients still need a nonempty group username in displayName.
  mvc.perform(post(login).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"displayName\":\"Alice\",\"pin\":\"123456\"}"))
   .andExpect(status().isOk());
 }
 @Test void usernameReminderOnlyGoesToVerifiedEmailAndDoesNotChangeCredentials() throws Exception {
  String address="verified-"+creator+"@example.test";
  db.update("UPDATE gift_group_accounts SET email=?,email_verified_at=now(),pin_hash='unchanged-pin' WHERE id=?",address,creator);
  String input="{\"accountKind\":\"GROUP_ADMIN\",\"identifier\":\""+address+"\",\"eventToken\":\""+token+"\"}";
  var response=mvc.perform(post("/api/auth/recovery/username/request").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(input))
   .andExpect(status().isOk()).andExpect(header().string("Cache-Control",containsString("no-store")))
   .andExpect(jsonPath("$.username").doesNotExist()).andReturn().getResponse().getContentAsString();
  org.mockito.Mockito.verify(usernameEmail).send(org.mockito.ArgumentMatchers.eq(address),org.mockito.ArgumentMatchers.contains("username is: creator"));
  String missing="{\"accountKind\":\"GROUP_ADMIN\",\"identifier\":\"unknown@example.test\",\"eventToken\":\""+token+"\"}";
  mvc.perform(post("/api/auth/recovery/username/request").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(missing))
   .andExpect(status().isOk()).andExpect(content().json(response));
  var credentials=db.queryForMap("SELECT password_hash,pin_hash FROM gift_group_accounts WHERE id=?",creator);
  org.junit.jupiter.api.Assertions.assertEquals("hash",credentials.get("password_hash"));
  org.junit.jupiter.api.Assertions.assertEquals("unchanged-pin",credentials.get("pin_hash"));
  org.junit.jupiter.api.Assertions.assertEquals(0,db.queryForObject("SELECT count(*) FROM credential_session_versions WHERE account_kind='GROUP_ADMIN' AND account_id=?",Integer.class,creator));
 }
 @Test void unverifiedEmailAndWrongGroupCannotReceiveUsername() throws Exception {
  String address="unverified-"+creator+"@example.test";
  db.update("UPDATE gift_group_accounts SET email=?,email_verified_at=NULL WHERE id=?",address,creator);
  String input="{\"accountKind\":\"GROUP_ADMIN\",\"identifier\":\""+address+"\",\"eventToken\":\""+token+"\"}";
  mvc.perform(post("/api/auth/recovery/username/request").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(input)).andExpect(status().isOk());
  db.update("UPDATE gift_group_accounts SET email_verified_at=now() WHERE id=?",creator);
  mvc.perform(post("/api/auth/recovery/username/request").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(input.replace(token.toString(),UUID.randomUUID().toString()))).andExpect(status().isOk());
  mvc.perform(post("/api/auth/recovery/username/request").contentType(MediaType.APPLICATION_JSON).content(input)).andExpect(status().isForbidden());
  org.mockito.Mockito.verify(usernameEmail,org.mockito.Mockito.never()).send(org.mockito.ArgumentMatchers.anyString(),org.mockito.ArgumentMatchers.anyString());
 }
 @Test void usernameRemindersAreLimitedPerAccountEvenAcrossAddresses() throws Exception {
  String address="limited-"+creator+"@example.test";
  db.update("UPDATE gift_group_accounts SET email=?,email_verified_at=now() WHERE id=?",address,creator);
  String input="{\"accountKind\":\"GROUP_ADMIN\",\"identifier\":\""+address+"\",\"eventToken\":\""+token+"\"}";
  for(int n=0;n<4;n++)mvc.perform(post("/api/auth/recovery/username/request").with(csrf()).with(req->{req.setRemoteAddr(UUID.randomUUID().toString());return req;}).contentType(MediaType.APPLICATION_JSON).content(input)).andExpect(status().isOk());
  org.mockito.Mockito.verify(usernameEmail,org.mockito.Mockito.times(3)).send(org.mockito.ArgumentMatchers.eq(address),org.mockito.ArgumentMatchers.anyString());
 }
 @Test void memberUsernameRecoveryUsesGroupContextForSharedEmail() throws Exception {
  String address="member-"+group+"@example.test";UUID secondGroup=UUID.randomUUID(),secondCreator=UUID.randomUUID();
  db.update("INSERT INTO gift_group_accounts(id,event_id,username,password_hash) VALUES(?,?,?,'hash')",secondCreator,event,"secondcreator");
  db.update("INSERT INTO gift_groups(id,event_id,admin_id,name,creator_name,creator_mobile,status) VALUES(?,?,?,'Other','Other','+919876543211','PUBLISHED')",secondGroup,event,secondCreator);
  for(UUID g:java.util.List.of(group,secondGroup))db.update("INSERT INTO gift_group_members(id,group_id,display_name,username,password_hash,status,email,email_verified_at) VALUES(?,?,?,?,'hash','APPROVED',?,now())",UUID.randomUUID(),g,g.equals(group)?"Alice":"Bob",g.equals(group)?"alice":"bob",address);
  String input="{\"accountKind\":\"GROUP_MEMBER\",\"identifier\":\""+address+"\",\"eventToken\":\""+token+"\",\"groupId\":\""+group+"\"}";
  mvc.perform(post("/api/auth/recovery/username/request").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(input)).andExpect(status().isOk());
  org.mockito.Mockito.verify(usernameEmail).send(org.mockito.ArgumentMatchers.eq(address),org.mockito.ArgumentMatchers.contains("username is: alice"));
  org.mockito.Mockito.verify(usernameEmail,org.mockito.Mockito.never()).send(org.mockito.ArgumentMatchers.anyString(),org.mockito.ArgumentMatchers.contains("username is: bob"));
 }
 @ParameterizedTest @CsvSource({"HOST,CUSTOMER","PLATFORM_ADMIN,PLATFORM_ADMIN"})
 void hostAndAdminUsernameRecoveryRequiresCorrectRoleAndVerifiedEnabledAccount(String kind,String role) throws Exception {
  String ip=UUID.randomUUID().toString();UUID id=UUID.randomUUID();String username="recover_"+id.toString().replace("-","").substring(0,12),address=id+"@example.test";
  db.update("INSERT INTO users(id,email,username,password_hash,role,enabled,email_verified_at) VALUES(?,?,?,'original-password',?,TRUE,now())",id,address,username,role);
  String input="{\"accountKind\":\""+kind+"\",\"identifier\":\""+address+"\"}";
  String response=mvc.perform(post("/api/auth/recovery/username/request").with(csrf()).with(req->{req.setRemoteAddr(ip);return req;}).contentType(MediaType.APPLICATION_JSON).content(input))
   .andExpect(status().isOk()).andExpect(header().string("Cache-Control",containsString("no-store")))
   .andExpect(jsonPath("$.username").doesNotExist()).andReturn().getResponse().getContentAsString();
  org.mockito.Mockito.verify(usernameEmail).send(org.mockito.ArgumentMatchers.eq(address),org.mockito.ArgumentMatchers.contains("username is: "+username));
  String other=kind.equals("HOST")?"PLATFORM_ADMIN":"HOST";
  mvc.perform(post("/api/auth/recovery/username/request").with(csrf()).with(req->{req.setRemoteAddr(ip);return req;}).contentType(MediaType.APPLICATION_JSON).content(input.replace(kind,other)))
   .andExpect(status().isOk()).andExpect(content().json(response));
  db.update("UPDATE users SET email_verified_at=NULL WHERE id=?",id);
  mvc.perform(post("/api/auth/recovery/username/request").with(csrf()).with(req->{req.setRemoteAddr(ip);return req;}).contentType(MediaType.APPLICATION_JSON).content(input))
   .andExpect(status().isOk()).andExpect(content().json(response));
  db.update("UPDATE users SET email_verified_at=now(),enabled=FALSE WHERE id=?",id);
  mvc.perform(post("/api/auth/recovery/username/request").with(csrf()).with(req->{req.setRemoteAddr(ip);return req;}).contentType(MediaType.APPLICATION_JSON).content(input))
   .andExpect(status().isOk()).andExpect(content().json(response));
  org.mockito.Mockito.verifyNoMoreInteractions(org.mockito.Mockito.ignoreStubs(usernameEmail));
  org.junit.jupiter.api.Assertions.assertEquals("original-password",db.queryForObject("SELECT password_hash FROM users WHERE id=?",String.class,id));
  org.junit.jupiter.api.Assertions.assertEquals(0,db.queryForObject("SELECT count(*) FROM credential_session_versions WHERE account_id=?",Integer.class,id));
 }
 @ParameterizedTest @CsvSource({"HOST,CUSTOMER","PLATFORM_ADMIN,PLATFORM_ADMIN"})
 void legacyHostAndAdminWithoutUsernameGetEmailSignInGuidance(String kind,String role) throws Exception {
  String ip=UUID.randomUUID().toString();UUID id=UUID.randomUUID();String address=id+"@example.test";
  db.update("INSERT INTO users(id,email,password_hash,role,enabled,email_verified_at) VALUES(?,?,'original-password',?,TRUE,now())",id,address,role);
  String input="{\"accountKind\":\""+kind+"\",\"identifier\":\""+address+"\"}";
  mvc.perform(post("/api/auth/recovery/username/request").with(csrf()).with(req->{req.setRemoteAddr(ip);return req;}).contentType(MediaType.APPLICATION_JSON).content(input))
   .andExpect(status().isOk()).andExpect(jsonPath("$.username").doesNotExist());
  org.mockito.Mockito.verify(usernameEmail).send(org.mockito.ArgumentMatchers.eq(address),org.mockito.ArgumentMatchers.contains("Sign in using your email address: "+address));
 }
 @Test void disabledUsernameDeliveryReturnsUnavailableWithoutClaimingEmailWasSent() throws Exception {
  org.mockito.Mockito.when(usernameEmail.isAvailable()).thenReturn(false);
  for(String kind:java.util.List.of("HOST","PLATFORM_ADMIN","GROUP_ADMIN","GROUP_MEMBER")){
   mvc.perform(post("/api/auth/recovery/username/request").with(csrf()).contentType(MediaType.APPLICATION_JSON)
    .content("{\"accountKind\":\""+kind+"\",\"identifier\":\"unknown@example.test\"}"))
    .andExpect(status().isServiceUnavailable());
  }
  org.mockito.Mockito.verify(usernameEmail,org.mockito.Mockito.never()).send(org.mockito.ArgumentMatchers.anyString(),org.mockito.ArgumentMatchers.anyString());
 }
 @ParameterizedTest @CsvSource({"CUSTOMER","PLATFORM_ADMIN"})
 void hostAndAdminCanVerifyEmailWithSigningKeyAndDeliveredCode(String role) throws Exception {
  UUID id=UUID.randomUUID();String address=id+"@example.test";
  db.update("INSERT INTO users(id,email,password_hash,role,enabled) VALUES(?,?,'original-password',?,TRUE)",id,address,role);
  var session=signedIn(id,role);
  String issued=mvc.perform(post("/api/auth/recovery/enroll-email").session(session).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{}"))
   .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
  var delivered=org.mockito.ArgumentCaptor.forClass(String.class);
  org.mockito.Mockito.verify(verificationEmail).send(org.mockito.ArgumentMatchers.eq(address),delivered.capture());
  var match=java.util.regex.Pattern.compile("[0-9]{6}").matcher(delivered.getValue());
  org.junit.jupiter.api.Assertions.assertTrue(match.find());String code=match.group();
  String challenge=new tools.jackson.databind.ObjectMapper().readTree(issued).path("challengeId").asText();
  org.junit.jupiter.api.Assertions.assertNull(db.queryForObject("SELECT email_verified_at FROM users WHERE id=?",java.sql.Timestamp.class,id));
  mvc.perform(post("/api/auth/recovery/verify-email").session(session).with(csrf()).contentType(MediaType.APPLICATION_JSON)
   .content("{\"challengeId\":\""+challenge+"\",\"code\":\"000000\"}".replace("000000",code.equals("000000")?"111111":"000000")))
   .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Invalid or expired code"));
  mvc.perform(post("/api/auth/recovery/verify-email").session(session).with(csrf()).contentType(MediaType.APPLICATION_JSON)
   .content("{\"challengeId\":\""+challenge+"\",\"code\":\""+code+"\"}"))
   .andExpect(status().isOk());
  org.junit.jupiter.api.Assertions.assertNotNull(db.queryForObject("SELECT email_verified_at FROM users WHERE id=?",java.sql.Timestamp.class,id));
  org.junit.jupiter.api.Assertions.assertEquals("original-password",db.queryForObject("SELECT password_hash FROM users WHERE id=?",String.class,id));
 }
 @Test void verificationDeliveryFailureReturnsActionableSafeMessage() throws Exception {
  var session=signedIn(host,"CUSTOMER");
  org.mockito.Mockito.doThrow(new com.giftplatform.security.delivery.EmailDeliveryException("The email sender domain is not verified. Verify the sending domain in Resend, then try again."))
   .when(verificationEmail).send(org.mockito.ArgumentMatchers.anyString(),org.mockito.ArgumentMatchers.anyString());
  mvc.perform(post("/api/auth/recovery/enroll-email").session(session).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{}"))
   .andExpect(status().isBadGateway()).andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("sender domain is not verified")));
 }
 String signupProof(String kind,String email) throws Exception {
  var input=new java.util.LinkedHashMap<String,Object>();input.put("accountKind",kind);input.put("email",email);
  if(!kind.equals("HOST"))input.put("eventToken",token);
  if(kind.equals("GROUP_MEMBER"))input.put("groupId",group);
  String body=new tools.jackson.databind.ObjectMapper().writeValueAsString(input);
  String issued=mvc.perform(post("/api/auth/signup-email/start").with(csrf()).with(req->{req.setRemoteAddr(UUID.randomUUID().toString());return req;}).contentType(MediaType.APPLICATION_JSON).content(body))
   .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
  var sent=org.mockito.ArgumentCaptor.forClass(String.class);
  org.mockito.Mockito.verify(verificationEmail,org.mockito.Mockito.atLeastOnce()).send(org.mockito.ArgumentMatchers.eq(email),sent.capture());
  var code=java.util.regex.Pattern.compile("[0-9]{6}").matcher(sent.getAllValues().getLast());org.junit.jupiter.api.Assertions.assertTrue(code.find());
  String challenge=new tools.jackson.databind.ObjectMapper().readTree(issued).path("challengeId").asText();
  String verified=mvc.perform(post("/api/auth/signup-email/verify").with(csrf()).contentType(MediaType.APPLICATION_JSON)
   .content("{\"challengeId\":\""+challenge+"\",\"code\":\""+code.group()+"\"}"))
   .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
  return new tools.jackson.databind.ObjectMapper().readTree(verified).path("verificationToken").asText();
 }
 @ParameterizedTest @org.junit.jupiter.params.provider.ValueSource(strings={"HOST","GROUP_ADMIN","GROUP_MEMBER"})
 void signupVerifiesEmailWithoutExistingLoginSession(String kind) throws Exception {
  String address=UUID.randomUUID()+"@example.test",proof=signupProof(kind,address),username="newuser_"+UUID.randomUUID().toString().replace("-","").substring(0,8);
  var input=new java.util.LinkedHashMap<String,Object>();input.put("email",address);input.put("emailVerificationToken",proof);
  input.put("password","StrongPassword123!");input.put("mobile","+91"+java.util.concurrent.ThreadLocalRandom.current().nextLong(6000000000L,9999999999L));
  String route,table;
  if(kind.equals("HOST")){input.put("fullName","New Host");input.put("username",username);route="/api/auth/register";table="users";}
  else if(kind.equals("GROUP_ADMIN")){input.put("fullName","New Creator");input.put("username",username);input.put("pin","468291");input.put("confirmPin","468291");route=groups()+"/register";table="gift_group_accounts";}
  else{input.put("displayName","New Member");input.put("confirmPassword","StrongPassword123!");input.put("pin","468291");input.put("confirmPin","468291");route=groups()+"/join/"+group;table="gift_group_members";}
  mvc.perform(post(route).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(new tools.jackson.databind.ObjectMapper().writeValueAsString(input)))
   .andExpect(status().is(kind.equals("GROUP_MEMBER")?200:201));
  org.junit.jupiter.api.Assertions.assertNotNull(db.queryForObject("SELECT email_verified_at FROM "+table+" WHERE email=?",java.sql.Timestamp.class,address));
  org.junit.jupiter.api.Assertions.assertEquals(1,db.queryForObject("SELECT count(*) FROM signup_email_verifications WHERE email=? AND consumed_at IS NOT NULL",Integer.class,address));
 }
 @Test void signupEmailProofCannotBeUsedForDifferentEmailScopeOrTwice() throws Exception {
  String address=UUID.randomUUID()+"@example.test",proof=signupProof("HOST",address);
  for(String kind:java.util.List.of("GROUP_ADMIN","GROUP_MEMBER")){
   org.junit.jupiter.api.Assertions.assertThrows(org.springframework.web.server.ResponseStatusException.class,()->transactions.execute(status->signupEmails.consume(proof,kind,group,address)));
  }
  org.junit.jupiter.api.Assertions.assertThrows(org.springframework.web.server.ResponseStatusException.class,()->transactions.execute(status->signupEmails.consume(proof,"HOST",null,"other@example.test")));
  org.junit.jupiter.api.Assertions.assertTrue(Boolean.TRUE.equals(transactions.execute(status->signupEmails.consume(proof,"HOST",null,address))));
  org.junit.jupiter.api.Assertions.assertThrows(org.springframework.web.server.ResponseStatusException.class,()->transactions.execute(status->signupEmails.consume(proof,"HOST",null,address)));
 }
 @ParameterizedTest @org.junit.jupiter.params.provider.ValueSource(strings={"HOST","GROUP_ADMIN","GROUP_MEMBER"})
 void signupAllowsFormatValidEmailWithoutProofButRejectsForgedProof(String kind) throws Exception {
  String address=UUID.randomUUID()+"@example.test";var input=new java.util.LinkedHashMap<String,Object>();
  input.put("email",address);input.put("fullName","Unverified User");input.put("username","unverified_"+UUID.randomUUID().toString().replace("-","").substring(0,8));
  input.put("displayName","Unverified Member");input.put("password","StrongPassword123!");input.put("confirmPassword","StrongPassword123!");
  input.put("mobile","+91"+java.util.concurrent.ThreadLocalRandom.current().nextLong(6000000000L,9999999999L));input.put("pin","468291");input.put("confirmPin","468291");
  String route=kind.equals("HOST")?"/api/auth/register":kind.equals("GROUP_ADMIN")?groups()+"/register":groups()+"/join/"+group;
  String table=kind.equals("HOST")?"users":kind.equals("GROUP_ADMIN")?"gift_group_accounts":"gift_group_members";
  for(String proof:java.util.Arrays.asList("not-a-proof",null)){
   input.put("emailVerificationToken",proof);
   mvc.perform(post(route).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(new tools.jackson.databind.ObjectMapper().writeValueAsString(input)))
    .andExpect(proof==null?status().is2xxSuccessful():status().isBadRequest());
   org.junit.jupiter.api.Assertions.assertEquals(proof==null?1:0,db.queryForObject("SELECT count(*) FROM "+table+" WHERE email=?",Integer.class,address));
   if(proof==null)org.junit.jupiter.api.Assertions.assertNull(db.queryForObject("SELECT email_verified_at FROM "+table+" WHERE email=?",java.sql.Timestamp.class,address));
  }
 }
 @Test void signupProofEmailMismatchRollsBackAccountCreation() throws Exception {
  String address=UUID.randomUUID()+"@example.test",proof=signupProof("HOST",address),other=UUID.randomUUID()+"@example.test";
  String input="{\"fullName\":\"Wrong Email\",\"username\":\"wrong_"+UUID.randomUUID().toString().replace("-","").substring(0,8)+"\",\"email\":\""+other+"\",\"mobile\":\""+("+91"+java.util.concurrent.ThreadLocalRandom.current().nextLong(6000000000L,9999999999L))+"\",\"password\":\"StrongPassword123!\",\"emailVerificationToken\":\""+proof+"\"}";
  mvc.perform(post("/api/auth/register").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(input)).andExpect(status().isBadRequest());
  org.junit.jupiter.api.Assertions.assertEquals(0,db.queryForObject("SELECT count(*) FROM users WHERE email=?",Integer.class,other));
  org.junit.jupiter.api.Assertions.assertEquals(0,db.queryForObject("SELECT count(*) FROM signup_email_verifications WHERE email=? AND consumed_at IS NOT NULL",Integer.class,address));
 }
 @Test void signupEmailEndpointsKeepCsrfProtectionAndGroupPermissions() throws Exception {
  String body="{\"accountKind\":\"GROUP_ADMIN\",\"email\":\"signup@example.test\",\"eventToken\":\""+token+"\"}";
  mvc.perform(post("/api/auth/signup-email/start").contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isForbidden());
  grants(false,false);
  mvc.perform(post("/api/auth/signup-email/start").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isNotFound());
 }
 @Test void incorrectSignupCodesAreCountedAndCannotVerifyAfterFiveGuesses() throws Exception {
  String address=UUID.randomUUID()+"@example.test";
  String issued=mvc.perform(post("/api/auth/signup-email/start").with(csrf()).with(req->{req.setRemoteAddr(UUID.randomUUID().toString());return req;})
   .contentType(MediaType.APPLICATION_JSON).content("{\"accountKind\":\"HOST\",\"email\":\""+address+"\"}"))
   .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
  var sent=org.mockito.ArgumentCaptor.forClass(String.class);org.mockito.Mockito.verify(verificationEmail).send(org.mockito.ArgumentMatchers.eq(address),sent.capture());
  var match=java.util.regex.Pattern.compile("[0-9]{6}").matcher(sent.getValue());org.junit.jupiter.api.Assertions.assertTrue(match.find());
  String correct=match.group(),wrong=correct.equals("111111")?"222222":"111111",challenge=new tools.jackson.databind.ObjectMapper().readTree(issued).path("challengeId").asText();
  for(int n=0;n<5;n++)mvc.perform(post("/api/auth/signup-email/verify").with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"challengeId\":\""+challenge+"\",\"code\":\""+wrong+"\"}"))
   .andExpect(status().isBadRequest());
  org.junit.jupiter.api.Assertions.assertEquals(5,db.queryForObject("SELECT attempt_count FROM signup_email_verifications WHERE id=?",Integer.class,UUID.fromString(challenge)));
  mvc.perform(post("/api/auth/signup-email/verify").with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"challengeId\":\""+challenge+"\",\"code\":\""+correct+"\"}"))
   .andExpect(status().isBadRequest());
  org.junit.jupiter.api.Assertions.assertNull(db.queryForObject("SELECT verified_at FROM signup_email_verifications WHERE id=?",java.sql.Timestamp.class,UUID.fromString(challenge)));
 }
 @Test void disablingHostDoesNotDisableAnotherHostsFeatures() throws Exception {
  grants(true,false);UUID other=UUID.randomUUID(),otherToken=UUID.randomUUID();
  db.update("INSERT INTO users(id,email,password_hash) VALUES (?,?,?)",other,other+"@example.test","hash");
  db.update("INSERT INTO events(id,title,occasion_type,status,owner_id,host_display_name,public_token) VALUES (?,?,?,?,?,?,?)",UUID.randomUUID(),"Other event","BIRTHDAY","PUBLISHED",other,"Other host",otherToken);
  for(String code:new String[]{"GROUP_GIFTING","WHATSAPP_SHARING"})db.update("INSERT INTO platform_host_module_grants(host_id,module_code,enabled) VALUES (?,?,TRUE)",other,code);
  mvc.perform(get("/api/public/events/"+otherToken+"/features")).andExpect(status().isOk()).andExpect(jsonPath("$.GROUP_GIFTING").value(true)).andExpect(jsonPath("$.WHATSAPP_SHARING").value(true));
  mvc.perform(get("/api/public/events/"+token+"/features")).andExpect(status().isOk()).andExpect(jsonPath("$.GROUP_GIFTING").value(false)).andExpect(jsonPath("$.WHATSAPP_SHARING").value(false));
 }
 @Test void privilegeRevocationAppliesToAnAlreadySignedInCreatorAndLogoutStillWorks() throws Exception {
  String page=UUID.randomUUID().toString();var session=new MockHttpSession();
  session.setAttribute("gift.group.admin."+event,creator);session.setAttribute("gift.group.admin.epoch."+event,0L);session.setAttribute("gift.group.activity."+event,System.currentTimeMillis());session.setAttribute("gift.group.page."+event,page);
  mvc.perform(get(groups()+"/dashboard").session(session).header("X-Group-Page-Id",page)).andExpect(status().isOk());
  grants(false,true);
  mvc.perform(get(groups()+"/dashboard").session(session).header("X-Group-Page-Id",page)).andExpect(status().isNotFound());
  mvc.perform(get("/api/public/events/"+token+"/features")).andExpect(status().isOk()).andExpect(jsonPath("$.GROUP_GIFTING").value(false));
  mvc.perform(post(groups()+"/logout").session(session).header("X-Group-Page-Id",page).with(csrf())).andExpect(status().isOk());
 }
 @Test void missingHostGrantDoesNotEnableFeatures() throws Exception {
  db.update("DELETE FROM platform_host_module_grants WHERE host_id=?",host);
  mvc.perform(get("/api/public/events/"+token+"/features")).andExpect(status().isOk()).andExpect(jsonPath("$.GROUP_GIFTING").value(false)).andExpect(jsonPath("$.WHATSAPP_SHARING").value(false));
  mvc.perform(get(groups()+"/guest/"+group+"/invitation")).andExpect(status().isNotFound());
 }
 @Test void draftEventDoesNotExposeFeatureFlags() throws Exception {
  db.update("UPDATE events SET status='DRAFT' WHERE id=?",event);
  mvc.perform(get("/api/public/events/"+token+"/features")).andExpect(status().isNotFound());
 }
 MockHttpSession signedIn(UUID id,String role){
  var principal=new com.giftplatform.security.GiftUserPrincipal(id,id+"@example.test","hash",role,true);
  var context=org.springframework.security.core.context.SecurityContextHolder.createEmptyContext();
  context.setAuthentication(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(principal,null,principal.getAuthorities()));
  var session=new MockHttpSession();session.setAttribute("SPRING_SECURITY_CONTEXT",context);session.setAttribute("gift.credential.epoch",0L);return session;
 }
 @Test void experienceSwitchIsAdminOnlyCsrfProtectedPersistentAndAudited() throws Exception {
  db.update("UPDATE platform_experience_settings SET view_mode='CLASSIC',changed_by=NULL WHERE id=1");
  UUID admin=UUID.randomUUID();db.update("INSERT INTO users(id,email,password_hash,role) VALUES (?,?,?,?)",admin,admin+"@example.test","hash","PLATFORM_ADMIN");
  var adminSession=signedIn(admin,"PLATFORM_ADMIN");
  mvc.perform(get("/api/public/experience")).andExpect(status().isOk()).andExpect(jsonPath("$.viewMode").value("CLASSIC"));
  mvc.perform(post("/api/admin/console/experience").with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"viewMode\":\"FOCUSED\"}")).andExpect(status().isUnauthorized());
  mvc.perform(post("/api/admin/console/experience").session(signedIn(host,"CUSTOMER")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"viewMode\":\"FOCUSED\"}")).andExpect(status().isForbidden());
  mvc.perform(post("/api/admin/console/experience").session(adminSession).contentType(MediaType.APPLICATION_JSON).content("{\"viewMode\":\"FOCUSED\"}")).andExpect(status().isForbidden());
  mvc.perform(post("/api/admin/console/experience").session(adminSession).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"viewMode\":\"FOCUSED\"}")).andExpect(status().isOk()).andExpect(jsonPath("$.viewMode").value("FOCUSED"));
  mvc.perform(get("/api/public/experience")).andExpect(status().isOk()).andExpect(header().string("Cache-Control",containsString("no-store"))).andExpect(jsonPath("$.viewMode").value("FOCUSED")).andExpect(jsonPath("$.changedBy").doesNotExist());
  org.junit.jupiter.api.Assertions.assertEquals(1L,db.queryForObject("SELECT count(*) FROM platform_admin_action_audit WHERE actor_id=? AND action='EXPERIENCE_FOCUSED'",Long.class,admin));
  mvc.perform(post("/api/admin/console/experience").session(adminSession).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"viewMode\":\"UNKNOWN\"}")).andExpect(status().isBadRequest());
  mvc.perform(get("/api/public/experience")).andExpect(jsonPath("$.viewMode").value("FOCUSED"));
  mvc.perform(post("/api/admin/console/experience").session(adminSession).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"viewMode\":\"CLASSIC\"}")).andExpect(status().isOk()).andExpect(jsonPath("$.viewMode").value("CLASSIC"));
 }
 @Test void draftPreviewFeaturesRequireEventOwnership() throws Exception {
  db.update("UPDATE events SET status='DRAFT' WHERE id=?",event);
  mvc.perform(get("/api/v1/events/"+event+"/features")).andExpect(status().isUnauthorized());
  mvc.perform(get("/api/v1/events/"+event+"/features").session(signedIn(host,"CUSTOMER"))).andExpect(status().isOk()).andExpect(jsonPath("$.GROUP_GIFTING").value(true));
  mvc.perform(get("/api/v1/events/"+event+"/features").session(signedIn(UUID.randomUUID(),"CUSTOMER"))).andExpect(status().isNotFound());
  mvc.perform(get("/api/public/events/"+token)).andExpect(status().isNotFound());
 }

}
