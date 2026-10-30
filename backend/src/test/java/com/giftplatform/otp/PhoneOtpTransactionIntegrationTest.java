package com.giftplatform.otp;

import com.giftplatform.otp.entity.PhoneOtpChallenge;
import com.giftplatform.otp.repository.PhoneOtpChallengeRepository;
import com.giftplatform.otp.service.OtpCryptoService;
import com.giftplatform.otp.service.PhoneOtpTransactionService;
import com.giftplatform.otp.service.PhoneOtpVerificationResult;
import com.giftplatform.user.entity.User;
import com.giftplatform.user.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.DataIntegrityViolationException;

@SpringBootTest
@Testcontainers
class PhoneOtpTransactionIntegrationTest {

    @Container
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
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
        registry.add(
                "gift.otp.secret-base64",
                () -> java.util.Base64.getEncoder()
                        .encodeToString(new byte[32])
        );
    }

    @Autowired
    private UserRepository users;

    @Autowired
    private PhoneOtpChallengeRepository challenges;

    @Autowired
    private PhoneOtpTransactionService service;

    @Autowired
    private OtpCryptoService crypto;

    @Autowired
    private JdbcTemplate jdbc;

    private User user;
    private PhoneOtpChallenge challenge;

    private static final String CODE = "123456";
    private static final String PHONE = "+919876543210";

    @BeforeEach
    void setup() {
        challenges.deleteAll();
        users.deleteAll();

        user = users.save(
                new User(
                        "otp_integration",
                        "otp.integration@example.com",
                        "hashed-password"
                )
        );

        UUID challengeId = UUID.randomUUID();
        Instant now = Instant.now();

        challenge = new PhoneOtpChallenge(
                challengeId,
                user.getId(),
                PHONE,
                crypto.digest(challengeId, CODE),
                now.minusSeconds(10),
                now.plusSeconds(290)
        );

        challenge.markSent(now.minusSeconds(9));
        challenge = challenges.saveAndFlush(challenge);
    }

    @Test
    void incorrectCodePersistsAttempt() {
        var result = service.verify(
                user.getId(),
                challenge.getId(),
                "000000"
        );

        assertEquals(
                PhoneOtpVerificationResult.INVALID_CODE,
                result
        );

        var persisted = challenges.findById(
                challenge.getId()
        ).orElseThrow();

        assertEquals(1, persisted.getAttemptCount());
    }

    @Test
    void successfulVerificationPersistsBothChanges() {
        var result = service.verify(
                user.getId(),
                challenge.getId(),
                CODE
        );

        assertEquals(
                PhoneOtpVerificationResult.VERIFIED,
                result
        );

        var persistedUser = users.findById(
                user.getId()
        ).orElseThrow();

        var persistedChallenge = challenges.findById(
                challenge.getId()
        ).orElseThrow();

        assertEquals(PHONE, persistedUser.getPhoneNumber());
        assertNotNull(persistedUser.getPhoneVerifiedAt());
        assertTrue(persistedChallenge.isConsumed());
    }

    @Test
    void consumedOtpCannotBeReused() {
        assertEquals(
                PhoneOtpVerificationResult.VERIFIED,
                service.verify(
                        user.getId(),
                        challenge.getId(),
                        CODE
                )
        );

        assertEquals(
                PhoneOtpVerificationResult.ALREADY_USED,
                service.verify(
                        user.getId(),
                        challenge.getId(),
                        CODE
                )
        );
    }

    @Test
    void fiveIncorrectAttemptsArePersisted() {
        for (int i = 0; i < 5; i++) {
            assertEquals(
                    PhoneOtpVerificationResult.INVALID_CODE,
                    service.verify(
                            user.getId(),
                            challenge.getId(),
                            "000000"
                    )
            );
        }

        var persisted = challenges.findById(
                challenge.getId()
        ).orElseThrow();

        assertEquals(5, persisted.getAttemptCount());

        assertEquals(
                PhoneOtpVerificationResult.MAX_ATTEMPTS,
                service.verify(
                        user.getId(),
                        challenge.getId(),
                        CODE
                )
        );
    }

    @Test
    void wrongUserCannotVerifyChallenge() {
        var other = users.save(
                new User(
                        "another_otp_user",
                        "another.otp@example.com",
                        "hashed-password"
                )
        );

        assertEquals(
                PhoneOtpVerificationResult.NOT_AVAILABLE,
                service.verify(
                        other.getId(),
                        challenge.getId(),
                        CODE
                )
        );

        assertFalse(
                challenges.findById(
                        challenge.getId()
                ).orElseThrow().isConsumed()
        );
    }

    @Test
    void onlyOneActiveChallengeAllowedPerUser() {
        UUID secondId = UUID.randomUUID();
        Instant now = Instant.now();

        var second = new PhoneOtpChallenge(
                secondId,
                user.getId(),
                PHONE,
                crypto.digest(secondId, CODE),
                now,
                now.plusSeconds(300)
        );

        assertThrows(
                DataIntegrityViolationException.class,
                () -> challenges.saveAndFlush(second)
        );
    }

    @Test
    void consumedChallengeDoesNotBlockNewChallenge() {

        assertEquals(
                PhoneOtpVerificationResult.VERIFIED,
                service.verify(
                        user.getId(),
                        challenge.getId(),
                        CODE
                )
        );

        UUID newChallengeId = UUID.randomUUID();
        Instant now = Instant.now();

        var nextChallenge = new PhoneOtpChallenge(
                newChallengeId,
                user.getId(),
                PHONE,
                crypto.digest(newChallengeId, CODE),
                now,
                now.plusSeconds(300)
        );

        challenges.saveAndFlush(nextChallenge);

        assertTrue(
                challenges.existsById(newChallengeId)
        );
    }



    @Test
    void concurrentVerificationAllowsOnlyOneSuccess()
            throws Exception {

        UUID userId = user.getId();
        UUID challengeId = challenge.getId();

        ExecutorService executor =
                Executors.newFixedThreadPool(2);

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        Callable<PhoneOtpVerificationResult> task = () -> {
            ready.countDown();

            if (!start.await(10, TimeUnit.SECONDS)) {
                throw new TimeoutException(
                        "Concurrent test start timed out"
                );
            }

            return service.verify(
                    userId,
                    challengeId,
                    CODE
            );
        };

        try {
            Future<PhoneOtpVerificationResult> first =
                    executor.submit(task);

            Future<PhoneOtpVerificationResult> second =
                    executor.submit(task);

            assertTrue(
                    ready.await(10, TimeUnit.SECONDS),
                    "Both workers must be ready"
            );

            start.countDown();

            var results = List.of(
                    first.get(20, TimeUnit.SECONDS),
                    second.get(20, TimeUnit.SECONDS)
            );

            assertEquals(
                    1,
                    results.stream()
                            .filter(r ->
                                    r == PhoneOtpVerificationResult.VERIFIED)
                            .count()
            );

            assertEquals(
                    1,
                    results.stream()
                            .filter(r ->
                                    r == PhoneOtpVerificationResult.ALREADY_USED)
                            .count()
            );

            assertTrue(
                    challenges.findById(challengeId)
                            .orElseThrow()
                            .isConsumed()
            );

            assertNotNull(
                    users.findById(userId)
                            .orElseThrow()
                            .getPhoneVerifiedAt()
            );

        } finally {
            start.countDown();
            executor.shutdownNow();

            assertTrue(
                    executor.awaitTermination(
                            10,
                            TimeUnit.SECONDS
                    ),
                    "Worker threads did not terminate"
            );
        }
    }


    @Test
    void databaseFailureRollsBackPhoneVerificationAndConsumption() {

        // Reject consumption of this particular challenge.
        // This trigger fires when Hibernate flushes the update.
        jdbc.execute("""
        CREATE OR REPLACE FUNCTION test_reject_otp_consumption()
        RETURNS trigger AS $$
        BEGIN
            IF NEW.id = '%s'::uuid
               AND NEW.consumed_at IS NOT NULL
               AND OLD.consumed_at IS NULL THEN
                RAISE EXCEPTION
                    'Forced OTP consumption failure'
                    USING ERRCODE = '23514';
            END IF;
            RETURN NEW;
        END;
        $$ LANGUAGE plpgsql
        """.formatted(challenge.getId()));

        jdbc.execute("""
        CREATE TRIGGER test_otp_rollback_trigger
        BEFORE UPDATE ON phone_otp_challenges
        FOR EACH ROW
        EXECUTE FUNCTION test_reject_otp_consumption()
        """);

        try {
            assertThrows(
                    DataIntegrityViolationException.class,
                    () -> service.verify(
                            user.getId(),
                            challenge.getId(),
                            CODE
                    )
            );

            var persistedUser =
                    users.findById(user.getId()).orElseThrow();

            var persistedChallenge =
                    challenges.findById(challenge.getId())
                            .orElseThrow();

            assertNull(persistedUser.getPhoneNumber());
            assertNull(persistedUser.getPhoneVerifiedAt());
            assertFalse(persistedChallenge.isConsumed());

        } finally {
            jdbc.execute("""
            DROP TRIGGER IF EXISTS
            test_otp_rollback_trigger
            ON phone_otp_challenges
            """);

            jdbc.execute("""
            DROP FUNCTION IF EXISTS
            test_reject_otp_consumption()
            """);
        }
    }

    @Test
    void expiredOtpCannotBeVerified() {
        UUID challengeId = challenge.getId();

        jdbc.update("""
        UPDATE phone_otp_challenges
        SET created_at = CURRENT_TIMESTAMP - INTERVAL '10 minutes',
            expires_at = CURRENT_TIMESTAMP - INTERVAL '5 minutes'
        WHERE id = ?
        """, challengeId);

        assertEquals(
                PhoneOtpVerificationResult.EXPIRED,
                service.verify(user.getId(), challengeId, CODE)
        );

        assertNull(
                users.findById(user.getId())
                        .orElseThrow()
                        .getPhoneVerifiedAt()
        );

        assertFalse(
                challenges.findById(challengeId)
                        .orElseThrow()
                        .isConsumed()
        );
    }

    @Test
    void pendingOtpCannotBeVerified() {
        // Remove the existing SENT challenge so the database's
        // one-active-challenge-per-user constraint remains satisfied.
        challenges.deleteById(challenge.getId());
        challenges.flush();

        UUID pendingId = UUID.randomUUID();
        Instant now = Instant.now();

        PhoneOtpChallenge pending = new PhoneOtpChallenge(
                pendingId,
                user.getId(),
                PHONE,
                crypto.digest(pendingId, CODE),
                now,
                now.plusSeconds(300)
        );

        challenges.saveAndFlush(pending);

        assertEquals(
                PhoneOtpVerificationResult.NOT_AVAILABLE,
                service.verify(user.getId(), pendingId, CODE)
        );

        assertNull(
                users.findById(user.getId())
                        .orElseThrow()
                        .getPhoneVerifiedAt()
        );

        assertFalse(
                challenges.findById(pendingId)
                        .orElseThrow()
                        .isConsumed()
        );
    }

    @Test
    void disabledUserCannotVerifyOtp() {
        UUID challengeId = challenge.getId();

        jdbc.update("""
        UPDATE users
        SET enabled = false
        WHERE id = ?
        """, user.getId());

        assertEquals(
                PhoneOtpVerificationResult.NOT_AVAILABLE,
                service.verify(user.getId(), challengeId, CODE)
        );

        assertFalse(
                challenges.findById(challengeId)
                        .orElseThrow()
                        .isConsumed()
        );
    }


    // Batch 2G.2: challenge-ID-free verification

    @Test
    void latestOtpVerifiesAndPersistsBothChanges() {
        assertEquals(PhoneOtpVerificationResult.VERIFIED,
                service.verifyLatest(user.getId(), CODE));
        assertEquals(PHONE, users.findById(user.getId()).orElseThrow().getPhoneNumber());
        assertNotNull(users.findById(user.getId()).orElseThrow().getPhoneVerifiedAt());
        assertTrue(challenges.findById(challenge.getId()).orElseThrow().isConsumed());
    }

    @Test
    void latestOtpWrongCodePersistsAttempt() {
        assertEquals(PhoneOtpVerificationResult.INVALID_CODE,
                service.verifyLatest(user.getId(), "000000"));
        assertEquals(1, challenges.findById(challenge.getId())
                .orElseThrow().getAttemptCount());
        assertNull(users.findById(user.getId()).orElseThrow().getPhoneVerifiedAt());
    }

    @Test
    void latestOtpReplayIsRejected() {
        assertEquals(PhoneOtpVerificationResult.VERIFIED,
                service.verifyLatest(user.getId(), CODE));
        assertEquals(PhoneOtpVerificationResult.ALREADY_USED,
                service.verifyLatest(user.getId(), CODE));
    }

    @Test
    void latestOtpExpiredIsRejected() {
        jdbc.update("""
                UPDATE phone_otp_challenges
                SET created_at = CURRENT_TIMESTAMP - INTERVAL '10 minutes',
                    expires_at = CURRENT_TIMESTAMP - INTERVAL '5 minutes'
                WHERE id = ?
                """, challenge.getId());
        assertEquals(PhoneOtpVerificationResult.EXPIRED,
                service.verifyLatest(user.getId(), CODE));
        assertNull(users.findById(user.getId()).orElseThrow().getPhoneVerifiedAt());
    }

    @Test
    void latestOtpPendingIsRejected() {
        challenges.deleteById(challenge.getId());
        challenges.flush();
        UUID pendingId = UUID.randomUUID();
        Instant now = Instant.now();
        challenges.saveAndFlush(new PhoneOtpChallenge(
                pendingId, user.getId(), PHONE,
                crypto.digest(pendingId, CODE), now, now.plusSeconds(300)));
        assertEquals(PhoneOtpVerificationResult.NOT_AVAILABLE,
                service.verifyLatest(user.getId(), CODE));
        assertFalse(challenges.findById(pendingId).orElseThrow().isConsumed());
    }

    @Test
    void latestOtpDisabledUserIsRejected() {
        jdbc.update("UPDATE users SET enabled = false WHERE id = ?", user.getId());
        assertEquals(PhoneOtpVerificationResult.NOT_AVAILABLE,
                service.verifyLatest(user.getId(), CODE));
        assertFalse(challenges.findById(challenge.getId()).orElseThrow().isConsumed());
    }

    @Test
    void concurrentLatestVerificationAllowsExactlyOneSuccess() throws Exception {
        UUID userId = user.getId();
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        Callable<PhoneOtpVerificationResult> task = () -> {
            ready.countDown();
            if (!start.await(10, TimeUnit.SECONDS)) {
                throw new TimeoutException("Workers did not start together");
            }
            return service.verifyLatest(userId, CODE);
        };
        try {
            Future<PhoneOtpVerificationResult> first = executor.submit(task);
            Future<PhoneOtpVerificationResult> second = executor.submit(task);
            assertTrue(ready.await(10, TimeUnit.SECONDS));
            start.countDown();
            var results = List.of(first.get(20, TimeUnit.SECONDS),
                    second.get(20, TimeUnit.SECONDS));
            assertEquals(1, results.stream()
                    .filter(r -> r == PhoneOtpVerificationResult.VERIFIED).count());
            assertEquals(1, results.stream()
                    .filter(r -> r == PhoneOtpVerificationResult.ALREADY_USED).count());
            assertTrue(challenges.findById(challenge.getId()).orElseThrow().isConsumed());
        } finally {
            start.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        }
    }

    // Batch 2G.3: latest-challenge selection must never revive older OTPs.

    @Test
    void latestInvalidatedOtpCannotBeVerified() {
        jdbc.update("UPDATE phone_otp_challenges SET delivery_status = 'INVALIDATED' WHERE id = ?",
                challenge.getId());

        assertEquals(PhoneOtpVerificationResult.NOT_AVAILABLE,
                service.verifyLatest(user.getId(), CODE));
        assertNull(users.findById(user.getId()).orElseThrow().getPhoneVerifiedAt());
        assertFalse(challenges.findById(challenge.getId()).orElseThrow().isConsumed());
    }

    @Test
    void replacementOtpRejectsPreviousCodeAndAcceptsNewCode() {
        jdbc.update("UPDATE phone_otp_challenges SET delivery_status = 'INVALIDATED' WHERE id = ?",
                challenge.getId());

        UUID replacementId = UUID.randomUUID();
        Instant now = Instant.now();
        String replacementCode = "654321";
        PhoneOtpChallenge replacement = new PhoneOtpChallenge(
                replacementId, user.getId(), PHONE,
                crypto.digest(replacementId, replacementCode), now, now.plusSeconds(300));
        replacement.markSent(now);
        challenges.saveAndFlush(replacement);

        assertEquals(PhoneOtpVerificationResult.INVALID_CODE,
                service.verifyLatest(user.getId(), CODE));
        assertEquals(1, challenges.findById(replacementId).orElseThrow().getAttemptCount());
        assertEquals(PhoneOtpVerificationResult.VERIFIED,
                service.verifyLatest(user.getId(), replacementCode));
        assertFalse(challenges.findById(challenge.getId()).orElseThrow().isConsumed());
        assertTrue(challenges.findById(replacementId).orElseThrow().isConsumed());
    }

    @Test
    void consumedNewestChallengeDoesNotFallBackToOlderUnconsumedChallenge() {
        jdbc.update("UPDATE phone_otp_challenges SET delivery_status = 'INVALIDATED' WHERE id = ?",
                challenge.getId());

        UUID newestId = UUID.randomUUID();
        Instant now = Instant.now();
        PhoneOtpChallenge newest = new PhoneOtpChallenge(
                newestId, user.getId(), PHONE,
                crypto.digest(newestId, "654321"), now, now.plusSeconds(300));
        newest.markSent(now);
        challenges.saveAndFlush(newest);

        assertEquals(PhoneOtpVerificationResult.VERIFIED,
                service.verifyLatest(user.getId(), "654321"));
        assertEquals(PhoneOtpVerificationResult.ALREADY_USED,
                service.verifyLatest(user.getId(), CODE));
        assertFalse(challenges.findById(challenge.getId()).orElseThrow().isConsumed());
        assertTrue(challenges.findById(newestId).orElseThrow().isConsumed());
    }
}
