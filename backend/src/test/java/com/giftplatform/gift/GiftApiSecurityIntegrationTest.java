package com.giftplatform.gift;

import com.giftplatform.gift.service.GiftManagementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import java.util.UUID;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties="gift.otp.secret-base64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
@AutoConfigureMockMvc
@Testcontainers
class GiftApiSecurityIntegrationTest {
 @Container static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");
 @DynamicPropertySource static void db(DynamicPropertyRegistry r) {
  r.add("spring.datasource.url", postgres::getJdbcUrl);
  r.add("spring.datasource.username", postgres::getUsername);
  r.add("spring.datasource.password", postgres::getPassword);
 }
 @Autowired MockMvc mvc;
 @MockitoBean GiftManagementService service;
 @MockitoBean com.giftplatform.gift.photo.GiftPhotoService photos;
 static final UUID EVENT = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
 static final UUID GIFT = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
 static final String BASE = "/api/v1/events/"+EVENT+"/gifts";
 static final String VALID = "{\"name\":\"Mixer\",\"targetAmount\":10000.00}";
 @Test void anonymousPhotoUploadIsUnauthorized() throws Exception {
  mvc.perform(multipart(BASE+"/photos").file("photo",new byte[]{1,2,3}).with(csrf())).andExpect(status().isUnauthorized());verifyNoInteractions(photos);
 }
 @Test void photoUploadRequiresCsrf() throws Exception {
  mvc.perform(multipart(BASE+"/photos").file("photo",new byte[]{1,2,3}).with(user("host"))).andExpect(status().isForbidden());verifyNoInteractions(photos);
 }
 @Test void publicPhotoRouteIsReachableWithoutHostLogin() throws Exception {
  when(photos.publicPhoto(EVENT,GIFT)).thenReturn(new byte[]{(byte)255,(byte)216,(byte)255});
  mvc.perform(get("/api/public/events/"+EVENT+"/gifts/"+GIFT+"/photo")).andExpect(status().isOk()).andExpect(content().contentType("image/jpeg"));
  verify(photos).publicPhoto(EVENT,GIFT);
 }
 @Test void privatePhotoRouteRequiresLogin() throws Exception {
  mvc.perform(get(BASE+"/photos/"+GIFT)).andExpect(status().isUnauthorized());verifyNoInteractions(photos);
 }
 @Test void anonymousListUnauthorized() throws Exception {mvc.perform(get(BASE)).andExpect(status().isUnauthorized()); verifyNoInteractions(service);}
 @Test void anonymousGetUnauthorized() throws Exception {mvc.perform(get(BASE+"/"+GIFT)).andExpect(status().isUnauthorized()); verifyNoInteractions(service);}
 @Test void anonymousCreateWithCsrfUnauthorized() throws Exception {mvc.perform(post(BASE).with(csrf()).contentType("application/json").content(VALID)).andExpect(status().isUnauthorized()); verifyNoInteractions(service);}
 @Test void createWithoutCsrfForbidden() throws Exception {mvc.perform(post(BASE).with(user("host")).contentType("application/json").content(VALID)).andExpect(status().isForbidden()); verifyNoInteractions(service);}
 @Test void updateWithoutCsrfForbidden() throws Exception {mvc.perform(put(BASE+"/"+GIFT).with(user("host")).contentType("application/json").content(VALID)).andExpect(status().isForbidden()); verifyNoInteractions(service);}
 @Test void cancelWithoutCsrfForbidden() throws Exception {mvc.perform(post(BASE+"/"+GIFT+"/cancel").with(user("host"))).andExpect(status().isForbidden()); verifyNoInteractions(service);}
 @Test void authenticatedListAllowed() throws Exception {mvc.perform(get(BASE).with(user("host"))).andExpect(status().isOk()); verify(service).list(EVENT);}
 @Test void createWithCsrfAllowed() throws Exception {mvc.perform(post(BASE).with(user("host")).with(csrf()).contentType("application/json").content(VALID)).andExpect(status().isCreated()); verify(service).create(eq(EVENT),any());}
 @Test void updateWithCsrfAllowed() throws Exception {mvc.perform(put(BASE+"/"+GIFT).with(user("host")).with(csrf()).contentType("application/json").content(VALID)).andExpect(status().isOk()); verify(service).update(eq(EVENT),eq(GIFT),any());}
 @Test void cancelWithCsrfAllowed() throws Exception {mvc.perform(post(BASE+"/"+GIFT+"/cancel").with(user("host")).with(csrf())).andExpect(status().isOk()); verify(service).cancel(EVENT,GIFT);}
 @Test void blankNameRejected() throws Exception {mvc.perform(post(BASE).with(user("host")).with(csrf()).contentType("application/json").content("{\"name\":\" \",\"targetAmount\":100}")).andExpect(status().isBadRequest()); verifyNoInteractions(service);}
 @Test void missingTargetRejected() throws Exception {mvc.perform(post(BASE).with(user("host")).with(csrf()).contentType("application/json").content("{\"name\":\"Mixer\"}")).andExpect(status().isBadRequest()); verifyNoInteractions(service);}
 @Test void malformedJsonRejected() throws Exception {mvc.perform(post(BASE).with(user("host")).with(csrf()).contentType("application/json").content("{")).andExpect(status().isBadRequest()); verifyNoInteractions(service);}
 @Test void unsupportedDeleteRejected() throws Exception {mvc.perform(delete(BASE+"/"+GIFT).with(user("host")).with(csrf())).andExpect(status().isMethodNotAllowed());}
 @Test void hostCannotAccessAdminAudit() throws Exception {mvc.perform(get("/api/admin/audit").with(user("host").roles("CUSTOMER"))).andExpect(status().isForbidden());}
}
