package com.giftplatform.security.verification;

import com.giftplatform.event.entity.*;
import com.giftplatform.event.service.EventPublishReadiness;
import com.giftplatform.group.GroupReadinessService;
import com.giftplatform.security.ActorContext;
import com.giftplatform.security.delivery.MessageDelivery;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import org.springframework.web.server.ResponseStatusException;
import org.testcontainers.junit.jupiter.*;
import org.testcontainers.postgresql.PostgreSQLContainer;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

@SpringBootTest(properties={"gift.otp.secret-base64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=","gift.otp.hmac-key-base64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=","gift.otp.email.provider=disabled"})
@AutoConfigureMockMvc
@Testcontainers
class VerificationFlowIntegrationTest {
 @Container static PostgreSQLContainer postgres=new PostgreSQLContainer("postgres:17");
 @DynamicPropertySource static void database(DynamicPropertyRegistry r){r.add("spring.datasource.url",postgres::getJdbcUrl);r.add("spring.datasource.username",postgres::getUsername);r.add("spring.datasource.password",postgres::getPassword);}
 @Autowired JdbcTemplate db;@Autowired MobileVerificationService mobile;@Autowired VerificationSettings settings;
 @Autowired GroupReadinessService groups;@Autowired EventPublishReadiness events;@Autowired MockMvc mvc;
 @MockitoBean(name="otpSmsDelivery") MessageDelivery sms;
 @MockitoBean(name="otpEmailDelivery") MessageDelivery email;
 @MockitoBean ActorContext actor;
 UUID admin;Map<String,String> codes=new HashMap<>();
 @BeforeEach void resetState(){
  db.update("DELETE FROM platform_verification_settings");reset(sms,email,actor);codes.clear();
  admin=account("PLATFORM_ADMIN");when(actor.requireAuthenticatedUserId()).thenReturn(admin);when(actor.isPlatformAdmin()).thenReturn(true);
  when(sms.isAvailable()).thenReturn(true);when(sms.establishesMobileOwnership()).thenReturn(true);when(email.isAvailable()).thenReturn(true);
  doAnswer(call->{codes.put(call.getArgument(0),call.getArgument(1));return null;}).when(sms).sendCode(anyString(),anyString());
 }
 UUID account(String role){UUID id=UUID.randomUUID();db.update("INSERT INTO users(id,email,password_hash,role) VALUES(?,?,'hash',?)",id,id+"@example.test",role);return id;}
 void flags(boolean otp,boolean mail){db.update("INSERT INTO platform_verification_settings(id,mobile_otp_enabled,email_verification_enabled,changed_by) VALUES(1,?,?,?) ON CONFLICT(id) DO UPDATE SET mobile_otp_enabled=EXCLUDED.mobile_otp_enabled,email_verification_enabled=EXCLUDED.email_verification_enabled",otp,mail,admin);}
 UUID[] groupFixture(){UUID host=account("CUSTOMER"),event=UUID.randomUUID(),creator=UUID.randomUUID(),group=UUID.randomUUID(),member=UUID.randomUUID();db.update("INSERT INTO events(id,title,occasion_type,owner_id,host_display_name) VALUES(?,'Party','BIRTHDAY',?,'Host')",event,host);db.update("INSERT INTO gift_group_accounts(id,event_id,username,password_hash) VALUES(?,?,'creator','hash')",creator,event);db.update("INSERT INTO gift_groups(id,event_id,admin_id,name,creator_name,creator_mobile) VALUES(?,?,?,'Friends','Creator','+919876543210')",group,event,creator);db.update("INSERT INTO gift_group_members(id,group_id,display_name,username,password_hash,mobile,status) VALUES(?,?,'Member','member','hash','+919876543211','APPROVED')",member,group);return new UUID[]{host,event,creator,group,member};}
 @Test void defaultsHideAndDisableAllVerificationRoutes()throws Exception{
  mvc.perform(get("/api/public/verification-settings")).andExpect(status().isOk()).andExpect(jsonPath("$.mobileOtpEnabled").value(false)).andExpect(jsonPath("$.emailVerificationEnabled").value(false));
  mvc.perform(post("/api/auth/contact-mobile/start").with(user("host")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"mobile\":\"+919876543210\"}")).andExpect(status().isConflict());
  mvc.perform(post("/api/auth/signup-email/start").with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"accountKind\":\"HOST\",\"email\":\"host@example.test\"}")).andExpect(status().isConflict());
  mvc.perform(post("/api/v1/phone/otp/request").with(user("host")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"phoneNumber\":\"+919876543210\"}")).andExpect(status().isConflict());
  verify(sms,never()).sendCode(anyString(),anyString());verify(email,never()).send(anyString(),anyString());
 }
 @Test void onlyPlatformAdminCanChangePolicyAndCsrfIsRequired()throws Exception{
  String input="{\"mobileOtpEnabled\":true,\"emailVerificationEnabled\":true}";
  mvc.perform(put("/api/admin/verification-settings").with(user("host").roles("CUSTOMER")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(input)).andExpect(status().isForbidden());
  mvc.perform(put("/api/admin/verification-settings").with(user("admin").roles("PLATFORM_ADMIN")).contentType(MediaType.APPLICATION_JSON).content(input)).andExpect(status().isForbidden());
  mvc.perform(put("/api/admin/verification-settings").with(user("admin").roles("PLATFORM_ADMIN")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(input)).andExpect(status().isOk());
  assertTrue(settings.mobileEnabled());assertTrue(settings.emailEnabled());
  assertEquals(1,db.queryForObject("SELECT count(*) FROM platform_admin_action_audit WHERE actor_id=? AND action LIKE 'VERIFICATION_%'",Integer.class,admin));
 }
 @Test void realDeliveryAndAccountBoundProofWorkForAllFourAccountKinds(){
  flags(true,false);UUID[] g=groupFixture();
  var identities=List.of(Map.entry("PLATFORM_ADMIN",admin),Map.entry("HOST",g[0]),Map.entry("GROUP_ADMIN",g[2]),Map.entry("GROUP_MEMBER",g[4]));
  for(int i=0;i<identities.size();i++){
   var identity=identities.get(i);String phone="+91987654000"+i;
   var challenge=mobile.start(identity.getKey(),identity.getValue(),phone);
   assertFalse(mobile.status(identity.getKey(),identity.getValue()).verified());
   assertThrows(ResponseStatusException.class,()->mobile.complete(identity.getKey(),UUID.randomUUID(),challenge.challengeId(),codes.get(phone)));
   String wrong="000000".equals(codes.get(phone))?"111111":"000000";
   assertThrows(ResponseStatusException.class,()->mobile.complete(identity.getKey(),identity.getValue(),challenge.challengeId(),wrong));
   assertEquals(1,db.queryForObject("SELECT attempt_count FROM account_contact_challenges WHERE id=?",Integer.class,challenge.challengeId()));
   mobile.complete(identity.getKey(),identity.getValue(),challenge.challengeId(),codes.get(phone));
   assertTrue(mobile.status(identity.getKey(),identity.getValue()).verified());
   assertThrows(ResponseStatusException.class,()->mobile.complete(identity.getKey(),identity.getValue(),challenge.challengeId(),codes.get(phone)));
  }
 }
 @Test void groupEndpointsRequireBoundCreatorPageAndApprovedMemberSession()throws Exception{
  flags(true,false);UUID[] g=groupFixture();
  db.update("UPDATE events SET status='PUBLISHED' WHERE id=?",g[1]);
  db.update("UPDATE platform_module_controls SET enabled=TRUE WHERE module_code='GROUP_GIFTING'");
  db.update("INSERT INTO platform_host_module_grants(host_id,module_code,enabled) VALUES(?,'GROUP_GIFTING',TRUE)",g[0]);
  UUID token=db.queryForObject("SELECT public_token FROM events WHERE id=?",UUID.class,g[1]);String base="/api/public/groups/"+token+"/contact-mobile";
  var creator=new org.springframework.mock.web.MockHttpSession();String page=UUID.randomUUID().toString();
  creator.setAttribute("gift.group.admin."+g[1],g[2]);creator.setAttribute("gift.group.admin.epoch."+g[1],0L);creator.setAttribute("gift.group.page."+g[1],page);creator.setAttribute("gift.group.activity."+g[1],System.currentTimeMillis());
  mvc.perform(post(base+"/start").session(creator).with(csrf()).header("X-Group-Page-Id",UUID.randomUUID().toString()).contentType(MediaType.APPLICATION_JSON).content("{\"mobile\":\"+919876543000\"}")).andExpect(status().isUnauthorized());
  var response=mvc.perform(post(base+"/start").session(creator).with(csrf()).header("X-Group-Page-Id",page).contentType(MediaType.APPLICATION_JSON).content("{\"mobile\":\"+919876543000\"}")).andExpect(status().isOk()).andReturn();
  String challenge=new tools.jackson.databind.ObjectMapper().readTree(response.getResponse().getContentAsString()).path("challengeId").asText();
  mvc.perform(post(base+"/complete").session(creator).with(csrf()).header("X-Group-Page-Id",page).contentType(MediaType.APPLICATION_JSON).content("{\"challengeId\":\""+challenge+"\",\"code\":\""+codes.get("+919876543000")+"\"}")).andExpect(status().isOk());
  assertTrue(mobile.status("GROUP_ADMIN",g[2]).verified());
  var member=new org.springframework.mock.web.MockHttpSession();member.setAttribute("gift.group.member."+g[3],g[4]);member.setAttribute("gift.group.member.epoch."+g[3],0L);
  mvc.perform(get(base+"/status").session(member).param("groupId",g[3].toString())).andExpect(status().isOk()).andExpect(jsonPath("$.verified").value(false));
  mvc.perform(get(base+"/status").param("groupId",g[3].toString())).andExpect(status().isUnauthorized());
 }
 @Test void providerFailureCountsAgainstRateLimitAndCannotVerify(){
  flags(true,false);UUID host=account("CUSTOMER");doThrow(new IllegalStateException("provider unavailable")).when(sms).sendCode(anyString(),anyString());
  assertThrows(ResponseStatusException.class,()->mobile.start("HOST",host,"+919876549900"));
  assertEquals(1,db.queryForObject("SELECT count(*) FROM account_contact_challenges WHERE account_id=? AND consumed_at IS NOT NULL",Integer.class,host));
  assertFalse(mobile.status("HOST",host).verified());
  var limited=assertThrows(ResponseStatusException.class,()->mobile.start("HOST",host,"+919876549900"));assertEquals(429,limited.getStatusCode().value());
 }
 @Test void expiredChallengesAndFiveWrongAttemptsNeverVerify(){
  flags(true,false);UUID host=account("CUSTOMER");var first=mobile.start("HOST",host,"+919876549901");String actual=codes.get("+919876549901");String wrong="000000".equals(actual)?"111111":"000000";
  for(int i=0;i<5;i++)assertThrows(ResponseStatusException.class,()->mobile.complete("HOST",host,first.challengeId(),wrong));
  assertThrows(ResponseStatusException.class,()->mobile.complete("HOST",host,first.challengeId(),actual));assertFalse(mobile.status("HOST",host).verified());
  UUID other=account("CUSTOMER");var second=mobile.start("HOST",other,"+919876549902");db.update("UPDATE account_contact_challenges SET expires_at=now()-interval '1 second' WHERE id=?",second.challengeId());
  assertThrows(ResponseStatusException.class,()->mobile.complete("HOST",other,second.challengeId(),codes.get("+919876549902")));assertFalse(mobile.status("HOST",other).verified());
 }
 @Test void bypassRemovesVerificationBlockersWithoutMarkingContactsVerified(){
  UUID[] g=groupFixture();UUID bank=UUID.randomUUID();
  db.update("INSERT INTO gift_group_payment_destinations(id,group_id,kind,label,upi_ciphertext,upi_fingerprint) VALUES(?,?,'UPI','Account','cipher',?)",UUID.randomUUID(),g[3],UUID.randomUUID().toString());
  flags(true,true);assertFalse(groups.invitation(g[3]).enabled());
  flags(false,false);assertTrue(groups.invitation(g[3]).enabled());assertNull(db.queryForObject("SELECT email_verified_at FROM gift_group_accounts WHERE id=?",java.sql.Timestamp.class,g[2]));assertNull(db.queryForObject("SELECT mobile_verified_at FROM gift_group_accounts WHERE id=?",java.sql.Timestamp.class,g[2]));
  Event event=new Event("Event",OccasionType.BIRTHDAY);event.assignOwner(g[0]);
  assertFalse(events.reasons(event).stream().anyMatch(reason->reason.contains("Verify")));assertTrue(events.reasons(event).stream().anyMatch(reason->reason.contains("account")));
 }
 @Test void bypassDuringOutageNeedsNoDeliveryAndCannotResetCredentials()throws Exception{
  flags(true,true);when(sms.isAvailable()).thenReturn(false);when(email.isAvailable()).thenReturn(false);
  mvc.perform(put("/api/admin/verification-settings").with(user("admin").roles("PLATFORM_ADMIN")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"mobileOtpEnabled\":false,\"emailVerificationEnabled\":false}")).andExpect(status().isOk());
  mvc.perform(post("/api/auth/recovery/complete").with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isConflict());
  verify(sms,never()).sendCode(anyString(),anyString());verify(email,never()).send(anyString(),anyString());
 }
}
