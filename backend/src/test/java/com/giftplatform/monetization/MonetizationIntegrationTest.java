package com.giftplatform.monetization;

import com.giftplatform.security.ActorContext;
import com.giftplatform.payment.service.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.testcontainers.junit.jupiter.*;
import org.testcontainers.postgresql.PostgreSQLContainer;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(properties={"gift.otp.secret-base64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=","spring.jpa.hibernate.ddl-auto=validate","gift.payment.razorpay.key-id=rzp_test_fees"})
@Testcontainers
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
class MonetizationIntegrationTest {
    @Container static PostgreSQLContainer postgres=new PostgreSQLContainer("postgres:17");
    @DynamicPropertySource static void database(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url",postgres::getJdbcUrl);r.add("spring.datasource.username",postgres::getUsername);r.add("spring.datasource.password",postgres::getPassword);
    }
    @Autowired MonetizationService service;
    @Autowired GuestCheckoutService checkout;
    @Autowired PaymentNotificationService notifications;
    @Autowired org.springframework.test.web.servlet.MockMvc mvc;
    @MockitoBean com.giftplatform.payment.provider.RazorpayClient provider;
    @Autowired PaymentOrderService orders;
    @Autowired ContributionAllocationService allocations;
    @Autowired JdbcTemplate db;
    @MockitoBean ActorContext actor;
    UUID admin;
    @BeforeEach void setup() {
        admin=UUID.randomUUID();
        db.update("INSERT INTO users(id,email,password_hash,role) VALUES(?,?,?,'PLATFORM_ADMIN')",admin,admin+"@example.test","hash");
        when(actor.isPlatformAdmin()).thenReturn(true);when(actor.requireAuthenticatedUserId()).thenReturn(admin);
        var old=service.current();
        service.update(new MonetizationService.Update(old.revision(),false,old.streams().stream().map(s->new MonetizationService.Stream(s.code(),false,s.feeMode(),s.fixedPaise(),s.rateBps(),s.capPaise(),s.feeBearer(),s.collectionReady())).toList(),"Reset test settings"));
    }
    private MonetizationService.Settings enable(String bearer) {
        var old=service.current();
        return service.update(new MonetizationService.Update(old.revision(),true,old.streams().stream().map(s->s.code().equals("WALLET")?
                new MonetizationService.Stream(s.code(),true,"PERCENTAGE",0,200,0,bearer,true):s).toList(),"Enable wallet fee"));
    }
    private UUID event() {
        UUID event=UUID.randomUUID();
        db.update("INSERT INTO events(id,title,occasion_type,status,owner_id,host_display_name) VALUES(?,'Fee test','BIRTHDAY','PUBLISHED',?,'Host')",event,admin);
        return event;
    }
    @Test void adminOnlyVersionedUpdatesPreserveBeforeAndAfter() {
        var old=service.current();var saved=enable("GUEST");
        assertEquals(old.revision()+1,saved.revision());
        var rows=service.history(1,Long.MAX_VALUE);
        assertEquals("Enable wallet fee",rows.getFirst().get("reason"));assertNotNull(rows.getFirst().get("before_config"));assertNotNull(rows.getFirst().get("after_config"));
        assertEquals(409,assertThrows(ResponseStatusException.class,()->service.update(new MonetizationService.Update(old.revision(),false,old.streams(),"Stale"))).getStatusCode().value());
        when(actor.isPlatformAdmin()).thenReturn(false);
        assertEquals(403,assertThrows(ResponseStatusException.class,()->service.update(new MonetizationService.Update(saved.revision(),false,saved.streams(),"Unauthorized"))).getStatusCode().value());
        assertThrows(ResponseStatusException.class,()->service.history(1,Long.MAX_VALUE));
    }
    @Test void feesMustBeReviewedBeforeProviderCreation() {
        enable("GUEST");UUID event=event();String key=UUID.randomUUID().toString();
        assertEquals(409,assertThrows(ResponseStatusException.class,()->checkout.create(new GuestCheckoutService.Request(event,null,"EVENT_WALLET",100000,key,"Guest",null,null))).getStatusCode().value());
        verifyNoInteractions(provider);
        when(provider.createOrder(anyLong(),anyString())).thenAnswer(inv->new com.giftplatform.payment.provider.RazorpayClient.ProviderOrder("order_testFee",inv.getArgument(0),"INR","created"));
        var result=checkout.create(new GuestCheckoutService.Request(event,null,"EVENT_WALLET",100000,key,"Guest",null,null,2000L,102000L,100000L));
        assertEquals(102000,result.amountPaise());assertEquals(2000,result.platformFeePaise());
        assertEquals(102000,db.queryForObject("SELECT amount_paise FROM contributions WHERE payment_order_id=?",Long.class,result.localOrderId()));
        verify(provider).createOrder(eq(102000L),anyString());
    }
    @Test void publicQuotesRetainCsrfAndAdminRoutesRejectGuests() throws Exception {
        UUID event=event();
        String request="{\"eventId\":\""+event+"\",\"contributionPaise\":100000}";
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/public/payments/hybrid/quote")
                .contentType("application/json").content(request)).andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isForbidden());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/public/payments/hybrid/quote")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                .contentType("application/json").content(request)).andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/admin/console/monetization"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isUnauthorized());
    }
    @Test void pricingAndHistoryAreImmutableInDatabase() {
        enable("GUEST");var order=orders.create(event(),null,"EVENT_WALLET",100000,UUID.randomUUID().toString());
        assertThrows(org.springframework.dao.DataAccessException.class,()->db.update("UPDATE payment_orders SET platform_fee_paise=0 WHERE id=?",order.getId()));
        assertThrows(org.springframework.dao.DataAccessException.class,()->db.update("UPDATE platform_monetization_history SET reason='Changed' WHERE revision=?",order.getMonetizationRevision()));
    }
    @Test void giftNetSplitAndNotificationsExcludePlatformFee() {
        var current=service.current();
        service.update(new MonetizationService.Update(current.revision(),true,current.streams().stream().map(s->s.code().equals("CONTRIBUTION")?
                new MonetizationService.Stream(s.code(),true,"PERCENTAGE",0,200,0,"HOST",true):s).toList(),"Enable gift fees"));
        UUID event=event(),gift=UUID.randomUUID();
        db.update("INSERT INTO gifts(id,event_id,name,target_amount,visibility) VALUES(?,?,'Gift',500,'PUBLIC')",gift,event);
        var order=orders.create(event,gift,"GIFT_PARTIAL",100000,UUID.randomUUID().toString());
        db.update("UPDATE payment_orders SET status='CAPTURED' WHERE id=?",order.getId());
        UUID contribution=UUID.randomUUID();
        db.update("INSERT INTO contributions(id,payment_order_id,event_id,gift_id,guest_name,amount_paise) VALUES(?,?,?,?,'Guest',?)",contribution,order.getId(),event,gift,order.getAmountPaise());
        db.update("INSERT INTO payment_transactions(id,payment_order_id,provider_payment_id,amount_paise,currency,status,verified_at) VALUES(?,?,?,?,'INR','CAPTURED',now())",UUID.randomUUID(),order.getId(),"pay_"+UUID.randomUUID(),order.getAmountPaise());
        var result=allocations.allocate(contribution);
        assertEquals(50000,result.giftPaise());assertEquals(48000,result.walletPaise());
        notifications.recordVerifiedContribution(new RazorpayCaptureService.CaptureResult(order.getId(),contribution,"pay_test",result.giftPaise(),result.walletPaise(),false));
        assertEquals(2,db.queryForObject("SELECT count(*) FROM payment_notifications WHERE contribution_id=?",Integer.class,contribution));
        assertEquals(0,db.queryForObject("SELECT SUM(amount_paise) FROM wallet_ledger_entries WHERE contribution_id=?",Long.class,contribution));
    }
    @Test void invalidPurchasePricingAndMissingCodesCannotBeActivated() {
        var current=service.current();
        var unsupported=current.streams().stream().map(s->s.code().equals("HOST_PLAN")?
                new MonetizationService.Stream(s.code(),true,"PERCENTAGE",s.fixedPaise(),s.rateBps(),s.capPaise(),s.feeBearer(),true):s).toList();
        assertEquals(400,assertThrows(ResponseStatusException.class,()->service.update(new MonetizationService.Update(current.revision(),true,unsupported,"Attempt unsupported collection"))).getStatusCode().value());
        assertEquals(current.revision(),service.current().revision());
        assertThrows(ResponseStatusException.class,()->service.update(new MonetizationService.Update(current.revision(),true,List.of(),"Missing streams")));
    }
    @Test void snapshotSurvivesSwitchOffAndLedgerBalancesOnRetry() {
        for(String bearer:List.of("GUEST","HOST")) {
            enable(bearer);UUID event=event();String key=UUID.randomUUID().toString();
            var order=orders.create(event,null,"EVENT_WALLET",100000,key);
            assertEquals(2000,order.getPlatformFeePaise());
            assertEquals(bearer.equals("GUEST")?102000:100000,order.getAmountPaise());
            var current=service.current();service.update(new MonetizationService.Update(current.revision(),false,current.streams(),"Pause collection"));
            var retry=orders.create(event,null,"EVENT_WALLET",100000,key);
            assertEquals(order.getId(),retry.getId());assertEquals(order.getPlatformFeePaise(),retry.getPlatformFeePaise());
            db.update("UPDATE payment_orders SET status='CAPTURED' WHERE id=?",order.getId());
            UUID contribution=UUID.randomUUID();
            db.update("INSERT INTO contributions(id,payment_order_id,event_id,guest_name,amount_paise) VALUES(?,?,?,'Guest',?)",contribution,order.getId(),event,order.getAmountPaise());
            db.update("INSERT INTO payment_transactions(id,payment_order_id,provider_payment_id,amount_paise,currency,status,verified_at) VALUES(?,?,?,?,'INR','CAPTURED',now())",UUID.randomUUID(),order.getId(),"pay_"+UUID.randomUUID(),order.getAmountPaise());
            var result=allocations.allocate(contribution);
            assertEquals(order.getHostReceivesPaise(),result.walletPaise());
            assertEquals(0,db.queryForObject("SELECT SUM(amount_paise) FROM wallet_ledger_entries WHERE contribution_id=?",Long.class,contribution));
            assertEquals(2000,db.queryForObject("SELECT SUM(amount_paise) FROM wallet_ledger_entries WHERE contribution_id=? AND account_code='PLATFORM_REVENUE'",Long.class,contribution));
            assertTrue(allocations.allocate(contribution).alreadyProcessed());
            assertEquals(3,db.queryForObject("SELECT count(*) FROM wallet_ledger_entries WHERE contribution_id=?",Integer.class,contribution));
        }
    }
}
