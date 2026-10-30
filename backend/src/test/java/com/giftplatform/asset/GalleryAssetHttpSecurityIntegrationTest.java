package com.giftplatform.asset;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import java.util.List;
import java.util.UUID;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties="gift.otp.secret-base64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
@AutoConfigureMockMvc
@Testcontainers
class GalleryAssetHttpSecurityIntegrationTest {
 @Container static PostgreSQLContainer postgres=new PostgreSQLContainer("postgres:17");
 @DynamicPropertySource static void database(DynamicPropertyRegistry r) {
  r.add("spring.datasource.url",postgres::getJdbcUrl);
  r.add("spring.datasource.username",postgres::getUsername);
  r.add("spring.datasource.password",postgres::getPassword);
 }
 @Autowired MockMvc mvc;
 @MockitoBean GalleryAssetService service;
 final UUID id=UUID.fromString("11111111-1111-1111-1111-111111111111");
 @Test void anonymousCannotListAdminAssets() throws Exception {
  mvc.perform(get("/api/admin/invitation-assets")).andExpect(status().isUnauthorized());
  verifyNoInteractions(service);
 }
 @Test void hostCannotListAdminAssets() throws Exception {
  mvc.perform(get("/api/admin/invitation-assets").with(user("host").roles("CUSTOMER"))).andExpect(status().isForbidden());
  verifyNoInteractions(service);
 }
 @Test void hostCannotPublish() throws Exception {
  mvc.perform(post("/api/admin/invitation-assets/"+id+"/publish").with(user("host").roles("CUSTOMER")).with(csrf()))
   .andExpect(status().isForbidden());
  verifyNoInteractions(service);
 }
 @Test void anonymousCannotUpload() throws Exception {
  mvc.perform(multipart("/api/admin/invitation-assets")
   .file("file",new byte[]{1,2,3}).param("category","BACKGROUND").param("occasion","BIRTHDAY").param("title","Test")
   .with(csrf())).andExpect(status().isUnauthorized());
  verifyNoInteractions(service);
 }
 @Test void adminCanList() throws Exception {
  when(service.all()).thenReturn(List.of());
  mvc.perform(get("/api/admin/invitation-assets").with(user("admin").roles("PLATFORM_ADMIN")))
   .andExpect(status().isOk()).andExpect(content().json("[]"));
  verify(service).all();
 }
 @Test void publicCanListPublishedAssetsWithoutLogin() throws Exception {
  when(service.published()).thenReturn(List.of());
  mvc.perform(get("/api/public/invitation-assets")).andExpect(status().isOk()).andExpect(content().json("[]"));
  verify(service).published();
 }
 @Test void adminPostRequiresCsrf() throws Exception {
  mvc.perform(post("/api/admin/invitation-assets/"+id+"/publish").with(user("admin").roles("PLATFORM_ADMIN")))
   .andExpect(status().isForbidden());
  verifyNoInteractions(service);
 }
}
