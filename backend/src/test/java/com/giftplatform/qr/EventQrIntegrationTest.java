package com.giftplatform.qr;

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
import tools.jackson.databind.ObjectMapper;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"gift.otp.secret-base64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=", "gift.guest-base-url=https://gifts.example.test"})
@AutoConfigureMockMvc
@Testcontainers
class EventQrIntegrationTest {
    @Container static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");
    @DynamicPropertySource static void db(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
    }
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder encoder;
    @Autowired JdbcTemplate jdbc;
    @Autowired ObjectMapper mapper;
    private MockHttpSession login() throws Exception {
        String email = "qr-" + UUID.randomUUID() + "@example.test";
        users.saveAndFlush(new User(email, encoder.encode("SecurePassword123!")));
        var result = mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(java.util.Map.of("identifier", email, "password", "SecurePassword123!"))))
                .andExpect(status().isOk()).andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }
    private UUID event(MockHttpSession host) throws Exception {
        var result = mvc.perform(post("/api/v1/events").session(host).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"Birthday\",\"occasionType\":\"BIRTHDAY\",\"hostName\":\"Fixture host\",\"addressLine1\":\"1 Fixture Street\",\"city\":\"Bengaluru\",\"state\":\"Karnataka\",\"postalCode\":\"560001\",\"country\":\"India\",\"contactName\":\"Fixture contact\",\"contactPhone\":\"+919876543210\"}"))
                .andExpect(status().isCreated()).andReturn();
        return UUID.fromString(mapper.readTree(result.getResponse().getContentAsString()).get("id").asText());
    }
    private String base(UUID id) { return "/api/v1/events/" + id + "/qr"; }
    @Test void ownerGetsStableTokenAndScannablePng() throws Exception {
        var host = login(); var id = event(host);
        var a = mvc.perform(get(base(id)).session(host)).andExpect(status().isOk()).andReturn();
        var b = mvc.perform(get(base(id)).session(host)).andExpect(status().isOk()).andReturn();
        var token = mapper.readTree(a.getResponse().getContentAsString()).get("publicToken").asText();
        assertEquals(token, mapper.readTree(b.getResponse().getContentAsString()).get("publicToken").asText());
        assertEquals("https://gifts.example.test/e/" + token,
            mapper.readTree(a.getResponse().getContentAsString()).get("url").asText());
        mvc.perform(get(base(id) + "/png").session(host)).andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.IMAGE_PNG));
    }
    @Test void themeChangeDoesNotRotateToken() throws Exception {
        var host = login(); var id = event(host);
        var a = mvc.perform(get(base(id)).session(host)).andReturn();
        var token = mapper.readTree(a.getResponse().getContentAsString()).get("publicToken").asText();
        mvc.perform(put(base(id) + "/theme").session(host).with(csrf())
            .contentType(MediaType.APPLICATION_JSON).content("{\"theme\":\"ELEGANT_GOLD\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.theme").value("ELEGANT_GOLD"))
            .andExpect(jsonPath("$.publicToken").value(token));
    }
    @Test void foreignHostCannotReadOrChangeQr() throws Exception {
        var owner = login(); var other = login(); var id = event(owner);
        mvc.perform(get(base(id)).session(other)).andExpect(status().isNotFound());
        mvc.perform(get(base(id) + "/png").session(other)).andExpect(status().isNotFound());
        mvc.perform(put(base(id) + "/theme").session(other).with(csrf())
            .contentType(MediaType.APPLICATION_JSON).content("{\"theme\":\"ELEGANT_GOLD\"}"))
            .andExpect(status().isNotFound());
    }
    @Test void anonymousCannotReadQr() throws Exception {
        var host = login(); var id = event(host);
        mvc.perform(get(base(id))).andExpect(status().isUnauthorized());
    }
    @Test void tokensAreUniqueAndDatabaseEnforcesUniqueness() throws Exception {
        var host = login(); var a = event(host); var b = event(host);
        UUID tokenA = jdbc.queryForObject("SELECT public_token FROM events WHERE id=?", UUID.class, a);
        UUID tokenB = jdbc.queryForObject("SELECT public_token FROM events WHERE id=?", UUID.class, b);
        assertNotEquals(tokenA, tokenB);
        assertThrows(org.springframework.dao.DataIntegrityViolationException.class,
            () -> jdbc.update("UPDATE events SET public_token=? WHERE id=?", tokenA, b));
    }
}
