package com.giftplatform.event;

import com.giftplatform.event.entity.Event;
import com.giftplatform.event.entity.OccasionType;
import com.giftplatform.event.service.EventPublishReadiness;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class EventAccountScopeIntegrationTest {
    @Container static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");
    static JdbcTemplate jdbc;
    @BeforeAll static void migrate() {
        var ds=new DriverManagerDataSource(postgres.getJdbcUrl(),postgres.getUsername(),postgres.getPassword());
        Flyway.configure().dataSource(ds).load().migrate();
        jdbc=new JdbcTemplate(ds);
    }
    private UUID host() {
        UUID id=UUID.randomUUID();
        jdbc.update("INSERT INTO users(id,email,password_hash,role) VALUES (?,?,?,'CUSTOMER')",id,id+"@example.test","test");
        return id;
    }
    private Event event(UUID host) {
        Event e=new Event("Test",OccasionType.BIRTHDAY); e.assignOwner(host);
        jdbc.update("INSERT INTO events(id,title,occasion_type,owner_id,host_display_name) VALUES (?,'Test','BIRTHDAY',?,'Host')",e.getId(),host);
        return e;
    }
    private UUID bank(UUID host,UUID event) {
        UUID id=UUID.randomUUID();
        jdbc.update("""
            INSERT INTO host_bank_accounts(id,host_id,event_id,account_number_encrypted,account_fingerprint,
              account_number_masked,ifsc_encrypted,ifsc_masked,account_holder_encrypted)
            VALUES (?,?,?,'cipher',?,'****1234','cipher','MASK','cipher')
            """,id,host,event,id.toString());
        if(event!=null)links(host).link(event,"BANK",id);
        return id;
    }
    private com.giftplatform.hostpayment.service.EventAccountLinkService links(UUID host){
        var actor=org.mockito.Mockito.mock(com.giftplatform.security.ActorContext.class);
        org.mockito.Mockito.when(actor.requireAuthenticatedUserId()).thenReturn(host);
        return new com.giftplatform.hostpayment.service.EventAccountLinkService(jdbc,actor,
            org.mockito.Mockito.mock(com.giftplatform.hostpayment.qr.UpiQrImageStorage.class),
            org.mockito.Mockito.mock(com.giftplatform.audit.service.AuditService.class));
    }
    private void prefer(UUID id) {
        jdbc.update("UPDATE host_bank_accounts SET host_confirmed_at=now(),preferred=TRUE WHERE id=?",id);
        jdbc.update("UPDATE event_payment_account_links SET preferred=TRUE WHERE bank_account_id=?",id);
    }
    @Test void eventAccountOnlyUnlocksItsOwnEventAndMustBeActiveAndPreferred() {
        UUID h=host(); Event first=event(h),second=event(h);
        var readiness=new EventPublishReadiness(jdbc);
        assertFalse(readiness.canPublish(first));
        UUID id=bank(h,first.getId());
        assertFalse(readiness.canPublish(first));
        prefer(id);
        assertTrue(readiness.canPublish(first));
        assertFalse(readiness.canPublish(second));
        jdbc.update("UPDATE host_bank_accounts SET enabled=FALSE,preferred=FALSE WHERE id=?",id);
        assertFalse(readiness.canPublish(first));
    }
    @Test void hostPreferenceNeverUnlocksAnEventWithoutItsOwnEnabledPreferredLink() {
        UUID h=host(); Event first=event(h),second=event(h),foreign=event(host());
        UUID id=bank(h,null); prefer(id);
        var readiness=new EventPublishReadiness(jdbc);
        assertFalse(readiness.canPublish(first)); assertFalse(readiness.canPublish(second));
        var links=links(h);links.link(first.getId(),"BANK",id);
        assertFalse(readiness.canPublish(first)); // Host preference is not inherited by a link.
        assertEquals(id,links.banks(first.getId()).getFirst().id());
        assertFalse(links.banks(first.getId()).getFirst().preferred());
        links.update(first.getId(),"BANK",id,"preferred");
        assertTrue(readiness.canPublish(first));assertFalse(readiness.canPublish(second));assertFalse(readiness.canPublish(foreign));
        links.link(second.getId(),"BANK",id);links.update(second.getId(),"BANK",id,"preferred");
        assertTrue(readiness.canPublish(second));
        links.update(first.getId(),"BANK",id,"disable");
        assertFalse(readiness.canPublish(first));assertTrue(readiness.canPublish(second));
        links.update(second.getId(),"BANK",id,"unlink");assertFalse(readiness.canPublish(second));
        assertTrue(jdbc.queryForObject("SELECT enabled AND preferred FROM host_bank_accounts WHERE id=?",Boolean.class,id));
        assertThrows(org.springframework.web.server.ResponseStatusException.class,()->links.link(foreign.getId(),"BANK",id));
    }
    @Test void guestChoicesOnlyContainTheEventsPreferredLinkedAccounts() {
        UUID h=host();Event first=event(h),second=event(h);UUID id=bank(h,null);prefer(id);
        jdbc.update("UPDATE events SET status='PUBLISHED' WHERE id IN (?,?)",first.getId(),second.getId());
        UUID token=jdbc.queryForObject("SELECT public_token FROM events WHERE id=?",UUID.class,first.getId());
        UUID secondToken=jdbc.queryForObject("SELECT public_token FROM events WHERE id=?",UUID.class,second.getId());
        var guest=new com.giftplatform.payment.direct.GuestDirectTransferService(jdbc,true,false);
        assertTrue(guest.destinations(token).isEmpty());
        var links=links(h);links.link(first.getId(),"BANK",id);assertTrue(guest.destinations(token).isEmpty());
        links.update(first.getId(),"BANK",id,"preferred");
        assertEquals(id,guest.destinations(token).getFirst().id());assertTrue(guest.destinations(secondToken).isEmpty());
        links.update(first.getId(),"BANK",id,"unlink");assertTrue(guest.destinations(token).isEmpty());
    }
    @Test void upiRequiresExplicitPreferenceAndQrAndHostDisableClearsEventPreference() {
        UUID h=host();Event e=event(h);UUID id=UUID.randomUUID();
        jdbc.update("INSERT INTO host_upi_accounts(id,host_id,upi_id_encrypted,upi_id_fingerprint,upi_id_masked,host_confirmed_at,preferred) VALUES(?,?,'cipher',?,'j***@bank',now(),true)",id,h,id.toString());
        var readiness=new EventPublishReadiness(jdbc);assertFalse(readiness.canPublish(e));
        var actor=org.mockito.Mockito.mock(com.giftplatform.security.ActorContext.class);org.mockito.Mockito.when(actor.requireAuthenticatedUserId()).thenReturn(h);
        var qr=org.mockito.Mockito.mock(com.giftplatform.hostpayment.qr.UpiQrImageStorage.class);
        var links=new com.giftplatform.hostpayment.service.EventAccountLinkService(jdbc,actor,qr,org.mockito.Mockito.mock(com.giftplatform.audit.service.AuditService.class));
        links.link(e.getId(),"UPI",id);assertFalse(readiness.canPublish(e));
        assertThrows(org.springframework.web.server.ResponseStatusException.class,()->links.update(e.getId(),"UPI",id,"preferred"));
        org.mockito.Mockito.when(qr.hasValidQr(h,id)).thenReturn(true);
        jdbc.update("INSERT INTO host_upi_qr_images(host_id,upi_account_id,payload_sha256,source) VALUES(?,?,?,'GENERATED')",h,id,"a".repeat(64));
        links.update(e.getId(),"UPI",id,"preferred");assertTrue(readiness.canPublish(e));
        jdbc.update("UPDATE host_upi_accounts SET enabled=false,preferred=false WHERE id=?",id);assertFalse(readiness.canPublish(e));
        jdbc.update("UPDATE host_upi_accounts SET enabled=true WHERE id=?",id);assertFalse(readiness.canPublish(e));
        assertFalse(links.upis(e.getId()).getFirst().preferred());
    }
    @Test void removingEventOnlyAccountArchivesItWithoutAffectingSharedHostAccounts() {
        UUID h=host();Event e=event(h);UUID shared=bank(h,null),local=bank(h,e.getId());prefer(local);
        links(h).update(e.getId(),"BANK",local,"unlink");
        assertFalse(new EventPublishReadiness(jdbc).canPublish(e));assertTrue(links(h).banks(e.getId()).isEmpty());
        assertTrue(jdbc.queryForObject("SELECT archived_at IS NOT NULL FROM host_bank_accounts WHERE id=?",Boolean.class,local));
        assertTrue(jdbc.queryForObject("SELECT archived_at IS NULL AND enabled FROM host_bank_accounts WHERE id=?",Boolean.class,shared));
    }
    @Test void databaseRejectsCrossHostAssignment() {
        UUID h=host(); Event foreign=event(host());
        assertThrows(org.springframework.dao.DataIntegrityViolationException.class,()->bank(h,foreign.getId()));
    }

    @Test void sandboxResultDoesNotPromoteAccountButProductionEvidenceDoes() {
        UUID h=host(); UUID id=bank(h,null);
        byte[] encryption=new byte[32],fingerprint=new byte[32];java.util.Arrays.fill(fingerprint,(byte)1);
        var protector=new com.giftplatform.hostpayment.security.PaymentDataProtector(encryption,fingerprint);
        jdbc.update("UPDATE host_bank_accounts SET account_number_encrypted=?,ifsc_encrypted=?,account_holder_encrypted=? WHERE id=?",
            protector.encrypt(h,id,"account_number","1234567890"),protector.encrypt(h,id,"ifsc","HDFC0001234"),protector.encrypt(h,id,"account_holder","Jane Doe"),id);
        var provider=org.mockito.Mockito.mock(com.giftplatform.hostpayment.verification.account.AccountVerificationClient.class);
        var actor=org.mockito.Mockito.mock(com.giftplatform.security.ActorContext.class);
        var audit=org.mockito.Mockito.mock(com.giftplatform.audit.service.AuditService.class);
        org.mockito.Mockito.when(actor.requireAuthenticatedUserId()).thenReturn(h);
        org.mockito.Mockito.when(provider.available()).thenReturn(true);
        org.mockito.Mockito.when(provider.environment()).thenReturn("SANDBOX");
        var json=new tools.jackson.databind.ObjectMapper().readTree("{\"reference_id\":123,\"account_status\":\"VALID\",\"name_at_bank\":\"Jane Doe\",\"name_match_result\":\"DIRECT_MATCH\"}");
        org.mockito.Mockito.when(provider.verify(org.mockito.ArgumentMatchers.eq("BANK"),org.mockito.ArgumentMatchers.anyMap())).thenReturn(json);
        var tx=new org.springframework.transaction.support.TransactionTemplate(new org.springframework.jdbc.datasource.DataSourceTransactionManager(jdbc.getDataSource()));
        var api=new com.giftplatform.hostpayment.verification.account.AccountVerificationController(jdbc,actor,protector,tx,provider,audit);
        var input=new com.giftplatform.hostpayment.verification.account.AccountVerificationController.Request(null,true);
        assertEquals("SANDBOX_VALID",api.verify("bank",id,input).getBody().outcome());
        assertEquals("PENDING",jdbc.queryForObject("SELECT verification_status FROM host_bank_accounts WHERE id=?",String.class,id));
        org.mockito.Mockito.verifyNoInteractions(audit);
        org.mockito.Mockito.when(provider.environment()).thenReturn("PRODUCTION");
        assertEquals("VERIFIED",api.verify("bank",id,input).getBody().outcome());
        assertEquals("VERIFIED",jdbc.queryForObject("SELECT verification_status FROM host_bank_accounts WHERE id=?",String.class,id));
        assertEquals(2,api.history("bank",id).getBody().size());
        org.mockito.Mockito.when(actor.requireAuthenticatedUserId()).thenReturn(host());
        assertThrows(org.springframework.web.server.ResponseStatusException.class,()->api.history("bank",id));
    }

    @Test void transactionDetailsKeepGuestReportsSeparateFromProviderPayments() {
        UUID h=host();Event event=event(h);UUID claim=UUID.randomUUID(),order=UUID.randomUUID();
        jdbc.update("INSERT INTO guest_direct_transfer_claims(id,event_id,destination_id,method,amount_paise,guest_name,reference_hash,client_key) VALUES(?,?,?,'BANK',500,'Guest',?,?)",
            claim,event.getId(),UUID.randomUUID(),claim.toString().replace("-","").repeat(2),claim.toString());
        jdbc.update("INSERT INTO payment_orders(id,event_id,contribution_type,amount_paise,idempotency_key) VALUES(?,?,'EVENT_WALLET',1000,?)",order,event.getId(),order.toString());
        var actor=org.mockito.Mockito.mock(com.giftplatform.security.ActorContext.class);
        org.mockito.Mockito.when(actor.requireAuthenticatedUserId()).thenReturn(h);
        var capture=org.mockito.Mockito.mock(com.giftplatform.payment.service.RazorpayCaptureService.class);
        var api=new com.giftplatform.payment.verification.HostTransactionVerificationController(jdbc,actor,capture);
        var rows=api.list(event.getId(),20,0).getBody();
        assertEquals(2,rows.size());
        var direct=rows.stream().filter(r->r.id().equals(claim)).findFirst().orElseThrow();
        assertEquals("DIRECT_TRANSFER",direct.source());assertEquals("AWAITING_VERIFICATION",direct.status());assertNull(direct.verifiedAt());
        assertThrows(org.springframework.web.server.ResponseStatusException.class,()->api.verify(event.getId(),order));
        org.mockito.Mockito.verifyNoInteractions(capture);
        org.mockito.Mockito.when(actor.requireAuthenticatedUserId()).thenReturn(host());
        assertThrows(org.springframework.web.server.ResponseStatusException.class,()->api.list(event.getId(),20,0));
    }
}
