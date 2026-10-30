
package com.giftplatform.otp.service;

import com.giftplatform.otp.entity.OtpDeliveryStatus;
import com.giftplatform.otp.entity.PhoneOtpChallenge;
import com.giftplatform.otp.repository.PhoneOtpChallengeRepository;
import com.giftplatform.otp.service.OtpCryptoService;
import com.giftplatform.otp.service.PhoneOtpIssuanceResult;
import com.giftplatform.otp.service.PhoneOtpIssuanceTransactionService;
import com.giftplatform.user.entity.User;
import com.giftplatform.user.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import com.giftplatform.otp.service.PhoneOtpDeliveryTransactionService;
@SpringBootTest
@Testcontainers
class PhoneOtpIssuanceIntegrationTest {

    private static final Instant BASE_TIME =
            Instant.parse("2026-10-12T06:00:00Z");

    private static final MutableClock TEST_CLOCK =
            new MutableClock(BASE_TIME);

    @Autowired
    private PhoneOtpDeliveryTransactionService delivery;

    @TestConfiguration
    static class ClockConfig {
        @Bean
        @Primary
        Clock testOtpClock() {
            return TEST_CLOCK;
        }
    }

    static class MutableClock extends Clock {

        private volatile Instant current;

        MutableClock(Instant initial) {
            this.current = initial;
        }

        void set(Instant instant) {
            this.current = instant;
        }

