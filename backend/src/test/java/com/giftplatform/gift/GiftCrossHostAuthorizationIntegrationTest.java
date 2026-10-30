package com.giftplatform.gift;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.giftplatform.user.entity.User;
import com.giftplatform.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "gift.otp.secret-base64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
@AutoConfigureMockMvc
@Testcontainers
class GiftCrossHostAuthorizationIntegrationTest {
    @Container static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");
    @DynamicPropertySource static void db(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
    }
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder passwords;
    @Autowired JdbcTemplate jdbc;
    @Autowired ObjectMapper mapper;

    static final String PASSWORD = "SecurePassword123!";
    static final String GIFT = "{\"name\":\"Mixer\",\"description\":\"Kitchen appliance\",\"targetAmount\":10000.00}";
    static final String EVENT = "{\"title\":\"Wedding\",\"occasionType\":\"WEDDING\"}";

    private MockHttpSession login() throws Exception {
        String email = "gift-host-" + UUID.randomUUID() + "@example.test";
        users.saveAndFlush(new User(email, passwords.encode(PASSWORD)));
        String payload = mapper.writeValueAsString(java.util.Map.of("identifier", email, "password", PASSWORD));
        var result = mvc.perform(post("/api/auth/login").with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isOk()).andReturn();
        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        assertNotNull(session);
        return session;
    }
    private UUID createEvent(MockHttpSession session) throws Exception {
        var result = mvc.perform(post("/api/v1/events").session(session).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(EVENT))
                .andExpect(status().isCreated()).andReturn();
        return UUID.fromString(mapper.readTree(result.getResponse().getContentAsString()).get("id").asText());
    }
    private UUID createGift(MockHttpSession session, UUID eventId) throws Exception {
        var result = mvc.perform(post(base(eventId)).session(session).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(GIFT))
                .andExpect(status().isCreated()).andReturn();
        return UUID.fromString(mapper.readTree(result.getResponse().getContentAsString()).get("id").asText());
    }
    private String base(UUID eventId) { return "/api/v1/events/" + eventId + "/gifts"; }
    private int auditCount(UUID giftId) {
        return jdbc.queryForObject("SELECT count(*) FROM audit_logs WHERE entity_type='GIFT' AND entity_id=?", Integer.class, giftId);
    }

    @Test void ownerCanCreateReadUpdateAndCancelGift() throws Exception {
        var host = login(); var event = createEvent(host); var gift = createGift(host, event);
        mvc.perform(get(base(event)).session(host)).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(gift.toString()));
        mvc.perform(get(base(event) + "/" + gift).session(host)).andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Mixer"));
        String changed = "{\"name\":\"Coffee Machine\",\"targetAmount\":12000.00}";
        mvc.perform(put(base(event) + "/" + gift).session(host).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(changed))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Coffee Machine"));
        mvc.perform(post(base(event) + "/" + gift + "/cancel").session(host).with(csrf()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CANCELLED"));
        assertEquals("CANCELLED", jdbc.queryForObject("SELECT status FROM gifts WHERE id=?", String.class, gift));
        assertEquals(3, auditCount(gift));
    }

    @Test void foreignHostCannotListReadUpdateOrCancelAndCreatesNoAudit() throws Exception {
        var owner = login(); var foreign = login(); var event = createEvent(owner); var gift = createGift(owner, event);
        int before = auditCount(gift);
        mvc.perform(get(base(event)).session(foreign)).andExpect(status().isNotFound());
        mvc.perform(get(base(event) + "/" + gift).session(foreign)).andExpect(status().isNotFound());
        mvc.perform(put(base(event) + "/" + gift).session(foreign).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(GIFT)).andExpect(status().isNotFound());
        mvc.perform(post(base(event) + "/" + gift + "/cancel").session(foreign).with(csrf()))
                .andExpect(status().isNotFound());
        assertEquals(before, auditCount(gift));
        assertEquals("ACTIVE", jdbc.queryForObject("SELECT status FROM gifts WHERE id=?", String.class, gift));
    }

    @Test void changingEventIdInUrlCannotAccessGift() throws Exception {
        var host = login(); var eventA = createEvent(host); var eventB = createEvent(host);
        var gift = createGift(host, eventA);
        mvc.perform(get(base(eventB) + "/" + gift).session(host)).andExpect(status().isNotFound());
        mvc.perform(put(base(eventB) + "/" + gift).session(host).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(GIFT)).andExpect(status().isNotFound());
        assertEquals(1, auditCount(gift));
    }

    @Test void foreignHostCannotCreateGiftUnderAnotherHostsEvent() throws Exception {
        var owner = login(); var foreign = login(); var event = createEvent(owner);
        int before = jdbc.queryForObject("SELECT count(*) FROM gifts WHERE event_id=?", Integer.class, event);
        mvc.perform(post(base(event)).session(foreign).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(GIFT)).andExpect(status().isNotFound());
        assertEquals(before, jdbc.queryForObject("SELECT count(*) FROM gifts WHERE event_id=?", Integer.class, event));
    }

    @Test void customerCannotReadAdministrativeAudit() throws Exception {
        var host = login();
        mvc.perform(get("/api/admin/audit-logs").session(host)).andExpect(status().isForbidden());
    }

    @Test void unauthenticatedGiftListRejected() throws Exception {
        mvc.perform(get(base(UUID.randomUUID()))).andExpect(status().isUnauthorized());
    }

    @Test void authenticatedGiftWriteWithoutCsrfRejected() throws Exception {
        var host = login(); var event = createEvent(host);
        mvc.perform(post(base(event)).session(host).contentType(MediaType.APPLICATION_JSON).content(GIFT))
                .andExpect(status().isForbidden());
        assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM gifts WHERE event_id=?", Integer.class, event));
    }
}
