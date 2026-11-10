package com.giftplatform.celebrationplanning;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.test.context.*;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.testcontainers.junit.jupiter.*;
import org.testcontainers.postgresql.PostgreSQLContainer;
import java.util.UUID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest(properties={"gift.otp.hmac-key-base64=${gift.otp.secret-base64}","gift.payment.direct-transfers.enabled=true"})
@AutoConfigureMockMvc @Testcontainers
class CelebrationPlanningIntegrationTest {
 @Container static PostgreSQLContainer postgres=new PostgreSQLContainer("postgres:17-alpine");
 @DynamicPropertySource static void database(DynamicPropertyRegistry r){r.add("spring.datasource.url",postgres::getJdbcUrl);r.add("spring.datasource.username",postgres::getUsername);r.add("spring.datasource.password",postgres::getPassword);}
 @Autowired MockMvc mvc;@Autowired JdbcTemplate db;
 @Autowired org.springframework.transaction.support.TransactionTemplate transactions;
 UUID host,event,token,admin;
 @BeforeEach void setup(){host=user("CUSTOMER");admin=user("PLATFORM_ADMIN");event=UUID.randomUUID();token=UUID.randomUUID();db.update("INSERT INTO events(id,title,occasion_type,status,owner_id,host_display_name,public_token,event_date,address_line1,address_city,address_state,address_postal_code,address_country,contact_phone) VALUES(?,?,'FAREWELL','DRAFT',?,'Our team',?,'2026-12-01','Office street','City','State','560001','India','PRIVATE_PHONE')",event,"Thank you for everything",host,token);for(String code:com.giftplatform.admin.EventModuleGrantsController.CODES){db.update("UPDATE platform_module_controls SET enabled=TRUE WHERE module_code=?",code);db.update("INSERT INTO platform_host_module_grants(host_id,module_code,enabled) VALUES(?,?,TRUE)",host,code);db.update("INSERT INTO platform_event_module_grants(event_id,module_code,enabled) VALUES(?,?,TRUE)",event,code);}}
 UUID user(String role){UUID id=UUID.randomUUID();db.update("INSERT INTO users(id,email,password_hash,role) VALUES(?,?,'hash',?)",id,id+"@example.test",role);return id;}
 MockHttpSession session(UUID id,String role){var principal=new com.giftplatform.security.GiftUserPrincipal(id,id+"@example.test","hash",role,true);var context=org.springframework.security.core.context.SecurityContextHolder.createEmptyContext();context.setAuthentication(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(principal,null,principal.getAuthorities()));var session=new MockHttpSession();session.setAttribute("SPRING_SECURITY_CONTEXT",context);session.setAttribute("gift.credential.epoch",0L);return session;}
 RequestPostProcessor csrf(){return request->{var t=CookieCsrfTokenRepository.withHttpOnlyFalse().generateToken(request);request.setCookies(new Cookie("XSRF-TOKEN",t.getToken()));request.addHeader("X-XSRF-TOKEN",t.getToken());return request;};}
 String path(){return "/api/v1/events/"+event+"/celebration-settings";}
 String body(boolean gifting,boolean share,long version){return "{\"giftingEnabled\":"+gifting+",\"welcomeMessage\":\"Your presence is our favourite gift\",\"shareVenue\":"+share+",\"startTime\":\"18:30\",\"timezone\":\"Asia/Kolkata\",\"durationMinutes\":120,\"arrivalNotes\":\"Parking at gate 2\",\"version\":"+version+"}";}
 @Test void allThreeGatesAndOwnershipAreRequired() throws Exception {
  mvc.perform(get(path())).andExpect(status().isUnauthorized());
  mvc.perform(get(path()).session(session(user("CUSTOMER"),"CUSTOMER"))).andExpect(status().isNotFound());
  for(String table:new String[]{"platform_module_controls","platform_host_module_grants","platform_event_module_grants"}){
   db.update("UPDATE "+table+" SET enabled=FALSE WHERE module_code='CELEBRATION_PLANNING'");mvc.perform(get(path()).session(session(host,"CUSTOMER"))).andExpect(status().isNotFound());db.update("UPDATE "+table+" SET enabled=TRUE WHERE module_code='CELEBRATION_PLANNING'");
  }
  mvc.perform(get(path()).session(session(host,"CUSTOMER"))).andExpect(status().isOk()).andExpect(jsonPath("$.giftingEnabled").value(true));
 }
 @Test void csrfVersionAndDraftProtectSettings() throws Exception {
  mvc.perform(put(path()).session(session(host,"CUSTOMER")).contentType(MediaType.APPLICATION_JSON).content(body(false,false,0))).andExpect(status().isForbidden());
  mvc.perform(put(path()).session(session(host,"CUSTOMER")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body(false,false,0))).andExpect(status().isOk()).andExpect(jsonPath("$.version").value(1));
  mvc.perform(put(path()).session(session(host,"CUSTOMER")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body(true,false,0))).andExpect(status().isConflict());
  db.update("UPDATE events SET status='PUBLISHED' WHERE id=?",event);
  mvc.perform(put(path()).session(session(host,"CUSTOMER")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body(true,false,1))).andExpect(status().isConflict());
 }
 @Test void invitationOnlyCanPublishWithoutAnAccountAndClosesGiftEntryPoints() throws Exception {
  mvc.perform(put(path()).session(session(host,"CUSTOMER")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body(false,false,0))).andExpect(status().isOk());
  mvc.perform(post("/api/v1/events/"+event+"/publish").session(session(host,"CUSTOMER")).with(csrf())).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PUBLISHED"));
  mvc.perform(get("/api/public/events/"+token)).andExpect(status().isOk()).andExpect(jsonPath("$.gifts",hasSize(0))).andExpect(jsonPath("$.features.GIFTING_ENABLED").value(false)).andExpect(jsonPath("$.features.GROUP_GIFTING").value(false));
  mvc.perform(get("/api/public/payments/context/"+token)).andExpect(status().isNotFound());
  mvc.perform(get("/api/public/groups/"+token+"/dashboard")).andExpect(status().isNotFound());
  mvc.perform(get("/api/public/payments/direct/"+token+"/destinations")).andExpect(status().isNotFound());
 }
 @Test void publicVenueIsOptInAndExcludesPrivateContacts() throws Exception {
  mvc.perform(put(path()).session(session(host,"CUSTOMER")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body(false,false,0))).andExpect(status().isOk());db.update("UPDATE events SET status='PUBLISHED' WHERE id=?",event);
  mvc.perform(get("/api/public/events/"+token+"/celebration-details")).andExpect(status().isOk()).andExpect(jsonPath("$.venue").isEmpty()).andExpect(content().string(not(containsString("PRIVATE_PHONE"))));
  mvc.perform(get("/api/public/events/"+token+"/calendar")).andExpect(status().isOk()).andExpect(content().string(containsString("DTSTART:20261201T130000Z"))).andExpect(content().string(not(containsString("Office street"))));
  db.update("UPDATE event_celebration_settings SET share_venue=TRUE WHERE event_id=?",event);
  mvc.perform(get("/api/public/events/"+token+"/celebration-details")).andExpect(jsonPath("$.venue",containsString("Office street")));
  db.update("UPDATE platform_event_module_grants SET enabled=FALSE WHERE event_id=? AND module_code='EVENT_ESSENTIALS'",event);
  mvc.perform(get("/api/public/events/"+token+"/celebration-details")).andExpect(jsonPath("$.venue").isEmpty());
  mvc.perform(get("/api/public/events/"+token+"/calendar")).andExpect(status().isNotFound());
 }
 @Test void giftingDefaultStillRequiresAccount() throws Exception {
  mvc.perform(post("/api/v1/events/"+event+"/publish").session(session(host,"CUSTOMER")).with(csrf())).andExpect(status().isConflict());
 }
 @Test void repeatCreatesPrivateDraftWithoutGrantsAccountsOrDate() throws Exception {
  var result=mvc.perform(post("/api/v1/events/"+event+"/repeat").session(session(host,"CUSTOMER")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"Next farewell\"}")).andExpect(status().isCreated()).andReturn();
  String text=result.getResponse().getContentAsString();UUID created=UUID.fromString(text.substring(text.indexOf(':')+2,text.indexOf('"',text.indexOf(':')+2)));
  var row=db.queryForMap("SELECT status,event_date,public_token,owner_id,occasion_type FROM events WHERE id=?",created);assertEquals("DRAFT",row.get("status"));assertNull(row.get("event_date"));assertNotEquals(token,row.get("public_token"));assertEquals(host,row.get("owner_id"));assertEquals("FAREWELL",row.get("occasion_type"));
  assertEquals(0,db.queryForObject("SELECT count(*) FROM platform_event_module_grants WHERE event_id=?",Integer.class,created));assertEquals(0,db.queryForObject("SELECT count(*) FROM event_payment_account_links WHERE event_id=?",Integer.class,created));
 }
 @Test void onlyAdminCanChangeEventGrantAndChangeIsAudited() throws Exception {
  String url="/api/admin/console/events/"+event+"/modules/EVENT_REUSE";
  mvc.perform(post(url).session(session(host,"CUSTOMER")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"enabled\":false}")).andExpect(status().isForbidden());
  mvc.perform(post(url).session(session(admin,"PLATFORM_ADMIN")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"enabled\":false}")).andExpect(status().isOk());
  assertEquals(1,db.queryForObject("SELECT count(*) FROM platform_admin_action_audit WHERE target_id=? AND action='EVENT_MODULE_CHANGED'",Integer.class,event));
  mvc.perform(post("/api/v1/events/"+event+"/repeat").session(session(host,"CUSTOMER")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"Another event\"}")).andExpect(status().isNotFound());
 }
 @Test void invalidTimezoneAndLongWelcomeAreRejected() throws Exception {
  mvc.perform(put(path()).session(session(host,"CUSTOMER")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body(false,false,0).replace("Asia/Kolkata","Not/AZone"))).andExpect(status().isBadRequest());
  mvc.perform(put(path()).session(session(host,"CUSTOMER")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body(false,false,0).replace("Your presence is our favourite gift","a".repeat(501)))).andExpect(status().isBadRequest());
 }
 @Test void keepsakeIncludesOnlyApprovedWishesAndRequiresAlbumAccess() throws Exception {
  db.update("UPDATE platform_module_controls SET enabled=TRUE WHERE module_code='MEMORY_ALBUM'");db.update("INSERT INTO platform_host_module_grants(host_id,module_code,enabled) VALUES(?,'MEMORY_ALBUM',TRUE)",host);db.update("INSERT INTO platform_event_album_settings(event_id,enabled) VALUES(?,TRUE)",event);
  for(String state:new String[]{"APPROVED","PENDING","HIDDEN"})db.update("INSERT INTO event_guestbook(id,event_id,guest_name,message,status) VALUES(?,?,'<script>bad</script>',?,?)",UUID.randomUUID(),event,"Wish "+state,state);
  String url="/api/v1/events/"+event+"/album/keepsake";
  mvc.perform(get(url).session(session(host,"CUSTOMER"))).andExpect(status().isOk()).andExpect(header().string("Cache-Control",containsString("no-store"))).andExpect(content().string(containsString("Wish APPROVED"))).andExpect(content().string(not(containsString("Wish PENDING")))).andExpect(content().string(not(containsString("Wish HIDDEN")))).andExpect(content().string(not(containsString("<script>"))));
  mvc.perform(get(url).session(session(user("CUSTOMER"),"CUSTOMER"))).andExpect(status().isNotFound());
  db.update("UPDATE platform_event_album_settings SET enabled=FALSE WHERE event_id=?",event);mvc.perform(get(url).session(session(host,"CUSTOMER"))).andExpect(status().isNotFound());
 }

