package com.giftplatform.payment.receipt;

import com.giftplatform.payment.control.PaymentControlService;
import com.giftplatform.payment.direct.GuestDirectTransferService;
import com.giftplatform.security.ActorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties={"gift.otp.secret-base64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=","gift.payment.direct-transfers.enabled=true","java.awt.headless=true"})
@Testcontainers
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
class CustomReceiptIntegrationTest {
    @Container static PostgreSQLContainer postgres=new PostgreSQLContainer("postgres:17");
    @DynamicPropertySource static void database(DynamicPropertyRegistry r){r.add("spring.datasource.url",postgres::getJdbcUrl);r.add("spring.datasource.username",postgres::getUsername);r.add("spring.datasource.password",postgres::getPassword);}
    @Autowired GuestDirectTransferService direct;
    @Autowired PaymentControlService controls;
    @Autowired JdbcTemplate db;
    @Autowired MockMvc mvc;
    @MockitoBean ActorContext actor;
    UUID host,event,token,bank;

    @BeforeEach void setup(){
        host=UUID.randomUUID();event=UUID.randomUUID();bank=UUID.randomUUID();
        db.update("INSERT INTO users(id,email,password_hash,role) VALUES(?,?,?,'CUSTOMER')",host,host+"@example.test","hash");
        db.update("INSERT INTO events(id,title,occasion_type,status,owner_id,host_display_name,event_date) VALUES(?,'Meera & Arjun','WEDDING','PUBLISHED',?,'Meera',DATE '2026-12-12')",event,host);
        token=db.queryForObject("SELECT public_token FROM events WHERE id=?",UUID.class,event);
        db.update("""
            INSERT INTO host_bank_accounts(id,host_id,account_number_encrypted,account_fingerprint,account_number_masked,ifsc_encrypted,ifsc_masked,account_holder_encrypted,verification_status,host_confirmed_at)
            VALUES(?,?,'enc',?,'XXXX1234','enc','HDFC0****','enc','PENDING',now())""",bank,host,bank.toString().replace("-",""));
        db.update("INSERT INTO event_payment_account_links(event_id,host_id,bank_account_id,preferred) VALUES(?,?,?,TRUE)",event,host,bank);
        when(actor.isPlatformAdmin()).thenReturn(true);when(actor.requireAuthenticatedUserId()).thenReturn(host);
        long rev=controls.read("PLATFORM",null).revision();controls.change("PLATFORM",null,"BANK",new PaymentControlService.Change(rev,true,"Test"));
        platform(false);
    }
    void platform(boolean on){db.update("UPDATE platform_growth_features SET enabled=? WHERE code='CUSTOM_RECEIPTS'",on);}
    String base(){return "/api/v1/events/"+event+"/receipt-template";}
    String design(long version,boolean enabled,String prefix){return """
        {"enabled":%s,"layout":"ELEGANT","accentColor":"#9a6b2f","title":"Wedding gift receipt","headerText":"Meera & Arjun",
         "headerSubtext":"12 December 2026 · Kochi","thankYouMessage":"Thank you for blessing us.","footerText":"With love, the families",
         "signatureName":"Meera & Arjun","referencePrefix":"%s","showGuestName":true,"showGiftName":true,"showEventDate":true,
         "showPaymentMethod":true,"showPaymentCode":false,"version":%d}""".formatted(enabled,prefix,version);}
    GuestDirectTransferService.ClaimResult pay(String reference){
        return direct.claim(token,new GuestDirectTransferService.ClaimRequest(bank,"BANK",null,25100,"Ravi",null,null,reference,UUID.randomUUID().toString(),null));
    }

