package com.giftplatform.payment;

import com.giftplatform.payment.service.GiftFundingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.server.ResponseStatusException;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
    "gift.otp.secret-base64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
    "spring.jpa.hibernate.ddl-auto=validate"
})
@Testcontainers
class GiftFundingServiceIntegrationTest {

    @Container
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17");

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired GiftFundingService funding;
    @Autowired JdbcTemplate jdbc;

    private UUID createEvent() {
        UUID user = UUID.randomUUID();
        UUID event = UUID.randomUUID();

        jdbc.update(
            "INSERT INTO users(id,email,password_hash,role) VALUES (?,?,?,?)",
            user, user + "@example.test", "test-hash", "CUSTOMER");

        jdbc.update(
            """
            INSERT INTO events(id,title,occasion_type,status,owner_id,host_display_name)
            VALUES (?,?,?,?,?,'Fixture host')
            """,
            event, "Funding Test", "BIRTHDAY", "PUBLISHED", user);

        return event;
    }

    private UUID createGift(UUID event) {
        UUID gift = UUID.randomUUID();

        jdbc.update(
            """
            INSERT INTO gifts(id,event_id,name,target_amount,status,visibility)
            VALUES (?,?,?,?,?,?)
            """,
            gift, event, "Test Gift", new BigDecimal("10000.00"),
            "ACTIVE", "PUBLIC");

        return gift;
    }

    private void addContribution(
            UUID event, UUID gift, long amount, String allocation) {

        UUID order = UUID.randomUUID();

        jdbc.update(
            """
            INSERT INTO payment_orders(
                id,event_id,gift_id,contribution_type,
                amount_paise,idempotency_key,status
            ) VALUES (?,?,?,?,?,?,?)
            """,
            order, event, gift, "GIFT_PARTIAL",
            amount, UUID.randomUUID().toString(), "CAPTURED");

        jdbc.update(
            """
            INSERT INTO contributions(
                id,payment_order_id,event_id,gift_id,
                guest_name,amount_paise,allocation_status
            ) VALUES (?,?,?,?,?,?,?)
            """,
            UUID.randomUUID(), order, event, gift,
            "Test Guest", amount, allocation);
    }

    @Test
    void emptyGiftHasFullRemainingBalance() {
        UUID event = createEvent();
        UUID gift = createGift(event);

        var result = funding.getSummary(event, gift);

        assertEquals(1000000L, result.targetPaise());
        assertEquals(0L, result.fundedPaise());
        assertEquals(1000000L, result.remainingPaise());
    }

    @Test
    void partialFundingReducesRemainingBalance() {
        UUID event = createEvent();
        UUID gift = createGift(event);

        addContribution(event, gift, 250000, "GIFT_ALLOCATED");

        var result = funding.getSummary(event, gift);

        assertEquals(250000L, result.fundedPaise());
        assertEquals(750000L, result.remainingPaise());
    }

    @Test
    void fullyFundedGiftHasZeroRemaining() {
        UUID event = createEvent();
        UUID gift = createGift(event);

        addContribution(event, gift, 400000, "GIFT_ALLOCATED");
        addContribution(event, gift, 600000, "GIFT_ALLOCATED");

        var result = funding.getSummary(event, gift);

        assertEquals(1000000L, result.fundedPaise());
        assertEquals(0L, result.remainingPaise());
    }

    @Test
    void reallocatedContributionIsNotCounted() {
        UUID event = createEvent();
        UUID gift = createGift(event);

        addContribution(event, gift, 300000, "GIFT_ALLOCATED");
        addContribution(event, gift, 200000, "REALLOCATED");

        var result = funding.getSummary(event, gift);

        assertEquals(300000L, result.fundedPaise());
        assertEquals(700000L, result.remainingPaise());
    }

    @Test
    void crossEventGiftIsNotAccessible() {
        UUID eventA = createEvent();
        UUID eventB = createEvent();
        UUID giftB = createGift(eventB);

        ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () -> funding.getSummary(eventA, giftB));