 @Test void existingCollectionCannotBeHiddenByInvitationOnlyMode() throws Exception {
  UUID creator=UUID.randomUUID();db.update("INSERT INTO gift_group_accounts(id,event_id,username,password_hash) VALUES(?,?,?,'hash')",creator,event,"creator");
  db.update("INSERT INTO gift_groups(id,event_id,admin_id,name,creator_name,creator_mobile) VALUES(?,?,?,'Our circle','Creator','+919876543210')",UUID.randomUUID(),event,creator);
  mvc.perform(put(path()).session(session(host,"CUSTOMER")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body(false,false,0))).andExpect(status().isConflict());
  assertEquals(Boolean.TRUE,db.queryForObject("SELECT gifting_enabled FROM events WHERE id=?",Boolean.class,event));
 }
 @Test void essentialsCannotBypassPlanningAndAdminToggleRequiresExplicitBoolean() throws Exception {
  db.update("UPDATE platform_event_module_grants SET enabled=FALSE WHERE event_id=? AND module_code='CELEBRATION_PLANNING'",event);db.update("UPDATE events SET status='PUBLISHED' WHERE id=?",event);
  mvc.perform(get("/api/public/events/"+token+"/features")).andExpect(status().isOk()).andExpect(jsonPath("$.EVENT_ESSENTIALS").value(false));
  mvc.perform(post("/api/admin/console/events/"+event+"/modules/EVENT_REUSE").session(session(admin,"PLATFORM_ADMIN")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isBadRequest());
 }
 @Test void productionStatusIsAdminOnlyAndDoesNotClaimExternalReadiness() throws Exception {
  String url="/api/admin/console/production-status";mvc.perform(get(url).session(session(host,"CUSTOMER"))).andExpect(status().isForbidden());
  mvc.perform(get(url).session(session(admin,"PLATFORM_ADMIN"))).andExpect(status().isOk()).andExpect(jsonPath("$.livePaymentsReady").value(false)).andExpect(jsonPath("$.backupRestoreVerified").value(false)).andExpect(header().string("Cache-Control",containsString("no-store")));
 }

 UUID creator(){UUID id=UUID.randomUUID();db.update("INSERT INTO gift_group_accounts(id,event_id,username,password_hash) VALUES(?,?,?,'hash')",id,event,"racecreator");return id;}
 void insertGroup(UUID creator){db.update("INSERT INTO gift_groups(id,event_id,admin_id,name,creator_name,creator_mobile) VALUES(?,?,?,'Friends','Creator','+919876543210')",UUID.randomUUID(),event,creator);}
 @Test void databasePolicyRejectsBypassedGroupInsertWhenGiftingIsOff(){
  UUID creator=creator();db.update("UPDATE events SET gifting_enabled=FALSE WHERE id=?",event);
  var error=assertThrows(org.springframework.dao.DataIntegrityViolationException.class,()->insertGroup(creator));
  assertTrue(error.getMostSpecificCause().getMessage().contains("Gifting is not enabled"));
 }
 @Test void modeChangeWaitsForConcurrentGiftThenPreservesItsActivity() throws Exception {
  UUID creator=creator();var inserted=new java.util.concurrent.CountDownLatch(1);var release=new java.util.concurrent.CountDownLatch(1);var pool=java.util.concurrent.Executors.newFixedThreadPool(2);
  var gift=pool.submit(()->transactions.executeWithoutResult(tx->{insertGroup(creator);inserted.countDown();try{if(!release.await(30,java.util.concurrent.TimeUnit.SECONDS))throw new IllegalStateException("Test lock timeout");}catch(InterruptedException ex){Thread.currentThread().interrupt();throw new IllegalStateException(ex);}}));
  try{
   assertTrue(inserted.await(10,java.util.concurrent.TimeUnit.SECONDS));
   var change=pool.submit(()->mvc.perform(put(path()).session(session(host,"CUSTOMER")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body(false,false,0))).andReturn().getResponse().getStatus());
   boolean waiting=false;long deadline=System.nanoTime()+java.util.concurrent.TimeUnit.SECONDS.toNanos(10);
   while(System.nanoTime()<deadline){if(Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM pg_locks WHERE locktype='advisory' AND classid=1195984468::oid AND NOT granted)",Boolean.class))){waiting=true;break;}Thread.sleep(25);}
   assertTrue(waiting,"Settings must wait for the in-flight shared gift lock");assertFalse(change.isDone());release.countDown();gift.get(10,java.util.concurrent.TimeUnit.SECONDS);assertEquals(409,change.get(10,java.util.concurrent.TimeUnit.SECONDS));assertEquals(Boolean.TRUE,db.queryForObject("SELECT gifting_enabled FROM events WHERE id=?",Boolean.class,event));
  }finally{release.countDown();pool.shutdownNow();}
 }

 @Test void minimalHealthIsPublicButMetricsRemainPrivate() throws Exception {
  mvc.perform(get("/actuator/health")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP")).andExpect(jsonPath("$.components").doesNotExist());
  mvc.perform(get("/actuator/metrics")).andExpect(status().isUnauthorized());
 }

}
