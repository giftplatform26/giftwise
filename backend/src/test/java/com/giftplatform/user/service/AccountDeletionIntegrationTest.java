package com.giftplatform.user.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.server.ResponseStatusException;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(properties = "gift.otp.secret-base64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
@Testcontainers
class AccountDeletionIntegrationTest {
    @Container static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");
    @DynamicPropertySource static void database(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
    }

    private static final String PASSWORD = "Correct-Horse-1";
    @Autowired JdbcTemplate db;
    @Autowired PasswordEncoder passwords;
    @Autowired AccountDeletionService deletion;

    private UUID host(String role) {
        UUID id = UUID.randomUUID();
        String handle = "u" + id.toString().replace("-", "").substring(0, 12);
        db.update("INSERT INTO users(id,email,password_hash,role,username,full_name,phone_number) VALUES(?,?,?,?,?,?,?)",
            id, handle + "@example.test", passwords.encode(PASSWORD), role, handle, "Asha Host", "+9198" + (10000000 + (int) (Math.random() * 89999999)));
        return id;
    }
    private UUID event(UUID owner, String status) {
        UUID id = UUID.randomUUID();
        db.update("INSERT INTO events(id,title,occasion_type,owner_id,host_display_name,status) VALUES(?,'Party','BIRTHDAY',?,'Host',?)", id, owner, status);
        return id;
    }
    private UUID upi(UUID owner) {
        UUID id = UUID.randomUUID();
        db.update("INSERT INTO host_upi_accounts(id,host_id,upi_id_encrypted,upi_id_fingerprint,upi_id_masked,verification_status,enabled) VALUES(?,?,'enc',?,'a***@upi','PENDING',TRUE)",
            id, owner, id.toString().replace("-", ""));
        return id;
    }

    @Test void deletionAnonymisesDisablesAndCancelsDraftsButKeepsRecords() {
        UUID user = host("CUSTOMER");
        UUID draft = event(user, "DRAFT"), done = event(user, "COMPLETED");
        UUID upi = upi(user);
        db.update("INSERT INTO user_profile_preferences(user_id,timezone) VALUES(?,'Asia/Kolkata')", user);

        deletion.delete(user, PASSWORD, "DELETE");

        Map<String, Object> row = db.queryForMap("SELECT email,username,full_name,phone_number,enabled,deleted_at,password_hash FROM users WHERE id=?", user);
        assertThat(row.get("email")).isEqualTo("deleted+" + user + "@deleted.invalid");
        assertThat(row.get("username")).isNull();
        assertThat(row.get("full_name")).isNull();
        assertThat(row.get("phone_number")).isNull();
        assertThat(row.get("enabled")).isEqualTo(false);
        assertThat(row.get("deleted_at")).isNotNull();
        assertThat(passwords.matches(PASSWORD, (String) row.get("password_hash"))).isFalse();
        assertThat(db.queryForObject("SELECT status FROM events WHERE id=?", String.class, draft)).isEqualTo("CANCELLED");
        assertThat(db.queryForObject("SELECT status FROM events WHERE id=?", String.class, done)).isEqualTo("COMPLETED");
        assertThat(db.queryForObject("SELECT enabled FROM host_upi_accounts WHERE id=?", Boolean.class, upi)).isFalse();
        assertThat(db.queryForObject("SELECT count(*) FROM user_profile_preferences WHERE user_id=?", Integer.class, user)).isZero();
    }

    @Test void liveCelebrationsBlockDeletionWithoutChangingAnything() {
        UUID user = host("CUSTOMER");
        UUID live = event(user, "PUBLISHED"), draft = event(user, "DRAFT");
        assertThatThrownBy(() -> deletion.delete(user, PASSWORD, "DELETE"))
            .isInstanceOf(ResponseStatusException.class).hasMessageContaining("409").hasMessageContaining("1 live celebration");
        assertThat(db.queryForObject("SELECT enabled FROM users WHERE id=?", Boolean.class, user)).isTrue();
        assertThat(db.queryForObject("SELECT status FROM events WHERE id=?", String.class, draft)).isEqualTo("DRAFT");
        assertThat(db.queryForObject("SELECT status FROM events WHERE id=?", String.class, live)).isEqualTo("PUBLISHED");
    }

    @Test void wrongPasswordMissingConfirmationAndAdminsAreRejected() {
        UUID user = host("CUSTOMER");
        assertThatThrownBy(() -> deletion.delete(user, "wrong", "DELETE")).hasMessageContaining("password is incorrect");
        assertThatThrownBy(() -> deletion.delete(user, PASSWORD, "delete")).hasMessageContaining("Type DELETE");
        UUID admin = host("PLATFORM_ADMIN");
        assertThatThrownBy(() -> deletion.delete(admin, PASSWORD, "DELETE")).hasMessageContaining("403");
        assertThat(db.queryForObject("SELECT count(*) FROM users WHERE id IN (?,?) AND enabled", Integer.class, user, admin)).isEqualTo(2);
    }
}
