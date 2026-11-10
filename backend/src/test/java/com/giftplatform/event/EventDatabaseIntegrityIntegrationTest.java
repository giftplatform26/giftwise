package com.giftplatform.event;

import com.giftplatform.audit.entity.AuditAction;
import com.giftplatform.audit.entity.AuditActorType;
import com.giftplatform.audit.service.AuditService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "gift.otp.secret-base64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
@Testcontainers
class EventDatabaseIntegrityIntegrationTest {
    @Container static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");
    @DynamicPropertySource static void database(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
    }
    @Autowired JdbcTemplate jdbc;
    @Autowired PlatformTransactionManager txManager;
    @Autowired AuditService audit;

    private UUID user() {
        UUID id = UUID.randomUUID();
        jdbc.update("INSERT INTO users(id,email,password_hash,role) VALUES (?,?,?,?)",
                id, id + "@example.test", "test-hash", "CUSTOMER");
        return id;
    }
    private UUID event(UUID owner) {
        UUID id = UUID.randomUUID();
        jdbc.update("INSERT INTO events(id,title,occasion_type,status,owner_id,host_display_name) VALUES (?,?,?,?,?,'Fixture host')",
                id, "Test", "BIRTHDAY", "DRAFT", owner);
        return id;
    }
    private int count(String table, UUID id) {
        return jdbc.queryForObject("SELECT count(*) FROM " + table + " WHERE id=?", Integer.class, id);
    }

    @Test void flywayV9RecordedSuccessfully() {
        assertEquals(1, jdbc.queryForObject(
                "SELECT count(*) FROM flyway_schema_history WHERE version='9' AND success=true", Integer.class));
    }
    @Test void ownerColumnIsUuid() {
        assertEquals("uuid", jdbc.queryForObject(
                "SELECT data_type FROM information_schema.columns WHERE table_schema=current_schema() AND table_name='events' AND column_name='owner_id'", String.class));
    }
    @Test void ownerForeignKeyExists() {
        assertEquals(1, jdbc.queryForObject(
                "SELECT count(*) FROM pg_constraint WHERE conname='fk_events_owner' AND conrelid='events'::regclass AND contype='f'", Integer.class));
    }
    @Test void ownerIndexExists() {
        assertEquals(1, jdbc.queryForObject(
                "SELECT count(*) FROM pg_indexes WHERE tablename='events' AND indexname='idx_events_owner_id'", Integer.class));
    }
    @Test void eventOwnerPersistsAcrossDatabaseQueries() {
        UUID owner = user(); UUID id = event(owner);
        assertEquals(owner, jdbc.queryForObject("SELECT owner_id FROM events WHERE id=?", UUID.class, id));
    }
    @Test void legacyEventsMayRemainUnowned() {
        UUID id = event(null);
        assertNull(jdbc.queryForObject("SELECT owner_id FROM events WHERE id=?", UUID.class, id));
    }
    @Test void auditAndEventRollbackTogether() {
        UUID owner = user(); UUID id = UUID.randomUUID();
        new TransactionTemplate(txManager).execute(status -> {
            jdbc.update("INSERT INTO events(id,title,occasion_type,status,owner_id,host_display_name) VALUES (?,?,?,?,?,'Fixture host')",
                    id, "Rollback", "BIRTHDAY", "DRAFT", owner);
            audit.recordChange(owner, AuditActorType.CUSTOMER, "EVENT", id,
                    AuditAction.CREATE, null, Map.of("title", "Rollback"), null);
            status.setRollbackOnly();
            return null;
        });
        assertEquals(0, count("events", id));
        assertEquals(0, jdbc.queryForObject(
                "SELECT count(*) FROM audit_logs WHERE entity_type='EVENT' AND entity_id=?", Integer.class, id));
    }
    @Test void auditAndEventCommitTogether() {
        UUID owner = user(); UUID id = UUID.randomUUID();
        new TransactionTemplate(txManager).execute(status -> {
            jdbc.update("INSERT INTO events(id,title,occasion_type,status,owner_id,host_display_name) VALUES (?,?,?,?,?,'Fixture host')",
                    id, "Commit", "BIRTHDAY", "DRAFT", owner);
            audit.recordChange(owner, AuditActorType.CUSTOMER, "EVENT", id,
                    AuditAction.CREATE, null, Map.of("title", "Commit"), null);
            return null;
        });
        assertEquals(1, count("events", id));
        assertEquals(1, jdbc.queryForObject(
                "SELECT count(*) FROM audit_logs WHERE entity_type='EVENT' AND entity_id=?", Integer.class, id));
    }
}
