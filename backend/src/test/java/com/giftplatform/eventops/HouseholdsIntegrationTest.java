package com.giftplatform.eventops;

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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
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

/** Batch E1: guest list & households, and how households feed seating, passes and check-in. */
@SpringBootTest(properties = {"gift.otp.secret-base64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=", "gift.whatsapp.worker-initial-delay-ms=3600000"})
@Testcontainers
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
class HouseholdsIntegrationTest {
    @Container static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");
    @DynamicPropertySource static void database(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl); r.add("spring.datasource.username", postgres::getUsername); r.add("spring.datasource.password", postgres::getPassword);
    }
    @Autowired JdbcTemplate db;
    @Autowired MockMvc mvc;
    @MockitoBean ActorContext actor;
    UUID host, event;
    String base;

    @BeforeEach void setup() {
        host = newUser("Meera Host"); event = UUID.randomUUID();
        db.update("INSERT INTO events(id,title,occasion_type,status,owner_id,host_display_name,event_date) VALUES(?,'Meera & Arjun','WEDDING','PUBLISHED',?,'Meera',DATE '2026-12-12')", event, host);
        base = "/api/v1/events/" + event + "/households";
        as(host);
    }
    UUID newUser(String name) {
        UUID id = UUID.randomUUID();
        db.update("INSERT INTO users(id,email,password_hash,role,full_name) VALUES(?,?,?,'CUSTOMER',?)", id, id + "@example.test", "hash", name);
        return id;
    }
    void as(UUID who) { when(actor.requireAuthenticatedUserId()).thenReturn(who); }
    static String q(String s) { return s.replace('\'', '"'); }
    MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder b, String body) { return b.with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(q(body)); }
    void enable(String... modules) {
        for (String m : modules) {
            db.update("UPDATE platform_module_controls SET enabled=TRUE WHERE module_code=?", m);
            db.update("INSERT INTO platform_host_module_grants(host_id,module_code,enabled) VALUES(?,?,TRUE) ON CONFLICT(host_id,module_code) DO UPDATE SET enabled=TRUE", host, m);
            db.update("INSERT INTO platform_event_module_grants(event_id,module_code,enabled) VALUES(?,?,TRUE) ON CONFLICT(event_id,module_code) DO UPDATE SET enabled=TRUE", event, m);
        }
    }
    void eventSwitch(String module, boolean on) { db.update("UPDATE platform_event_module_grants SET enabled=? WHERE event_id=? AND module_code=?", on, event, module); }

    @Test void guestListIsOffUntilSwitchedOnAtEveryLevel() throws Exception {
        mvc.perform(get(base).with(user("h"))).andExpect(status().isNotFound());
        enable("GUEST_HOUSEHOLDS");
        mvc.perform(get(base).with(user("h"))).andExpect(status().isOk()).andExpect(jsonPath("$.households", hasSize(0)));
        eventSwitch("GUEST_HOUSEHOLDS", false);
        mvc.perform(get(base).with(user("h"))).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/events/" + event + "/features").with(user("h"))).andExpect(jsonPath("$.GUEST_HOUSEHOLDS").value(false));
        eventSwitch("GUEST_HOUSEHOLDS", true);
        mvc.perform(get("/api/v1/events/" + event + "/features").with(user("h"))).andExpect(jsonPath("$.GUEST_HOUSEHOLDS").value(true));
        as(newUser("Stranger"));
        mvc.perform(get(base).with(user("x"))).andExpect(status().isNotFound());
    }

    @Test void householdsCountMembersAndTrackSideRsvpAndMeals() throws Exception {
        enable("GUEST_HOUSEHOLDS");
        mvc.perform(json(post(base), "{'name':'Sharma family','side':'BRIDE','group':'Pune cousins','phone':'+91 98765 43210','city':'Pune','outstation':true,'rsvp':'YES','members':["
                + "{'name':'Priya','ageGroup':'ADULT','meal':'VEG'},{'name':'Rohan','ageGroup':'CHILD','meal':'JAIN'},{'name':'Dadi','attending':false}]}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.party").value(2)).andExpect(jsonPath("$.children").value(1)).andExpect(jsonPath("$.phone").value("+919876543210"));
        mvc.perform(json(post(base), "{'name':'Ravi Kumar','side':'GROOM','expectedSize':3}")).andExpect(jsonPath("$.party").value(3));
        mvc.perform(json(post(base), "{'name':'  sharma   FAMILY. '}")).andExpect(status().isConflict()).andExpect(jsonPath("$.error", containsString("already on the guest list")));
        mvc.perform(json(post(base), "{'name':'X','side':'MARS'}")).andExpect(status().isBadRequest());
        mvc.perform(json(post(base), "{'name':'X','email':'not-an-email'}")).andExpect(status().isBadRequest());
        mvc.perform(json(post(base), "{'name':'X','expectedSize':31}")).andExpect(status().isBadRequest());
        mvc.perform(get(base).with(user("h"))).andExpect(jsonPath("$.totals.households").value(2)).andExpect(jsonPath("$.totals.people").value(5))
            .andExpect(jsonPath("$.totals.yesPeople").value(2)).andExpect(jsonPath("$.totals.outstation").value(1))
            .andExpect(jsonPath("$.totals.meals.VEG").value(1)).andExpect(jsonPath("$.totals.meals.JAIN").value(1))
            .andExpect(jsonPath("$.groups[0]").value("Pune cousins"));

        UUID ravi = db.queryForObject("SELECT id FROM event_households WHERE event_id=? AND name='Ravi Kumar'", UUID.class, event);
        mvc.perform(json(post(base + "/" + ravi + "/status"), "{'inviteStatus':'SENT','rsvp':'MAYBE'}")).andExpect(jsonPath("$.inviteStatus").value("SENT")).andExpect(jsonPath("$.rsvp").value("MAYBE"));
        long version = db.queryForObject("SELECT version FROM event_households WHERE id=?", Long.class, ravi);
        mvc.perform(json(put(base + "/" + ravi), "{'name':'Ravi Kumar','expectedSize':4,'version':" + version + "}")).andExpect(jsonPath("$.party").value(4));
        mvc.perform(json(put(base + "/" + ravi), "{'name':'Ravi Kumar','expectedSize':5,'version':" + version + "}")).andExpect(status().isConflict());
        mvc.perform(post(base + "/" + ravi + "/delete").with(user("h")).with(csrf())).andExpect(status().isOk());
        mvc.perform(post(base + "/" + ravi + "/delete").with(user("h")).with(csrf())).andExpect(status().isNotFound());
    }

    @Test void pastedSpreadsheetRowsAreImportedAndBadRowsReported() throws Exception {
        enable("GUEST_HOUSEHOLDS");
        String text = "Name,People,Phone,Side,Group,City,Email,Outstation\n"
            + "Sharma family,4,+919876543210,Bride side,College,Pune,,Yes\n"
            + "\"Iyer, Lakshmi\",2,,Groom's side,,Chennai,l@x.com,no\n"
            + "Bad Phone,1,12ab,,,,,\n"
            + "sharma FAMILY,9\n"
            + "Office team,6,,Colleagues";
        mvc.perform(post(base + "/import").with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(new tools.jackson.databind.ObjectMapper().writeValueAsString(java.util.Map.of("text", text))))
            .andExpect(status().isOk()).andExpect(jsonPath("$.created").value(3)).andExpect(jsonPath("$.skipped").value(1))
            .andExpect(jsonPath("$.errors", hasSize(1))).andExpect(jsonPath("$.errors[0].line").value(3));
        assertEquals("GROOM", db.queryForObject("SELECT side FROM event_households WHERE event_id=? AND name='Iyer, Lakshmi'", String.class, event));
        assertEquals("WORK", db.queryForObject("SELECT side FROM event_households WHERE event_id=? AND name='Office team'", String.class, event));
        assertTrue(db.queryForObject("SELECT outstation FROM event_households WHERE event_id=? AND name='Sharma family'", Boolean.class, event));

        db.update("INSERT INTO event_rsvps(id,event_id,guest_name,guest_email,attendance,party_size) VALUES(?,?,'Asha Rao','a@x.com','YES',3),(?,?,'Sharma Family','s@x.com','YES',2)",
            UUID.randomUUID(), event, UUID.randomUUID(), event);
        mvc.perform(post(base + "/import-existing").with(user("h")).with(csrf())).andExpect(jsonPath("$.created").value(1)).andExpect(jsonPath("$.skipped").value(1));
        assertEquals("YES", db.queryForObject("SELECT rsvp FROM event_households WHERE event_id=? AND name='Asha Rao'", String.class, event));

        db.update("UPDATE event_households SET note='=HYPERLINK(\"x\")' WHERE event_id=? AND name='Asha Rao'", event);
        String csv = mvc.perform(get(base + "/export.csv").with(user("h"))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(csv.contains("\"Iyer, Lakshmi\""));
        assertTrue(csv.contains("\"'=HYPERLINK(\"\"x\"\")\""), csv);
    }

    @Test void householdsFeedSeatingPassesAndCheckinOnlyWhileSwitchedOn() throws Exception {
        enable("GUEST_HOUSEHOLDS", "SEATING_PLAN", "GUEST_PASSES", "GUEST_CHECKIN");
        db.update("INSERT INTO event_rsvps(id,event_id,guest_name,guest_email,attendance,party_size) VALUES(?,?,'Sharma Family','s@x.com','YES',6)", UUID.randomUUID(), event);
        mvc.perform(json(post(base), "{'name':'Sharma family','rsvp':'YES','members':[{'name':'A'},{'name':'B'}]}")).andExpect(status().isOk());
        mvc.perform(json(post(base), "{'name':'Not Coming','rsvp':'NO','expectedSize':4}")).andExpect(status().isOk());
        mvc.perform(json(post(base), "{'name':'Pending Pillai','expectedSize':3}")).andExpect(status().isOk());

        // The curated household wins over the RSVP's party size; "no" households are left out.
        mvc.perform(get("/api/v1/events/" + event + "/seating").with(user("h"))).andExpect(jsonPath("$.expectedPeople").value(5))
            .andExpect(jsonPath("$.unseated[*].name", containsInAnyOrder("Sharma family", "Pending Pillai")))
            .andExpect(jsonPath("$.unseated[?(@.name=='Sharma family')].source", hasItem("HOUSEHOLD")));
        mvc.perform(get("/api/v1/events/" + event + "/checkin").with(user("h"))).andExpect(jsonPath("$.expectedPeople").value(5));
        mvc.perform(get("/api/v1/events/" + event + "/passes").with(user("h"))).andExpect(jsonPath("$.withoutPass").value(2));

        // Switching the guest list off for the event restores the previous behaviour exactly.
        eventSwitch("GUEST_HOUSEHOLDS", false);
        mvc.perform(get("/api/v1/events/" + event + "/seating").with(user("h"))).andExpect(jsonPath("$.expectedPeople").value(6))
            .andExpect(jsonPath("$.unseated[*].source", everyItem(is("RSVP"))));
        mvc.perform(get("/api/v1/events/" + event + "/checkin").with(user("h"))).andExpect(jsonPath("$.expectedPeople").value(6));
    }

    @Test void autoSeatKeepsGroupsTogetherAndNeverOverfills() throws Exception {
        enable("GUEST_HOUSEHOLDS", "SEATING_PLAN");
        String seating = "/api/v1/events/" + event + "/seating";
        mvc.perform(post(seating + "/auto").with(user("h")).with(csrf())).andExpect(status().isBadRequest());
        for (String t : new String[]{"T1", "T2"})
            mvc.perform(json(post(seating + "/tables"), "{'name':'" + t + "','capacity':6}")).andExpect(status().isOk());
        mvc.perform(json(post(base), "{'name':'Bride A','side':'BRIDE','expectedSize':3,'rsvp':'YES'}"));
        mvc.perform(json(post(base), "{'name':'Groom A','side':'GROOM','expectedSize':4,'rsvp':'YES'}"));
        mvc.perform(json(post(base), "{'name':'Bride B','side':'BRIDE','expectedSize':3,'rsvp':'YES'}"));
        mvc.perform(json(post(base), "{'name':'Big Family','side':'FRIENDS','expectedSize':7,'rsvp':'YES'}"));
        mvc.perform(post(seating + "/auto").with(user("h")).with(csrf())).andExpect(status().isOk())
            .andExpect(jsonPath("$.families").value(3)).andExpect(jsonPath("$.people").value(10)).andExpect(jsonPath("$.left").value(1));
        Integer brideTables = db.queryForObject("SELECT count(DISTINCT table_id) FROM event_seat_assignments WHERE event_id=? AND guest_name LIKE 'Bride%'", Integer.class, event);
        assertEquals(1, brideTables);
        assertEquals(0, db.queryForObject("""
            SELECT count(*) FROM event_seating_tables t WHERE t.event_id=? AND t.capacity < (SELECT COALESCE(SUM(party_size),0) FROM event_seat_assignments a WHERE a.table_id=t.id)""", Integer.class, event));
        mvc.perform(get(seating).with(user("h"))).andExpect(jsonPath("$.unseated[0].name").value("Big Family")).andExpect(jsonPath("$.unseated[0].group").value("Friends"));
    }

    @Test void coHostsCanManageTheListButGiftDeskHelpersCannot() throws Exception {
        enable("GUEST_HOUSEHOLDS", "CO_HOSTS");
        UUID cohost = newUser("Cousin"), desk = newUser("Desk");
        db.update("INSERT INTO event_co_hosts(event_id,user_id,role,invited_by) VALUES(?,?,'CO_HOST',?),(?,?,'GIFT_DESK',?)", event, cohost, host, event, desk, host);
        as(cohost);
        mvc.perform(json(post(base), "{'name':'Added by cousin'}")).andExpect(status().isOk());
        as(desk);
        mvc.perform(get(base).with(user("d"))).andExpect(status().isForbidden());
    }
}
