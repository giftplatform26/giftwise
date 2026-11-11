package com.giftplatform.payment.control;

import com.giftplatform.security.ActorContext;
import com.giftplatform.payment.provider.RazorpayClient;
import com.giftplatform.payment.service.*;
import com.giftplatform.payment.direct.GuestDirectTransferService;
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

@SpringBootTest(properties={"gift.otp.secret-base64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=","spring.jpa.hibernate.ddl-auto=validate","gift.payment.razorpay.key-id=rzp_test_controls","gift.payment.direct-transfers.enabled=true"})
@Testcontainers
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
class PaymentControlsIntegrationTest {
    @Container static PostgreSQLContainer postgres=new PostgreSQLContainer("postgres:17");
    @DynamicPropertySource static void database(DynamicPropertyRegistry r){r.add("spring.datasource.url",postgres::getJdbcUrl);r.add("spring.datasource.username",postgres::getUsername);r.add("spring.datasource.password",postgres::getPassword);}
    @Autowired PaymentControlService controls;
    @Autowired RoutingCommissionService commission;
    @Autowired PaymentOrderService orders;
    @Autowired RazorpayOrderCreationService providerOrders;
    @Autowired GuestCheckoutService checkout;
    @Autowired RazorpayCaptureService capture;
    @Autowired GuestDirectTransferService direct;
    @Autowired JdbcTemplate db;
    @Autowired org.springframework.test.web.servlet.MockMvc mvc;
    @MockitoBean ActorContext actor;
    @MockitoBean RazorpayClient provider;
    UUID host,event,token;
    @BeforeEach void setup(){
        host=UUID.randomUUID();event=UUID.randomUUID();
        db.update("INSERT INTO users(id,email,password_hash,role) VALUES(?,?,?,'CUSTOMER')",host,host+"@example.test","hash");
        db.update("INSERT INTO events(id,title,occasion_type,status,owner_id,host_display_name) VALUES(?,'Payment controls','BIRTHDAY','PUBLISHED',?,'Host')",event,host);
        token=db.queryForObject("SELECT public_token FROM events WHERE id=?",UUID.class,event);
        when(actor.isPlatformAdmin()).thenReturn(true);when(actor.requireAuthenticatedUserId()).thenReturn(host);
        for(String channel:PaymentControlService.CHANNELS)change("PLATFORM",null,channel,!channel.equals("ROUTING"));
        commission.update(new RoutingCommissionService.Update(commission.current().revision(),100,"Reset test routing commission"));
        when(provider.createOrder(anyLong(),anyString())).thenAnswer(inv->new RazorpayClient.ProviderOrder("order_"+UUID.randomUUID().toString().replace("-",""),inv.getArgument(0),"INR","created"));
    }
    void change(String scope,UUID id,String channel,Boolean enabled){long revision=controls.read(scope,id).revision();controls.change(scope,id,channel,new PaymentControlService.Change(revision,enabled,"Test control update"));}
    @Test void parentDisableAlwaysWinsAndRemovingOverrideRestoresInheritance(){
        assertTrue(controls.event(event).get("UPI"));assertFalse(controls.event(event).get("ROUTING"));
        change("HOST",host,"UPI",false);change("EVENT",event,"UPI",true);assertFalse(controls.event(event).get("UPI"));
        change("HOST",host,"UPI",null);assertTrue(controls.event(event).get("UPI"));
        change("PLATFORM",null,"UPI",false);assertFalse(controls.event(event).get("UPI"));
        change("PLATFORM",null,"UPI",true);change("EVENT",event,"UPI",false);assertFalse(controls.event(event).get("UPI"));
        change("EVENT",event,"UPI",null);assertTrue(controls.event(event).get("UPI"));
    }
    @Test void disabledRazorpayBlocksNewOrdersButExistingOrderCanStillReconcile(){
        String key=UUID.randomUUID().toString();var old=orders.create(event,null,"EVENT_WALLET",10000,key);
        change("HOST",host,"RAZORPAY",false);
        assertEquals(old.getId(),orders.create(event,null,"EVENT_WALLET",10000,key).getId());
        assertEquals(409,assertThrows(ResponseStatusException.class,()->orders.create(event,null,"EVENT_WALLET",10000,UUID.randomUUID().toString())).getStatusCode().value());
        assertEquals(409,assertThrows(ResponseStatusException.class,()->providerOrders.create(event,null,"EVENT_WALLET",10000,key)).getStatusCode().value());
        verifyNoInteractions(provider);
    }
    @Test void manualUpiAndBankReportsAreRejectedBeforeDestinationLookupWhenDisabled(){
        for(String method:List.of("UPI","BANK")){
            change("EVENT",event,method,false);
            // UPI references are 12 digits; use a well-formed one so the disabled-channel check is what rejects it.
            var request=new GuestDirectTransferService.ClaimRequest(UUID.randomUUID(),method,null,10000,"Guest",null,null,method.equals("UPI")?"123456789012":"REF123456789",UUID.randomUUID().toString());
            var error=assertThrows(ResponseStatusException.class,()->direct.claim(token,request));
            assertEquals(409,error.getStatusCode().value());assertTrue(error.getReason().contains(method));
        }
    }
    @Test void routingRemainsDisabledAtEveryScopeAndNormalRazorpayOrdersStillWork(){
        for(String scope:List.of("PLATFORM","HOST","EVENT")) {
            UUID id=scope.equals("HOST")?host:scope.equals("EVENT")?event:null;
            assertEquals(409,assertThrows(ResponseStatusException.class,()->change(scope,id,"ROUTING",true)).getStatusCode().value());
        }
        assertFalse(controls.event(event).get("ROUTING"));
        assertTrue(controls.event(event).get("RAZORPAY"));
        assertNotNull(orders.create(event,null,"EVENT_WALLET",10000,UUID.randomUUID().toString()));
        verifyNoInteractions(provider);
    }
    @Test void routingCommissionStartsAtOnePercentAndDoesNotChargeNormalCheckout(){
        assertEquals(100,commission.current().rateBps());assertFalse(commission.current().routingActive());
        var initial=commission.history(100,Long.MAX_VALUE).stream().filter(h->((Number)h.get("revision")).longValue()==0).findFirst().orElseThrow();
        assertEquals(100,initial.get("after_rate_bps"));assertNull(initial.get("actor_id"));
        commission.update(new RoutingCommissionService.Update(commission.current().revision(),250,"Change future routing share"));
        var order=orders.create(event,null,"EVENT_WALLET",100000,UUID.randomUUID().toString());
        assertEquals(100000,order.getAmountPaise());assertEquals(0,order.getPlatformFeePaise());
        assertFalse(controls.event(event).get("ROUTING"));
    }
    @Test void routingCommissionUpdatesHaveImmutableHistoryAndRejectStaleEditors(){
        var old=commission.current();var saved=commission.update(new RoutingCommissionService.Update(old.revision(),205,"Adjust routing commission"));
        assertEquals(old.revision()+1,saved.revision());assertEquals(205,saved.rateBps());
        var entry=commission.history(1,Long.MAX_VALUE).getFirst();
        assertEquals(100,entry.get("before_rate_bps"));assertEquals(205,entry.get("after_rate_bps"));assertEquals(host,entry.get("actor_id"));assertNotNull(entry.get("created_at"));
        assertEquals(409,assertThrows(ResponseStatusException.class,()->commission.update(new RoutingCommissionService.Update(old.revision(),300,"Stale rate edit"))).getStatusCode().value());
        assertThrows(org.springframework.dao.DataAccessException.class,()->db.update("UPDATE routing_commission_history SET reason='Changed' WHERE revision=?",saved.revision()));
        assertEquals(205,commission.current().rateBps());
    }
    @Test void routingCommissionRequiresAdminAndValidPercentage(){
        long revision=commission.current().revision();
        for(Integer value:Arrays.asList(-1,10001,null))assertEquals(400,assertThrows(ResponseStatusException.class,()->commission.update(new RoutingCommissionService.Update(revision,value,"Invalid rate"))).getStatusCode().value());
        assertEquals(400,assertThrows(ResponseStatusException.class,()->commission.update(new RoutingCommissionService.Update(revision,100," "))).getStatusCode().value());
        assertEquals(100,commission.current().rateBps());
        when(actor.isPlatformAdmin()).thenReturn(false);
        assertEquals(403,assertThrows(ResponseStatusException.class,commission::current).getStatusCode().value());
        assertEquals(403,assertThrows(ResponseStatusException.class,()->commission.update(new RoutingCommissionService.Update(revision,200,"Forbidden"))).getStatusCode().value());
        assertThrows(ResponseStatusException.class,()->commission.history(1,Long.MAX_VALUE));
    }
    @Test void updatesAreVersionedAuditedAndRestrictedToAdmins(){
        var view=controls.read("EVENT",event);change("EVENT",event,"BANK",false);
        assertEquals(409,assertThrows(ResponseStatusException.class,()->controls.change("EVENT",event,"BANK",new PaymentControlService.Change(view.revision(),true,"Stale editor"))).getStatusCode().value());
        var history=controls.history(1,Long.MAX_VALUE).getFirst();assertEquals(event,history.get("scope_id"));assertEquals(host,history.get("actor_id"));assertEquals(false,history.get("after_enabled"));
        assertThrows(org.springframework.dao.DataAccessException.class,()->db.update("UPDATE payment_control_history SET reason='Changed' WHERE revision=?",history.get("revision")));
        when(actor.isPlatformAdmin()).thenReturn(false);assertEquals(403,assertThrows(ResponseStatusException.class,()->controls.read("PLATFORM",null)).getStatusCode().value());
    }
    @Test void providerChargesAreStoredOnlyFromVerifiedCaptureAndCreditRemainsUnconfirmed() throws Exception {
        var order=checkout.create(new GuestCheckoutService.Request(event,null,"EVENT_WALLET",100000,UUID.randomUUID().toString(),"Guest",null,null));
        String payment="pay_controls123";
        when(provider.fetchPayment(payment)).thenReturn(new RazorpayClient.ProviderPayment(payment,order.razorpayOrderId(),100000,"INR","captured","upi",null,null,null,null,2360L,360L));
        change("EVENT",event,"RAZORPAY",false);
        capture.confirmVerifiedWebhook(order.localOrderId(),order.razorpayOrderId(),payment);
        assertEquals(2360,db.queryForObject("SELECT provider_fee_paise FROM payment_transactions WHERE payment_order_id=?",Long.class,order.localOrderId()));
        assertEquals(360,db.queryForObject("SELECT provider_tax_paise FROM payment_transactions WHERE payment_order_id=?",Long.class,order.localOrderId()));
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/events/"+event+"/payment-breakdown").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("host").roles("CUSTOMER")))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$[0].razorpay_fee_including_tax_paise").value(2360))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$[0].razorpay_tax_component_paise").value(360))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$[0].host_credited_paise").doesNotExist());
    }
    @Test void publicAvailabilityAndPrivateAdminEndpointsHaveCorrectBoundaries() throws Exception {
        change("EVENT",event,"RAZORPAY",false);
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/public/payments/controls/"+token))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.channels.RAZORPAY").value(false));
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/admin/console/payments/controls"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isUnauthorized());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/admin/console/payments/routing-commission"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isUnauthorized());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/admin/console/payments/routing-commission").contentType("application/json").content("{}"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isForbidden());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/admin/console/payments/controls/UPI").contentType("application/json").content("{}"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isForbidden());
    }
}
