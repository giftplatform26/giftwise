package com.giftplatform.payment.direct;

import com.giftplatform.payment.control.PaymentControlService;
import com.giftplatform.security.ActorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties={"gift.otp.secret-base64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=","gift.payment.direct-transfers.enabled=true"})
@Testcontainers
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
class DirectTransferConfirmationIntegrationTest {
    @Container static PostgreSQLContainer postgres=new PostgreSQLContainer("postgres:17");
    @DynamicPropertySource static void database(DynamicPropertyRegistry r){r.add("spring.datasource.url",postgres::getJdbcUrl);r.add("spring.datasource.username",postgres::getUsername);r.add("spring.datasource.password",postgres::getPassword);}
    @Autowired GuestDirectTransferService direct;
    @Autowired PaymentControlService controls;
    @Autowired JdbcTemplate db;
    @Autowired MockMvc mvc;
    @MockitoBean ActorContext actor;
    UUID host,event,token,bank,gift;

    @BeforeEach void setup(){
        host=UUID.randomUUID();event=UUID.randomUUID();bank=UUID.randomUUID();gift=UUID.randomUUID();
        db.update("INSERT INTO users(id,email,password_hash,role) VALUES(?,?,?,'CUSTOMER')",host,host+"@example.test","hash");
        db.update("INSERT INTO events(id,title,occasion_type,status,owner_id,host_display_name) VALUES(?,'Asha turns 30','BIRTHDAY','PUBLISHED',?,'Asha')",event,host);
        token=db.queryForObject("SELECT public_token FROM events WHERE id=?",UUID.class,event);
        db.update("INSERT INTO gifts(id,event_id,name,target_amount,status,visibility) VALUES(?,?,'Cycle',1000.00,'ACTIVE','PUBLIC')",gift,event);
        db.update("""
            INSERT INTO host_bank_accounts(id,host_id,account_number_encrypted,account_fingerprint,account_number_masked,ifsc_encrypted,ifsc_masked,account_holder_encrypted,verification_status,host_confirmed_at)
            VALUES(?,?,'enc',?,'XXXX1234','enc','HDFC0****','enc','PENDING',now())""",bank,host,bank.toString().replace("-",""));
        db.update("INSERT INTO event_payment_account_links(event_id,host_id,bank_account_id,preferred) VALUES(?,?,?,TRUE)",event,host,bank);
        when(actor.isPlatformAdmin()).thenReturn(true);when(actor.requireAuthenticatedUserId()).thenReturn(host);
        for(String channel:java.util.List.of("UPI","BANK")){long rev=controls.read("PLATFORM",null).revision();controls.change("PLATFORM",null,channel,new PaymentControlService.Change(rev,true,"Test"));}
    }
    GuestDirectTransferService.ClaimRequest claim(String method,String reference,String code,long paise){
        return new GuestDirectTransferService.ClaimRequest(bank,method,gift,paise,"Ravi",null,null,reference,UUID.randomUUID().toString(),code);
    }
    long progress(){return db.queryForObject("SELECT COALESCE(SUM(a.gift_paise),0) FROM guest_manual_display_allocations a JOIN guest_direct_transfer_claims c ON c.id=a.claim_id WHERE a.gift_id=? AND c.status IN ('AWAITING_VERIFICATION','HOST_CONFIRMED')",Long.class,gift);}

    @Test void claimStoresCodeAndReferenceTailAndReturnsAWorkingReceipt() throws Exception {
        var result=direct.claim(token,claim("BANK","HDFCN52026111012345","GW-7K3P9Q",50000));
        assertEquals("GW-7K3P9Q",result.paymentCode());assertNotNull(result.receiptToken());
        var row=db.queryForMap("SELECT payment_code,reference_tail,status FROM guest_direct_transfer_claims WHERE id=?",result.claimId());
        assertEquals("GW-7K3P9Q",row.get("payment_code"));assertEquals("012345",row.get("reference_tail"));assertEquals("AWAITING_VERIFICATION",row.get("status"));
        mvc.perform(get("/api/public/payments/direct/receipts/"+result.receiptToken())).andExpect(status().isOk())
            .andExpect(jsonPath("$.paymentCode").value("GW-7K3P9Q")).andExpect(jsonPath("$.amountPaise").value(50000))
            .andExpect(jsonPath("$.giftName").value("Cycle")).andExpect(jsonPath("$.status").value("AWAITING_VERIFICATION"))
            .andExpect(jsonPath("$.referenceTail").value("012345")).andExpect(jsonPath("$.eventTitle").value("Asha turns 30"));
        mvc.perform(get("/api/public/payments/direct/receipts/"+UUID.randomUUID())).andExpect(status().isNotFound());
    }

