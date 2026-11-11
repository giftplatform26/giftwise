package com.giftplatform.acquisition;

import com.giftplatform.monetization.CommercialAccessService;
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
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;
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
class AcquisitionIntegrationTest {
    @Container static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");
    @DynamicPropertySource static void database(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl); r.add("spring.datasource.username", postgres::getUsername); r.add("spring.datasource.password", postgres::getPassword);
    }
    @Autowired JdbcTemplate db;
    @Autowired MockMvc mvc;
    @Autowired CommercialAccessService commerce;
    @MockitoBean ActorContext actor;
    UUID host, event, token;

    @BeforeEach void setup() {
        host = newUser("Meera Host"); event = UUID.randomUUID();
        db.update("INSERT INTO events(id,title,occasion_type,status,owner_id,host_display_name,event_date) VALUES(?,'Meera & Arjun','WEDDING','PUBLISHED',?,'Meera',DATE '2026-12-12')", event, host);
        token = db.queryForObject("SELECT public_token FROM events WHERE id=?", UUID.class, event);
        for (String m : new String[]{"STORY_CARDS", "PARTNER_ACCOUNTS", "GROUP_COLLECTIONS", "GUEST_FIRST_FREE", "CELEBRATION_SHOWCASE", "GUESTBOOK", "TRY_WITHOUT_ACCOUNT", "SHAGUN_REGISTER", "CO_HOSTS"}) grant(host, m);
        for (String m : new String[]{"STORY_CARDS", "PARTNER_ACCOUNTS", "GUEST_FIRST_FREE", "CELEBRATION_SHOWCASE", "SHAGUN_REGISTER", "CO_HOSTS"}) eventGrant(event, m, true);
        as(host);
    }
    UUID newUser(String name) {
        UUID id = UUID.randomUUID();
        db.update("INSERT INTO users(id,email,password_hash,role,full_name,email_verified_at) VALUES(?,?,?,'CUSTOMER',?,now())", id, id + "@example.test", "hash", name);
        return id;
    }
    void grant(UUID who, String m) {
        db.update("UPDATE platform_module_controls SET enabled=TRUE WHERE module_code=?", m);
        db.update("INSERT INTO platform_host_module_grants(host_id,module_code,enabled) VALUES(?,?,TRUE) ON CONFLICT(host_id,module_code) DO UPDATE SET enabled=TRUE", who, m);
    }
    void eventGrant(UUID e, String m, boolean on) {
        db.update("INSERT INTO platform_event_module_grants(event_id,module_code,enabled) VALUES(?,?,?) ON CONFLICT(event_id,module_code) DO UPDATE SET enabled=EXCLUDED.enabled", e, m, on);
    }
    void as(UUID who) { when(actor.requireAuthenticatedUserId()).thenReturn(who); }
    String q(String s) { return s.replace('\'', '"'); }
    UUID wish(String name, String status) {
        UUID w = UUID.randomUUID();
        db.update("INSERT INTO event_guestbook(id,event_id,guest_name,message,status) VALUES(?,?,?,'Blessings to you both',?)", w, event, name, status);
        return w;
    }
    UUID bank(UUID owner) {
        UUID b = UUID.randomUUID();
        db.update("""
            INSERT INTO host_bank_accounts(id,host_id,account_number_encrypted,account_fingerprint,account_number_masked,ifsc_encrypted,ifsc_masked,account_holder_encrypted,verification_status,host_confirmed_at)
            VALUES(?,?,'enc',?,'XXXX1234','enc','HDFC0****','enc','PENDING',now())""", b, owner, b.toString().replace("-", ""));
        return b;
    }

    @Test void publicFlagsAndEventFeaturesExposeOnlyTheNewSwitches() throws Exception {
        mvc.perform(get("/api/public/acquisition-modules")).andExpect(status().isOk()).andExpect(jsonPath("$.TRY_WITHOUT_ACCOUNT").value(true))
            .andExpect(jsonPath("$.GROUP_GIFTING").doesNotExist());
        mvc.perform(get("/api/public/events/" + token + "/features")).andExpect(jsonPath("$.STORY_CARDS").value(true));
        eventGrant(event, "STORY_CARDS", false);
        mvc.perform(get("/api/public/events/" + token + "/features")).andExpect(jsonPath("$.STORY_CARDS").value(false));
        db.update("UPDATE platform_module_controls SET enabled=FALSE WHERE module_code='TRY_WITHOUT_ACCOUNT'");
        mvc.perform(get("/api/public/acquisition-modules")).andExpect(jsonPath("$.TRY_WITHOUT_ACCOUNT").value(false));
    }

    // ---- First celebration free ----

    @Test void guestClaimsFirstCelebrationFreeOnceAndGetsPremium() throws Exception {
        mvc.perform(get("/api/public/events/" + token + "/first-free")).andExpect(jsonPath("$.available").value(true)).andExpect(jsonPath("$.days").value(90));
        UUID guest = newUser("Priya Guest"); as(guest);
        assertFalse(commerce.premium(guest));
        mvc.perform(post("/api/v1/me/offers/first-free").with(user("g")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(q("{'eventToken':'" + token + "'}")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.granted").value(true));
        assertTrue(commerce.premium(guest));
        mvc.perform(post("/api/v1/me/offers/first-free").with(user("g")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(q("{'eventToken':'" + token + "'}")))
            .andExpect(jsonPath("$.granted").value(false)).andExpect(jsonPath("$.message").value(containsString("already")));
        UUID old = newUser("Old Guest"); db.update("UPDATE users SET created_at=now()-interval '30 days' WHERE id=?", old); as(old);
        mvc.perform(post("/api/v1/me/offers/first-free").with(user("o")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(q("{'eventToken':'" + token + "'}")))
            .andExpect(jsonPath("$.granted").value(false));
        as(host);
        mvc.perform(post("/api/v1/me/offers/first-free").with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(q("{'eventToken':'" + token + "'}")))
            .andExpect(status().isBadRequest());
        eventGrant(event, "GUEST_FIRST_FREE", false);
        mvc.perform(get("/api/public/events/" + token + "/first-free")).andExpect(jsonPath("$.available").value(false));
        UUID late = newUser("Late Guest"); as(late);
        mvc.perform(post("/api/v1/me/offers/first-free").with(user("l")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(q("{'eventToken':'" + token + "'}")))
            .andExpect(status().isBadRequest());
    }

    // ---- Showcase ----

    @Test void showcaseNeedsHostConsentAndAdminApproval() throws Exception {
        UUID ok = wish("Asha Rao", "APPROVED"), pending = wish("Spam Bot", "PENDING");
        String base = "/api/v1/events/" + event + "/showcase";
        mvc.perform(put(base).with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content(q("{'displayTitle':'Meera & Arjun, Kochi','featuredWishIds':['" + pending + "']}"))).andExpect(status().isBadRequest());
        mvc.perform(put(base).with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content(q("{'displayTitle':'Meera & Arjun, Kochi','blurb':'A monsoon wedding','city':'Kochi','featuredWishIds':['" + ok + "']}")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PENDING"));
        mvc.perform(get("/api/public/showcase")).andExpect(jsonPath("$", hasSize(0)));
        when(actor.isPlatformAdmin()).thenReturn(true);
        mvc.perform(get("/api/admin/console/showcase").with(user("a").roles("PLATFORM_ADMIN"))).andExpect(jsonPath("$[0].displayTitle").value("Meera & Arjun, Kochi"));
        mvc.perform(post("/api/admin/console/showcase/" + event + "/review").with(user("a").roles("PLATFORM_ADMIN")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content(q("{'decision':'APPROVED'}"))).andExpect(status().isOk());
        when(actor.isPlatformAdmin()).thenReturn(false);
        mvc.perform(get("/api/public/showcase")).andExpect(jsonPath("$", hasSize(1))).andExpect(jsonPath("$[0].wishes[0].guestName").value("Asha"))
            .andExpect(jsonPath("$[0].monthYear").value("December 2026"));
        // Editing sends it back for review.
        mvc.perform(put(base).with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(q("{'displayTitle':'New title'}"))).andExpect(jsonPath("$.status").value("PENDING"));
        mvc.perform(get("/api/public/showcase")).andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(post(base + "/withdraw").with(user("h")).with(csrf())).andExpect(status().isOk());
        mvc.perform(get("/api/admin/console/showcase").with(user("x"))).andExpect(status().isForbidden());
    }

    // ---- Group collections ----

    @Test void organiserCreatesAPublishedCollectionThatReusesPayments() throws Exception {
        UUID b = bank(host);
        mvc.perform(get("/api/v1/me/collections/accounts").with(user("h"))).andExpect(jsonPath("$[0].kind").value("BANK"));
        String body = mvc.perform(post("/api/v1/me/collections").with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(q("{'recipientName':'Ravi','occasion':'farewell','message':'Thank you Ravi!','targetPaise':1500000,'closesOn':'" + LocalDate.now().plusDays(10) + "','accountKind':'BANK','accountId':'" + b + "'}")))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        var node = JsonMapper.builder().build().readTree(body);
        UUID cid = UUID.fromString(node.get("eventId").asString()); String ctoken = node.get("publicToken").asString();
        assertEquals("PUBLISHED", db.queryForObject("SELECT status FROM events WHERE id=?", String.class, cid));
        assertEquals("COLLECTION", db.queryForObject("SELECT kind FROM events WHERE id=?", String.class, cid));
        assertEquals("PUBLIC", db.queryForObject("SELECT visibility FROM gifts WHERE event_id=?", String.class, cid));
        mvc.perform(get("/api/public/events/" + ctoken + "/collection")).andExpect(status().isOk()).andExpect(jsonPath("$.recipientName").value("Ravi"))
            .andExpect(jsonPath("$.occasionLabel").value("Farewell")).andExpect(jsonPath("$.raisedPaise").value(0)).andExpect(jsonPath("$.targetPaise").value(1500000));
        mvc.perform(get("/api/v1/me/collections").with(user("h"))).andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(post("/api/v1/me/collections").with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content(q("{'recipientName':'X','occasion':'FAREWELL','targetPaise':1500000,'accountKind':'BANK','accountId':'" + UUID.randomUUID() + "'}"))).andExpect(status().isBadRequest());
        eventGrant(cid, "GROUP_COLLECTIONS", false);
        mvc.perform(get("/api/public/events/" + ctoken + "/collection")).andExpect(status().isNotFound());
        assertNotEquals(0, db.queryForObject("SELECT count(*) FROM event_payment_account_links WHERE event_id=? AND preferred", Integer.class, cid));
    }

    @Test void collectionsNeedTheHostSwitch() throws Exception {
        db.update("UPDATE platform_host_module_grants SET enabled=FALSE WHERE host_id=? AND module_code='GROUP_COLLECTIONS'", host);
        mvc.perform(get("/api/v1/me/collections").with(user("h"))).andExpect(status().isNotFound());
    }

    // ---- Partners ----

    @Test void partnerHandsACelebrationToTheClientAndStaysAsCoHost() throws Exception {
        mvc.perform(put("/api/v1/me/partner").with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content(q("{'slug':'lotus-weddings','businessName':'Lotus Weddings','kind':'planner','city':'Kochi','phone':'+91 98765 43210','website':'https://lotus.example'}")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.phone").value("+919876543210"));
        UUID other = newUser("Rival"); grant(other, "PARTNER_ACCOUNTS"); as(other);
        mvc.perform(put("/api/v1/me/partner").with(user("o")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content(q("{'slug':'lotus-weddings','businessName':'Copy','kind':'PLANNER'}"))).andExpect(status().isConflict());
        as(host);
        String body = mvc.perform(post("/api/v1/events/" + event + "/partner/handover").with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content(q("{'clientLabel':'Meera & Arjun'}"))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String t = JsonMapper.builder().build().readTree(body).get("token").asString();
        mvc.perform(get("/api/public/events/" + token + "/partner")).andExpect(jsonPath("$.businessName").value("Lotus Weddings"));
        mvc.perform(get("/api/v1/me/partner/clients").with(user("h"))).andExpect(jsonPath("$[0].handover").value("INVITED"));
        UUID client = newUser("Meera Client"); as(client);
        mvc.perform(get("/api/v1/me/partner-handover/" + t).with(user("c"))).andExpect(jsonPath("$.partnerName").value("Lotus Weddings"));
        mvc.perform(post("/api/v1/me/partner-handover/" + t + "/accept").with(user("c")).with(csrf())).andExpect(status().isOk());
        assertEquals(client, db.queryForObject("SELECT owner_id FROM events WHERE id=?", UUID.class, event));
        assertEquals("PARTNER", db.queryForObject("SELECT role FROM event_co_hosts WHERE event_id=? AND user_id=?", String.class, event, host));
        assertTrue(Boolean.TRUE.equals(db.queryForObject("SELECT enabled FROM platform_host_module_grants WHERE host_id=? AND module_code='SHAGUN_REGISTER'", Boolean.class, client)));
        mvc.perform(post("/api/v1/me/partner-handover/" + t + "/accept").with(user("c")).with(csrf())).andExpect(status().isNotFound());
        as(host);
        mvc.perform(get("/api/v1/events/" + event + "/register").with(user("h"))).andExpect(status().isOk());
        mvc.perform(get("/api/v1/events/" + event).with(user("h"))).andExpect(status().isNotFound());
        mvc.perform(get("/api/public/partners/lotus-weddings")).andExpect(status().isOk()).andExpect(jsonPath("$.businessName").value("Lotus Weddings"));
    }

    @Test void handoverIsRefusedOnceMoneyIsInvolved() throws Exception {
        mvc.perform(put("/api/v1/me/partner").with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content(q("{'slug':'venue-one','businessName':'Venue One','kind':'VENUE'}"))).andExpect(status().isOk());
        db.update("INSERT INTO event_payment_account_links(event_id,host_id,bank_account_id,preferred) VALUES(?,?,?,TRUE)", event, host, bank(host));
        mvc.perform(post("/api/v1/events/" + event + "/partner/handover").with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.error").value(containsString("before connecting receiving accounts")));
    }
}
