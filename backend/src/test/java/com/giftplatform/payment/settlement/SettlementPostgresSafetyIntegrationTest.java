package com.giftplatform.payment.settlement;

import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.testcontainers.containers.PostgreSQLContainer;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

/** Real PostgreSQL tests for V33-V35 and the internal settlement services.
 * Runs against an isolated container, never against the developer database.
 */
class SettlementPostgresSafetyIntegrationTest {
    static PostgreSQLContainer<?> pg;
    JdbcTemplate jdbc;
    TransactionTemplate tx;
    UUID host, event, bank, request;
    PayoutReservationService reservations;
    PayoutSafetyFinalizationService safety;
    SettlementBalanceService balances;

    @BeforeAll static void start() {
        pg = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("gift_5cd_safety").withUsername("test").withPassword("test");
        pg.start();
    }
    @AfterAll static void stop() { if (pg != null) pg.stop(); }

    @BeforeEach void setup() {
        var ds = new DriverManagerDataSource(pg.getJdbcUrl(),pg.getUsername(),pg.getPassword());
        jdbc = new JdbcTemplate(ds);
        tx = new TransactionTemplate(new DataSourceTransactionManager(ds));
        // Dedicated schema per test; no reliance on other migrations or application data.
        jdbc.execute("DROP SCHEMA IF EXISTS public CASCADE; CREATE SCHEMA public");
        jdbc.execute("CREATE TABLE users(id uuid PRIMARY KEY)");
        jdbc.execute("CREATE TABLE events(id uuid PRIMARY KEY, owner_id uuid REFERENCES users(id))");
        jdbc.execute("CREATE TABLE gifts(id uuid PRIMARY KEY)");
        jdbc.execute("CREATE TABLE host_bank_accounts(id uuid PRIMARY KEY,event_id uuid,archived_at timestamptz, host_id uuid REFERENCES users(id), enabled boolean, verification_status varchar(30), preferred boolean, created_at timestamptz DEFAULT now())");
        jdbc.execute("CREATE TABLE event_payment_account_links(event_id uuid,host_id uuid,bank_account_id uuid,enabled boolean,preferred boolean)");
        jdbc.execute("CREATE TABLE wallet_ledger_entries(event_id uuid REFERENCES events(id), account_code varchar(40), amount_paise bigint, currency varchar(3))");
        for (int v = 33; v <= 35; v++) {
            final int version = v;
            var files = java.util.stream.Stream.of(Objects.requireNonNull(
                new java.io.File("src/main/resources/db/migration").listFiles()))
                .filter(f -> f.getName().startsWith("V" + version + "__") && f.getName().endsWith(".sql"))
                .toList();
            assertEquals(1,files.size(),"Migration missing or duplicated: V"+v);
            new ResourceDatabasePopulator(new FileSystemResource(files.get(0))).execute(ds);
        }
        host=UUID.randomUUID(); event=UUID.randomUUID(); bank=UUID.randomUUID(); request=UUID.randomUUID();
        jdbc.update("INSERT INTO users(id) VALUES (?)",host);
        jdbc.update("INSERT INTO events(id,owner_id) VALUES (?,?)",event,host);
        jdbc.update("INSERT INTO host_bank_accounts(id,host_id,enabled,verification_status,preferred) VALUES (?,?,true,'VERIFIED',true)",bank,host);
        jdbc.update("INSERT INTO event_payment_account_links(event_id,host_id,bank_account_id,enabled,preferred) VALUES(?,?,?,true,true)",event,host,bank);
        jdbc.update("INSERT INTO host_payout_requests(id,host_id,event_id,destination_id,amount_paise,idempotency_key) VALUES (?,?,?,?,?,?)",request,host,event,bank,600L,"case-1");
        balances=new SettlementBalanceService(jdbc);
        reservations=new PayoutReservationService(jdbc,balances);
        safety=new PayoutSafetyFinalizationService(jdbc);
    }
    void available(long amount) {
        jdbc.update("INSERT INTO wallet_ledger_entries(event_id,account_code,amount_paise,currency) VALUES (?,'HOST_AVAILABLE',?,'INR')",event,amount);
    }
    PayoutReservationService.Reservation reserve(UUID id) { return tx.execute(s -> reservations.reserve(id)); }
    PayoutSafetyFinalizationService.Cancellation cancel(UUID h,UUID id) { return tx.execute(s -> safety.cancel(h,id)); }

    @Test void pendingFundsAreNeverWithdrawable() {
        jdbc.update("INSERT INTO wallet_ledger_entries(event_id,account_code,amount_paise,currency) VALUES (?,'HOST_PENDING',1000,'INR')",event);
        assertEquals(0,balances.read(event).withdrawablePaise());
        assertThrows(Exception.class,()->reserve(request));
        assertEquals(0L,jdbc.queryForObject("SELECT COUNT(*) FROM payout_reservations",Long.class));
    }
    @Test void reservationRetryAndCancellationAreIdempotent() {
        available(1000);
        var first=reserve(request);
        var retry=reserve(request);
        assertEquals(first.id(),retry.id());
        assertTrue(retry.existing());
        assertEquals(400,balances.read(event).withdrawablePaise());
        assertEquals(1,cancel(host,request).releasedHolds());
        assertTrue(cancel(host,request).alreadyCancelled());
        assertEquals(1000,balances.read(event).withdrawablePaise());
        assertThrows(Exception.class,()->reserve(request));
        assertEquals(1L,jdbc.queryForObject("SELECT COUNT(*) FROM payout_reservations",Long.class));
    }
    @Test void differentHostCannotCancel() {
        available(1000); reserve(request);
        assertThrows(Exception.class,()->cancel(UUID.randomUUID(),request));
        assertEquals(600,balances.read(event).heldPaise());
    }
    @Test void observationsNeverChangeRequestOrReservation() {
        available(1000); reserve(request);
        tx.executeWithoutResult(s -> safety.recordInternalObservation(request,"SUCCEEDED"));
        assertEquals("BLOCKED",jdbc.queryForObject("SELECT state FROM host_payout_requests WHERE id=?",String.class,request));
        assertEquals("HELD",jdbc.queryForObject("SELECT state FROM payout_reservations WHERE payout_request_id=?",String.class,request));
        assertEquals(600,balances.read(event).heldPaise());
    }
    @Test void simultaneousReservationsCannotOverdraw() throws Exception {
        available(1000);
        UUID second=UUID.randomUUID();
        jdbc.update("INSERT INTO host_payout_requests(id,host_id,event_id,destination_id,amount_paise,idempotency_key) VALUES (?,?,?,?,?,?)",second,host,event,bank,600L,"case-2");
        var pool=Executors.newFixedThreadPool(2);
        var gate=new CountDownLatch(1);
        try {
            Callable<Boolean> a=()->{gate.await(); try {reserve(request); return true;} catch(Exception ex){return false;}};
            Callable<Boolean> b=()->{gate.await(); try {reserve(second); return true;} catch(Exception ex){return false;}};
            var f1=pool.submit(a); var f2=pool.submit(b); gate.countDown();
            int successes=(f1.get(20,TimeUnit.SECONDS)?1:0)+(f2.get(20,TimeUnit.SECONDS)?1:0);
            assertEquals(1,successes,"Only one of two 600-paise holds fits in 1000");
            assertEquals(600,balances.read(event).heldPaise());
            assertEquals(400,balances.read(event).withdrawablePaise());
        } finally { pool.shutdownNow(); }
    }
}
