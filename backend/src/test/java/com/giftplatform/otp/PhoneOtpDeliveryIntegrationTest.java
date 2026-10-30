
package com.giftplatform.otp;

import com.giftplatform.otp.entity.OtpDeliveryStatus;
import com.giftplatform.otp.repository.PhoneOtpChallengeRepository;
import com.giftplatform.otp.service.PhoneOtpDeliveryTransactionService;
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
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.List;
import java.util.concurrent.*;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class PhoneOtpDeliveryIntegrationTest {

    @Container
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
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
    private PhoneOtpIssuanceTransactionService issuance;

    @Autowired
    private PhoneOtpDeliveryTransactionService delivery;

    private UUID userId;
    private UUID challengeId;

    @BeforeEach
    void setup() {
        challenges.deleteAll();
        users.deleteAll();

        User user = users.saveAndFlush(
                new User(
                        "delivery_test",
                        "delivery.test@example.com",
                        "hashed-password"
                )
        );

        userId = user.getId();
        challengeId = UUID.randomUUID();

        var result = issuance.reserve(
                userId,
                "+919876543210",
                "123456",
                challengeId
        );

        assertEquals(
                PhoneOtpIssuanceResult.Status.RESERVED,
                result.status()
        );
    }

    @Test
    void acceptedDeliveryPersistsSentStatus() {
        assertTrue(delivery.markSent(userId, challengeId));

        var persisted = challenges.findById(challengeId)
                .orElseThrow();

        assertEquals(
                OtpDeliveryStatus.SENT,
                persisted.getDeliveryStatus()
        );

        assertNotNull(persisted.getDeliveryUpdatedAt());
    }

    @Test
    void failedDeliveryPersistsFailedStatus() {
        assertTrue(delivery.markFailed(userId, challengeId));

        assertEquals(
                OtpDeliveryStatus.FAILED,
                challenges.findById(challengeId)
                        .orElseThrow()
                        .getDeliveryStatus()
        );
    }

    @Test
    void duplicateSentCallbackIsRejected() {
        assertTrue(delivery.markSent(userId, challengeId));
        assertFalse(delivery.markSent(userId, challengeId));
    }

    @Test
    void failedChallengeCannotBeMarkedSentLater() {
        assertTrue(delivery.markFailed(userId, challengeId));
        assertFalse(delivery.markSent(userId, challengeId));

        assertEquals(
                OtpDeliveryStatus.FAILED,
                challenges.findById(challengeId)
                        .orElseThrow()
                        .getDeliveryStatus()
        );
    }

    @Test
    void sentChallengeCannotBeMarkedFailedLater() {
        assertTrue(delivery.markSent(userId, challengeId));
        assertFalse(delivery.markFailed(userId, challengeId));

        assertEquals(
                OtpDeliveryStatus.SENT,
                challenges.findById(challengeId)
                        .orElseThrow()
                        .getDeliveryStatus()
        );
    }

    @Test
    void anotherUserCannotFinalizeChallenge() {
        assertFalse(
                delivery.markSent(UUID.randomUUID(), challengeId)
        );

        assertEquals(
                OtpDeliveryStatus.PENDING,
                challenges.findById(challengeId)
                        .orElseThrow()
                        .getDeliveryStatus()
        );
    }

    @Test
    void unknownChallengeCannotBeFinalized() {
        assertFalse(
                delivery.markSent(userId, UUID.randomUUID())
        );
    }


    @Test
    void concurrentSentCallbacksOnlyOneSucceeds() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        Callable<Boolean> task = () -> {
            ready.countDown();

            if (!start.await(10, TimeUnit.SECONDS)) {
                throw new TimeoutException("Start timed out");
            }

            return delivery.markSent(userId, challengeId);
        };

        try {
            Future<Boolean> first = executor.submit(task);
            Future<Boolean> second = executor.submit(task);

            assertTrue(ready.await(10, TimeUnit.SECONDS));
            start.countDown();

            List<Boolean> results = List.of(
                    first.get(20, TimeUnit.SECONDS),
                    second.get(20, TimeUnit.SECONDS)
            );

            assertEquals(1, results.stream()
                    .filter(Boolean::booleanValue)
                    .count());

            assertEquals(
                    OtpDeliveryStatus.SENT,
                    challenges.findById(challengeId)
                            .orElseThrow()
                            .getDeliveryStatus()
            );

        } finally {
            start.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(
                    10, TimeUnit.SECONDS));
        }
    }

    @Test
    void concurrentSentAndFailedCallbacksCannotBothSucceed()
            throws Exception {

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        Callable<Boolean> sentTask = () -> {
            ready.countDown();

            if (!start.await(10, TimeUnit.SECONDS)) {
                throw new TimeoutException("Start timed out");
            }

            return delivery.markSent(userId, challengeId);
        };

        Callable<Boolean> failedTask = () -> {
            ready.countDown();

            if (!start.await(10, TimeUnit.SECONDS)) {
                throw new TimeoutException("Start timed out");
            }

            return delivery.markFailed(userId, challengeId);
        };

        try {
            Future<Boolean> sent = executor.submit(sentTask);
            Future<Boolean> failed = executor.submit(failedTask);

            assertTrue(ready.await(10, TimeUnit.SECONDS));
            start.countDown();

            boolean sentResult = sent.get(20, TimeUnit.SECONDS);
            boolean failedResult = failed.get(20, TimeUnit.SECONDS);

            assertNotEquals(sentResult, failedResult);

            OtpDeliveryStatus persisted =
                    challenges.findById(challengeId)
                            .orElseThrow()
                            .getDeliveryStatus();

            assertEquals(
                    sentResult
                            ? OtpDeliveryStatus.SENT
                            : OtpDeliveryStatus.FAILED,
                    persisted
            );

        } finally {
            start.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(
                    10, TimeUnit.SECONDS));
        }
    }

}
