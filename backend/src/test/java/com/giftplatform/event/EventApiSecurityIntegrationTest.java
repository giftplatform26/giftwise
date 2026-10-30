package com.giftplatform.event;

import com.giftplatform.event.dto.EventResponse;
import com.giftplatform.event.entity.EventStatus;
import com.giftplatform.event.entity.OccasionType;
import com.giftplatform.event.service.EventManagementService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "gift.otp.secret-base64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
@AutoConfigureMockMvc
@Testcontainers
class EventApiSecurityIntegrationTest {
    @Container static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");
    @DynamicPropertySource static void postgresProps(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
    }
    @Autowired MockMvc mvc;
    @MockitoBean EventManagementService service;
    static final String BASE = "/api/v1/events";
    static final String VALID = "{\"hostName\":\"Test Host\",\"title\":\"Birthday\",\"occasionType\":\"BIRTHDAY\"}";
    static final UUID ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    static final UUID OWNER = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    static final EventResponse RESPONSE = new EventResponse(ID, OWNER, "Birthday", null, OccasionType.BIRTHDAY, null, EventStatus.DRAFT, UUID.randomUUID(), "Test Host", false);

    @Test void anonymousListIsUnauthorized() throws Exception { mvc.perform(get(BASE)).andExpect(status().isUnauthorized()); verifyNoInteractions(service); }
    @Test void anonymousCreateIsUnauthorized() throws Exception { mvc.perform(post(BASE).with(csrf()).contentType("application/json").content(VALID)).andExpect(status().isUnauthorized()); verifyNoInteractions(service); }
    @Test void authenticatedListWorks() throws Exception { when(service.list()).thenReturn(List.of(RESPONSE)); mvc.perform(get(BASE).with(user("host"))).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(ID.toString())); }
    @Test void authenticatedGetWorks() throws Exception { when(service.get(ID)).thenReturn(RESPONSE); mvc.perform(get(BASE+"/"+ID).with(user("host"))).andExpect(status().isOk()).andExpect(jsonPath("$.title").value("Birthday")); }
    @Test void createWithoutCsrfForbidden() throws Exception { mvc.perform(post(BASE).with(user("host")).contentType("application/json").content(VALID)).andExpect(status().isForbidden()); verifyNoInteractions(service); }
    @Test void createWithCsrfReturnsCreated() throws Exception { when(service.create(any())).thenReturn(RESPONSE); mvc.perform(post(BASE).with(user("host")).with(csrf()).contentType("application/json").content(VALID)).andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(ID.toString())); }
    @Test void updateWithoutCsrfForbidden() throws Exception { mvc.perform(put(BASE+"/"+ID).with(user("host")).contentType("application/json").content(VALID)).andExpect(status().isForbidden()); verifyNoInteractions(service); }
    @Test void updateWithCsrfWorks() throws Exception { when(service.update(eq(ID),any())).thenReturn(RESPONSE); mvc.perform(put(BASE+"/"+ID).with(user("host")).with(csrf()).contentType("application/json").content(VALID)).andExpect(status().isOk()); }
    @Test void publishWithoutCsrfForbidden() throws Exception { mvc.perform(post(BASE+"/"+ID+"/publish").with(user("host"))).andExpect(status().isForbidden()); }
    @Test void completeWithoutCsrfForbidden() throws Exception { mvc.perform(post(BASE+"/"+ID+"/complete").with(user("host"))).andExpect(status().isForbidden()); }
    @Test void cancelWithoutCsrfForbidden() throws Exception { mvc.perform(post(BASE+"/"+ID+"/cancel").with(user("host"))).andExpect(status().isForbidden()); }
    @Test void blankTitleRejected() throws Exception { mvc.perform(post(BASE).with(user("host")).with(csrf()).contentType("application/json").content("{\"title\":\" \",\"occasionType\":\"BIRTHDAY\"}")).andExpect(status().isBadRequest()); verifyNoInteractions(service); }
    @Test void missingOccasionRejected() throws Exception { mvc.perform(post(BASE).with(user("host")).with(csrf()).contentType("application/json").content("{\"title\":\"Birthday\"}")).andExpect(status().isBadRequest()); verifyNoInteractions(service); }
    @Test void malformedJsonRejected() throws Exception { mvc.perform(post(BASE).with(user("host")).with(csrf()).contentType("application/json").content("{" )).andExpect(status().isBadRequest()); verifyNoInteractions(service); }
    @Test void unsupportedMethodRejected() throws Exception { mvc.perform(delete(BASE+"/"+ID).with(user("host")).with(csrf())).andExpect(status().isMethodNotAllowed()); }
    @Test void customerCannotReadAdminAudit() throws Exception { mvc.perform(get("/api/admin/audit").with(user("host").roles("CUSTOMER"))).andExpect(status().isForbidden()); }
}