    @Test void upiReferencesMustBeTwelveDigitsAndCodesMustBeWellFormed(){
        assertEquals(400,assertThrows(ResponseStatusException.class,()->direct.claim(token,claim("UPI","ABC123456789","GW-7K3P9Q",10000))).getStatusCode().value());
        assertEquals(400,assertThrows(ResponseStatusException.class,()->direct.claim(token,claim("BANK","HDFCN52026111099999","GW-0O1I00",10000))).getStatusCode().value());
    }

    @Test void hostDecisionsUpdateStatusKeepProgressAndCanBeUndone() throws Exception {
        var result=direct.claim(token,claim("BANK","HDFCN52026111054321","GW-ABCDEF",40000));
        assertEquals(40000,progress());
        String base="/api/v1/events/"+event+"/transactions/claims/"+result.claimId()+"/decision";
        mvc.perform(post(base).with(user("host")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"decision\":\"RECEIVED\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("HOST_CONFIRMED"));
        assertEquals(40000,progress(),"confirmed transfers still count toward the gift");
        mvc.perform(get("/api/v1/events/"+event+"/transactions").with(user("host"))).andExpect(status().isOk())
            .andExpect(jsonPath("$[0].paymentCode").value("GW-ABCDEF")).andExpect(jsonPath("$[0].referenceTail").value("054321"))
            .andExpect(jsonPath("$[0].guestName").value("Ravi")).andExpect(jsonPath("$[0].giftName").value("Cycle"))
            .andExpect(jsonPath("$[0].status").value("HOST_CONFIRMED"));
        mvc.perform(get("/api/public/payments/direct/receipts/"+result.receiptToken())).andExpect(jsonPath("$.status").value("HOST_CONFIRMED"))
            .andExpect(jsonPath("$.hostDecidedAt").isNotEmpty());
        mvc.perform(post(base).with(user("host")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"decision\":\"NOT_RECEIVED\"}"))
            .andExpect(jsonPath("$.status").value("REJECTED"));
        assertEquals(0,progress(),"transfers marked not received stop counting");
        mvc.perform(post(base).with(user("host")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"decision\":\"UNDECIDED\"}"))
            .andExpect(jsonPath("$.status").value("AWAITING_VERIFICATION"));
        mvc.perform(post(base).with(user("host")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"decision\":\"MAYBE\"}")).andExpect(status().isBadRequest());
        mvc.perform(post(base).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"decision\":\"RECEIVED\"}")).andExpect(status().isUnauthorized());
    }

    @Test void anotherHostCannotDecideOnThisEventsTransfers() throws Exception {
        var result=direct.claim(token,claim("BANK","HDFCN52026111077777","GW-QWERTY",20000));
        UUID other=UUID.randomUUID();
        db.update("INSERT INTO users(id,email,password_hash,role) VALUES(?,?,?,'CUSTOMER')",other,other+"@example.test","hash");
        when(actor.requireAuthenticatedUserId()).thenReturn(other);
        mvc.perform(post("/api/v1/events/"+event+"/transactions/claims/"+result.claimId()+"/decision").with(user("other")).with(csrf())
            .contentType(MediaType.APPLICATION_JSON).content("{\"decision\":\"RECEIVED\"}")).andExpect(status().isNotFound());
        assertEquals("AWAITING_VERIFICATION",db.queryForObject("SELECT status FROM guest_direct_transfer_claims WHERE id=?",String.class,result.claimId()));
    }

    @Test void razorpayStatusIsAdminOnlyAndNeverReturnsKeys() throws Exception {
        mvc.perform(get("/api/admin/console/payments/razorpay-status").with(user("admin").roles("PLATFORM_ADMIN"))).andExpect(status().isOk())
            .andExpect(jsonPath("$.mode").exists()).andExpect(jsonPath("$.keyId").doesNotExist()).andExpect(jsonPath("$.keySecret").doesNotExist());
        mvc.perform(get("/api/admin/console/payments/razorpay-status").with(user("host").roles("CUSTOMER"))).andExpect(status().isForbidden());
    }

    @Test void paymentQrIsRefusedForDestinationsThatAreNotEligibleUpi() throws Exception {
        mvc.perform(get("/api/public/payments/verified-destinations/"+token+"/payment-qr").param("destinationId",bank.toString())
            .param("amountPaise","50000").param("code","GW-7K3P9Q")).andExpect(status().is4xxClientError());
        mvc.perform(get("/api/public/payments/verified-destinations/"+token+"/payment-qr").param("destinationId",bank.toString())
            .param("amountPaise","50000").param("code","bad")).andExpect(status().is4xxClientError());
    }
}
