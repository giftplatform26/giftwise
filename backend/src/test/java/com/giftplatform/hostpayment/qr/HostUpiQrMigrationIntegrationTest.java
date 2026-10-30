package com.giftplatform.hostpayment.qr;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Real PostgreSQL 17/Flyway migration contract checks.
 * This suite does not substitute for authenticated HTTP or account-verification tests.
 */
@SpringBootTest(properties = {
    "gift.payment.profiles.enabled=false"
})
class HostUpiQrMigrationIntegrationTest {
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17")
        .withDatabaseName("giftplatform_qr_test")
        .withUsername("testuser")
        .withPassword("testpassword");
    static { POSTGRES.start(); }

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired JdbcTemplate jdbc;

    private int count(String sql, Object... params) {
        return jdbc.queryForObject(sql, Integer.class, params);
    }

    @Test void flywayAppliedVersions25Through28Successfully() {
        for (String version : new String[]{"25","26","27","28"}) {
            assertEquals(1, count(
                "SELECT COUNT(*) FROM flyway_schema_history WHERE version = ? AND success = TRUE",
                version), "Migration V" + version + " must succeed");
        }
    }

    @Test void originalHostUpiAccountsTableStillExists() {
        assertEquals(1, count("""
            SELECT COUNT(*) FROM information_schema.tables
            WHERE table_schema='public' AND table_name='host_upi_accounts'
            """));
    }

    @Test void originalHostBankAccountsTableStillExists() {
        assertEquals(1, count("""
            SELECT COUNT(*) FROM information_schema.tables
            WHERE table_schema='public' AND table_name='host_bank_accounts'
            """));
    }

    @Test void qrMetadataTableExists() {
        assertEquals(1, count("""
            SELECT COUNT(*) FROM information_schema.tables
            WHERE table_schema='public' AND table_name='host_upi_qr_assets'
            """));
    }

    @Test void qrMetadataHasHostAndAccountScope() {
        for (String column : new String[]{"id","host_id","upi_account_id","source",
                "payload_sha256","verified_payee_match","active"}) {
            assertEquals(1, count("""
                SELECT COUNT(*) FROM information_schema.columns
                WHERE table_schema='public' AND table_name='host_upi_qr_assets'
                  AND column_name=?
                """, column), "Missing QR metadata column: " + column);
        }
    }

    @Test void qrMetadataForeignKeysExist() {
        assertTrue(count("""
            SELECT COUNT(*) FROM pg_constraint c
            JOIN pg_class t ON t.oid=c.conrelid
            WHERE t.relname='host_upi_qr_assets' AND c.contype='f'
            """) >= 2, "QR metadata must reference host and UPI account");
    }

    @Test void originalUpiPreferredUniquenessStillExists() {
        assertEquals(1, count("""
            SELECT COUNT(*) FROM pg_indexes
            WHERE schemaname='public' AND tablename='host_upi_accounts'
              AND indexname='ux_host_preferred_upi'
            """));
    }

    @Test void qrActiveUniquenessStillExists() {
        assertEquals(1, count("""
            SELECT COUNT(*) FROM pg_indexes
            WHERE schemaname='public' AND tablename='host_upi_qr_assets'
              AND indexname='ux_host_upi_qr_active'
            """));
    }

    @Test void qrActiveRequiresMatchedPayee() {
        assertEquals(1, count("""
            SELECT COUNT(*) FROM pg_constraint c
            JOIN pg_class t ON t.oid=c.conrelid
            WHERE t.relname='host_upi_qr_assets'
              AND c.conname='chk_host_upi_qr_active' AND c.contype='c'
            """));
    }
}
