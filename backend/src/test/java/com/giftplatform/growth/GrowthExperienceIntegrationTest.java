package com.giftplatform.growth;

import com.giftplatform.admin.GrowthSettingsService;
import com.giftplatform.gift.photo.GiftPhotoService;
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
import java.util.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest(properties={"gift.otp.hmac-key-base64=${gift.otp.secret-base64}","java.awt.headless=true"})
@AutoConfigureMockMvc @Testcontainers
class GrowthExperienceIntegrationTest {
 @Container static PostgreSQLContainer postgres=new PostgreSQLContainer("postgres:17-alpine");
 @DynamicPropertySource static void database(DynamicPropertyRegistry r){r.add("spring.datasource.url",postgres::getJdbcUrl);r.add("spring.datasource.username",postgres::getUsername);r.add("spring.datasource.password",postgres::getPassword);}
 @Autowired MockMvc mvc;@Autowired JdbcTemplate db;@Autowired GrowthSettingsService growth;
 UUID host,other,admin,event,token;
 @BeforeEach void setup(){
  host=user("CUSTOMER");other=user("CUSTOMER");admin=user("PLATFORM_ADMIN");event=UUID.randomUUID();token=UUID.randomUUID();
  db.update("UPDATE platform_growth_features SET enabled=TRUE");db.update("UPDATE platform_growth_features SET enabled=FALSE WHERE code IN ('SHARE_PREVIEWS','GUEST_DISCOVERY','PUBLIC_KEEPSAKES')");
  db.update("INSERT INTO events(id,title,occasion_type,status,owner_id,public_token,event_date,host_display_name) VALUES(?,'Private celebration','BIRTHDAY','DRAFT',?,?,'2026-12-01','Test host')",event,host,token);
 }
 UUID user(String role){UUID id=UUID.randomUUID();db.update("INSERT INTO users(id,email,password_hash,role) VALUES(?,?,'hash',?)",id,id+"@example.test",role);return id;}
 MockHttpSession session(UUID id,String role){var principal=new com.giftplatform.security.GiftUserPrincipal(id,id+"@example.test","hash",role,true);var context=org.springframework.security.core.context.SecurityContextHolder.createEmptyContext();context.setAuthentication(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(principal,null,principal.getAuthorities()));var session=new MockHttpSession();session.setAttribute("SPRING_SECURITY_CONTEXT",context);session.setAttribute("gift.credential.epoch",0L);return session;}
 RequestPostProcessor csrf(){return request->{var t=CookieCsrfTokenRepository.withHttpOnlyFalse().generateToken(request);request.setCookies(new Cookie("XSRF-TOKEN",t.getToken()));request.addHeader("X-XSRF-TOKEN",t.getToken());return request;};}
 String settings(long version){var values=new ArrayList<String>();for(String code:GrowthSettingsService.CODES)values.add("\""+code+"\":false");return "{\"version\":"+version+",\"features\":{"+String.join(",",values)+"}}";}
 String own(){return "/api/v1/events/"+event+"/share-preview";}
 String publicPreview(){return "/api/public/events/"+token+"/share-preview";}
 void enable(){db.update("UPDATE platform_growth_features SET enabled=TRUE WHERE code='SHARE_PREVIEWS'");}
 @Test void freshMigrationAndPublicDefaultsAreAvailable()throws Exception{
  // Every migration (including V107 growth controls and later ones) applied successfully.
  assertEquals(0,db.queryForObject("SELECT count(*) FROM flyway_schema_history WHERE NOT success",Integer.class));
  org.junit.jupiter.api.Assertions.assertTrue(db.queryForObject("SELECT max(version::int) FROM flyway_schema_history WHERE version IS NOT NULL",Integer.class)>=107);
  mvc.perform(get("/api/public/growth-settings")).andExpect(status().isOk()).andExpect(header().string("Cache-Control","no-store")).andExpect(jsonPath("$.features.SHARE_PREVIEWS").value(false)).andExpect(jsonPath("$.features.PUBLIC_DEMO").value(true));
 }
 @Test void onlyAdministratorWithCsrfCanSaveAndStaleVersionsAreRejected()throws Exception{
  long revision=growth.current().version();String body=settings(revision);
  mvc.perform(put("/api/admin/console/growth-settings").session(session(host,"CUSTOMER")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isForbidden());
  mvc.perform(put("/api/admin/console/growth-settings").session(session(admin,"PLATFORM_ADMIN")).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isForbidden());
  mvc.perform(put("/api/admin/console/growth-settings").session(session(admin,"PLATFORM_ADMIN")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isOk()).andExpect(jsonPath("$.version").value(revision+1));
  mvc.perform(put("/api/admin/console/growth-settings").session(session(admin,"PLATFORM_ADMIN")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isConflict());
  assertEquals(1,db.queryForObject("SELECT count(*) FROM platform_admin_action_audit WHERE actor_id=? AND action='GROWTH_SETTINGS_UPDATED'",Integer.class,admin));
 }
 @Test void incompleteSwitchPayloadCannotPartiallyResetSettings()throws Exception{
  mvc.perform(put("/api/admin/console/growth-settings").session(session(admin,"PLATFORM_ADMIN")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"version\":0,\"features\":{\"UNKNOWN\":true}}" )).andExpect(status().isBadRequest());
 }
 @Test void previewsRequirePlatformGateHostConsentAndPublishedEvent()throws Exception{
  mvc.perform(get(publicPreview())).andExpect(status().isNotFound());enable();
  mvc.perform(put(own()).session(session(host,"CUSTOMER")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"enabled\":true}")).andExpect(status().isOk());
  mvc.perform(get(publicPreview())).andExpect(status().isNotFound());db.update("UPDATE events SET status='PUBLISHED' WHERE id=?",event);
  mvc.perform(get(publicPreview())).andExpect(status().isOk()).andExpect(jsonPath("$.title").value("Private celebration")).andExpect(jsonPath("$.venue").doesNotExist()).andExpect(jsonPath("$.ownerId").doesNotExist());
  byte[] image=mvc.perform(get(publicPreview()+".png")).andExpect(status().isOk()).andExpect(content().contentType(MediaType.IMAGE_PNG)).andExpect(header().string("Cache-Control","no-store")).andReturn().getResponse().getContentAsByteArray();
  var decoded=javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(image));assertEquals(1200,decoded.getWidth());assertEquals(630,decoded.getHeight());
  db.update("UPDATE platform_growth_features SET enabled=FALSE WHERE code='SHARE_PREVIEWS'");mvc.perform(get(publicPreview()+".png")).andExpect(status().isNotFound());
 }
 @Test void otherHostsCannotReadOrChangePreviewConsent()throws Exception{
  enable();mvc.perform(get(own()).session(session(other,"CUSTOMER"))).andExpect(status().isNotFound());
  mvc.perform(put(own()).session(session(other,"CUSTOMER")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"enabled\":true}")).andExpect(status().isNotFound());
  mvc.perform(put(own()).session(session(host,"CUSTOMER")).contentType(MediaType.APPLICATION_JSON).content("{\"enabled\":true}")).andExpect(status().isForbidden());
 }
 @Test void disablingAnalyticsStopsBrowserAndServerWrites()throws Exception{
  db.update("UPDATE platform_growth_features SET enabled=FALSE WHERE code='INTERACTION_ANALYTICS'");
  UUID interaction=UUID.randomUUID();String body="{\"events\":[{\"id\":\""+interaction+"\",\"kind\":\"OPERATION\",\"action\":\"INVITATION_SHARE\",\"page\":\"/demo\",\"outcome\":\"SUCCESS\"}]}";
  mvc.perform(post("/api/public/interactions").with(csrf()).header("X-Gift-Analytics-Consent","v2").contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isNoContent());
  assertEquals(0,db.queryForObject("SELECT count(*) FROM application_interactions WHERE id=?",Integer.class,interaction));
  db.update("UPDATE events SET status='PUBLISHED' WHERE id=?",event);
  int before=db.queryForObject("SELECT count(*) FROM application_interactions",Integer.class);
  mvc.perform(get("/api/public/events/"+token).header("X-Gift-Analytics-Consent","v2")).andExpect(status().isOk());
  assertEquals(before,db.queryForObject("SELECT count(*) FROM application_interactions",Integer.class));
 }
 @Test void workspaceSummaryIsBoundedAndOwnerScoped()throws Exception{
  UUID privateEvent=UUID.randomUUID();db.update("INSERT INTO events(id,title,occasion_type,status,owner_id,host_display_name,public_token) VALUES(?,'Other host private event','BIRTHDAY','DRAFT',?,'Other host',gen_random_uuid())",privateEvent,other);
  mvc.perform(get("/api/v1/events/workspace-summary").session(session(host,"CUSTOMER"))).andExpect(status().isOk()).andExpect(jsonPath("$",hasSize(1))).andExpect(jsonPath("$[0].id").value(event.toString())).andExpect(jsonPath("$[0].responses").doesNotExist());
  db.update("UPDATE platform_growth_features SET enabled=FALSE WHERE code='SMART_DASHBOARD'");mvc.perform(get("/api/v1/events/workspace-summary").session(session(host,"CUSTOMER"))).andExpect(status().isNotFound());
 }
 @Test void thumbnailsRemainAuthorizedAndLegacyPhotosAreBackfilled()throws Exception{
  var image=new java.awt.image.BufferedImage(1200,800,java.awt.image.BufferedImage.TYPE_INT_RGB);var out=new java.io.ByteArrayOutputStream();javax.imageio.ImageIO.write(image,"jpeg",out);UUID photo=UUID.randomUUID(),gift=UUID.randomUUID();
  db.update("INSERT INTO gift_photos(id,event_id,actor_id,content,width,height) VALUES(?,?,?,?,1200,800)",photo,event,host,out.toByteArray());
  db.update("INSERT INTO gifts(id,event_id,name,target_amount,image_key,visibility) VALUES(?,?,'Test gift',100,?,'PUBLIC')",gift,event,"gift-photo:"+photo);
  String path="/api/public/events/"+token+"/gifts/"+gift+"/photo?thumbnail=true";
  mvc.perform(get(path)).andExpect(status().isNotFound());db.update("UPDATE events SET status='PUBLISHED' WHERE id=?",event);
  byte[] bytes=mvc.perform(get(path)).andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray();
  var decoded=javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(bytes));assertEquals(480,decoded.getWidth());assertEquals(320,decoded.getHeight());assertTrue(bytes.length<out.size());
  assertNotNull(db.queryForObject("SELECT thumbnail FROM gift_photos WHERE id=?",byte[].class,photo));
  db.update("UPDATE gifts SET visibility='DRAFT' WHERE id=?",gift);mvc.perform(get(path)).andExpect(status().isNotFound());
 }
 @Test void publicKeepsakeNeedsExplicitConsentAndExposesOnlyApprovedWishes()throws Exception{
  db.update("UPDATE platform_growth_features SET enabled=TRUE WHERE code='PUBLIC_KEEPSAKES'");
  db.update("UPDATE platform_module_controls SET enabled=TRUE WHERE module_code='GUESTBOOK'");
  db.update("INSERT INTO platform_host_module_grants(host_id,module_code,enabled) VALUES(?,'GUESTBOOK',TRUE)",host);
  String path="/api/v1/events/"+event+"/keepsake-sharing";
  mvc.perform(put(path).session(session(other,"CUSTOMER")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"enabled\":true}")).andExpect(status().isNotFound());
  mvc.perform(put(path).session(session(host,"CUSTOMER")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"enabled\":true}")).andExpect(status().isConflict());
  db.update("UPDATE events SET status='COMPLETED' WHERE id=?",event);
  for(String status:List.of("APPROVED","PENDING","HIDDEN"))db.update("INSERT INTO event_guestbook(id,event_id,guest_name,message,status) VALUES(?,?,'Guest',?,?)",UUID.randomUUID(),event,status+" message",status);
  mvc.perform(get("/api/public/keepsakes/"+token)).andExpect(status().isNotFound());
  mvc.perform(put(path).session(session(host,"CUSTOMER")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"enabled\":true}")).andExpect(status().isOk());
  mvc.perform(get("/api/public/keepsakes/"+token)).andExpect(status().isOk()).andExpect(jsonPath("$.wishes",hasSize(1))).andExpect(jsonPath("$.wishes[0].message").value("APPROVED message")).andExpect(jsonPath("$.wishes[0].photo_path").doesNotExist()).andExpect(jsonPath("$.owner_id").doesNotExist());
  mvc.perform(put(path).session(session(host,"CUSTOMER")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"enabled\":false}")).andExpect(status().isOk());
  mvc.perform(get("/api/public/keepsakes/"+token)).andExpect(status().isNotFound());
 }
 @Test void journeyReportRequiresAdmin()throws Exception{
  mvc.perform(get("/api/admin/console/interactions/funnel").session(session(host,"CUSTOMER"))).andExpect(status().isForbidden());
  mvc.perform(get("/api/admin/console/interactions/funnel").session(session(admin,"PLATFORM_ADMIN"))).andExpect(status().isOk());
 }
}