    @Test void defaultsSuitTheOccasionAndSavingNeedsThePlatformSwitch() throws Exception {
        mvc.perform(get(base()).with(user("host"))).andExpect(status().isOk())
            .andExpect(jsonPath("$.platformEnabled").value(false)).andExpect(jsonPath("$.layout").value("ELEGANT"))
            .andExpect(jsonPath("$.headerText").value("Meera & Arjun")).andExpect(jsonPath("$.nextReceiptNumber").value("GIFT-000001"));
        mvc.perform(put(base()).with(user("host")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(design(0,true,"MA2026")))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.error").value("Custom receipts are not enabled on this platform yet"));
        platform(true);
        mvc.perform(put(base()).with(user("host")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(design(0,true,"ma2026")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.referencePrefix").value("MA2026")).andExpect(jsonPath("$.accentColor").value("#9A6B2F"))
            .andExpect(jsonPath("$.version").value(1)).andExpect(jsonPath("$.nextReceiptNumber").value("MA2026-000001"));
        mvc.perform(put(base()).with(user("host")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(design(0,true,"MA2026")))
            .andExpect(status().isConflict());
        mvc.perform(put(base()).with(user("host")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(design(1,true,"BAD PREFIX!")))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").exists());
    }

    @Test void onlyTheEventHostCanSeeOrEditTheDesign() throws Exception {
        UUID other=UUID.randomUUID();
        db.update("INSERT INTO users(id,email,password_hash,role) VALUES(?,?,?,'CUSTOMER')",other,other+"@example.test","hash");
        when(actor.requireAuthenticatedUserId()).thenReturn(other);platform(true);
        mvc.perform(get(base()).with(user("other"))).andExpect(status().isNotFound());
        mvc.perform(put(base()).with(user("other")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(design(0,true,"MA2026"))).andExpect(status().isNotFound());
        mvc.perform(get(base())).andExpect(status().isUnauthorized());
    }

    @Test void receiptNumbersAreUniqueSequentialAndKeepTheirOriginalPrefix() throws Exception {
        var first=pay("HDFCN52026111000001");
        assertEquals("GIFT-000001",db.queryForObject("SELECT receipt_number FROM guest_direct_transfer_claims WHERE id=?",String.class,first.claimId()));
        platform(true);
        mvc.perform(put(base()).with(user("host")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(design(0,false,"MA2026"))).andExpect(status().isOk());
        var second=pay("HDFCN52026111000002");
        assertEquals("MA2026-000002",db.queryForObject("SELECT receipt_number FROM guest_direct_transfer_claims WHERE id=?",String.class,second.claimId()));
        assertEquals("GIFT-000001",db.queryForObject("SELECT receipt_number FROM guest_direct_transfer_claims WHERE id=?",String.class,first.claimId()),"issued numbers never change");
        mvc.perform(get("/api/v1/events/"+event+"/transactions").with(user("host"))).andExpect(jsonPath("$[0].receiptNumber").value("MA2026-000002"));
    }

    @Test void customDesignAppearsOnTheReceiptOnlyWhenPlatformAndEventAreBothOn() throws Exception {
        var paid=pay("HDFCN52026111000003");
        String receipt="/api/public/payments/direct/receipts/"+paid.receiptToken();
        mvc.perform(get(receipt)).andExpect(jsonPath("$.receiptNumber").value("GIFT-000001")).andExpect(jsonPath("$.design").doesNotExist())
            .andExpect(jsonPath("$.eventDate").value("2026-12-12"));
        platform(true);
        mvc.perform(put(base()).with(user("host")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(design(0,false,"MA2026"))).andExpect(status().isOk());
        mvc.perform(get(receipt)).andExpect(jsonPath("$.design").doesNotExist());
        mvc.perform(put(base()).with(user("host")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(design(1,true,"MA2026"))).andExpect(status().isOk());
        mvc.perform(get(receipt)).andExpect(jsonPath("$.design.layout").value("ELEGANT")).andExpect(jsonPath("$.design.title").value("Wedding gift receipt"))
            .andExpect(jsonPath("$.design.footerText").value("With love, the families")).andExpect(jsonPath("$.design.showPaymentCode").value(false))
            .andExpect(jsonPath("$.design.nextReceiptNumber").doesNotExist()).andExpect(jsonPath("$.receiptNumber").value("GIFT-000001"));
        platform(false);
        mvc.perform(get(receipt)).andExpect(jsonPath("$.design").doesNotExist());
    }

    @Test void logosAreReencodedSafelyAndCanBeRemoved() throws Exception {
        platform(true);
        BufferedImage image=new BufferedImage(800,400,BufferedImage.TYPE_INT_RGB);var out=new ByteArrayOutputStream();ImageIO.write(image,"jpeg",out);
        mvc.perform(multipart(base()+"/logo").file(new MockMultipartFile("file","logo.jpg","image/jpeg",out.toByteArray())).with(user("host")).with(csrf()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.logoDataUrl").value(org.hamcrest.Matchers.startsWith("data:image/png;base64,")));
        mvc.perform(multipart(base()+"/logo").file(new MockMultipartFile("file","x.svg","image/svg+xml","<svg onload=alert(1)/>".getBytes())).with(user("host")).with(csrf()))
            .andExpect(status().isBadRequest());
        mvc.perform(post(base()+"/logo/remove").with(user("host")).with(csrf())).andExpect(status().isOk()).andExpect(jsonPath("$.logoDataUrl").doesNotExist());
    }
}
