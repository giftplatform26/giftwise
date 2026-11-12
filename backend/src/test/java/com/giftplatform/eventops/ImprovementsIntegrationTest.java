package com.giftplatform.eventops;

import com.giftplatform.attention.AttentionService;
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
class ImprovementsIntegrationTest {
    @Container static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");
    @DynamicPropertySource static void database(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl); r.add("spring.datasource.username", postgres::getUsername); r.add("spring.datasource.password", postgres::getPassword);
    }
    @Autowired JdbcTemplate db;
    @Autowired MockMvc mvc;
    @Autowired AttentionService attention;
    @MockitoBean ActorContext actor;
    UUID host, event, token;

    @BeforeEach void setup() {
        host = newUser("Meera Host"); event = UUID.randomUUID();
        db.update("INSERT INTO events(id,title,occasion_type,status,owner_id,host_display_name,event_date) VALUES(?,'Meera & Arjun','WEDDING','PUBLISHED',?,'Meera',DATE '2026-12-12')", event, host);
        token = db.queryForObject("SELECT public_token FROM events WHERE id=?", UUID.class, event);
        as(host);
    }
    UUID newUser(String name) {
        UUID id = UUID.randomUUID();
        db.update("INSERT INTO users(id,email,password_hash,role,full_name) VALUES(?,?,?,'CUSTOMER',?)", id, id + "@example.test", "hash", name);
        return id;
    }
    void as(UUID who) { when(actor.requireAuthenticatedUserId()).thenReturn(who); }
    void admin() { when(actor.isPlatformAdmin()).thenReturn(true); }
    String q(String s) { return s.replace('\'', '"'); }
    void applyWedding() throws Exception {
        admin();
        for (String scope : new String[]{"PLATFORM", "HOST", "EVENT"})
            mvc.perform(post("/api/admin/console/presets/WEDDING/apply").with(user("a").roles("PLATFORM_ADMIN")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(q("{'scope':'" + scope + "','hostId':'" + host + "','eventId':'" + event + "'}"))).andExpect(status().isOk());
        when(actor.isPlatformAdmin()).thenReturn(false);
    }

    // ---- Presets ----

    @Test void weddingPresetSwitchesOnEveryLevelInOneGo() throws Exception {
        admin();
        mvc.perform(get("/api/admin/console/presets").with(user("a").roles("PLATFORM_ADMIN"))).andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.code=='WEDDING')].modules[*]", hasItem("SEATING_PLAN")));
        applyWedding();
        mvc.perform(get("/api/v1/events/" + event + "/features").with(user("h"))).andExpect(jsonPath("$.SEATING_PLAN").value(true))
            .andExpect(jsonPath("$.GUEST_CHECKIN").value(true)).andExpect(jsonPath("$.MEMORY_ALBUM").value(true)).andExpect(jsonPath("$.PARTNER_ACCOUNTS").value(false));
        admin();
        mvc.perform(post("/api/admin/console/presets/WEDDING/apply").with(user("a").roles("PLATFORM_ADMIN")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content(q("{'scope':'HOST','hostId':'" + host + "'}"))).andExpect(jsonPath("$.changed").value(0));
        mvc.perform(post("/api/admin/console/presets/WEDDING/apply").with(user("a").roles("PLATFORM_ADMIN")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content(q("{'scope':'MARS'}"))).andExpect(status().isBadRequest());
        when(actor.isPlatformAdmin()).thenReturn(false);
        mvc.perform(get("/api/admin/console/presets").with(user("h"))).andExpect(status().isForbidden());
    }

    @Test void oneModuleCanBeGrantedToEveryHost() throws Exception {
        UUID other = newUser("Other");
        admin();
        mvc.perform(post("/api/admin/console/presets/modules/GIFT_LEDGER/all-hosts").with(user("a").roles("PLATFORM_ADMIN")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content("{\"enabled\":true}")).andExpect(status().isOk());
        assertTrue(db.queryForObject("SELECT enabled FROM platform_host_module_grants WHERE host_id=? AND module_code='GIFT_LEDGER'", Boolean.class, other));
        mvc.perform(post("/api/admin/console/presets/modules/NOT_A_MODULE/all-hosts").with(user("a").roles("PLATFORM_ADMIN")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content("{\"enabled\":true}")).andExpect(status().isBadRequest());
    }

    // ---- Seating ----

    @Test void familiesAreSeatedWithoutOverfillingAndGuestsCanFindTheirTable() throws Exception {
        applyWedding();
        db.update("INSERT INTO event_rsvps(id,event_id,guest_name,guest_email,attendance,party_size) VALUES(?,?,'Asha Rao','a@x.com','YES',4),(?,?,'Ravi Kumar','r@x.com','YES',3)",
            UUID.randomUUID(), event, UUID.randomUUID(), event);
        String base = "/api/v1/events/" + event + "/seating";
        mvc.perform(post(base + "/tables").with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(q("{'name':'Table 1','capacity':6}"))).andExpect(status().isOk());
        UUID t1 = db.queryForObject("SELECT id FROM event_seating_tables WHERE event_id=?", UUID.class, event);
        mvc.perform(get(base).with(user("h"))).andExpect(jsonPath("$.unseated", hasSize(2))).andExpect(jsonPath("$.expectedPeople").value(7));
        mvc.perform(post(base + "/assign").with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(q("{'guestName':'Asha Rao','partySize':4,'tableId':'" + t1 + "'}"))).andExpect(status().isOk());
        mvc.perform(post(base + "/assign").with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(q("{'guestName':'Ravi Kumar','partySize':3,'tableId':'" + t1 + "'}")))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.error").value("Table 1 has 2 seats left"));
        mvc.perform(get(base).with(user("h"))).andExpect(jsonPath("$.tables[0].seated").value(4)).andExpect(jsonPath("$.unseated", hasSize(1)));
        mvc.perform(put(base + "/tables/" + t1).with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(q("{'name':'Table 1','capacity':2}"))).andExpect(status().isConflict());
        mvc.perform(get("/api/public/celebrations/" + token + "/find-table").param("name", "asha")).andExpect(status().isNotFound());
        mvc.perform(put(base + "/settings").with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"guestLookup\":true}")).andExpect(status().isOk());
        mvc.perform(get("/api/public/celebrations/" + token + "/find-table").param("name", "asha")).andExpect(jsonPath("$[0].table").value("Table 1"));
        mvc.perform(get("/api/public/celebrations/" + token + "/find-table").param("name", "rao")).andExpect(jsonPath("$[0].name").value("Asha Rao"));
        mvc.perform(get("/api/public/celebrations/" + token + "/find-table").param("name", "as")).andExpect(status().isBadRequest());
    }

    // ---- Passes ----

    @Test void passesCheckFamiliesInByCodeOrLinkOnce() throws Exception {
        applyWedding();
        db.update("INSERT INTO event_rsvps(id,event_id,guest_name,guest_email,attendance,party_size) VALUES(?,?,'Asha Rao','a@x.com','YES',4)", UUID.randomUUID(), event);
        mvc.perform(post("/api/v1/events/" + event + "/passes/generate").with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(jsonPath("$.created").value(1));
        mvc.perform(post("/api/v1/events/" + event + "/passes/generate").with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(jsonPath("$.created").value(0));
        String code = db.queryForObject("SELECT short_code FROM event_guest_passes WHERE event_id=?", String.class, event);
        UUID passToken = db.queryForObject("SELECT pass_token FROM event_guest_passes WHERE event_id=?", UUID.class, event);
        mvc.perform(get("/api/public/passes/" + passToken)).andExpect(status().isOk()).andExpect(jsonPath("$.guestName").value("Asha Rao")).andExpect(jsonPath("$.partySize").value(4));
        mvc.perform(post("/api/v1/events/" + event + "/checkin/pass").with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(q("{'code':'" + code.toLowerCase() + "'}")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.alreadyArrived").value(false));
        mvc.perform(post("/api/v1/events/" + event + "/checkin/pass").with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(q("{'code':'https://x/pass/" + passToken + "'}")))
            .andExpect(jsonPath("$.alreadyArrived").value(true));
        assertEquals(1, db.queryForObject("SELECT count(*) FROM event_checkins WHERE event_id=?", Integer.class, event));
        mvc.perform(post("/api/v1/events/" + event + "/checkin/pass").with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(q("{'code':'ZZZZZZ'}"))).andExpect(status().isBadRequest());
        UUID pid = db.queryForObject("SELECT id FROM event_guest_passes WHERE event_id=?", UUID.class, event);
        mvc.perform(post("/api/v1/events/" + event + "/passes/" + pid + "/revoke").with(user("h")).with(csrf())).andExpect(status().isOk());
        mvc.perform(get("/api/public/passes/" + passToken)).andExpect(status().isNotFound());
    }

    // ---- Attention and digest ----

    @Test void attentionListsWhatNeedsTheHostAndTheDigestSendsOncePerDay() throws Exception {
        db.update("UPDATE platform_module_controls SET enabled=TRUE WHERE module_code='HOST_NOTIFICATIONS'");
        db.update("INSERT INTO platform_host_module_grants(host_id,module_code,enabled) VALUES(?,'HOST_NOTIFICATIONS',TRUE)", host);
        db.update("INSERT INTO event_guestbook(id,event_id,guest_name,message,status) VALUES(?,?,'Asha','Blessings','PENDING')", UUID.randomUUID(), event);
        db.update("INSERT INTO event_tasks(id,event_id,title,due_date,created_by) VALUES(?,?,'Book band',CURRENT_DATE-3,?)", UUID.randomUUID(), event, host);
        db.update("INSERT INTO event_rsvps(id,event_id,guest_name,guest_email,attendance,party_size) VALUES(?,?,'R','r@x.com','YES',2)", UUID.randomUUID(), event);
        mvc.perform(get("/api/v1/me/attention").with(user("h"))).andExpect(status().isOk())
            .andExpect(jsonPath("$.items[0].kind").value("TASKS_LATE")).andExpect(jsonPath("$.summary").value(containsString("1 wish to approve")))
            .andExpect(jsonPath("$.summary").value(containsString("1 new RSVP")));
        int first = attention.sendDigests();
        assertTrue(first >= 1);
        assertEquals(0, attention.sendDigests());
        assertEquals(1, db.queryForObject("SELECT count(*) FROM transactional_email_outbox WHERE template_key='attention-digest' AND recipient=?", Integer.class, host + "@example.test"));
        db.update("UPDATE platform_host_module_grants SET enabled=FALSE WHERE host_id=? AND module_code='HOST_NOTIFICATIONS'", host);
        mvc.perform(get("/api/v1/me/attention").with(user("h"))).andExpect(status().isNotFound());
    }

    // ---- Measurement ----

    @Test void acquisitionEventsAreCountedForTheAdmin() throws Exception {
        mvc.perform(post("/api/public/acquisition-events").contentType(MediaType.APPLICATION_JSON).content(q("{'kind':'TRY_STARTED'}"))).andExpect(status().isNoContent());
        mvc.perform(post("/api/public/acquisition-events").contentType(MediaType.APPLICATION_JSON).content(q("{'kind':'STORY_CARD_SHARED','eventToken':'" + token + "'}"))).andExpect(status().isNoContent());
        mvc.perform(post("/api/public/acquisition-events").contentType(MediaType.APPLICATION_JSON).content(q("{'kind':'OFFER_CLAIMED'}"))).andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/me/acquisition-events").with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(q("{'kind':'TRY_CLAIMED'}"))).andExpect(status().isNoContent());
        mvc.perform(post("/api/v1/me/acquisition-events").with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(q("{'kind':'TRY_CLAIMED'}"))).andExpect(status().isNoContent());
        admin();
        mvc.perform(get("/api/admin/console/growth-results").with(user("a").roles("PLATFORM_ADMIN"))).andExpect(status().isOk())
            .andExpect(jsonPath("$.events.TRY_STARTED").value(1)).andExpect(jsonPath("$.events.TRY_CLAIMED").value(1))
            .andExpect(jsonPath("$.events.STORY_CARD_SHARED").value(1)).andExpect(jsonPath("$.tryClaimedThenPublished").value(1));
        for (int i = 0; i < 31; i++) mvc.perform(post("/api/public/acquisition-events").contentType(MediaType.APPLICATION_JSON).content(q("{'kind':'TRY_STARTED'}")));
        assertEquals(30, db.queryForObject("SELECT count(*) FROM acquisition_events WHERE kind='TRY_STARTED'", Integer.class));
    }

    // ---- Setup guide ----

    @Test void setupGuideShowsOnlySwitchedOnTools() throws Exception {
        mvc.perform(get("/api/v1/events/" + event + "/setup").with(user("h"))).andExpect(status().isOk())
            .andExpect(jsonPath("$.steps[*].code", not(hasItem("SEATING_PLAN")))).andExpect(jsonPath("$.steps[*].code", hasItem("PUBLISH")));
        applyWedding();
        mvc.perform(post("/api/v1/events/" + event + "/tasks/starter").with(user("h")).with(csrf())).andExpect(status().isOk());
        mvc.perform(get("/api/v1/events/" + event + "/setup").with(user("h"))).andExpect(jsonPath("$.steps[*].code", hasItem("SEATING_PLAN")))
            .andExpect(jsonPath("$.steps[?(@.code=='PLANNING_CHECKLIST')].done").value(true)).andExpect(jsonPath("$.steps[?(@.code=='PUBLISH')].done").value(true));
    }
}
