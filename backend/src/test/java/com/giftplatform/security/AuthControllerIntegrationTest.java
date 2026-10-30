
package com.giftplatform.security;

import com.giftplatform.user.entity.User;
import com.giftplatform.user.repository.UserRepository;

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
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


import com.giftplatform.user.entity.UserRole;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {


    @Autowired
    JdbcTemplate jdbcTemplate;

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
            DynamicPropertyRegistry registry
    ) {
        registry.add("spring.datasource.url",
                postgres::getJdbcUrl);
        registry.add("spring.datasource.username",
                postgres::getUsername);
        registry.add("spring.datasource.password",
                postgres::getPassword);
    }

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository repository;

    @Autowired
    PasswordEncoder encoder;

    private String uniqueEmail() {
        return "login-" + UUID.randomUUID() + "@example.com";
    }

    private String loginJson(String identifier, String password) {
        return """
        {"identifier":"%s","password":"%s"}
        """.formatted(identifier, password);
    }

    private User createCustomer(String email) {
        return repository.saveAndFlush(
                new User(
                        email,
                        encoder.encode("SecurePassword123!")
                )
        );
    }

    private MockHttpSession login(String email)
            throws Exception {

        var result = mockMvc.perform(
                        post("/api/auth/login")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginJson(
                                        email, "SecurePassword123!"
                                ))
                )
                .andExpect(status().isOk())
                .andReturn();

        return (MockHttpSession)
                result.getRequest().getSession(false);
    }

    @Test
    void shouldLoginWithValidCredentials() throws Exception {
        String email = uniqueEmail();
        createCustomer(email);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(
                                email, "SecurePassword123!"
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.userId").exists());
    }

    @Test
    void shouldRejectIncorrectPassword() throws Exception {
        String email = uniqueEmail();
        createCustomer(email);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(email, "WrongPassword123!")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectNonexistentAccount() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(
                                uniqueEmail(), "SecurePassword123!"
                        )))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectLoginWithoutCsrf() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(
                                uniqueEmail(), "SecurePassword123!"
                        )))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectInvalidCsrf() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf().useInvalidToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(
                                uniqueEmail(), "SecurePassword123!"
                        )))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectMalformedLoginJson() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid }"))
                .andExpect(status().isBadRequest());
    }


    @Test
    void shouldRejectMissingPassword() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"identifier":"customer@example.com"}
                        """))
                .andExpect(status().isBadRequest());
    }


    @Test
    void shouldAllowAuthenticatedMe() throws Exception {
        String email = uniqueEmail();
        createCustomer(email);

        MockHttpSession session = login(email);

        assertThat(session).isNotNull();

        mockMvc.perform(get("/api/auth/me")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void shouldRejectAnonymousMe() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectAnonymousAdminAccess() throws Exception {
        mockMvc.perform(get("/api/admin/audit-logs"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectCustomerAdminAccess() throws Exception {
        String email = uniqueEmail();
        createCustomer(email);

        MockHttpSession session = login(email);

        mockMvc.perform(get("/api/admin/audit-logs")
                        .session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectAnonymousEventCreation() throws Exception {
        mockMvc.perform(post("/api/events")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectCustomerEventCreationUntilEnabled()
            throws Exception {

        String email = uniqueEmail();
        createCustomer(email);

        MockHttpSession session = login(email);

        mockMvc.perform(post("/api/events")
                        .with(csrf())
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldLogoutSuccessfully() throws Exception {
        String email = uniqueEmail();
        createCustomer(email);

        MockHttpSession session = login(email);

        mockMvc.perform(post("/api/auth/logout")
                        .session(session)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        assertThat(session.isInvalid()).isTrue();
    }

    @Test
    void shouldNotExposePasswordInLoginResponse()
            throws Exception {

        String email = uniqueEmail();
        createCustomer(email);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(
                                email, "SecurePassword123!"
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }



    @Test
    void shouldLoginWithUsername() throws Exception {
        String username = uniqueUsername();
        String email = uniqueEmail();

        createCustomerWithUsername(username, email);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(
                                username, "SecurePassword123!"
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.userId").exists());
    }

    @Test
    void shouldLoginWithUppercaseUsername() throws Exception {
        String username = uniqueUsername();
        String email = uniqueEmail();

        createCustomerWithUsername(username, email);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(
                                username.toUpperCase(),
                                "SecurePassword123!"
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void shouldLoginWithUppercaseEmail() throws Exception {
        String email = uniqueEmail();
        createCustomer(email);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(
                                email.toUpperCase(),
                                "SecurePassword123!"
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void shouldRejectBlankIdentifier() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(
                                "   ", "SecurePassword123!"
                        )))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectDisabledAccount() throws Exception {
        String email = uniqueEmail();
        User user = createCustomer(email);

        jdbcTemplate.update(
                "UPDATE users SET enabled = false WHERE id = ?",
                user.getId()
        );

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(
                                email, "SecurePassword123!"
                        )))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRotateExistingSessionOnLogin() throws Exception {
        String email = uniqueEmail();
        createCustomer(email);

        MockHttpSession originalSession =
                new MockHttpSession();

        String originalId = originalSession.getId();

        MvcResult result = mockMvc.perform(
                        post("/api/auth/login")
                                .session(originalSession)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginJson(
                                        email, "SecurePassword123!"
                                )))
                .andExpect(status().isOk())
                .andReturn();

        var authenticatedSession =
                result.getRequest().getSession(false);

        assertThat(authenticatedSession).isNotNull();
        assertThat(authenticatedSession.getId())
                .isNotEqualTo(originalId);
    }

    @Test
    void shouldRejectMeAfterLogout() throws Exception {
        String email = uniqueEmail();
        createCustomer(email);

        MockHttpSession session = login(email);

        mockMvc.perform(get("/api/auth/me")
                        .session(session))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/logout")
                        .session(session)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        assertThat(session.isInvalid()).isTrue();

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectLogoutWithoutCsrf() throws Exception {
        String email = uniqueEmail();
        createCustomer(email);

        MockHttpSession session = login(email);

        mockMvc.perform(post("/api/auth/logout")
                        .session(session))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/auth/me")
                        .session(session))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectCustomerAuditAccessAfterUsernameLogin()
            throws Exception {

        String username = uniqueUsername();
        String email = uniqueEmail();

        createCustomerWithUsername(username, email);

        MvcResult result = mockMvc.perform(
                        post("/api/auth/login")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginJson(
                                        username,
                                        "SecurePassword123!"
                                )))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session = (MockHttpSession)
                result.getRequest().getSession(false);

        assertThat(session).isNotNull();

        mockMvc.perform(get("/api/admin/audit-logs")
                        .session(session))
                .andExpect(status().isForbidden());
    }

    private String uniqueUsername() {
        return "user" + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 12);
    }

    private User createCustomerWithUsername(
            String username,
            String email
    ) {
        return repository.saveAndFlush(
                new User(
                        username,
                        email,
                        encoder.encode("SecurePassword123!")
                )
        );
    }

}
