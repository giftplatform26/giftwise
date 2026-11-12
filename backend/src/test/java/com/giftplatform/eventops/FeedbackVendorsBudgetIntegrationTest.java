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
import java.util.Map;
import java.nio.file.*;
import org.springframework.mock.web.MockMultipartFile;
import tools.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Batch E3: guest feedback, vendor quotes & bookings, and sharing the budget with family. */
@SpringBootTest(properties = {"gift.otp.secret-base64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=", "gift.whatsapp.worker-initial-delay-ms=3600000", "gift.shared-album.dir=${java.io.tmpdir}/gift-album-it"})
@Testcontainers
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
class FeedbackVendorsBudgetIntegrationTest {
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
        base = "/api/v1/events/" + event;
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

    final ObjectMapper om = new ObjectMapper();
    String body(MockHttpServletRequestBuilder b) throws Exception { return mvc.perform(b).andReturn().getResponse().getContentAsString(); }

    UUID token() { return db.queryForObject("SELECT public_token FROM events WHERE id=?", UUID.class, event); }

    // ---- Feedback ----

    @Test void guestsGiveFeedbackOnlyWhileOpenAndHostsSeeAverages() throws Exception {
        String pub = "/api/public/celebrations/" + token() + "/feedback";
        mvc.perform(get(pub)).andExpect(status().isNotFound());
        enable("GUEST_FEEDBACK");
        mvc.perform(get(pub)).andExpect(status().isOk()).andExpect(jsonPath("$.open").value(false));
        mvc.perform(post(pub).contentType(MediaType.APPLICATION_JSON).content(q("{'overall':5}"))).andExpect(status().isConflict());
        mvc.perform(json(put(base + "/feedback/settings"), "{'collecting':true,'question':'What was your favourite moment?'}")).andExpect(jsonPath("$.collecting").value(true));
        mvc.perform(get(pub)).andExpect(jsonPath("$.open").value(true)).andExpect(jsonPath("$.question").value("What was your favourite moment?"));
        mvc.perform(post(pub).contentType(MediaType.APPLICATION_JSON).content(q("{'overall':5,'food':4,'venue':5,'comment':'Lovely!','answer':'The baraat','guestName':'Asha'}"))).andExpect(status().isCreated());
        mvc.perform(post(pub).contentType(MediaType.APPLICATION_JSON).content(q("{'overall':3,'food':2}"))).andExpect(status().isCreated());
        mvc.perform(post(pub).contentType(MediaType.APPLICATION_JSON).content(q("{'overall':6}"))).andExpect(status().isBadRequest());
        mvc.perform(post(pub).contentType(MediaType.APPLICATION_JSON).content(q("{'food':4}"))).andExpect(status().isBadRequest());
        mvc.perform(get(base + "/feedback").with(user("h"))).andExpect(jsonPath("$.summary.count").value(2)).andExpect(jsonPath("$.summary.overall").value(4.0))
            .andExpect(jsonPath("$.summary.aspects.food").value(3.0)).andExpect(jsonPath("$.summary.aspects.venue").value(5.0))
            .andExpect(jsonPath("$.summary.aspects.hospitality").doesNotExist()).andExpect(jsonPath("$.summary.stars[4]").value(1));
        UUID low = db.queryForObject("SELECT id FROM event_feedback WHERE event_id=? AND overall=3", UUID.class, event);
        mvc.perform(json(post(base + "/feedback/" + low + "/hide"), "{'hidden':true}")).andExpect(status().isOk());
        mvc.perform(get(base + "/feedback").with(user("h"))).andExpect(jsonPath("$.summary.count").value(1)).andExpect(jsonPath("$.responses", hasSize(2)));
        String csv = mvc.perform(get(base + "/feedback/export.csv").with(user("h"))).andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(csv.contains("Lovely!") && csv.contains("The baraat"));
        // Per-device daily limit (5).
        for (int i = 0; i < 3; i++) mvc.perform(post(pub).contentType(MediaType.APPLICATION_JSON).content(q("{'overall':4}"))).andExpect(status().isCreated());
        mvc.perform(post(pub).contentType(MediaType.APPLICATION_JSON).content(q("{'overall':4}"))).andExpect(status().isTooManyRequests());
    }

    // ---- Vendor bookings ----

