package com.giftplatform.gift;

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

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "gift.otp.secret-base64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
@Testcontainers
class GiftVisibilityPersistenceIntegrationTest {
    @Container static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");
    @DynamicPropertySource static void database(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
    @Autowired JdbcTemplate jdbc;
    @Autowired PlatformTransactionManager transactionManager;

    private UUID event() {
        UUID owner = UUID.randomUUID();
        UUID event = UUID.randomUUID();
        jdbc.update("INSERT INTO users(id,email,password_hash,role) VALUES (?,?,?,?)", owner, owner + "@example.test", "test-hash", "CUSTOMER");
        jdbc.update("INSERT INTO events(id,title,occasion_type,status,owner_id,host_display_name) VALUES (?,?,?,?,?,'Fixture host')", event, "Test", "BIRTHDAY", "DRAFT", owner);
        return event;
    }
    private UUID gift(UUID event) {
        UUID id = UUID.randomUUID();
        jdbc.update("INSERT INTO gifts(id,event_id,name,target_amount) VALUES (?,?,?,?)", id, event, "Camera", new BigDecimal("1000.00"));
        return id;
    }
    private String field(UUID id, String column) {
        if (!column.equals("visibility") && !column.equals("product_url")) throw new IllegalArgumentException();
        return jdbc.queryForObject("SELECT " + column + " FROM gifts WHERE id=?", String.class, id);
    }
    @Test void flywayV14WasApplied() {
        assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM flyway_schema_history WHERE version='14' AND success=true", Integer.class));
    }
    @Test void insertedLegacyStyleGiftDefaultsToPrivateDraft() {
        assertEquals("DRAFT", field(gift(event()), "visibility"));
    }
    @Test void publicationAndProductLinkPersistAcrossSeparateReads() {
        UUID id = gift(event());
        jdbc.update("UPDATE gifts SET visibility='PUBLIC', product_url=? WHERE id=?", "https://example.com/item", id);
        assertEquals("PUBLIC", field(id, "visibility"));
        assertEquals("https://example.com/item", field(id, "product_url"));
    }
    @Test void unpublishRetainsProductLink() {
        UUID id = gift(event());
        jdbc.update("UPDATE gifts SET visibility='PUBLIC', product_url=? WHERE id=?", "https://example.com/item", id);
        jdbc.update("UPDATE gifts SET visibility='DRAFT' WHERE id=?", id);
        assertEquals("DRAFT", field(id, "visibility"));
        assertEquals("https://example.com/item", field(id, "product_url"));
    }
    @Test void invalidVisibilityRejectedByDatabase() {
        UUID id = gift(event());
        assertThrows(Exception.class, () -> jdbc.update("UPDATE gifts SET visibility='HIDDEN' WHERE id=?", id));
        assertEquals("DRAFT", field(id, "visibility"));
    }
    @Test void nullVisibilityRejectedByDatabase() {
        UUID id = gift(event());
        assertThrows(Exception.class, () -> jdbc.update("UPDATE gifts SET visibility=NULL WHERE id=?", id));
    }
    @Test void tooLongProductUrlRejectedByDatabase() {
        UUID id = gift(event());
        assertThrows(Exception.class, () -> jdbc.update("UPDATE gifts SET product_url=? WHERE id=?", "x".repeat(2049), id));
    }
    @Test void transactionRollbackRestoresVisibilityAndProductUrl() {
        UUID id = gift(event());
        new TransactionTemplate(transactionManager).execute(status -> {
            jdbc.update("UPDATE gifts SET visibility='PUBLIC', product_url=? WHERE id=?", "https://example.com/item", id);
            status.setRollbackOnly();
            return null;
        });
        assertEquals("DRAFT", field(id, "visibility"));
        assertNull(field(id, "product_url"));
    }
    @Test void eventScopedPublishedQueryExcludesDrafts() {
        UUID event = event();
        UUID draft = gift(event);
        UUID published = gift(event);
        jdbc.update("UPDATE gifts SET visibility='PUBLIC' WHERE id=?", published);
        var ids = jdbc.queryForList("SELECT id FROM gifts WHERE event_id=? AND visibility='PUBLIC'", UUID.class, event);
        assertEquals(1, ids.size());
        assertTrue(ids.contains(published));
        assertFalse(ids.contains(draft));
    }
}
