
package com.giftplatform.user.controller;

import tools.jackson.databind.ObjectMapper;
import com.giftplatform.user.entity.User;
import com.giftplatform.user.entity.UserRole;
import com.giftplatform.user.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.util.UUID;
@SpringBootTest
@AutoConfigureMockMvc
class RegistrationControllerIntegrationTest {

    static final PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17")
                    .withDatabaseName("giftplatform_test")
                    .withUsername("testuser")
                    .withPassword("testpassword");

    static {
        postgres.start();
    }

    @DynamicPropertySource
    static void configureDatabase(
            DynamicPropertyRegistry registry) {

        registry.add("spring.datasource.url",
                postgres::getJdbcUrl);
        registry.add("spring.datasource.username",
                postgres::getUsername);
        registry.add("spring.datasource.password",
                postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String uniqueEmail() {
        return "customer-" + UUID.randomUUID() + "@example.com";
    }

    private String requestBody(String email, String password)
            throws Exception {

        return objectMapper.writeValueAsString(
                Map.of(
                        "username", "user_" +
                                UUID.randomUUID().toString()
                                        .replace("-", "").substring(0, 12),
                        "fullName", "Fixture host",
                        "mobile", "+919"+String.format(java.util.Locale.ROOT,"%09d",Long.remainderUnsigned(UUID.randomUUID().getLeastSignificantBits(),1_000_000_000L)),
                        "email", email,
                        "password", password,
                        "termsAccepted", true
                )
        );
    }

    @Test
    void shouldRegisterCustomerSuccessfully() throws Exception {
        String email = uniqueEmail();

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody(
                                email, "SecurePassword123!"
                        )))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void shouldPersistCustomerInPostgreSQL() throws Exception {
        String email = uniqueEmail();

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody(
                                email, "SecurePassword123!"
                        )))
                .andExpect(status().isCreated());

        User user = userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow();

        assertThat(user.getRole())
                .isEqualTo(UserRole.CUSTOMER);
        assertThat(user.isEnabled()).isTrue();
    }

    @Test
    void shouldStoreOnlyHashedPassword() throws Exception {
        String email = uniqueEmail();
        String password = "SecurePassword123!";

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody(email, password)))
                .andExpect(status().isCreated());

        User user = userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow();

        assertThat(user.getPasswordHash())
                .isNotEqualTo(password);

        assertThat(passwordEncoder.matches(
                password, user.getPasswordHash()
        )).isTrue();
    }

    @Test
    void shouldRejectDuplicateEmail() throws Exception {
        String email = uniqueEmail();
        String body = requestBody(email, "SecurePassword123!");

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldRejectCaseInsensitiveDuplicateEmail()
            throws Exception {

        String email = uniqueEmail();

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody(
                                email, "SecurePassword123!"
                        )))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody(
                                email.toUpperCase(),
                                "SecurePassword123!"
                        )))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldRejectInvalidEmail() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody(
                                "invalid-email",
                                "SecurePassword123!"
                        )))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectShortPassword() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody(
                                uniqueEmail(),
                                "Short123"
                        )))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectPasswordExceeding72Bytes()
            throws Exception {

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody(
                                uniqueEmail(),
                                "A".repeat(73)
                        )))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectMissingEmail() throws Exception {
        String body = """
                {
                  "password": "SecurePassword123!"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectMissingPassword() throws Exception {
        String body = """
                {
                  "email": "customer@example.com"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectMalformedJson() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid json }"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectRegistrationWithoutCsrf()
            throws Exception {

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody(
                                uniqueEmail(),
                                "SecurePassword123!"
                        )))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectInvalidCsrfToken()
            throws Exception {

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf().useInvalidToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody(
                                uniqueEmail(),
                                "SecurePassword123!"
                        )))
                .andExpect(status().isForbidden());
    }


    @Test
    void shouldIgnoreAttemptToAssignAdminRole() throws Exception {

        String username = "user" + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 12);

        String email = uniqueEmail();

        String body = objectMapper.writeValueAsString(
                Map.of(
                        "username", username,
                        "fullName", "Fixture host",
                        "mobile", "+919"+String.format(java.util.Locale.ROOT,"%09d",Long.remainderUnsigned(UUID.randomUUID().getLeastSignificantBits(),1_000_000_000L)),
                        "email", email,
                        "password", "SecurePassword123!",
                        "termsAccepted", true,
                        "role", "PLATFORM_ADMIN"
                )
        );

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));

        User user = userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow();

        assertThat(user.getUsername()).isEqualTo(username);
        assertThat(user.getRole()).isEqualTo(UserRole.CUSTOMER);
    }

    @Test
    void shouldDenyAnonymousEventsAccess()
            throws Exception {

        mockMvc.perform(get("/api/events"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldDenyAnonymousAdminAuditAccess()
            throws Exception {

        mockMvc.perform(get("/api/admin/audit-logs"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldDenyUnauthorizedEventCreation()
            throws Exception {

        mockMvc.perform(post("/api/events")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldNotExposePasswordInRegistrationResponse()
            throws Exception {

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody(
                                uniqueEmail(),
                                "SecurePassword123!"
                        )))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }
}
