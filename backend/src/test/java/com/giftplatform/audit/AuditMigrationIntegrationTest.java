
package com.giftplatform.audit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.sql.Timestamp;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Testcontainers
class AuditMigrationIntegrationTest {

    @Container
    static final PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17")
                    .withDatabaseName("giftplatform_test")
                    .withUsername("testuser")
                    .withPassword("testpassword");

    @DynamicPropertySource
    static void configureDatabase(
            DynamicPropertyRegistry registry) {

        registry.add(
                "spring.datasource.url",
                postgres::getJdbcUrl
        );

        registry.add(
                "spring.datasource.username",
                postgres::getUsername
        );

        registry.add(
                "spring.datasource.password",
                postgres::getPassword
        );
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldCreateAuditLogsTable() {

        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.tables
                WHERE table_schema = 'public'
                  AND table_name = 'audit_logs'
                """,
                Integer.class
        );

        assertThat(count).isEqualTo(1);
    }

    @Test
    void shouldContainAllRequiredColumns() {

        var columns = jdbcTemplate.queryForList(
                """
                SELECT column_name
                FROM information_schema.columns
                WHERE table_schema = 'public'
                  AND table_name = 'audit_logs'
                """,
                String.class
        );

        assertThat(columns).contains(
                "id",
                "actor_id",
                "actor_type",
                "entity_type",
                "entity_id",
                "action",
                "old_values",
                "new_values",
                "occurred_at",
                "request_id"
        );
    }

    @Test
    void shouldUseJsonbForAuditValues() {

        var types = jdbcTemplate.queryForList(
                """
                SELECT data_type
                FROM information_schema.columns
                WHERE table_schema = 'public'
                  AND table_name = 'audit_logs'
                    AND column_name IN (
                        'old_values', 'new_values'
                    )
                """,
                String.class
        );

        assertThat(types)
                .hasSize(2)
                .containsOnly("jsonb");
    }

    @Test
    void shouldInsertValidAuditRecord() {

        UUID id = UUID.randomUUID();
        UUID entityId = UUID.randomUUID();

        int rows = jdbcTemplate.update(
                """
                INSERT INTO audit_logs (
                    id,
                    actor_type,
                    entity_type,
                    entity_id,
                    action,
                    new_values
                )
                VALUES (?, ?, ?, ?, ?, ?::jsonb)
                """,
                id,
                "CUSTOMER",
                "EVENT",
                entityId,
                "CREATE",
                "{\"title\":\"Birthday\"}"
        );

        assertThat(rows).isEqualTo(1);
    }

    @Test
    void shouldGenerateTimestampAutomatically() {

        UUID id = UUID.randomUUID();

        jdbcTemplate.update(
                """
                INSERT INTO audit_logs (
                    id,
                    actor_type,
                    entity_type,
                    entity_id,
                    action
                )
                VALUES (?, ?, ?, ?, ?)
                """,
                id,
                "SYSTEM",
                "EVENT",
                UUID.randomUUID(),
                "CREATE"
        );

        Timestamp timestamp = jdbcTemplate.queryForObject(
                """
                SELECT occurred_at
                FROM audit_logs
                WHERE id = ?
                """,
                Timestamp.class,
                id
        );

        assertThat(timestamp).isNotNull();
    }

    @Test
    void shouldRejectInvalidActorType() {

        assertThatThrownBy(() ->
                jdbcTemplate.update(
                        """
                        INSERT INTO audit_logs (
                            id,
                            actor_type,
                            entity_type,
                            entity_id,
                            action
                        )
                        VALUES (?, ?, ?, ?, ?)
                        """,
                        UUID.randomUUID(),
                        "UNKNOWN",
                        "EVENT",
                        UUID.randomUUID(),
                        "CREATE"
                )
        ).isInstanceOf(
                org.springframework.dao.DataIntegrityViolationException.class
        );
    }

    @Test
    void shouldRejectInvalidAction() {

        assertThatThrownBy(() ->
                jdbcTemplate.update(
                        """
                        INSERT INTO audit_logs (
                            id,
                            actor_type,
                            entity_type,
                            entity_id,
                            action
                        )
                        VALUES (?, ?, ?, ?, ?)
                        """,
                        UUID.randomUUID(),
                        "CUSTOMER",
                        "EVENT",
                        UUID.randomUUID(),
                        "INVALID_ACTION"
                )
        ).isInstanceOf(
                org.springframework.dao.DataIntegrityViolationException.class
        );
    }

    @Test
    void shouldRejectMissingEntityId() {

        assertThatThrownBy(() ->
                jdbcTemplate.update(
                        """
                        INSERT INTO audit_logs (
                            id,
                            actor_type,
                            entity_type,
                            action
                        )
                        VALUES (?, ?, ?, ?)
                        """,
                        UUID.randomUUID(),
                        "CUSTOMER",
                        "EVENT",
                        "CREATE"
                )
        ).isInstanceOf(
                org.springframework.dao.DataIntegrityViolationException.class
        );
    }

    @Test
    void shouldCreateAuditIndexes() {

        var indexes = jdbcTemplate.queryForList(
                """
                SELECT indexname
                FROM pg_indexes
                WHERE schemaname = 'public'
                  AND tablename = 'audit_logs'
                """,
                String.class
        );

        assertThat(indexes).contains(
                "idx_audit_entity",
                "idx_audit_actor",
                "idx_audit_occurred_at"
        );
    }

    @Test
    void shouldRegisterFlywayMigrationV2() {

        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM flyway_schema_history
                WHERE version = '2'
                  AND success = true
                """,
                Integer.class
        );

        assertThat(count).isEqualTo(1);
    }
}
