package com.giftplatform.asset;

import com.giftplatform.security.ActorContext;
import com.giftplatform.monetization.CommercialAccessService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.*;
import org.testcontainers.postgresql.PostgreSQLContainer;
import java.util.UUID;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties={"gift.otp.secret-base64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=","java.awt.headless=true"})
@AutoConfigureMockMvc
@Testcontainers
class HostInvitationUploadsIntegrationTest {
 @Container static PostgreSQLContainer postgres=new PostgreSQLContainer("postgres:17");
 @DynamicPropertySource static void database(DynamicPropertyRegistry r){r.add("spring.datasource.url",postgres::getJdbcUrl);r.add("spring.datasource.username",postgres::getUsername);r.add("spring.datasource.password",postgres::getPassword);}
 @Autowired MockMvc mvc;@Autowired JdbcTemplate db;@Autowired HostInvitationUploadService service;
 @MockitoBean ActorContext actor;@MockitoBean CommercialAccessService commerce;
 UUID host,other,admin;
 @BeforeEach void setup(){host=createUser("CUSTOMER");other=createUser("CUSTOMER");admin=createUser("PLATFORM_ADMIN");when(actor.requireAuthenticatedUserId()).thenReturn(host);when(actor.isPlatformAdmin()).thenReturn(false);}
 UUID createUser(String role){UUID id=UUID.randomUUID();db.update("INSERT INTO users(id,email,password_hash,role) VALUES(?,?,?,?)",id,id+"@example.test","unused",role);return id;}
 void grant(UUID id,boolean paid){db.update("INSERT INTO host_invitation_upload_controls(host_id,enabled,requires_payment,changed_by) VALUES(?,true,?,?)",id,paid,admin);}
 MockMultipartFile artwork()throws Exception{return new MockMultipartFile("file","floral.png","image/png",PremiumInvitationArtwork.render(0));}
 @Test void anonymousAndMissingCsrfRequestsAreRejected()throws Exception{
  mvc.perform(get("/api/v1/invitation-uploads/access")).andExpect(status().isUnauthorized());
  mvc.perform(multipart("/api/v1/invitation-uploads").file(artwork()).param("category","DECORATION").param("title","My flower").with(user("host").roles("CUSTOMER"))).andExpect(status().isForbidden());
 }
 @Test void adminGrantAndPaymentAreBothRequired()throws Exception{
  mvc.perform(multipart("/api/v1/invitation-uploads").file(artwork()).param("category","BACKGROUND").param("title","My art").with(user("host").roles("CUSTOMER")).with(csrf())).andExpect(status().isForbidden());
  grant(host,true);
  mvc.perform(multipart("/api/v1/invitation-uploads").file(artwork()).param("category","BACKGROUND").param("title","My art").with(user("host").roles("CUSTOMER")).with(csrf())).andExpect(status().isPaymentRequired());
  when(commerce.premium(host)).thenReturn(true);
  mvc.perform(multipart("/api/v1/invitation-uploads").file(artwork()).param("category","BACKGROUND").param("title","My art").with(user("host").roles("CUSTOMER")).with(csrf())).andExpect(status().isOk()).andExpect(jsonPath("category").value("BACKGROUND"));
 }
 @Test void privateImagesCannotBeFetchedByAnotherHostAndRevocationTakesEffect()throws Exception{
  grant(host,false);grant(other,false);var saved=service.upload("DECORATION","Rose",artwork().getBytes());assertTrue(service.image(saved.id()).length>100);
  when(actor.requireAuthenticatedUserId()).thenReturn(other);
  mvc.perform(get("/api/v1/invitation-uploads/"+saved.id()+"/image").with(user("other").roles("CUSTOMER"))).andExpect(status().isNotFound());
  when(actor.requireAuthenticatedUserId()).thenReturn(host);db.update("UPDATE host_invitation_upload_controls SET enabled=false WHERE host_id=?",host);
  mvc.perform(get("/api/v1/invitation-uploads/"+saved.id()+"/image").with(user("host").roles("CUSTOMER"))).andExpect(status().isForbidden());
 }
 @Test void onlyAdminCanGrantComplimentaryAccessAndChangeIsAudited()throws Exception{
  String controls="{\"enabled\":true,\"requiresPayment\":false}";
  mvc.perform(put("/api/admin/console/hosts/"+host+"/invitation-uploads").contentType("application/json").content(controls).with(user("host").roles("CUSTOMER")).with(csrf())).andExpect(status().isForbidden());
  when(actor.isPlatformAdmin()).thenReturn(true);when(actor.requireAuthenticatedUserId()).thenReturn(admin);
  mvc.perform(put("/api/admin/console/hosts/"+host+"/invitation-uploads").contentType("application/json").content(controls).with(user("admin").roles("PLATFORM_ADMIN")).with(csrf())).andExpect(status().isOk()).andExpect(jsonPath("accessible").value(true));
  assertTrue(service.adminAccess(host).accessible());
  assertEquals(1,db.queryForObject("SELECT count(*) FROM audit_logs WHERE entity_type='HOST_INVITATION_UPLOAD_ACCESS' AND entity_id=?",Integer.class,host));
 }
}
