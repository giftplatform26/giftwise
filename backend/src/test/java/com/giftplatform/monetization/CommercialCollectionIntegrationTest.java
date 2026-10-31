package com.giftplatform.monetization;

import com.giftplatform.security.ActorContext;
import com.giftplatform.payment.provider.RazorpayClient;
import com.giftplatform.payment.service.RazorpayWebhookService;
import com.giftplatform.asset.GalleryAssetService;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
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

@SpringBootTest(properties={"gift.otp.secret-base64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=","spring.jpa.hibernate.ddl-auto=validate","gift.payment.razorpay.key-id=rzp_test_commerce","gift.payment.razorpay.key-secret=commerce-secret","gift.payment.razorpay.webhook-secret=webhook-secret"})
@Testcontainers
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
class CommercialCollectionIntegrationTest {
    @Container static PostgreSQLContainer postgres=new PostgreSQLContainer("postgres:17");
    @DynamicPropertySource static void database(DynamicPropertyRegistry r){r.add("spring.datasource.url",postgres::getJdbcUrl);r.add("spring.datasource.username",postgres::getUsername);r.add("spring.datasource.password",postgres::getPassword);}
    @Autowired CommercialPurchaseService purchases;
    @Autowired CommercialAccessService access;
    @Autowired CommercialSetupService setup;
    @Autowired CommercialPublicService publicOffers;
    @Autowired MonetizationService pricing;
    @Autowired GalleryAssetService gallery;
    @Autowired RazorpayWebhookService webhooks;
    @Autowired JdbcTemplate db;
    @Autowired org.springframework.test.web.servlet.MockMvc mvc;
    @MockitoBean ActorContext actor;
    @MockitoBean RazorpayClient provider;
    UUID host;
    @BeforeEach void initialize(){
        host=UUID.randomUUID();db.update("INSERT INTO users(id,email,password_hash,role) VALUES(?,?,?,'PLATFORM_ADMIN')",host,host+"@example.test","hash");
        when(actor.isPlatformAdmin()).thenReturn(true);when(actor.requireAuthenticatedUserId()).thenReturn(host);
        var old=pricing.current();
        pricing.update(new MonetizationService.Update(old.revision(),true,old.streams().stream().map(s->new MonetizationService.Stream(s.code(),true,
                Set.of("AFFILIATE","CONTRIBUTION","WALLET").contains(s.code())?"PERCENTAGE":"FIXED",s.code().equals("SPONSORED_GIFT")?10000:s.fixedPaise(),s.rateBps(),s.capPaise(),s.feeBearer(),true)).toList(),"Enable integrated streams"));
        var policy=setup.policy();setup.policy(new CommercialSetupService.Policy(((Number)policy.get("revision")).longValue(),1,365,30,30,"Reset test policy"));
        when(provider.createOrder(anyLong(),anyString())).thenAnswer(inv->new RazorpayClient.ProviderOrder("order_"+UUID.randomUUID().toString().replace("-",""),inv.getArgument(0),"INR","created"));
    }
    CommercialPurchaseService.Buyer buyer(){return new CommercialPurchaseService.Buyer("HOST",host);}
    UUID event(){UUID id=UUID.randomUUID();db.update("INSERT INTO events(id,title,occasion_type,status,owner_id,host_display_name) VALUES(?,'Commerce event','WEDDING','PUBLISHED',?,'Host')",id,host);return id;}
    UUID template(){UUID id=UUID.randomUUID();db.update("INSERT INTO invitation_gallery_assets(id,category,occasion,title,content_type,image_png,width,height,published,tier) VALUES(?,'BACKGROUND','WEDDING','Premium flowers','image/png',?,32,32,true,'PREMIUM')",id,new byte[]{1,2});return id;}
    CommercialPurchaseService.Checkout create(CommercialPurchaseService.Buyer buyer,String stream,UUID resource){var offer=purchases.offer(buyer,stream,resource);return purchases.create(buyer,new CommercialPurchaseService.Request(stream,resource,UUID.randomUUID().toString(),offer.amountPaise()));}
    String providerPayment(CommercialPurchaseService.Checkout order,String status,long amount){String payment="pay_"+UUID.randomUUID().toString().replace("-","");when(provider.fetchPayment(payment)).thenReturn(new RazorpayClient.ProviderPayment(payment,order.providerOrderId(),amount,"INR",status));return payment;}
    CommercialPurchaseService.Checkout capture(CommercialPurchaseService.Buyer buyer,CommercialPurchaseService.Checkout order) throws Exception {
        String payment=providerPayment(order,"captured",order.amountPaise());return purchases.confirm(buyer,new CommercialPurchaseService.Confirmation(order.id(),order.providerOrderId(),payment,sign(order.providerOrderId()+"|"+payment,"commerce-secret")));
    }
    static String sign(String value,String secret) throws Exception {Mac mac=Mac.getInstance("HmacSHA256");mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),"HmacSHA256"));return HexFormat.of().formatHex(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));}
    @Test void hostPlanRequiresCaptureThenRenewsWithoutLosingExistingTime() throws Exception {
        UUID event=event();assertEquals(402,assertThrows(ResponseStatusException.class,()->access.requireEventCapacity(host,UUID.randomUUID())).getStatusCode().value());
        var order=create(buyer(),"HOST_PLAN",host);assertFalse(access.premium(host));
        capture(buyer(),order);assertTrue(access.premium(host));access.requireEventCapacity(host,UUID.randomUUID());
        var first=db.queryForObject("SELECT MAX(expires_at) FROM commercial_entitlements WHERE buyer_id=? AND stream='HOST_PLAN'",java.time.OffsetDateTime.class,host);
        capture(buyer(),create(buyer(),"HOST_PLAN",host));
        var last=db.queryForObject("SELECT MAX(expires_at) FROM commercial_entitlements WHERE buyer_id=? AND stream='HOST_PLAN'",java.time.OffsetDateTime.class,host);
        assertEquals(first.plusDays(365),last);
    }
    @Test void premiumTemplateHasOwnershipAndImageAccessChecks() throws Exception {
        UUID template=template();assertThrows(ResponseStatusException.class,()->gallery.publishedImage(template));
        assertEquals(402,assertThrows(ResponseStatusException.class,()->gallery.purchasedImage(template)).getStatusCode().value());
        var order=create(buyer(),"PREMIUM_TEMPLATE",template);capture(buyer(),order);assertNotNull(gallery.purchasedImage(template));
        when(actor.requireAuthenticatedUserId()).thenReturn(UUID.randomUUID());assertThrows(ResponseStatusException.class,()->gallery.purchasedImage(template));
        assertThrows(ResponseStatusException.class,()->purchases.confirm(new CommercialPurchaseService.Buyer("HOST",UUID.randomUUID()),new CommercialPurchaseService.Confirmation(order.id(),order.providerOrderId(),"pay_other","invalid")));
    }
    @Test void unverifiedAndMismatchedPaymentsNeverUnlockAndRetriesDoNotDuplicateRevenue() throws Exception {
        var order=create(buyer(),"HOST_PLAN",host);
        String mismatch=providerPayment(order,"captured",order.amountPaise()+1);assertThrows(ResponseStatusException.class,()->purchases.verifyCapture(order.id(),order.providerOrderId(),mismatch));
        String unverified=providerPayment(order,"authorized",order.amountPaise());assertThrows(ResponseStatusException.class,()->purchases.verifyCapture(order.id(),order.providerOrderId(),unverified));assertFalse(access.premium(host));
        String correct=providerPayment(order,"captured",order.amountPaise());
        purchases.verifyCapture(order.id(),order.providerOrderId(),correct);purchases.verifyCapture(order.id(),order.providerOrderId(),correct);
        assertEquals(1,db.queryForObject("SELECT count(*) FROM commercial_revenue WHERE order_id=?",Integer.class,order.id()));
        assertEquals(1,db.queryForObject("SELECT count(*) FROM commercial_entitlements WHERE order_id=?",Integer.class,order.id()));
    }
    @Test void priceAcceptanceAndPendingOrderRecoveryRetainOriginalSnapshot() {
        var offer=purchases.offer(buyer(),"HOST_PLAN",host);
        assertEquals(409,assertThrows(ResponseStatusException.class,()->purchases.create(buyer(),new CommercialPurchaseService.Request("HOST_PLAN",host,"wrong-price",1))).getStatusCode().value());verifyNoInteractions(provider);
        var order=create(buyer(),"HOST_PLAN",host);
        var config=pricing.current();pricing.update(new MonetizationService.Update(config.revision(),true,config.streams().stream().map(s->s.code().equals("HOST_PLAN")?new MonetizationService.Stream(s.code(),true,"FIXED",49900,0,0,"GUEST",true):s).toList(),"Raise future plan price"));
        assertEquals(order.amountPaise(),purchases.offer(buyer(),"HOST_PLAN",host).amountPaise());
        var retry=create(buyer(),"HOST_PLAN",host);assertEquals(order.id(),retry.id());verify(provider,times(1)).createOrder(anyLong(),anyString());
        assertThrows(org.springframework.dao.DataAccessException.class,()->db.update("UPDATE commercial_orders SET amount_paise=1 WHERE id=?",order.id()));
    }
    @Test void verifiedWebhookGrantsCommercialPurchaseEvenAfterSwitchOff() throws Exception {
        var order=create(buyer(),"HOST_PLAN",host);String payment=providerPayment(order,"captured",order.amountPaise());
        var config=pricing.current();pricing.update(new MonetizationService.Update(config.revision(),false,config.streams(),"Pause sales"));
        String raw="{\"event\":\"payment.captured\",\"payload\":{\"payment\":{\"entity\":{\"id\":\""+payment+"\",\"order_id\":\""+order.providerOrderId()+"\"}}}}";
        webhooks.receive(raw.getBytes(StandardCharsets.UTF_8),sign(raw,"webhook-secret"),"event_"+order.id());assertTrue(access.premium(host));
        webhooks.receive(raw.getBytes(StandardCharsets.UTF_8),sign(raw,"webhook-secret"),"event_"+order.id());
        assertEquals(1,db.queryForObject("SELECT count(*) FROM commercial_revenue WHERE order_id=?",Integer.class,order.id()));
    }
    @Test void vendorAndSponsoredPlacementsRequireApprovalPaymentAndUnexpiredAccess() throws Exception {
        for(String stream:List.of("VENDOR_LISTING","SPONSORED_GIFT")){
            var placement=setup.createPlacement(new CommercialSetupService.Placement(stream,"Partner","Great services","https://partner.example/products","Gifts","WEDDING"));UUID id=(UUID)placement.get("id");
            assertThrows(ResponseStatusException.class,()->create(buyer(),stream,id));
            setup.moderate(id,new CommercialSetupService.Moderation("APPROVED","Reviewed partner"));
            assertTrue(publicOffers.placements(stream,"WEDDING").stream().noneMatch(p->p.get("id").equals(id)));
            var order=create(buyer(),stream,id);capture(buyer(),order);
            assertTrue(publicOffers.placements(stream,"WEDDING").stream().anyMatch(p->p.get("id").equals(id)));
            assertEquals("https://partner.example/products",publicOffers.visit(id));
            db.update("UPDATE commercial_entitlements SET starts_at=now()-interval '40 days',expires_at=now()-interval '10 days' WHERE order_id=?",order.id());
            assertTrue(publicOffers.placements(stream,"WEDDING").stream().noneMatch(p->p.get("id").equals(id)));assertThrows(ResponseStatusException.class,()->publicOffers.visit(id));
        }
    }
    @Test void groupCreatorPaysCoordinationSeparatelyAndMemberReportsNeverUnlockIt() throws Exception {
        UUID event=event(),creator=UUID.randomUUID(),group=UUID.randomUUID();
        db.update("INSERT INTO gift_group_accounts(id,event_id,username,password_hash) VALUES(?,?,?,'hash')",creator,event,"creator_"+creator.toString().replace("-","").substring(0,12));
        db.update("INSERT INTO gift_groups(id,event_id,admin_id,name,creator_name,creator_mobile,target_paise) VALUES(?,?,?,'Group','Creator','9876543210',5000000)",group,event,creator);
        assertEquals(402,assertThrows(ResponseStatusException.class,()->access.requireGroupFee(group)).getStatusCode().value());
        assertThrows(ResponseStatusException.class,()->create(buyer(),"GROUP_GIFT",group));
        var buyer=new CommercialPurchaseService.Buyer("GROUP_ADMIN",creator);var order=create(buyer,"GROUP_GIFT",group);assertEquals(9900,order.amountPaise());
        capture(buyer,order);access.requireGroupFee(group);
        assertEquals(0,db.queryForObject("SELECT count(*) FROM wallet_ledger_entries WHERE event_id=?",Integer.class,event));
    }
    @Test void affiliateLinksTrackClicksAndStatementsWithHistoryAndReversals(){
        UUID event=event(),gift=UUID.randomUUID();String domain="shop-"+UUID.randomUUID()+".example";
        var partner=setup.partner(new CommercialSetupService.Partner(null,0,"Retailer",domain,true,"tag","our-tag","subid",null,"Approved affiliate account"));
        db.update("INSERT INTO gifts(id,event_id,name,target_amount,visibility,product_url) VALUES(?,?,'Gift',1000,'PUBLIC',?)",gift,event,"https://"+domain+"/product?tag=old&sku=123");
        UUID token=db.queryForObject("SELECT public_token FROM events WHERE id=?",UUID.class,event);
        String target=publicOffers.affiliate(token,gift);assertTrue(target.contains("tag=our-tag"));assertFalse(target.contains("tag=old"));assertTrue(target.contains("subid="));
        UUID click=db.queryForObject("SELECT id FROM affiliate_clicks WHERE event_id=?",UUID.class,event);
        var commission=setup.commission(new CommercialSetupService.Commission(null,0,(UUID)partner.get("id"),click,"statement-"+gift,2500,"PAID","Partner paid statement"));
        setup.commission(new CommercialSetupService.Commission((UUID)commission.get("id"),0,(UUID)partner.get("id"),click,"statement-"+gift,2500,"REVERSED","Partner reversed this sale"));
        assertEquals("REVERSED",db.queryForObject("SELECT state FROM affiliate_commissions WHERE id=?",String.class,commission.get("id")));
        assertTrue(setup.history().stream().anyMatch(h->h.get("resource_id").equals(commission.get("id").toString())));
        var config=pricing.current();pricing.update(new MonetizationService.Update(config.revision(),false,config.streams(),"Pause affiliate attribution"));assertTrue(publicOffers.affiliate(token,gift).contains("tag=old"));
    }
    @Test void advancedAnalyticsRequiresPlanAndEventOwnership() throws Exception {
        UUID event=event();
        var user=org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("host").roles("CUSTOMER");
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/commerce/analytics/"+event).with(user))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isPaymentRequired());
        capture(buyer(),create(buyer(),"HOST_PLAN",host));
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/commerce/analytics/"+event).with(user))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
        when(actor.requireAuthenticatedUserId()).thenReturn(UUID.randomUUID());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/commerce/analytics/"+event).with(user))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isNotFound());
    }
    @Test void switchesStopNewPurchasesAndGroupPaymentStillRequiresCreatorSession() throws Exception {
        var old=pricing.current();pricing.update(new MonetizationService.Update(old.revision(),false,old.streams(),"Disable new purchases"));
        assertEquals(409,assertThrows(ResponseStatusException.class,()->create(buyer(),"HOST_PLAN",host)).getStatusCode().value());
        verifyNoInteractions(provider);
        UUID event=event(),token=db.queryForObject("SELECT public_token FROM events WHERE id=?",UUID.class,event);
        String path="/api/public/groups/"+token+"/coordination/"+UUID.randomUUID();
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(path))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isNotFound());
        db.update("INSERT INTO platform_host_module_grants(host_id,module_code,enabled,changed_by) VALUES(?,'GROUP_GIFTING',true,?)",host,host);
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(path))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isUnauthorized());
    }
    @Test void commerceRoutesEnforceHostAndAdminAuthenticationAndCsrf() throws Exception {
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/commerce")).andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isUnauthorized());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/admin/console/monetization/revenue")).andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isUnauthorized());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/commerce/checkout").contentType("application/json").content("{}")).andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isForbidden());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/public/commerce/placements?stream=SPONSORED_GIFT")).andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
    }
}
