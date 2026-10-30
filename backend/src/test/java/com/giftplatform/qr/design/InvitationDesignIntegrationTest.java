package com.giftplatform.qr.design;

import com.giftplatform.user.entity.User;
import com.giftplatform.user.repository.UserRepository;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties={"gift.otp.secret-base64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=","gift.guest-base-url=https://gifts.example.test"})
@AutoConfigureMockMvc
@Testcontainers
class InvitationDesignIntegrationTest {
 @Container static PostgreSQLContainer postgres=new PostgreSQLContainer("postgres:17");
 @DynamicPropertySource static void db(DynamicPropertyRegistry r){
  r.add("spring.datasource.url",postgres::getJdbcUrl);r.add("spring.datasource.username",postgres::getUsername);r.add("spring.datasource.password",postgres::getPassword);
 }
 @Autowired MockMvc mvc;@Autowired UserRepository users;@Autowired PasswordEncoder encoder;@Autowired ObjectMapper mapper;
 private MockHttpSession login() throws Exception {
  String email="inv-"+UUID.randomUUID()+"@example.test";
  users.saveAndFlush(new User(email,encoder.encode("SecurePassword123!")));
  var result=mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
    .content(mapper.writeValueAsString(Map.of("identifier",email,"password","SecurePassword123!"))))
    .andExpect(status().isOk()).andReturn();
  return (MockHttpSession)result.getRequest().getSession(false);
 }
 private UUID event(MockHttpSession host) throws Exception {
  var result=mvc.perform(post("/api/v1/events").session(host).with(csrf()).contentType(MediaType.APPLICATION_JSON)
   .content("{\"title\":\"Wedding\",\"occasionType\":\"WEDDING\"}"))
   .andExpect(status().isCreated()).andReturn();
  return UUID.fromString(mapper.readTree(result.getResponse().getContentAsString()).get("id").asText());
 }
 private String path(UUID id){return "/api/v1/events/"+id+"/qr/invitations";}
 private UUID create(MockHttpSession host,UUID id) throws Exception {
  var result=mvc.perform(multipart(path(id)).param("name","Family card").param("style","FLORAL")
    .param("greeting","Celebrate with us").session(host).with(csrf()))
    .andExpect(status().isCreated()).andReturn();
  return UUID.fromString(mapper.readTree(result.getResponse().getContentAsString()).get("id").asText());
 }
 @Test void ownerCanCreateListAndDownloadScannablePng() throws Exception {
  var host=login();UUID id=event(host);UUID design=create(host,id);
  mvc.perform(get(path(id)).session(host)).andExpect(status().isOk()).andExpect(jsonPath("$[0].name").value("Family card"));
  mvc.perform(get(path(id)+"/"+design+"/png").session(host)).andExpect(status().isOk()).andExpect(content().contentType(MediaType.IMAGE_PNG));
 }
 @Test void otherHostCannotReadOrCreateDesigns() throws Exception {
  var owner=login();var foreign=login();UUID id=event(owner);UUID design=create(owner,id);
  mvc.perform(get(path(id)).session(foreign)).andExpect(status().isNotFound());
  mvc.perform(get(path(id)+"/"+design+"/png").session(foreign)).andExpect(status().isNotFound());
  mvc.perform(multipart(path(id)).session(foreign).with(csrf()).param("name","Intrusion").param("style","MINIMAL"))
   .andExpect(status().isNotFound());
 }
 @Test void anonymousAndMissingCsrfAreRejected() throws Exception {
  var host=login();UUID id=event(host);
  mvc.perform(get(path(id))).andExpect(status().isUnauthorized());
  mvc.perform(multipart(path(id)).session(host).param("name","Card").param("style","MINIMAL"))
   .andExpect(status().isForbidden());
 }
 @Test void invalidUploadDoesNotCreateDesign() throws Exception {
  var host=login();UUID id=event(host);
  var bad=new MockMultipartFile("image","evil.svg","image/svg+xml","<svg/>".getBytes());
  mvc.perform(multipart(path(id)).file(bad).session(host).with(csrf()).param("name","Bad").param("style","PHOTO_FRAME"))
   .andExpect(status().isBadRequest());
  mvc.perform(get(path(id)).session(host)).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
 }
}
