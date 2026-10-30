package com.giftplatform.hostpayment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Batch 5A.4B foundation: disabled feature boundary and V25 persistence schema.
 * No real host accounts, payment secrets or provider calls are used.
 */
@SpringBootTest(properties = "gift.payment.profiles.enabled=false")
@AutoConfigureMockMvc
class HostPaymentDisabledBoundaryIntegrationTest {
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17")
            .withDatabaseName("giftplatform_host_payment_test")
            .withUsername("testuser")
            .withPassword("testpassword");

    static { postgres.start(); }

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;

    @Test void anonymousUpiListingIsUnauthorized() throws Exception {
        mvc.perform(get("/api/v1/host/payment-profiles/upi"))
                .andExpect(status().isUnauthorized());
    }

    @Test void anonymousBankListingIsUnauthorized() throws Exception {
        mvc.perform(get("/api/v1/host/payment-profiles/banks"))
                .andExpect(status().isUnauthorized());
    }

    @Test void anonymousUpiMutationWithoutCsrfIsForbidden() throws Exception {
        mvc.perform(post("/api/v1/host/payment-profiles/upi")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"upiId\":\"nobody@example\"}"))
                .andExpect(status().isForbidden());
    }

    @Test void anonymousBankMutationWithoutCsrfIsForbidden() throws Exception {
        mvc.perform(post("/api/v1/host/payment-profiles/banks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accountNumber\":\"123456789\",\"ifsc\":\"ABCD0123456\",\"accountHolderName\":\"Example\"}"))
                .andExpect(status().isForbidden());
    }

    @Test void migrationV25WasApplied() {
        Integer count = jdbc.queryForObject("""
                SELECT count(*) FROM flyway_schema_history
                WHERE version = '25' AND success = true
                """, Integer.class);
        assertEquals(1, count);
    }

    @Test void upiTableStoresEncryptedAndFingerprintFields() {
        assertColumns("host_upi_accounts", "host_id", "upi_id_encrypted",
                "upi_id_fingerprint", "upi_id_masked", "verification_status", "preferred");
    }

    @Test void bankTableStoresEncryptedAndFingerprintFields() {
        assertColumns("host_bank_accounts", "host_id", "account_number_encrypted",
                "account_fingerprint", "account_number_masked", "ifsc_encrypted",
                "ifsc_masked", "account_holder_encrypted", "verification_status", "preferred");
    }

    @Test void plaintextPaymentColumnsAreAbsent() {
        List<String> names = jdbc.queryForList("""
                SELECT column_name FROM information_schema.columns
                WHERE table_schema = current_schema()
                  AND table_name IN ('host_upi_accounts', 'host_bank_accounts')
                """, String.class);
        assertFalse(names.contains("upi_id"));
        assertFalse(names.contains("account_number"));
        assertFalse(names.contains("account_holder"));
    }

    private void assertColumns(String table, String... expected) {
        List<String> actual = jdbc.queryForList("""
                SELECT column_name FROM information_schema.columns
                WHERE table_schema = current_schema() AND table_name = ?
                """, String.class, table);
        for (String column : expected) {
            assertTrue(actual.contains(column), () -> table + " missing " + column);
        }
    }
}
