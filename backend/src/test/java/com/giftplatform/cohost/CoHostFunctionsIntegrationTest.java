package com.giftplatform.cohost;

import tools.jackson.databind.ObjectMapper;
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
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"gift.otp.secret-base64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="})
@Testcontainers
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
class CoHostFunctionsIntegrationTest {
    @Container static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");
    @DynamicPropertySource static void database(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl); r.add("spring.datasource.username", postgres::getUsername); r.add("spring.datasource.password", postgres::getPassword);
    }
    @Autowired JdbcTemplate db;
    @Autowired MockMvc mvc;
    @MockitoBean ActorContext actor;
    final ObjectMapper json = new ObjectMapper();
    UUID owner, friend, event, token;

    @BeforeEach void setup() {
        owner = newUser(); friend = newUser(); event = UUID.randomUUID();
        db.update("INSERT INTO events(id,title,occasion_type,status,owner_id,host_display_name,event_date) VALUES(?,'Meera & Arjun','WEDDING','PUBLISHED',?,'Meera',DATE '2026-12-12')", event, owner);
        token = db.queryForObject("SELECT public_token FROM events WHERE id=?", UUID.class, event);
        for (String m : new String[]{"CO_HOSTS", "EVENT_FUNCTIONS", "SHAGUN_REGISTER", "LIVE_WALL", "RSVP", "GUESTBOOK"}) grant(m, true);
        as(owner);
    }
    UUID newUser() {
        UUID id = UUID.randomUUID();
        db.update("INSERT INTO users(id,email,password_hash,role,username) VALUES(?,?,?,'CUSTOMER',?)", id, id + "@example.test", "hash", "u" + id.toString().substring(0, 8));
        return id;
    }
    void as(UUID who) { when(actor.requireAuthenticatedUserId()).thenReturn(who); }
    void grant(String module, boolean on) {
        db.update("UPDATE platform_module_controls SET enabled=TRUE WHERE module_code=?", module);
        db.update("INSERT INTO platform_host_module_grants(host_id,module_code,enabled) VALUES(?,?,TRUE) ON CONFLICT(host_id,module_code) DO UPDATE SET enabled=TRUE", owner, module);
        if (!module.equals("RSVP") && !module.equals("GUESTBOOK"))
            db.update("INSERT INTO platform_event_module_grants(event_id,module_code,enabled) VALUES(?,?,?) ON CONFLICT(event_id,module_code) DO UPDATE SET enabled=EXCLUDED.enabled", event, module, on);
    }
    String q(String s) { return s.replace('\'', '"'); }
    String invite(String role) throws Exception {
        String body = mvc.perform(post("/api/v1/events/" + event + "/co-hosts/invites").with(user("o")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content(q("{'label':'Cousin Priya','role':'" + role + "'}"))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return json.readTree(body).get("token").asString();
    }
    void join(String role) throws Exception {
        String t = invite(role); as(friend);
        mvc.perform(post("/api/v1/me/co-host-invites/" + t + "/accept").with(user("f")).with(csrf())).andExpect(status().isOk());
    }

    // ---- Co-hosts ----

    @Test void inviteIsStoredHashedAndWorksOnce() throws Exception {
        String t = invite("CO_HOST");
        assertNoPlaintext(t);
        UUID third = newUser(); as(friend);
        mvc.perform(get("/api/v1/me/co-host-invites/" + t).with(user("f"))).andExpect(status().isOk())
            .andExpect(jsonPath("$.eventTitle").value("Meera & Arjun")).andExpect(jsonPath("$.status").value("OPEN"));
        mvc.perform(post("/api/v1/me/co-host-invites/" + t + "/accept").with(user("f")).with(csrf())).andExpect(status().isOk());
        mvc.perform(post("/api/v1/me/co-host-invites/" + t + "/accept").with(user("f")).with(csrf())).andExpect(status().isOk());
        as(third);
        mvc.perform(post("/api/v1/me/co-host-invites/" + t + "/accept").with(user("t")).with(csrf())).andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").value(containsString("already been used")));
        mvc.perform(post("/api/v1/me/co-host-invites/not-a-real-token-at-all-000000000/accept").with(user("t")).with(csrf())).andExpect(status().isNotFound());
        as(owner);
        mvc.perform(get("/api/v1/events/" + event + "/co-hosts").with(user("o"))).andExpect(jsonPath("$.members", hasSize(1)))
            .andExpect(jsonPath("$.invites[0].status").value("ACCEPTED"));
    }
    void assertNoPlaintext(String t) {
        Integer n = db.queryForObject("SELECT count(*) FROM event_co_host_invites WHERE token_hash=?", Integer.class, t);
        org.junit.jupiter.api.Assertions.assertEquals(0, n);
    }

    @Test void revokedExpiredAndOwnInvitesAreRefused() throws Exception {
        String revoked = invite("CO_HOST");
        UUID id = db.queryForObject("SELECT id FROM event_co_host_invites WHERE event_id=? ORDER BY created_at DESC LIMIT 1", UUID.class, event);
        mvc.perform(post("/api/v1/events/" + event + "/co-hosts/invites/" + id + "/revoke").with(user("o")).with(csrf())).andExpect(status().isOk());
        String expired = invite("CO_HOST");
        db.update("UPDATE event_co_host_invites SET expires_at=now()-interval '1 minute' WHERE accepted_at IS NULL AND revoked_at IS NULL");
        String own = invite("CO_HOST");
        mvc.perform(post("/api/v1/me/co-host-invites/" + own + "/accept").with(user("o")).with(csrf())).andExpect(status().isConflict());
        as(friend);
        mvc.perform(post("/api/v1/me/co-host-invites/" + revoked + "/accept").with(user("f")).with(csrf())).andExpect(jsonPath("$.error").value(containsString("cancelled")));
        mvc.perform(post("/api/v1/me/co-host-invites/" + expired + "/accept").with(user("f")).with(csrf())).andExpect(jsonPath("$.error").value(containsString("expired")));
    }

    @Test void coHostUsesEventDayToolsButNotOwnerAreas() throws Exception {
        join("CO_HOST");
        mvc.perform(get("/api/v1/events/" + event + "/register").with(user("f"))).andExpect(status().isOk()).andExpect(jsonPath("$.canManage").value(true));
        mvc.perform(post("/api/v1/events/" + event + "/register").with(user("f")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content(q("{'giverName':'Ravi','kind':'CASH','amountPaise':50100,'clientKey':'cohost-0001'}"))).andExpect(status().isOk());
        mvc.perform(get("/api/v1/events/" + event + "/register/export.csv").with(user("f"))).andExpect(status().isOk());
        mvc.perform(get("/api/v1/events/" + event + "/live-wall").with(user("f"))).andExpect(status().isOk());
        mvc.perform(get("/api/v1/events/" + event + "/functions").with(user("f"))).andExpect(status().isOk());
        mvc.perform(get("/api/v1/events/" + event + "/rsvps").with(user("f"))).andExpect(status().isOk());
        mvc.perform(get("/api/v1/events/" + event + "/wishes").with(user("f"))).andExpect(status().isOk());
        // Owner-only areas stay closed.
        mvc.perform(get("/api/v1/events/" + event).with(user("f"))).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/events/" + event + "/co-hosts").with(user("f"))).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/events/" + event + "/transactions").with(user("f"))).andExpect(status().is4xxClientError());
        mvc.perform(get("/api/v1/me/co-hosting").with(user("f"))).andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].role").value("CO_HOST")).andExpect(jsonPath("$[0].features.SHAGUN_REGISTER").value(true));
    }

    @Test void giftDeskHelperCanOnlyAddAndViewRegister() throws Exception {
        join("GIFT_DESK");
        mvc.perform(get("/api/v1/events/" + event + "/register").with(user("f"))).andExpect(status().isOk()).andExpect(jsonPath("$.canManage").value(false));
        mvc.perform(post("/api/v1/events/" + event + "/register").with(user("f")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content(q("{'giverName':'Ravi','kind':'CASH','amountPaise':50100,'clientKey':'desk-00001'}"))).andExpect(status().isOk());
        UUID entry = db.queryForObject("SELECT id FROM event_shagun_entries WHERE event_id=?", UUID.class, event);
        mvc.perform(post("/api/v1/events/" + event + "/register/" + entry + "/delete").with(user("f")).with(csrf())).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/events/" + event + "/register/export.csv").with(user("f"))).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/events/" + event + "/live-wall").with(user("f"))).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/events/" + event + "/wishes").with(user("f"))).andExpect(status().isForbidden());
        org.junit.jupiter.api.Assertions.assertEquals(friend, db.queryForObject("SELECT recorded_by FROM event_shagun_entries WHERE id=?", UUID.class, entry));
    }

    @Test void switchingCoHostsOffClosesAccessButOwnerCanStillRemove() throws Exception {
        join("CO_HOST");
        as(owner); grant("CO_HOSTS", false); as(friend);
        mvc.perform(get("/api/v1/events/" + event + "/register").with(user("f"))).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/me/co-hosting").with(user("f"))).andExpect(jsonPath("$", hasSize(0)));
        as(owner);
        mvc.perform(get("/api/v1/events/" + event + "/register").with(user("o"))).andExpect(status().isOk());
        mvc.perform(post("/api/v1/events/" + event + "/co-hosts/" + friend + "/remove").with(user("o")).with(csrf())).andExpect(status().isOk());
        grant("CO_HOSTS", true); as(friend);
        mvc.perform(get("/api/v1/events/" + event + "/register").with(user("f"))).andExpect(status().isNotFound());
    }

    @Test void ownerCanChangeRolesAndCoHostCanLeave() throws Exception {
        join("GIFT_DESK"); as(owner);
        mvc.perform(post("/api/v1/events/" + event + "/co-hosts/" + friend + "/role").with(user("o")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content(q("{'role':'CO_HOST'}"))).andExpect(status().isOk());
        as(friend);
        mvc.perform(get("/api/v1/events/" + event + "/live-wall").with(user("f"))).andExpect(status().isOk());
        mvc.perform(post("/api/v1/me/co-hosting/" + event + "/leave").with(user("f")).with(csrf())).andExpect(status().isOk());
        mvc.perform(get("/api/v1/events/" + event + "/live-wall").with(user("f"))).andExpect(status().isNotFound());
    }

    // ---- Functions ----

    String fn(String name, String date, String start, String end, long version) {
        return q("{'name':'" + name + "','functionDate':" + (date == null ? "null" : "'" + date + "'") + ",'startTime':" + (start == null ? "null" : "'" + start + "'")
            + ",'endTime':" + (end == null ? "null" : "'" + end + "'") + ",'venueName':'Lotus Lawns','mapUrl':'https://maps.example/x','rsvpEnabled':true,'version':" + version + "}");
    }

    @Test void hostBuildsAScheduleAndGuestsAnswerPerFunction() throws Exception {
        String base = "/api/v1/events/" + event + "/functions";
        mvc.perform(post(base).with(user("o")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(fn("Mehendi", "2026-12-10", "16:00", "21:00", 0))).andExpect(status().isOk());
        mvc.perform(post(base).with(user("o")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(fn("Reception", "2026-12-12", "19:00", null, 0))).andExpect(status().isOk());
        mvc.perform(post(base).with(user("o")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(fn("Bad", "2026-12-12", "19:00", "18:00", 0)))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").value("End time must be after the start time"));
        mvc.perform(post(base).with(user("o")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content(q("{'name':'X','mapUrl':'javascript:alert(1)'}"))).andExpect(status().isBadRequest());
        UUID mehendi = db.queryForObject("SELECT id FROM event_functions WHERE name='Mehendi'", UUID.class);
        UUID reception = db.queryForObject("SELECT id FROM event_functions WHERE name='Reception'", UUID.class);
        String answers = q("{'guestName':'Asha','guestEmail':'Asha@Example.com','answers':[{'functionId':'" + mehendi + "','attendance':'YES','partySize':3},{'functionId':'" + reception + "','attendance':'NO'}]}");
        mvc.perform(post("/api/public/celebrations/" + token + "/functions/rsvp").contentType(MediaType.APPLICATION_JSON).content(answers)).andExpect(status().isOk());
        // Changing an answer with the same email updates instead of duplicating.
        mvc.perform(post("/api/public/celebrations/" + token + "/functions/rsvp").contentType(MediaType.APPLICATION_JSON)
            .content(q("{'guestName':'Asha','guestEmail':'asha@example.com','answers':[{'functionId':'" + reception + "','attendance':'YES','partySize':2}]}"))).andExpect(status().isOk());
        mvc.perform(get("/api/public/celebrations/" + token + "/functions")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].name").value("Mehendi"));
        mvc.perform(get(base).with(user("o"))).andExpect(jsonPath("$[0].counts.guests").value(3)).andExpect(jsonPath("$[1].counts.yes").value(1))
            .andExpect(jsonPath("$[1].counts.guests").value(2)).andExpect(jsonPath("$[1].responses", hasSize(1)));
        mvc.perform(post(base + "/order").with(user("o")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("[\"" + reception + "\",\"" + mehendi + "\"]"))
            .andExpect(status().isOk()).andExpect(jsonPath("$[0].name").value("Reception"));
        mvc.perform(post(base + "/order").with(user("o")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("[\"" + reception + "\"]")).andExpect(status().isConflict());
    }

    @Test void editsAreVersionedAndClosedFunctionsRejectAnswers() throws Exception {
        String base = "/api/v1/events/" + event + "/functions";
        mvc.perform(post(base).with(user("o")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(fn("Sangeet", null, null, null, 0))).andExpect(status().isOk());
        UUID id = db.queryForObject("SELECT id FROM event_functions WHERE event_id=?", UUID.class, event);
        mvc.perform(put(base + "/" + id).with(user("o")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content(q("{'name':'Sangeet night','rsvpEnabled':false,'version':1}"))).andExpect(status().isOk()).andExpect(jsonPath("$.rsvpEnabled").value(false));
        mvc.perform(put(base + "/" + id).with(user("o")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content(q("{'name':'Again','version':1}"))).andExpect(status().isConflict());
        mvc.perform(post("/api/public/celebrations/" + token + "/functions/rsvp").contentType(MediaType.APPLICATION_JSON)
            .content(q("{'guestName':'Asha','guestEmail':'a@example.com','answers':[{'functionId':'" + id + "','attendance':'YES','partySize':1}]}"))).andExpect(status().isBadRequest());
        grant("EVENT_FUNCTIONS", false);
        mvc.perform(get("/api/public/celebrations/" + token + "/functions")).andExpect(status().isNotFound());
        mvc.perform(get(base).with(user("o"))).andExpect(status().isNotFound());
    }
}
