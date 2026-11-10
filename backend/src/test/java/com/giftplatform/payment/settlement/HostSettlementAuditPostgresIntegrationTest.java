package com.giftplatform.payment.settlement;

import com.giftplatform.audit.service.AuditService;
import com.giftplatform.security.ActorContext;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.testcontainers.containers.PostgreSQLContainer;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Isolated real PostgreSQL verification, including atomic audit writes. */
class HostSettlementAuditPostgresIntegrationTest {
    static PostgreSQLContainer<?> pg;
    JdbcTemplate jdbc; TransactionTemplate tx; HostSettlementUiService ui;
    UUID host,event,bank;
    @BeforeAll static void start(){pg=new PostgreSQLContainer<>("postgres:17-alpine")
        .withDatabaseName("gift_5ef_audit").withUsername("test").withPassword("test");pg.start();}
    @AfterAll static void stop(){if(pg!=null)pg.stop();}
    @BeforeEach void setup(){
        var ds=new DriverManagerDataSource(pg.getJdbcUrl(),pg.getUsername(),pg.getPassword());
        jdbc=new JdbcTemplate(ds); tx=new TransactionTemplate(new DataSourceTransactionManager(ds));
        jdbc.execute("DROP SCHEMA IF EXISTS public CASCADE; CREATE SCHEMA public");
        jdbc.execute("CREATE TABLE users(id uuid PRIMARY KEY)");
        jdbc.execute("CREATE TABLE events(id uuid PRIMARY KEY,owner_id uuid REFERENCES users(id))");
        jdbc.execute("CREATE TABLE gifts(id uuid PRIMARY KEY)");
        jdbc.execute("CREATE TABLE host_bank_accounts(id uuid PRIMARY KEY,event_id uuid,archived_at timestamptz,host_id uuid REFERENCES users(id),enabled boolean,verification_status varchar(30),preferred boolean,created_at timestamptz DEFAULT now(),account_number_masked varchar(32),ifsc_masked varchar(20))");
        jdbc.execute("CREATE TABLE event_payment_account_links(event_id uuid,host_id uuid,bank_account_id uuid,enabled boolean,preferred boolean)");
        jdbc.execute("CREATE TABLE wallet_ledger_entries(event_id uuid REFERENCES events(id),account_code varchar(40),amount_paise bigint,currency varchar(3))");
        jdbc.execute("CREATE TABLE audit_logs(entity_type varchar(100),entity_id uuid,action varchar(30),actor_id uuid)");
        for(int v=33;v<=35;v++){
            int n=v;
            var files=java.util.stream.Stream.of(Objects.requireNonNull(new java.io.File("src/main/resources/db/migration").listFiles()))
                .filter(f->f.getName().startsWith("V"+n+"__")&&f.getName().endsWith(".sql")).toList();
            assertEquals(1,files.size(),"Missing migration V"+v);
            new ResourceDatabasePopulator(new FileSystemResource(files.get(0))).execute(ds);
        }
        host=UUID.randomUUID();event=UUID.randomUUID();bank=UUID.randomUUID();
        jdbc.update("INSERT INTO users(id) VALUES (?)",host);
        jdbc.update("INSERT INTO events(id,owner_id) VALUES (?,?)",event,host);
        jdbc.update("INSERT INTO host_bank_accounts(id,host_id,enabled,verification_status,preferred,account_number_masked,ifsc_masked) VALUES (?,?,true,'VERIFIED',true,'****1234','ABCD*******')",bank,host);
        jdbc.update("INSERT INTO event_payment_account_links(event_id,host_id,bank_account_id,enabled,preferred) VALUES(?,?,?,true,true)",event,host,bank);
        ActorContext actor=mock(ActorContext.class);
        when(actor.requireAuthenticatedUserId()).thenReturn(host);
        AuditService audit=mock(AuditService.class);
        doAnswer(inv->{jdbc.update("INSERT INTO audit_logs(entity_type,entity_id,action,actor_id) VALUES (?,?,?,?)",
            inv.getArgument(2,String.class),inv.getArgument(3,UUID.class),inv.getArgument(4).toString(),inv.getArgument(0,UUID.class));return null;})
            .when(audit).recordChange(any(),any(),anyString(),any(),any(),org.mockito.ArgumentMatchers.nullable(java.util.Map.class),anyMap(),org.mockito.ArgumentMatchers.nullable(String.class));
        ui=new HostSettlementUiService(actor,jdbc,new SettlementBalanceService(jdbc),new HostPayoutRequestService(jdbc),
            new PayoutSafetyFinalizationService(jdbc),audit);
    }
    long count(String table){return jdbc.queryForObject("SELECT COUNT(*) FROM "+table,Long.class);}
    @Test void firstRequestAuditedReplayNotDuplicated(){
        var first=tx.execute(s->ui.request(event,bank,600,"same"));
        var again=tx.execute(s->ui.request(event,bank,600,"same"));
        assertEquals(first.requestId(),again.requestId());assertEquals("BLOCKED",first.state());
        assertEquals(1,count("host_payout_requests"));assertEquals(1,count("audit_logs"));
        assertEquals(0,count("payout_reservations"));
    }
    @Test void cancellationAuditedOnceAndRemainsBlocked(){
        var first=tx.execute(s->ui.request(event,bank,600,"cancel"));
        assertFalse(tx.execute(s->ui.cancel(event,first.requestId())).alreadyCancelled());
        assertTrue(tx.execute(s->ui.cancel(event,first.requestId())).alreadyCancelled());
        assertEquals(2,count("audit_logs"));
        assertEquals("BLOCKED",jdbc.queryForObject("SELECT state FROM host_payout_requests WHERE id=?",String.class,first.requestId()));
    }
    @Test void crossHostEventNeverExposesBalanceOrCreatesRequest(){
        UUID foreign=UUID.randomUUID();jdbc.update("INSERT INTO users(id) VALUES (?)",foreign);
        jdbc.update("UPDATE events SET owner_id=? WHERE id=?",foreign,event);
        assertEquals(HttpStatus.NOT_FOUND,assertThrows(ResponseStatusException.class,()->ui.wallet(event)).getStatusCode());
        assertThrows(ResponseStatusException.class,()->tx.execute(s->ui.request(event,bank,600,"foreign")));
        assertEquals(0,count("host_payout_requests"));assertEquals(0,count("audit_logs"));
    }
    @Test void pendingAndUnverifiedBankCannotCreatePayout(){
        jdbc.update("UPDATE host_bank_accounts SET verification_status='PENDING' WHERE id=?",bank);
        assertThrows(ResponseStatusException.class,()->tx.execute(s->ui.request(event,bank,600,"pending")));
        assertEquals(0,count("host_payout_requests"));
        jdbc.update("UPDATE host_bank_accounts SET verification_status='VERIFIED',enabled=false WHERE id=?",bank);
        assertThrows(ResponseStatusException.class,()->tx.execute(s->ui.request(event,bank,600,"disabled")));
        assertEquals(0,count("host_payout_requests"));
    }
    @Test void pendingWalletBalanceIsNeverWithdrawable(){
        jdbc.update("INSERT INTO wallet_ledger_entries(event_id,account_code,amount_paise,currency) VALUES (?,'HOST_PENDING',5000,'INR')",event);
        assertEquals(0,ui.wallet(event).withdrawablePaise());
        assertFalse(ui.wallet(event).livePayoutsEnabled());
    }
    @Test void payoutCreationAndAuditRollbackTogether(){
        // Force audit failure after request insert. Both must roll back.
        jdbc.execute("DROP TABLE audit_logs");
        assertThrows(Exception.class,()->tx.execute(s->ui.request(event,bank,600,"rollback")));
        assertEquals(0,count("host_payout_requests"));
    }
    @Test void duplicateKeyWithDifferentAmountRejected(){
        tx.execute(s->ui.request(event,bank,600,"conflict"));
        assertEquals(HttpStatus.CONFLICT,assertThrows(ResponseStatusException.class,
            ()->tx.execute(s->ui.request(event,bank,700,"conflict"))).getStatusCode());
        assertEquals(1,count("host_payout_requests"));assertEquals(1,count("audit_logs"));
    }
}