        @Override
        public Instant instant() {
            return current;
        }

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return Clock.fixed(current, zone);
        }
    }

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
    private PhoneOtpIssuanceTransactionService service;

    @Autowired
    private OtpCryptoService crypto;

    private User user;

    private static final String PHONE = "+919876543210";
    private static final String CODE = "123456";

    @BeforeEach
    void setup() {
        TEST_CLOCK.set(BASE_TIME);

        challenges.deleteAll();
        users.deleteAll();

        user = users.saveAndFlush(
                new User(
                        "otp_issuance",
                        "otp.issuance@example.com",
                        "hashed-password"
                )
        );
    }

    private PhoneOtpIssuanceResult reserve(UUID challengeId) {
        return service.reserve(
                user.getId(),
                PHONE,
                CODE,
                challengeId
        );
    }

    @Test
    void firstIssuanceCreatesPendingChallenge() {
        UUID id = UUID.randomUUID();

        var result = reserve(id);

        assertEquals(
                PhoneOtpIssuanceResult.Status.RESERVED,
                result.status()
        );

        var persisted = challenges.findById(id).orElseThrow();

        assertEquals(
                OtpDeliveryStatus.PENDING,
                persisted.getDeliveryStatus()
        );

        assertEquals(BASE_TIME, persisted.getCreatedAt());
        assertEquals(
                BASE_TIME.plusSeconds(300),
                persisted.getExpiresAt()
        );
        assertEquals(
                crypto.digest(id, CODE),
                persisted.getOtpDigest()
        );
    }

    @Test
    void immediateResendIsRejectedByCooldown() {
        UUID firstId = UUID.randomUUID();

        assertEquals(
                PhoneOtpIssuanceResult.Status.RESERVED,
                reserve(firstId).status()
        );

        var result = reserve(UUID.randomUUID());

        assertEquals(
                PhoneOtpIssuanceResult.Status.COOLDOWN,
                result.status()
        );

        assertEquals(60, result.retryAfterSeconds());
        assertEquals(1, challenges.count());
    }

    @Test
    void resendAfterCooldownInvalidatesPreviousChallenge() {
        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();

        assertEquals(
                PhoneOtpIssuanceResult.Status.RESERVED,
                reserve(firstId).status()
        );
        PhoneOtpChallenge firstChallenge =
                challenges.findById(firstId).orElseThrow();

        firstChallenge.markSent(
                BASE_TIME.plusSeconds(1)
        );

        challenges.saveAndFlush(firstChallenge);


        TEST_CLOCK.set(BASE_TIME.plusSeconds(60));

        assertEquals(
                PhoneOtpIssuanceResult.Status.RESERVED,
                reserve(secondId).status()
        );

        var previous =
                challenges.findById(firstId).orElseThrow();

        var replacement =
                challenges.findById(secondId).orElseThrow();

        assertEquals(
                OtpDeliveryStatus.INVALIDATED,
                previous.getDeliveryStatus()
        );

        assertEquals(
                OtpDeliveryStatus.PENDING,
                replacement.getDeliveryStatus()
        );
    }

    @Test
    void concurrentIssuanceAllowsOnlyOneReservation()
            throws Exception {

        ExecutorService executor =
                Executors.newFixedThreadPool(2);

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        Callable<PhoneOtpIssuanceResult> task = () -> {
            ready.countDown();

            if (!start.await(10, TimeUnit.SECONDS)) {
                throw new TimeoutException(
                        "Concurrent start timed out"
                );
            }

            return reserve(UUID.randomUUID());
        };

        try {
            Future<PhoneOtpIssuanceResult> first =
                    executor.submit(task);

            Future<PhoneOtpIssuanceResult> second =
                    executor.submit(task);

            assertTrue(
                    ready.await(10, TimeUnit.SECONDS)
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
                                    r.status() ==
                                            PhoneOtpIssuanceResult.Status.RESERVED)
                            .count()
            );

            assertEquals(
                    1,
                    results.stream()
                            .filter(r ->
                                    r.status() ==
                                            PhoneOtpIssuanceResult.Status.COOLDOWN)
                            .count()
            );

            assertEquals(1, challenges.count());

        } finally {
            start.countDown();
            executor.shutdownNow();
            assertTrue(
                    executor.awaitTermination(
                            10, TimeUnit.SECONDS)
            );
        }
    }

    @Test
    void duplicatePhoneIsRejected() {
        User other = users.saveAndFlush(
                new User(
                        "otp_other",
                        "otp.other@example.com",
                        "hashed-password"
                )
        );

        other.verifyPhoneNumber(
                PHONE,
                BASE_TIME.minusSeconds(60)
        );

        users.saveAndFlush(other);

        var result = reserve(UUID.randomUUID());

        assertEquals(
                PhoneOtpIssuanceResult.Status.PHONE_IN_USE,
                result.status()
        );

        assertEquals(0, challenges.count());
    }

    @Test
    void invalidPhoneDoesNotModifyDatabase() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.reserve(
                        user.getId(),
                        "9876543210",
                        CODE,
                        UUID.randomUUID()
                )
        );

        assertEquals(0, challenges.count());
    }

    @Test
    void consumedChallengeAllowsNewReservationAfterCooldown() {
        UUID firstId = UUID.randomUUID();

        assertEquals(
                PhoneOtpIssuanceResult.Status.RESERVED,
                reserve(firstId).status()
        );

        var previous =
                challenges.findById(firstId).orElseThrow();

        previous.markSent(
                BASE_TIME.plusSeconds(1)
        );
        previous.consume(
                BASE_TIME.plusSeconds(2)
        );

        challenges.saveAndFlush(previous);

        TEST_CLOCK.set(BASE_TIME.plusSeconds(60));

        UUID secondId = UUID.randomUUID();

        assertEquals(
                PhoneOtpIssuanceResult.Status.RESERVED,
                reserve(secondId).status()
        );

        assertEquals(
                OtpDeliveryStatus.PENDING,
                challenges.findById(secondId)
                        .orElseThrow()
                        .getDeliveryStatus()
        );
    }


    @Test
    void pendingDeliveryBlocksResendAfterCooldown() {
        UUID firstId = UUID.randomUUID();

        assertEquals(
                PhoneOtpIssuanceResult.Status.RESERVED,
                reserve(firstId).status()
        );

        TEST_CLOCK.set(BASE_TIME.plusSeconds(60));

        var result = reserve(UUID.randomUUID());

        assertEquals(
                PhoneOtpIssuanceResult.Status.DELIVERY_IN_PROGRESS,
                result.status()
        );

        assertEquals(1, challenges.count());

        assertEquals(
                OtpDeliveryStatus.PENDING,
                challenges.findById(firstId)
                        .orElseThrow()
                        .getDeliveryStatus()
        );
    }

    @Test
    void stalePendingChallengeCanBeReplaced() {
        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();

        assertEquals(
                PhoneOtpIssuanceResult.Status.RESERVED,
                reserve(firstId).status()
        );

        TEST_CLOCK.set(BASE_TIME.plusSeconds(120));

        var result = reserve(secondId);

        assertEquals(
                PhoneOtpIssuanceResult.Status.RESERVED,
                result.status()
        );

        assertEquals(
                OtpDeliveryStatus.INVALIDATED,
                challenges.findById(firstId)
                        .orElseThrow()
                        .getDeliveryStatus()
        );

        assertEquals(
                OtpDeliveryStatus.PENDING,
                challenges.findById(secondId)
                        .orElseThrow()
                        .getDeliveryStatus()
        );
    }

    @Test
    void lateDeliverySuccessCannotReactivateStaleChallenge() {
        UUID firstId = UUID.randomUUID();

        assertEquals(
                PhoneOtpIssuanceResult.Status.RESERVED,
                reserve(firstId).status()
        );

        TEST_CLOCK.set(BASE_TIME.plusSeconds(120));

        assertEquals(
                PhoneOtpIssuanceResult.Status.RESERVED,
                reserve(UUID.randomUUID()).status()
        );

        assertFalse(
                delivery.markSent(user.getId(), firstId)
        );

        assertEquals(
                OtpDeliveryStatus.INVALIDATED,
                challenges.findById(firstId)
                        .orElseThrow()
                        .getDeliveryStatus()
        );
    }
}
