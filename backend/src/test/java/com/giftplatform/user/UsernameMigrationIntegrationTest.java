
package com.giftplatform.user;

import com.giftplatform.user.entity.User;
import com.giftplatform.user.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class UsernameMigrationIntegrationTest {

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
    JdbcTemplate jdbcTemplate;

    @Autowired
    UserRepository userRepository;

    private String uniqueEmail() {
        return "migration-" + UUID.randomUUID()
                + "@example.com";
    }

    private String uniqueUsername() {
        return "user" + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 12);
    }

    private void insertUser(String username) {
        jdbcTemplate.update("""
            INSERT INTO users (
                id, email, password_hash,
                role, enabled, username
            )
            VALUES (?, ?, ?, ?, ?, ?)
            """,
                UUID.randomUUID(),
                uniqueEmail(),
                "$2a$12$testplaceholder",
                "CUSTOMER",
                true,
                username
        );
    }

    @Test
    void shouldHaveUsernameColumn() {
        Integer count = jdbcTemplate.queryForObject("""
            SELECT COUNT(*)
            FROM information_schema.columns
            WHERE table_name = 'users'
              AND column_name = 'username'
            """, Integer.class);

        assertThat(count).isEqualTo(1);
    }

    @Test
    void shouldAllowValidUsername() {
        String username = uniqueUsername();

        insertUser(username);

        Integer count = jdbcTemplate.queryForObject("""
            SELECT COUNT(*)
            FROM users
            WHERE username = ?
            """, Integer.class, username);

        assertThat(count).isEqualTo(1);
    }

    @Test
    void shouldAllowNullUsernameForLegacyUser() {
        insertUser(null);
    }

    @Test
    void shouldRejectDuplicateUsername() {
        String username = uniqueUsername();

        insertUser(username);

        assertThatThrownBy(() -> insertUser(username))
                .isInstanceOf(
                        DataIntegrityViolationException.class
                );
    }

    @Test
    void shouldRejectCaseInsensitiveDuplicate() {
        String username = uniqueUsername();

        insertUser(username);

        // PostgreSQL CHECK constraint permits
        // lowercase usernames only.
        // Uppercase must therefore be rejected.
        assertThatThrownBy(() ->
                insertUser(username.toUpperCase()))
                .isInstanceOf(
                        DataIntegrityViolationException.class
                );
    }

    @Test
    void shouldRejectUsernameContainingAtSymbol() {
        assertThatThrownBy(() ->
                insertUser("user@example"))
                .isInstanceOf(
                        DataIntegrityViolationException.class
                );
    }

    @Test
    void shouldRejectUsernameStartingWithNumber() {
        assertThatThrownBy(() ->
                insertUser("1username"))
                .isInstanceOf(
                        DataIntegrityViolationException.class
                );
    }

    @Test
    void shouldRejectUsernameTooShort() {
        assertThatThrownBy(() ->
                insertUser("abc"))
                .isInstanceOf(
                        DataIntegrityViolationException.class
                );
    }

    @Test
    void shouldRejectUsernameTooLong() {
        assertThatThrownBy(() ->
                insertUser("a".repeat(31)))
                .isInstanceOf(
                        DataIntegrityViolationException.class
                );
    }

    @Test
    void shouldFindUsernameIgnoringCase() {
        String username = uniqueUsername();

        insertUser(username);

        assertThat(userRepository
                .findByUsernameIgnoreCase(
                        username.toUpperCase()
                ))
                .isPresent();
    }

    @Test
    void shouldHaveNormalizedUniqueIndex() {
        Integer count = jdbcTemplate.queryForObject("""
            SELECT COUNT(*)
            FROM pg_indexes
            WHERE tablename = 'users'
              AND indexname =
                  'ux_users_username_normalized'
            """, Integer.class);

        assertThat(count).isEqualTo(1);
    }
}
