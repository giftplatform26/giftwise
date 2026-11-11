package com.giftplatform.growth;

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
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"gift.otp.secret-base64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=", "gift.whatsapp.worker-initial-delay-ms=3600000"})
@Testcontainers
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
class GrowthBatchFourIntegrationTest {
    @Container static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");
    @DynamicPropertySource static void database(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl); r.add("spring.datasource.username", postgres::getUsername); r.add("spring.datasource.password", postgres::getPassword);
    }
    @Autowired JdbcTemplate db;
    @Autowired MockMvc mvc;
    @MockitoBean ActorContext actor;
    UUID host, event, token;
    static final String[] HOST_MODULES = {"OCCASION_REMINDERS", "GIFT_LEDGER", "HOST_REFERRALS", "YEARLY_REHOST", "VENDOR_DIRECTORY", "MEMORY_BOOK", "EXPERIENCE_GIFTS", "GUESTBOOK", "SHAGUN_REGISTER"};

    @BeforeEach void setup() {
        host = newUser(); event = UUID.randomUUID();
        db.update("INSERT INTO events(id,title,occasion_type,status,owner_id,host_display_name,event_date) VALUES(?,'Meera & Arjun','WEDDING','PUBLISHED',?,'Meera',DATE '2026-12-12')", event, host);
        token = db.queryForObject("SELECT public_token FROM events WHERE id=?", UUID.class, event);
        for (String m : HOST_MODULES) enable(host, m);
        for (String m : new String[]{"MEMORY_BOOK", "EXPERIENCE_GIFTS", "SHAGUN_REGISTER"})
            db.update("INSERT INTO platform_event_module_grants(event_id,module_code,enabled) VALUES(?,?,TRUE)", event, m);
        as(host);
    }
    UUID newUser() {
        UUID id = UUID.randomUUID();
        db.update("INSERT INTO users(id,email,password_hash,role,full_name) VALUES(?,?,?,'CUSTOMER','Priya Shah')", id, id + "@example.test", "hash");
        return id;
    }
    void enable(UUID who, String m) {
        db.update("UPDATE platform_module_controls SET enabled=TRUE WHERE module_code=?", m);
        db.update("INSERT INTO platform_host_module_grants(host_id,module_code,enabled) VALUES(?,?,TRUE) ON CONFLICT(host_id,module_code) DO UPDATE SET enabled=TRUE", who, m);
    }
    void as(UUID who) { when(actor.requireAuthenticatedUserId()).thenReturn(who); }
    String q(String s) { return s.replace('\'', '"'); }
    UUID gift(String name) {
        UUID g = UUID.randomUUID();
        db.update("INSERT INTO gifts(id,event_id,name,target_amount) VALUES(?,?,?,10000)", g, event, name);
        return g;
    }

    // ---- Reminders ----

    @Test void remindersShowWhatIsComingUpAndWhatYouGaveLastTime() throws Exception {
        LocalDate soon = LocalDate.now(ZoneId.of("Asia/Kolkata")).plusDays(10);
        mvc.perform(post("/api/v1/me/ledger/given").with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content(q("{'recipientName':'Ravi Kumar','kind':'CASH','amountPaise':110000,'occasionDate':'2025-03-01'}"))).andExpect(status().isOk());
        mvc.perform(post("/api/v1/me/reminders").with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content(q("{'personName':'ravi kumar','occasion':'Birthday','month':" + soon.getMonthValue() + ",'day':" + soon.getDayOfMonth() + ",'sinceYear':1990}")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.daysAway").value(10)).andExpect(jsonPath("$.turning").value(soon.getYear() - 1990))
            .andExpect(jsonPath("$.lastGivenPaise").value(110000));
        mvc.perform(post("/api/v1/me/reminders").with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content(q("{'personName':'Nobody','occasion':'Birthday','month':2,'day':31}"))).andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/me/reminders").with(user("h"))).andExpect(jsonPath("$.upcoming", hasSize(1)));
        assertEquals(LocalDate.of(2027, 2, 28), com.giftplatform.register.OccasionRemindersControllerAccess.next(LocalDate.of(2027, 1, 1), 2, 29));
    }

    @Test void guestCanSaveAReceiptToTheirLedgerOnce() throws Exception {
        UUID bank = UUID.randomUUID(), receipt = UUID.randomUUID();
        db.update("""
            INSERT INTO host_bank_accounts(id,host_id,account_number_encrypted,account_fingerprint,account_number_masked,ifsc_encrypted,ifsc_masked,account_holder_encrypted,verification_status,host_confirmed_at)
            VALUES(?,?,'enc',?,'XXXX1234','enc','HDFC0****','enc','PENDING',now())""", bank, host, bank.toString().replace("-", ""));
        db.update("INSERT INTO guest_direct_transfer_claims(id,event_id,destination_id,method,amount_paise,guest_name,reference_hash,client_key,receipt_token) VALUES(?,?,?,'UPI',210100,'Priya',?,?,?)",
            UUID.randomUUID(), event, bank, (UUID.randomUUID() + "" + UUID.randomUUID()).replace("-", ""), UUID.randomUUID().toString(), receipt);
        UUID guest = newUser(); enable(guest, "GIFT_LEDGER"); as(guest);
        mvc.perform(post("/api/v1/me/ledger/from-receipt/" + receipt).with(user("g")).with(csrf())).andExpect(status().isOk())
            .andExpect(jsonPath("$.recipientName").value("Meera")).andExpect(jsonPath("$.amountPaise").value(210100)).andExpect(jsonPath("$.occasion").value("Meera & Arjun"));
        mvc.perform(post("/api/v1/me/ledger/from-receipt/" + receipt).with(user("g")).with(csrf())).andExpect(status().isOk());
        mvc.perform(get("/api/v1/me/ledger").with(user("g"))).andExpect(jsonPath("$.given", hasSize(1)));
        mvc.perform(post("/api/v1/me/ledger/from-receipt/" + UUID.randomUUID()).with(user("g")).with(csrf())).andExpect(status().isNotFound());
    }

    // ---- Referrals ----

    @Test void referralsAttributeQualifyAndGetRewardedByAdmin() throws Exception {
        String code = tools.jackson.databind.json.JsonMapper.builder().build().readTree(mvc.perform(get("/api/v1/me/referrals").with(user("h")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.link").value(containsString("/register?ref=GW"))).andReturn().getResponse().getContentAsString()).get("code").asString();
        mvc.perform(post("/api/v1/me/referrals/claim").with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(q("{'code':'" + code + "'}")))
            .andExpect(jsonPath("$.attributed").value(false));
        UUID friend = newUser(); as(friend);
        mvc.perform(post("/api/v1/me/referrals/claim").with(user("f")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(q("{'code':'" + code.toLowerCase() + "'}")))
            .andExpect(jsonPath("$.attributed").value(true));
        mvc.perform(post("/api/v1/me/referrals/claim").with(user("f")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(q("{'code':'" + code + "'}")))
            .andExpect(jsonPath("$.attributed").value(false));
        UUID old = newUser(); db.update("UPDATE users SET created_at=now()-interval '30 days' WHERE id=?", old); as(old);
        mvc.perform(post("/api/v1/me/referrals/claim").with(user("o")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(q("{'code':'" + code + "'}")))
            .andExpect(jsonPath("$.attributed").value(false));
        as(host);
        mvc.perform(get("/api/v1/me/referrals").with(user("h"))).andExpect(jsonPath("$.friends", hasSize(1))).andExpect(jsonPath("$.friends[0].status").value("ATTRIBUTED"));
        db.update("INSERT INTO events(id,title,occasion_type,status,owner_id,host_display_name) VALUES(?,'Friend party','BIRTHDAY','PUBLISHED',?,'Priya')", UUID.randomUUID(), friend);
        mvc.perform(get("/api/v1/me/referrals").with(user("h"))).andExpect(jsonPath("$.qualified").value(1));
        when(actor.isPlatformAdmin()).thenReturn(true);
        UUID referral = db.queryForObject("SELECT id FROM user_referrals WHERE referred_user_id=?", UUID.class, friend);
        mvc.perform(post("/api/admin/console/referrals/" + referral + "/reward").with(user("a").roles("PLATFORM_ADMIN")).with(csrf())).andExpect(status().isOk());
        mvc.perform(post("/api/admin/console/referrals/" + referral + "/reward").with(user("a").roles("PLATFORM_ADMIN")).with(csrf())).andExpect(status().isConflict());
        mvc.perform(get("/api/v1/me/referrals").with(user("h"))).andExpect(jsonPath("$.rewarded").value(1));
    }

    // ---- Yearly re-hosting ----

    @Test void lastYearsBirthdayIsSuggestedAndRepeatsOnceWithTheSameGuests() throws Exception {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
        LocalDate last = today.plusDays(20).minusYears(1);
        UUID bday = UUID.randomUUID();
        db.update("INSERT INTO events(id,title,occasion_type,status,owner_id,host_display_name,event_date) VALUES(?,?,'BIRTHDAY','COMPLETED',?,'Meera',?)",
            bday, "Asha turns 5 · " + last.getYear(), host, last);
        db.update("INSERT INTO event_guest_contacts(id,event_id,name,phone) VALUES(?,?,'Ravi','+919876543210'),(?,?,'Opted','+919876543211')", UUID.randomUUID(), bday, UUID.randomUUID(), bday);
        db.update("UPDATE event_guest_contacts SET opted_out_at=now() WHERE name='Opted' AND event_id=?", bday);
        mvc.perform(get("/api/v1/me/rehost").with(user("h"))).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].daysAway").value(20)).andExpect(jsonPath("$[0].suggestedTitle").value("Asha turns 5 · " + (last.getYear() + 1)))
            .andExpect(jsonPath("$[0].guests").value(1));
        mvc.perform(post("/api/v1/me/rehost/" + bday).with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(q("{'copyGuests':true}")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.guestsCopied").value(1)).andExpect(jsonPath("$.eventDate").value(today.plusDays(20).toString()));
        mvc.perform(post("/api/v1/me/rehost/" + bday).with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isConflict());
        mvc.perform(get("/api/v1/me/rehost").with(user("h"))).andExpect(jsonPath("$", hasSize(0)));
        assertEquals("DRAFT", db.queryForObject("SELECT status FROM events WHERE repeated_from=?", String.class, bday));
        mvc.perform(post("/api/v1/me/rehost/" + event).with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isBadRequest());
    }

    // ---- Memory book ----

    @Test void memoryBookCollectsWishesAndGiversAndHidesAmountsByDefault() throws Exception {
        db.update("INSERT INTO event_guestbook(id,event_id,guest_name,message,status) VALUES(?,?,'Asha','Bless you both','APPROVED'),(?,?,'Spam','x','PENDING')",
            UUID.randomUUID(), event, UUID.randomUUID(), event);
        db.update("INSERT INTO event_shagun_entries(id,event_id,giver_name,giver_key,kind,amount_paise,client_key,recorded_by) VALUES(?,?,'Ravi','ravi','CASH',510000,'k1',?)", UUID.randomUUID(), event, host);
        mvc.perform(get("/api/v1/events/" + event + "/memory-book").with(user("h"))).andExpect(status().isOk())
            .andExpect(jsonPath("$.wishes", hasSize(1))).andExpect(jsonPath("$.givers[0].name").value("Ravi")).andExpect(jsonPath("$.givers[0].amountPaise").doesNotExist());
        mvc.perform(get("/api/v1/events/" + event + "/memory-book").param("amounts", "true").with(user("h"))).andExpect(jsonPath("$.givers[0].amountPaise").value(510000));
        UUID other = newUser(); enable(other, "MEMORY_BOOK"); as(other);
        mvc.perform(get("/api/v1/events/" + event + "/memory-book").with(user("o"))).andExpect(status().isNotFound());
    }

    // ---- Vendors ----

    @Test void adminCuratesVendorsAndHostsShortlistThem() throws Exception {
        when(actor.isPlatformAdmin()).thenReturn(true);
        String body = mvc.perform(post("/api/admin/console/vendors").with(user("a").roles("PLATFORM_ADMIN")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content(q("{'name':'Lotus Decor','category':'decor','city':'Kochi','phone':'+91 98765 43210','website':'https://lotus.example','instagram':'@lotus.decor','verified':true}"))).andExpect(status().isOk())
            .andExpect(jsonPath("$.phone").value("+919876543210")).andExpect(jsonPath("$.instagram").value("lotus.decor")).andReturn().getResponse().getContentAsString();
        mvc.perform(post("/api/admin/console/vendors").with(user("a").roles("PLATFORM_ADMIN")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content(q("{'name':'Bad','category':'DECOR','city':'Kochi','website':'javascript:alert(1)'}"))).andExpect(status().isBadRequest());
        when(actor.isPlatformAdmin()).thenReturn(false);
        String id = tools.jackson.databind.json.JsonMapper.builder().build().readTree(body).get("id").asString();
        mvc.perform(get("/api/v1/me/vendors").param("city", "kochi").with(user("h"))).andExpect(jsonPath("$.vendors", hasSize(1))).andExpect(jsonPath("$.cities[0]").value("Kochi"));
        mvc.perform(post("/api/v1/me/vendors/" + id + "/shortlist").with(user("h")).with(csrf())).andExpect(status().isOk());
        mvc.perform(get("/api/v1/me/vendors").param("shortlist", "true").with(user("h"))).andExpect(jsonPath("$.vendors[0].shortlisted").value(true));
        mvc.perform(post("/api/admin/console/vendors").with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isForbidden());
    }

    // ---- Gift kinds ----

    @Test void charityWishesLinkOutAndCanNeverTakePayments() throws Exception {
        UUID dinner = gift("Honeymoon dinner"), cause = gift("Plant 100 trees"), paid = gift("Mixer");
        mvc.perform(put("/api/v1/events/" + event + "/gifts/" + dinner + "/kind").with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content(q("{'kind':'experience'}"))).andExpect(status().isOk());
        mvc.perform(put("/api/v1/events/" + event + "/gifts/" + cause + "/kind").with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content(q("{'kind':'CHARITY','charityName':'Green Earth','charityUrl':'http://insecure.example'}"))).andExpect(status().isBadRequest());
        mvc.perform(put("/api/v1/events/" + event + "/gifts/" + cause + "/kind").with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content(q("{'kind':'CHARITY','charityName':'Green Earth','charityUrl':'https://greenearth.example/donate'}"))).andExpect(status().isOk());
        mvc.perform(get("/api/public/events/" + token + "/gift-kinds")).andExpect(status().isOk())
            .andExpect(jsonPath("$['" + cause + "'].charityUrl").value("https://greenearth.example/donate")).andExpect(jsonPath("$['" + dinner + "'].kind").value("EXPERIENCE"));
        // The database itself refuses money toward a charity wish.
        var refused = assertThrows(org.springframework.dao.DataIntegrityViolationException.class, () -> db.update(
            "INSERT INTO payment_orders(id,event_id,gift_id,contribution_type,amount_paise,idempotency_key) VALUES(?,?,?,'GIFT_FULL',10000,?)",
            UUID.randomUUID(), event, cause, UUID.randomUUID().toString()));
        assertTrue(refused.getMessage().contains("charity_gift_no_payment") || refused.getMessage().contains("donated directly"), refused.getMessage());
        // A gift guests already paid toward cannot be turned into a charity wish.
        db.update("INSERT INTO payment_orders(id,event_id,gift_id,contribution_type,amount_paise,idempotency_key,status) VALUES(?,?,?,'GIFT_FULL',10000,?,'CAPTURED')",
            UUID.randomUUID(), event, paid, UUID.randomUUID().toString());
        mvc.perform(put("/api/v1/events/" + event + "/gifts/" + paid + "/kind").with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content(q("{'kind':'CHARITY','charityName':'X','charityUrl':'https://x.example'}"))).andExpect(status().isConflict());
        db.update("UPDATE platform_event_module_grants SET enabled=FALSE WHERE event_id=? AND module_code='EXPERIENCE_GIFTS'", event);
        mvc.perform(get("/api/public/events/" + token + "/gift-kinds")).andExpect(content().json("{}"));
    }
}
