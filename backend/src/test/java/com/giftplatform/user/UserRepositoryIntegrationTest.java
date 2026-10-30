
package com.giftplatform.user.repository;

import com.giftplatform.user.entity.User;
import com.giftplatform.user.entity.UserRole;

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
class UserRepositoryIntegrationTest {

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
    private UserRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private User newUser(String email) {
        return new User(email, "$2a$12$exampleHashForTest");
    }

    @Test
    void shouldSaveAndRetrieveUser() {
        User saved = repository.saveAndFlush(
                newUser("customer@example.com")
        );

        User found = repository.findById(saved.getId())
                .orElseThrow();

        assertThat(found.getEmail())
                .isEqualTo("customer@example.com");
    }

    @Test
    void shouldNormalizeEmail() {
        User user = newUser("  CUSTOMER@Example.COM  ");

        assertThat(user.getEmail())
                .isEqualTo("customer@example.com");
    }

    @Test
    void shouldAssignCustomerRoleByDefault() {
        User saved = repository.saveAndFlush(
                newUser("role@example.com")
        );

        assertThat(saved.getRole())
                .isEqualTo(UserRole.CUSTOMER);
    }

    @Test
    void shouldEnableNewUserByDefault() {
        User user = newUser("enabled@example.com");

        assertThat(user.isEnabled()).isTrue();
    }

    @Test
    void shouldFindEmailIgnoringCase() {
        repository.saveAndFlush(
                newUser("lookup@example.com")
        );

        assertThat(repository.findByEmailIgnoreCase(
                "LOOKUP@EXAMPLE.COM"
        )).isPresent();
    }

    @Test
    void shouldDetectExistingEmail() {
        repository.saveAndFlush(
                newUser("exists@example.com")
        );

        assertThat(repository.existsByEmailIgnoreCase(
                "EXISTS@example.com"
        )).isTrue();
    }

    @Test
    void shouldRejectCaseInsensitiveDuplicateEmail() {
        repository.saveAndFlush(
                newUser("duplicate@example.com")
        );

        assertThatThrownBy(() ->
                repository.saveAndFlush(
                        newUser("DUPLICATE@example.com")
                )
        ).isInstanceOf(
                DataIntegrityViolationException.class
        );
    }

    @Test
    void shouldRejectNullEmail() {
        assertThatThrownBy(() ->
                new User(null, "hash")
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectBlankEmail() {
        assertThatThrownBy(() ->
                new User("   ", "hash")
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectBlankPasswordHash() {
        assertThatThrownBy(() ->
                new User("valid@example.com", " ")
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldGenerateUniqueUserIds() {
        User first = newUser("first@example.com");
        User second = newUser("second@example.com");

        assertThat(first.getId())
                .isNotEqualTo(second.getId());
    }

    @Test
    void shouldGenerateDatabaseTimestamps() {
        User saved = repository.saveAndFlush(
                newUser("timestamps@example.com")
        );

        var result = jdbcTemplate.queryForMap(
                """
                SELECT created_at, updated_at
                FROM users
                WHERE id = ?
                """,
                saved.getId()
        );

        assertThat(result.get("created_at"))
                .isNotNull();
        assertThat(result.get("updated_at"))
                .isNotNull();
    }

    @Test
    void shouldRejectInvalidDatabaseRole() {
        assertThatThrownBy(() ->
                jdbcTemplate.update(
                        """
                        INSERT INTO users (
                            id, email, password_hash, role
                        )
                        VALUES (?, ?, ?, ?)
                        """,
                        UUID.randomUUID(),
                        "invalid-role@example.com",
                        "hash",
                        "SUPERUSER"
                )
        ).isInstanceOf(
                DataIntegrityViolationException.class
        );
    }

    @Test
    void shouldRegisterFlywayMigrationV3() {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM flyway_schema_history
                WHERE version = '3'
                  AND success = true
                """,
                Integer.class
        );

        assertThat(count).isEqualTo(1);
    }
}
