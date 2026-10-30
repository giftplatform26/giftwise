package com.giftplatform.group;

import com.giftplatform.event.entity.Event;
import com.giftplatform.event.entity.EventStatus;
import com.giftplatform.event.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;
import java.util.Optional;
import java.util.UUID;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class GroupContributionHttpSecurityIntegrationTest {
  static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17")
    .withDatabaseName("giftplatform_group_http_test")
    .withUsername("testuser").withPassword("testpassword");
  static { postgres.start(); }
  @DynamicPropertySource static void database(DynamicPropertyRegistry r) {
    r.add("spring.datasource.url", postgres::getJdbcUrl);
    r.add("spring.datasource.username", postgres::getUsername);
    r.add("spring.datasource.password", postgres::getPassword);
  }
  @MockitoBean com.giftplatform.admin.EffectiveModulePermissionService modulePermissions;
  @Autowired MockMvc mvc;
  @MockitoBean EventRepository events;
  UUID token;
  UUID eventId;
  @BeforeEach void publishedEvent() {
    token=UUID.randomUUID(); eventId=UUID.randomUUID();
    Event event=mock(Event.class);
    when(event.getId()).thenReturn(eventId);
    when(event.getStatus()).thenReturn(EventStatus.PUBLISHED);
    when(events.findByPublicToken(token)).thenReturn(Optional.of(event));
  }
  String url(String suffix) { return "/api/public/groups/" + token + suffix; }
  @Test void anonymousDashboardIsUnauthorizedOverHttp() throws Exception {
    mvc.perform(get(url("/dashboard"))).andExpect(status().isUnauthorized());
  }
  @Test void registerWithoutCsrfIsForbidden() throws Exception {
    mvc.perform(post(url("/register")).contentType(MediaType.APPLICATION_JSON)
      .content("{\"username\":\"alice123\",\"password\":\"strongpassword\"}"))
      .andExpect(status().isForbidden());
  }
  @Test void loginWithoutCsrfIsForbidden() throws Exception {
    mvc.perform(post(url("/login")).contentType(MediaType.APPLICATION_JSON)
      .content("{\"username\":\"alice123\",\"password\":\"strongpassword\"}"))
      .andExpect(status().isForbidden());
  }
  @Test void setupWithoutCsrfIsForbiddenEvenWithSession() throws Exception {
    MockHttpSession session = new MockHttpSession();
    session.setAttribute("gift.group.admin."+eventId, UUID.randomUUID());
    mvc.perform(post(url("/setup")).session(session).contentType(MediaType.APPLICATION_JSON)
      .content("{\"name\":\"Friends\",\"creatorName\":\"Alice\",\"creatorMobile\":\"9876543210\"}"))
      .andExpect(status().isForbidden());
  }
  @Test void forgedSessionCannotReadDashboard() throws Exception {
    MockHttpSession session = new MockHttpSession();
    session.setAttribute("gift.group.admin."+eventId, UUID.randomUUID());
    mvc.perform(get(url("/dashboard")).session(session)).andExpect(status().isUnauthorized());
  }
  @Test void unrelatedEventSessionCannotReadDashboard() throws Exception {
    MockHttpSession session = new MockHttpSession();
    session.setAttribute("gift.group.admin."+UUID.randomUUID(), UUID.randomUUID());
    mvc.perform(get(url("/dashboard")).session(session)).andExpect(status().isUnauthorized());
  }
}
