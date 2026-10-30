
package com.giftplatform.audit.repository;

import com.giftplatform.audit.entity.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.sql.Timestamp;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Testcontainers
class AuditLogRepositoryIntegrationTest {

    @Container
    static final PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17")
                    .withDatabaseName("giftplatform_test")
                    .withUsername("testuser")
                    .withPassword("testpassword");


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
    private AuditLogRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private AuditLog newAuditLog(UUID entityId) {
        return new AuditLog(
                UUID.randomUUID(),
                AuditActorType.CUSTOMER,
                "EVENT",
                entityId,
                AuditAction.CREATE,
                null,
                Map.of("title", "Birthday Celebration"),
                "test-request"
        );
    }

    @Test
    void shouldSaveAndRetrieveAuditLog() {
        AuditLog saved = repository.saveAndFlush(
                newAuditLog(UUID.randomUUID())
        );

        repository.findById(saved.getId())
                .ifPresentOrElse(
                        found -> {
                            assertThat(found.getId())
                                    .isEqualTo(saved.getId());
                            assertThat(found.getAction())
                                    .isEqualTo(AuditAction.CREATE);
                            assertThat(found.getActorType())
                                    .isEqualTo(AuditActorType.CUSTOMER);
                        },
                        () -> fail("Audit log not found")
                );
    }

    @Test
    void shouldPersistJsonbValues() {
        AuditLog saved = repository.saveAndFlush(
                new AuditLog(
                        UUID.randomUUID(),
                        AuditActorType.CUSTOMER,
                        "EVENT",
                        UUID.randomUUID(),
                        AuditAction.UPDATE,
                        Map.of("title", "Old Title"),
                        Map.of("title", "New Title"),
                        "json-test"
                )
        );

        String oldTitle = jdbcTemplate.queryForObject(
                """
                SELECT old_values ->> 'title'
                FROM audit_logs WHERE id = ?
                """,
                String.class,
                saved.getId()
        );

        String newTitle = jdbcTemplate.queryForObject(
                """
                SELECT new_values ->> 'title'
                FROM audit_logs WHERE id = ?
                """,
                String.class,
                saved.getId()
        );

        assertThat(oldTitle).isEqualTo("Old Title");
        assertThat(newTitle).isEqualTo("New Title");
    }

    @Test
    void shouldGenerateUniqueIds() {
        AuditLog first = newAuditLog(UUID.randomUUID());
        AuditLog second = newAuditLog(UUID.randomUUID());

        assertThat(first.getId())
                .isNotEqualTo(second.getId());
    }

    @Test
    void shouldAllowNullActorIdForSystemEvents() {
        AuditLog saved = repository.saveAndFlush(
                new AuditLog(
                        null,
                        AuditActorType.SYSTEM,
                        "EVENT",
                        UUID.randomUUID(),
                        AuditAction.STATUS_CHANGE,
                        null,
                        Map.of("status", "COMPLETED"),
                        null
                )
        );

        assertThat(saved.getActorId()).isNull();
    }

    @Test
    void shouldGenerateDatabaseTimestamp() {
        AuditLog saved = repository.saveAndFlush(
                newAuditLog(UUID.randomUUID())
        );

        Timestamp timestamp = jdbcTemplate.queryForObject(
                """
                SELECT occurred_at
                FROM audit_logs
                WHERE id = ?
                """,
                Timestamp.class,
                saved.getId()
        );

        assertThat(timestamp).isNotNull();
    }

    @Test
    void shouldFindAuditHistoryByEntity() {
        UUID entityId = UUID.randomUUID();

        repository.saveAndFlush(newAuditLog(entityId));
        repository.saveAndFlush(newAuditLog(entityId));

        var results =
                repository.findByEntityTypeAndEntityIdOrderByOccurredAtDesc(
                        "EVENT",
                        entityId,
                        PageRequest.of(0, 10)
                );

        assertThat(results.getTotalElements())
                .isEqualTo(2);
    }

    @Test
    void shouldFindAuditHistoryByActor() {
        UUID actorId = UUID.randomUUID();

        repository.saveAndFlush(
                new AuditLog(
                        actorId,
                        AuditActorType.CUSTOMER,
                        "EVENT",
                        UUID.randomUUID(),
                        AuditAction.CREATE,
                        null,
                        Map.of("title", "Test"),
                        "actor-test"
                )
        );

        var results =
                repository.findByActorIdOrderByOccurredAtDesc(
                        actorId,
                        PageRequest.of(0, 10)
                );

        assertThat(results.getTotalElements())
                .isEqualTo(1);
    }

    @Test
    void shouldRejectMissingRequiredActorType() {
        AuditLog invalid = new AuditLog(
                UUID.randomUUID(),
                null,
                "EVENT",
                UUID.randomUUID(),
                AuditAction.CREATE,
                null,
                null,
                null
        );

        assertThatThrownBy(
                () -> repository.saveAndFlush(invalid)
        ).isInstanceOf(
                DataIntegrityViolationException.class
        );
    }

    @Test
    void shouldRejectMissingEntityId() {
        AuditLog invalid = new AuditLog(
                UUID.randomUUID(),
                AuditActorType.CUSTOMER,
                "EVENT",
                null,
                AuditAction.CREATE,
                null,
                null,
                null
        );

        assertThatThrownBy(
                () -> repository.saveAndFlush(invalid)
        ).isInstanceOf(
                DataIntegrityViolationException.class
        );
    }
}
