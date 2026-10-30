
package com.giftplatform.otp;

import com.giftplatform.otp.entity.PhoneOtpChallenge;
import com.giftplatform.otp.repository.PhoneOtpChallengeRepository;
import com.giftplatform.user.entity.User;
import com.giftplatform.user.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class PhoneOtpPersistenceIntegrationTest {

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
    UserRepository userRepository;

    @Autowired
    PhoneOtpChallengeRepository otpRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    TransactionTemplate transactionTemplate;

    private UUID createUser() {
        User user = new User(
                "otp-" + UUID.randomUUID() + "@example.com",
                "$2a$12$exampleHashForTest"
        );

        return userRepository.saveAndFlush(user).getId();
    }

    private PhoneOtpChallenge newChallenge(UUID userId) {
        Instant now = Instant.now();

        return new PhoneOtpChallenge(
                userId,
                "+919876543210",
                "test-digest-not-a-real-otp",
                now,
                now.plusSeconds(300)
        );
    }

    @Test
    void shouldRegisterFlywayMigrationV5() {
        Integer count = jdbcTemplate.queryForObject("""
            SELECT COUNT(*)
            FROM flyway_schema_history
            WHERE version = '5'
              AND success = true
            """, Integer.class);

        assertThat(count).isEqualTo(1);
    }

    @Test
    void shouldSaveAndRetrieveChallenge() {
        UUID userId = createUser();

        PhoneOtpChallenge saved = otpRepository.saveAndFlush(
                newChallenge(userId)
        );

        PhoneOtpChallenge found = otpRepository
                .findById(saved.getId())
                .orElseThrow();

        assertThat(found.getUserId()).isEqualTo(userId);
        assertThat(found.getAttemptCount()).isZero();
        assertThat(found.getConsumedAt()).isNull();
    }

    @Test
    void shouldRejectInvalidPhoneNumber() {
        UUID userId = createUser();

        assertThatThrownBy(() ->
                new PhoneOtpChallenge(
                        userId,
                        "9876543210",
                        "digest",
                        Instant.now(),
                        Instant.now().plusSeconds(300)
                )
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectInvalidExpiration() {
        UUID userId = createUser();
        Instant now = Instant.now();

        assertThatThrownBy(() ->
                new PhoneOtpChallenge(
                        userId,
                        "+919876543210",
                        "digest",
                        now,
                        now
                )
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectMissingUserForeignKey() {
        assertThatThrownBy(() ->
                otpRepository.saveAndFlush(
                        newChallenge(UUID.randomUUID())
                )
        ).isInstanceOf(
                DataIntegrityViolationException.class
        );
    }

    @Test
    void shouldPersistFailedAttempt() {
        UUID userId = createUser();

        PhoneOtpChallenge challenge =
                otpRepository.saveAndFlush(
                        newChallenge(userId)
                );

        challenge.recordFailedAttempt();
        otpRepository.saveAndFlush(challenge);

        assertThat(otpRepository
                .findById(challenge.getId())
                .orElseThrow()
                .getAttemptCount())
                .isEqualTo(1);
    }

    @Test
    void shouldPersistConsumption() {
        UUID userId = createUser();

        PhoneOtpChallenge challenge =
                otpRepository.saveAndFlush(
                        newChallenge(userId)
                );
        challenge.markSent(
                challenge.getCreatedAt().plusSeconds(1)
        );
        challenge.consume(Instant.now());
        otpRepository.saveAndFlush(challenge);

        assertThat(otpRepository
                .findById(challenge.getId())
                .orElseThrow()
                .getConsumedAt())
                .isNotNull();
    }

    @Test
    void shouldPreventDoubleConsumption() {
        UUID userId = createUser();

        PhoneOtpChallenge challenge =
                newChallenge(userId);
        challenge.markSent(
                challenge.getCreatedAt().plusSeconds(1)
        );
        challenge.consume(Instant.now());

        assertThatThrownBy(() ->
                challenge.consume(Instant.now())
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldRejectConsumptionAfterExpiration() {
        UUID userId = createUser();

        Instant now = Instant.now();

        PhoneOtpChallenge challenge =
                new PhoneOtpChallenge(
                        userId,
                        "+919876543210",
                        "digest",
                        now.minusSeconds(600),
                        now.minusSeconds(300)
                );

        assertThatThrownBy(() ->
                challenge.consume(now)
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldEnforceMaximumAttempts() {
        PhoneOtpChallenge challenge =
                newChallenge(createUser());

        for (int i = 0; i < 5; i++) {
            challenge.recordFailedAttempt();
        }

        assertThat(challenge.getAttemptCount())
                .isEqualTo(5);

        assertThatThrownBy(
                challenge::recordFailedAttempt
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldFindChallengeWithWriteLock() {
        UUID userId = createUser();

        PhoneOtpChallenge saved =
                otpRepository.saveAndFlush(
                        newChallenge(userId)
                );

        UUID foundId = transactionTemplate.execute(
                status -> otpRepository
                        .findForVerification(
                                saved.getId(),
                                userId
                        )
                        .orElseThrow()
                        .getId()
        );

        assertThat(foundId).isEqualTo(saved.getId());
    }

    @Test
    void shouldNotFindChallengeForDifferentUser() {
        UUID ownerId = createUser();
        UUID otherId = createUser();

        PhoneOtpChallenge saved =
                otpRepository.saveAndFlush(
                        newChallenge(ownerId)
                );

        Boolean found = transactionTemplate.execute(
                status -> otpRepository
                        .findForVerification(
                                saved.getId(),
                                otherId
                        )
                        .isPresent()
        );

        assertThat(found).isFalse();
    }

    @Test
    void shouldRejectNegativeAttemptCountInDatabase() {
        UUID userId = createUser();

        PhoneOtpChallenge saved =
                otpRepository.saveAndFlush(
                        newChallenge(userId)
                );

        assertThatThrownBy(() ->
                jdbcTemplate.update("""
                    UPDATE phone_otp_challenges
                    SET attempt_count = -1
                    WHERE id = ?
                    """, saved.getId())
        ).isInstanceOf(
                DataIntegrityViolationException.class
        );
    }

    @Test
    void shouldRejectExcessiveAttemptCountInDatabase() {
        UUID userId = createUser();

        PhoneOtpChallenge saved =
                otpRepository.saveAndFlush(
                        newChallenge(userId)
                );

        assertThatThrownBy(() ->
                jdbcTemplate.update("""
                    UPDATE phone_otp_challenges
                    SET attempt_count = 6
                    WHERE id = ?
                    """, saved.getId())
        ).isInstanceOf(
                DataIntegrityViolationException.class
        );
    }

    @Test
    void shouldRejectDuplicateUserPhoneNumbers() {
        UUID first = createUser();
        UUID second = createUser();

        jdbcTemplate.update("""
            UPDATE users
            SET phone_number = ?
            WHERE id = ?
            """, "+919876543211", first);

        assertThatThrownBy(() ->
                jdbcTemplate.update("""
                    UPDATE users
                    SET phone_number = ?
                    WHERE id = ?
                    """, "+919876543211", second)
        ).isInstanceOf(
                DataIntegrityViolationException.class
        );
    }

    @Test
    void shouldRejectInvalidUserPhoneNumber() {
        UUID userId = createUser();

        assertThatThrownBy(() ->
                jdbcTemplate.update("""
                    UPDATE users
                    SET phone_number = ?
                    WHERE id = ?
                    """, "9876543210", userId)
        ).isInstanceOf(
                DataIntegrityViolationException.class
        );
    }

    @Test
    void shouldRejectVerificationTimestampWithoutPhone() {
        UUID userId = createUser();

        assertThatThrownBy(() ->
                jdbcTemplate.update("""
                    UPDATE users
                    SET phone_verified_at = CURRENT_TIMESTAMP
                    WHERE id = ?
                    """, userId)
        ).isInstanceOf(
                DataIntegrityViolationException.class
        );
    }
}