    String vendor(String body) throws Exception { return mvc.perform(json(post(base + "/vendors"), body)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(); }
    UUID idOf(String json) throws Exception { return UUID.fromString(new tools.jackson.databind.ObjectMapper().readTree(json).path("id").asText()); }

    @Test void quotesAreComparedAcceptedAndPaymentsTrackedWithBudgetSync() throws Exception {
        mvc.perform(get(base + "/vendors").with(user("h"))).andExpect(status().isNotFound());
        enable("VENDOR_BOOKINGS");
        UUID caterer = idOf(vendor("{'name':'Annapurna Caterers','category':'Food','phone':'+91 99999 88888'}"));
        UUID other = idOf(vendor("{'name':'Spice Route','category':'Food'}"));
        mvc.perform(json(post(base + "/vendors"), "{'name':'X','category':'Spaceships'}")).andExpect(status().isBadRequest());
        mvc.perform(json(post(base + "/vendors/" + caterer + "/quotes"), "{'amountPaise':45000000,'details':'500 plates, veg','validUntil':'2026-11-30'}"))
            .andExpect(jsonPath("$.status").value("QUOTED"));
        mvc.perform(json(post(base + "/vendors/" + caterer + "/quotes"), "{'amountPaise':52000000,'details':'500 plates + live counters'}")).andExpect(jsonPath("$.quotes", hasSize(2)));
        UUID cheaper = db.queryForObject("SELECT id FROM event_vendor_quotes WHERE vendor_id=? AND amount_paise=45000000", UUID.class, caterer);
        // Budget off: booking works, no budget line.
        mvc.perform(post(base + "/vendors/" + caterer + "/quotes/" + cheaper + "/accept").with(user("h")).with(csrf()))
            .andExpect(jsonPath("$.status").value("BOOKED")).andExpect(jsonPath("$.bookedPaise").value(45000000)).andExpect(jsonPath("$.budgetLinked").value(false))
            .andExpect(jsonPath("$.quotes[?(@.amountPaise==52000000)].status", hasItem("REJECTED")));
        assertEquals(0, db.queryForObject("SELECT count(*) FROM event_budget_lines WHERE event_id=?", Integer.class, event));

        // Budget on: the next change creates and then keeps a budget line in step.
        enable("EVENT_BUDGET");
        mvc.perform(json(post(base + "/vendors/" + caterer + "/payments"), "{'label':'Advance','amountPaise':10000000,'dueDate':'2026-11-01','paidOn':'2026-11-01'}"))
            .andExpect(jsonPath("$.paidPaise").value(10000000)).andExpect(jsonPath("$.balancePaise").value(35000000)).andExpect(jsonPath("$.budgetLinked").value(true));
        mvc.perform(json(post(base + "/vendors/" + caterer + "/payments"), "{'label':'Balance','amountPaise':35000000,'dueDate':'2026-12-10'}")).andExpect(jsonPath("$.nextDue").value("2026-12-10"));
        var line = db.queryForMap("SELECT * FROM event_budget_lines WHERE event_id=?", event);
        assertEquals(45000000L, ((Number) line.get("committed_paise")).longValue());
        assertEquals(10000000L, ((Number) line.get("paid_paise")).longValue());
        assertEquals("Food", line.get("category"));
        assertEquals("2026-12-10", line.get("due_date").toString());
        UUID balance = db.queryForObject("SELECT id FROM event_vendor_payments WHERE vendor_id=? AND label='Balance'", UUID.class, caterer);
        mvc.perform(json(post(base + "/vendors/" + caterer + "/payments/" + balance + "/paid"), "{'paidOn':'2026-12-09'}")).andExpect(jsonPath("$.balancePaise").value(0));
        assertEquals(45000000L, db.queryForObject("SELECT paid_paise FROM event_budget_lines WHERE event_id=?", Long.class, event));
        mvc.perform(get(base + "/budget").with(user("h"))).andExpect(jsonPath("$.summary.paidPaise").value(45000000)).andExpect(jsonPath("$.access").value("OWNER"));
        mvc.perform(get(base + "/vendors").with(user("h"))).andExpect(jsonPath("$.totals.booked").value(1)).andExpect(jsonPath("$.totals.bookedPaise").value(45000000))
            .andExpect(jsonPath("$.vendors[0].name").value("Annapurna Caterers")).andExpect(jsonPath("$.vendors[1].status").value("ENQUIRED")).andExpect(jsonPath("$.budgetOn").value(true));
        // Cancelling keeps only what was paid; deleting the vendor keeps the budget line.
        mvc.perform(post(base + "/vendors/" + other + "/cancel").with(user("h")).with(csrf())).andExpect(jsonPath("$.status").value("CANCELLED"));
        mvc.perform(post(base + "/vendors/" + caterer + "/delete").with(user("h")).with(csrf())).andExpect(status().isOk());
        assertEquals(1, db.queryForObject("SELECT count(*) FROM event_budget_lines WHERE event_id=?", Integer.class, event));
    }

    @Test void shortlistedDirectoryVendorsCanBeAddedInOneTap() throws Exception {
        enable("VENDOR_BOOKINGS");
        // The vendor directory is an account-wide (host-level) feature, with no event switch.
        db.update("UPDATE platform_module_controls SET enabled=TRUE WHERE module_code='VENDOR_DIRECTORY'");
        db.update("INSERT INTO platform_host_module_grants(host_id,module_code,enabled) VALUES(?,'VENDOR_DIRECTORY',TRUE)", host);
        UUID dir = UUID.randomUUID();
        db.update("INSERT INTO platform_vendors(id,name,category,city,phone,active) VALUES(?,'Lens & Light','PHOTOGRAPHY','Pune','+919000000001',TRUE)", dir);
        mvc.perform(json(post(base + "/vendors"), "{'directoryVendorId':'" + dir + "'}")).andExpect(status().isBadRequest());
        db.update("INSERT INTO user_vendor_shortlist(user_id,vendor_id) VALUES(?,?)", host, dir);
        mvc.perform(get(base + "/vendors").with(user("h"))).andExpect(jsonPath("$.shortlist[0].name").value("Lens & Light"));
        mvc.perform(json(post(base + "/vendors"), "{'directoryVendorId':'" + dir + "'}")).andExpect(jsonPath("$.category").value("Photos")).andExpect(jsonPath("$.phone").value("+919000000001"));
        mvc.perform(get(base + "/vendors").with(user("h"))).andExpect(jsonPath("$.shortlist", hasSize(0)));
    }

    // ---- Budget sharing ----

    @Test void familyCanViewOrEditTheBudgetOnlyWhenShared() throws Exception {
        enable("EVENT_BUDGET", "VENDOR_BOOKINGS", "CO_HOSTS");
        UUID mom = newUser("Mom"), cousin = newUser("Cousin");
        db.update("INSERT INTO event_co_hosts(event_id,user_id,role,invited_by) VALUES(?,?,'CO_HOST',?)", event, cousin, host);
        mvc.perform(json(post(base + "/budget/lines"), "{'category':'Venue','item':'Hall','plannedPaise':100000}")).andExpect(status().isOk());
        // Co-hosts never see money plans.
        as(cousin);
        mvc.perform(get(base + "/budget").with(user("c"))).andExpect(status().isNotFound());
        as(host);
        mvc.perform(get(base + "/budget/sharing").with(user("h"))).andExpect(status().isNotFound());
        enable("BUDGET_SHARING");
        String created = mvc.perform(json(post(base + "/budget/sharing/invites"), "{'label':'Mom','access':'VIEW'}")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String link = new tools.jackson.databind.ObjectMapper().readTree(created).path("token").asText();
        mvc.perform(json(post(base + "/budget/sharing/invites"), "{'label':'X','access':'ADMIN'}")).andExpect(status().isBadRequest());

        as(mom);
        mvc.perform(get(base + "/budget").with(user("m"))).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/me/budget-invites/" + link).with(user("m"))).andExpect(jsonPath("$.access").value("VIEW")).andExpect(jsonPath("$.status").value("OPEN"));
        mvc.perform(post("/api/v1/me/budget-invites/" + link + "/accept").with(user("m")).with(csrf())).andExpect(status().isOk());
        mvc.perform(get(base + "/budget").with(user("m"))).andExpect(status().isOk()).andExpect(jsonPath("$.access").value("VIEW")).andExpect(jsonPath("$.lines", hasSize(1)));
        mvc.perform(get(base + "/vendors").with(user("m"))).andExpect(status().isOk()).andExpect(jsonPath("$.access").value("VIEW"));
        mvc.perform(json(post(base + "/budget/lines"), "{'category':'Food','item':'Dinner'}")).andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error").value("You can view this budget but not change it"));
        mvc.perform(json(post(base + "/vendors"), "{'name':'X'}")).andExpect(status().isForbidden());
        mvc.perform(get(base + "/budget/sharing").with(user("m"))).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/me/shared-budgets").with(user("m"))).andExpect(jsonPath("$[0].access").value("VIEW")).andExpect(jsonPath("$[0].vendors").value(true));

        // Someone else can't reuse the link.
        as(cousin);
        mvc.perform(post("/api/v1/me/budget-invites/" + link + "/accept").with(user("c")).with(csrf())).andExpect(status().isConflict());

        as(host);
        mvc.perform(json(post(base + "/budget/sharing/members/" + mom + "/access"), "{'access':'EDIT'}")).andExpect(status().isOk());
        as(mom);
        mvc.perform(json(post(base + "/budget/lines"), "{'category':'Food','item':'Dinner'}")).andExpect(status().isOk());

        // Switching sharing off cuts access at once; the owner can still remove people.
        eventSwitch("BUDGET_SHARING", false);
        mvc.perform(get(base + "/budget").with(user("m"))).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/me/shared-budgets").with(user("m"))).andExpect(jsonPath("$", hasSize(0)));
        as(host);
        mvc.perform(get(base + "/budget").with(user("h"))).andExpect(status().isOk());
        mvc.perform(post(base + "/budget/sharing/members/" + mom + "/remove").with(user("h")).with(csrf())).andExpect(status().isOk());
        assertEquals(0, db.queryForObject("SELECT count(*) FROM event_budget_members WHERE event_id=?", Integer.class, event));
    }
}
