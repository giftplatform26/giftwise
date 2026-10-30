package com.giftplatform.payment;

import com.giftplatform.payment.service.ContributionAllocationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.server.ResponseStatusException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
    "gift.otp.secret-base64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
    "spring.jpa.hibernate.ddl-auto=validate"
})
@Testcontainers
class ContributionAllocationServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17");

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired ContributionAllocationService service;
    @Autowired JdbcTemplate jdbc;

    private UUID event() {
        UUID user = UUID.randomUUID();
        UUID event = UUID.randomUUID();

        jdbc.update("""
            INSERT INTO users(id,email,password_hash,role)
            VALUES (?,?,?,?)
            """, user, user + "@example.test",
            "test-hash", "CUSTOMER");

        jdbc.update("""
            INSERT INTO events(id,title,occasion_type,status,owner_id)
            VALUES (?,?,?,?,?)
            """, event, "Allocation Test",
            "BIRTHDAY", "PUBLISHED", user);

        return event;
    }

    private UUID gift(UUID event) {
        UUID id = UUID.randomUUID();

        jdbc.update("""
            INSERT INTO gifts
                (id,event_id,name,target_amount,status,visibility)
            VALUES (?,?,?,?,?,?)
            """, id, event, "Test Gift",
            new BigDecimal("10000.00"), "ACTIVE", "PUBLIC");

        return id;
    }

    private UUID contribution(
            UUID event, UUID gift, long amount,
            boolean verified) {

        UUID order = UUID.randomUUID();
        UUID contribution = UUID.randomUUID();

        jdbc.update("""
            INSERT INTO payment_orders
                (id,event_id,gift_id,contribution_type,
                 amount_paise,idempotency_key,status)
            VALUES (?,?,?,?,?,?,?)
            """, order, event, gift,
            gift == null ? "EVENT_WALLET" : "GIFT_PARTIAL",
            amount, UUID.randomUUID().toString(),
            verified ? "CAPTURED" : "CREATED");

        jdbc.update("""
            INSERT INTO contributions
                (id,payment_order_id,event_id,gift_id,
                 guest_name,amount_paise,allocation_status)
            VALUES (?,?,?,?,?,?,'PENDING')
            """, contribution, order, event, gift,
            "Test Guest", amount);

        if (verified) {
            jdbc.update("""
                INSERT INTO payment_transactions
                    (id,payment_order_id,provider_payment_id,
                     amount_paise,currency,status,verified_at)
                VALUES (?,?,?,?,'INR','CAPTURED',CURRENT_TIMESTAMP)
                """, UUID.randomUUID(), order,
                "pay_" + UUID.randomUUID(), amount);
        }

        return contribution;
    }

    private long sum(UUID contribution) {
        Long result = jdbc.queryForObject("""
            SELECT COALESCE(SUM(amount_paise),0)
            FROM wallet_ledger_entries
            WHERE contribution_id = ?
            """, Long.class, contribution);
        return result;
    }

    private int count(String table, UUID contribution) {
        return jdbc.queryForObject(
            "SELECT COUNT(*) FROM " + table +
            " WHERE contribution_id = ?",
            Integer.class, contribution);
    }

    @Test
    void giftOnlyAllocationBalancesLedger() {
        UUID event = event();
        UUID gift = gift(event);
        UUID c = contribution(event, gift, 200000, true);

        var result = service.allocate(c);

        assertEquals(200000L, result.giftPaise());
        assertEquals(0L, result.walletPaise());
        assertEquals(0L, sum(c));
        assertEquals(2, count("wallet_ledger_entries", c));
        assertEquals(1, count("contribution_allocations", c));
        assertFalse(result.alreadyProcessed());
    }

    @Test
    void overfundingSplitsIntoPendingWallet() {
        UUID event = event();
        UUID gift = gift(event);

        service.allocate(contribution(event, gift, 700000, true));

        UUID second = contribution(event, gift, 400000, true);
        var result = service.allocate(second);

        assertEquals(300000L, result.giftPaise());
        assertEquals(100000L, result.walletPaise());
        assertEquals(0L, sum(second));
        assertEquals(3, count("wallet_ledger_entries", second));

        Long pending = jdbc.queryForObject("""
            SELECT SUM(amount_paise)
            FROM wallet_ledger_entries
            WHERE contribution_id = ?
              AND account_code = 'HOST_PENDING'
            """, Long.class, second);

        assertEquals(100000L, pending);
    }

    @Test
    void walletOnlyAllocationDoesNotReserveGiftFunds() {
        UUID event = event();
        UUID c = contribution(event, null, 50000, true);

        var result = service.allocate(c);

        assertEquals(0L, result.giftPaise());
        assertEquals(50000L, result.walletPaise());
        assertEquals(0L, sum(c));
    }

    @Test
    void duplicateProcessingReturnsOriginalAllocation() {
        UUID event = event();
        UUID gift = gift(event);
        UUID c = contribution(event, gift, 25000, true);

        var first = service.allocate(c);
        var second = service.allocate(c);

        assertEquals(first.entryGroupId(), second.entryGroupId());
        assertEquals(first.giftPaise(), second.giftPaise());
        assertTrue(second.alreadyProcessed());
        assertEquals(1, count("contribution_allocation_postings", c));
        assertEquals(2, count("wallet_ledger_entries", c));
    }

    @Test
    void unverifiedPaymentCannotAllocate() {
        UUID event = event();
        UUID gift = gift(event);
        UUID c = contribution(event, gift, 10000, false);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.allocate(c));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertEquals(0, count("contribution_allocation_postings", c));
        assertEquals(0, count("wallet_ledger_entries", c));
    }

    @Test
    void failedPostingRollsBackEverything() {
        UUID event = event();
        UUID gift = gift(event);
        UUID c = contribution(event, gift, 10000, true);

        // Force ledger insertion to fail without changing
        // the application's normal schema.
        jdbc.execute("""
            ALTER TABLE wallet_ledger_entries
            ADD CONSTRAINT test_reject_allocation
            CHECK (entry_type <> 'CONTRIBUTION_ALLOCATION')
            """);

        try {
            assertThrows(Exception.class, () -> service.allocate(c));

            assertEquals(0, count("contribution_allocation_postings", c));
            assertEquals(0, count("contribution_allocations", c));
            assertEquals(0, count("wallet_ledger_entries", c));

            String status = jdbc.queryForObject("""
                SELECT allocation_status
                FROM contributions WHERE id = ?
                """, String.class, c);

            assertEquals("PENDING", status);
        } finally {
            jdbc.execute("""
                ALTER TABLE wallet_ledger_entries
                DROP CONSTRAINT test_reject_allocation
                """);
        }
    }

    @Autowired com.giftplatform.payment.controller.PublicGiftProgressController progressController;

    @Test
    void provisionalProgressCappedAndNeverPostsLedger() {
        UUID e=event(); UUID g=gift(e);
        UUID token=jdbc.queryForObject("SELECT public_token FROM events WHERE id=?",UUID.class,e);
        UUID claim=UUID.randomUUID();
        jdbc.update("""
          INSERT INTO guest_direct_transfer_claims
          (id,event_id,gift_id,destination_id,method,amount_paise,guest_name,reference_hash,client_key)
          VALUES (?,?,?,?,'UPI',700000,'Guest A',?,?)
          """,claim,e,g,UUID.randomUUID(),"a".repeat(64),UUID.randomUUID().toString());
        jdbc.update("""
          INSERT INTO guest_manual_display_allocations
          (claim_id,event_id,gift_id,gift_paise,wallet_paise) VALUES (?,?,?,?,?)
          """,claim,e,g,700000L,0L);
        var before=progressController.progress(token).stream()
          .filter(x->g.equals(x.giftId())).findFirst().orElseThrow();
        assertEquals(700000L,before.contributedPaise());
        UUID paid=contribution(e,g,600000L,true);
        service.allocate(paid);
        var after=progressController.progress(token).stream()
          .filter(x->g.equals(x.giftId())).findFirst().orElseThrow();
        assertEquals(1000000L,after.contributedPaise());
        assertEquals(2L,after.contributionCount());
        assertEquals(0L,sum(paid));
        jdbc.update("UPDATE guest_direct_transfer_claims SET status='REJECTED' WHERE id=?",claim);
        var rejected=progressController.progress(token).stream()
          .filter(x->g.equals(x.giftId())).findFirst().orElseThrow();
        assertEquals(600000L,rejected.contributedPaise());
    }

    @Test
    void concurrentPaymentsCannotOverallocateGift() throws Exception {
        UUID event = event();
        UUID gift = gift(event);

        service.allocate(contribution(event, gift, 900000, true));

        UUID a = contribution(event, gift, 80000, true);
        UUID b = contribution(event, gift, 80000, true);

        ExecutorService pool = Executors.newFixedThreadPool(2);

        try {
            Future<?> first = pool.submit(() -> service.allocate(a));
            Future<?> second = pool.submit(() -> service.allocate(b));

            first.get(30, TimeUnit.SECONDS);
            second.get(30, TimeUnit.SECONDS);

            Long giftTotal = jdbc.queryForObject("""
                SELECT COALESCE(SUM(amount_paise),0)
                FROM contribution_allocations
                WHERE gift_id = ? AND allocation_type = 'GIFT'
                """, Long.class, gift);

            Long walletTotal = jdbc.queryForObject("""
                SELECT COALESCE(SUM(amount_paise),0)
                FROM contribution_allocations
                WHERE contribution_id IN (?,?)
                  AND allocation_type = 'EVENT_WALLET'
                """, Long.class, a, b);

            assertEquals(1000000L, giftTotal);
            assertEquals(60000L, walletTotal);
            assertEquals(0L, sum(a));
            assertEquals(0L, sum(b));
        } finally {
            pool.shutdownNow();
        }
    }
}