        assertEquals(HttpStatus.NOT_FOUND.value(),
                ex.getStatusCode().value());
    }

    private UUID createSplitContribution(
            UUID event, UUID gift, long amount, String status) {

        UUID order = UUID.randomUUID();
        UUID contribution = UUID.randomUUID();

        jdbc.update("""
            INSERT INTO payment_orders(
                id,event_id,gift_id,contribution_type,
                amount_paise,idempotency_key,status
            ) VALUES (?,?,?,?,?,?,?)
            """,
            order, event, gift,
            gift == null ? "EVENT_WALLET" : "GIFT_PARTIAL",
            amount, UUID.randomUUID().toString(), "CAPTURED");

        jdbc.update("""
            INSERT INTO contributions(
                id,payment_order_id,event_id,gift_id,
                guest_name,amount_paise,allocation_status
            ) VALUES (?,?,?,?,?,?,?)
            """,
            contribution, order, event, gift,
            "Split Test Guest", amount, status);

        return contribution;
    }

    private void allocate(
            UUID contribution, UUID event, UUID gift,
            String type, long amount) {

        jdbc.update("""
            INSERT INTO contribution_allocations(
                id,contribution_id,event_id,gift_id,
                allocation_type,amount_paise
            ) VALUES (?,?,?,?,?,?)
            """,
            UUID.randomUUID(), contribution,
            event, gift, type, amount);
    }

    @Test
    void giftOnlySplitAllocationCountsTowardFunding() {
        UUID event = createEvent();
        UUID gift = createGift(event);

        UUID contribution = createSplitContribution(
                event, gift, 200000, "GIFT_ALLOCATED");

        allocate(contribution, event, gift, "GIFT", 200000);

        var result = funding.getSummary(event, gift);

        assertEquals(200000L, result.fundedPaise());
        assertEquals(800000L, result.remainingPaise());
    }

    @Test
    void giftAndWalletSplitCountsOnlyGiftPortion() {
        UUID event = createEvent();
        UUID gift = createGift(event);

        UUID contribution = createSplitContribution(
                event, gift, 400000, "REALLOCATED");

        allocate(contribution, event, gift, "GIFT", 300000);
        allocate(contribution, event, null, "EVENT_WALLET", 100000);

        var result = funding.getSummary(event, gift);

        assertEquals(300000L, result.fundedPaise());
        assertEquals(700000L, result.remainingPaise());
    }

    @Test
    void splitAllocationsCanFullyFundGift() {
        UUID event = createEvent();
        UUID gift = createGift(event);

        UUID first = createSplitContribution(
                event, gift, 600000, "GIFT_ALLOCATED");

        UUID second = createSplitContribution(
                event, gift, 500000, "REALLOCATED");

        allocate(first, event, gift, "GIFT", 600000);
        allocate(second, event, gift, "GIFT", 400000);
        allocate(second, event, null, "EVENT_WALLET", 100000);

        var result = funding.getSummary(event, gift);

        assertEquals(1000000L, result.fundedPaise());
        assertEquals(0L, result.remainingPaise());
    }

    @Test
    void legacyContributionWithoutSplitIsStillCounted() {
        UUID event = createEvent();
        UUID gift = createGift(event);

        addContribution(event, gift, 150000, "GIFT_ALLOCATED");

        var result = funding.getSummary(event, gift);

        assertEquals(150000L, result.fundedPaise());
        assertEquals(850000L, result.remainingPaise());
    }

    @Test
    void legacyStatusAndSplitRecordsAreNotDoubleCounted() {
        UUID event = createEvent();
        UUID gift = createGift(event);

        UUID contribution = createSplitContribution(
                event, gift, 400000, "GIFT_ALLOCATED");

        allocate(contribution, event, gift, "GIFT", 300000);
        allocate(contribution, event, null, "EVENT_WALLET", 100000);

        var result = funding.getSummary(event, gift);

        assertEquals(300000L, result.fundedPaise());
        assertEquals(700000L, result.remainingPaise());
    }

    @Test
    void walletOnlyAllocationDoesNotFundGift() {
        UUID event = createEvent();
        UUID gift = createGift(event);

        UUID contribution = createSplitContribution(
                event, null, 250000, "WALLET_ALLOCATED");

        allocate(contribution, event, null, "EVENT_WALLET", 250000);

        var result = funding.getSummary(event, gift);

        assertEquals(0L, result.fundedPaise());
        assertEquals(1000000L, result.remainingPaise());
    }

}
