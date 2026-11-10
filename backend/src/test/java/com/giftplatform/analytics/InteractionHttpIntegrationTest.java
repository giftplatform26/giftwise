package com.giftplatform.analytics;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.*;
import org.testcontainers.junit.jupiter.*;
import org.testcontainers.postgresql.PostgreSQLContainer;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(properties={"gift.otp.secret-base64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=","java.awt.headless=true"})
@AutoConfigureMockMvc
@Testcontainers
class InteractionHttpIntegrationTest {
 @Container static PostgreSQLContainer postgres=new PostgreSQLContainer("postgres:17");
 @DynamicPropertySource static void database(DynamicPropertyRegistry r){r.add("spring.datasource.url",postgres::getJdbcUrl);r.add("spring.datasource.username",postgres::getUsername);r.add("spring.datasource.password",postgres::getPassword);}
 @Autowired MockMvc mvc;
 @MockitoBean InteractionService service;
 String payload="""
  {"events":[{"id":"11111111-1111-1111-1111-111111111111","kind":"CLICK","action":"QR_DOWNLOAD","page":"/events/:id/qr","outcome":"CLICKED"}]}
  """;
 @Test void anonymousIntakeRequiresCsrf() throws Exception {mvc.perform(post("/api/public/interactions").contentType("application/json").content(payload)).andExpect(status().isForbidden());verifyNoInteractions(service);}
 @Test void anonymousVisitorCanReportClickWithCsrf() throws Exception {mvc.perform(post("/api/public/interactions").with(csrf()).header("X-Gift-Analytics-Consent","v2").contentType("application/json").content(payload)).andExpect(status().isNoContent());verify(service).browser(any(),any());}
 @Test void clicksAreNotRecordedWithoutAnalyticsConsent() throws Exception {mvc.perform(post("/api/public/interactions").with(csrf()).contentType("application/json").content(payload)).andExpect(status().isNoContent());verifyNoInteractions(service);}
 @Test void nullItemsAndServerEvidenceCannotBeSubmitted() throws Exception {
  mvc.perform(post("/api/public/interactions").with(csrf()).contentType("application/json").content("{\"events\":[null]}")).andExpect(status().isBadRequest());
  mvc.perform(post("/api/public/interactions").with(csrf()).contentType("application/json").content(payload.replace("CLICK\"","REQUEST\""))).andExpect(status().isBadRequest());verifyNoInteractions(service);
 }
 @Test void adminReportsAreUnavailableToAnonymousAndHosts() throws Exception {
  mvc.perform(get("/api/admin/console/interactions")).andExpect(status().isUnauthorized());
  mvc.perform(get("/api/admin/console/interactions").with(user("host").roles("CUSTOMER"))).andExpect(status().isForbidden());
 }
}
