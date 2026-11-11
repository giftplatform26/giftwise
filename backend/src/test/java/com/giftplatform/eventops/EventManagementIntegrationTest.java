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
class EventManagementIntegrationTest {
    @Container static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");
    @DynamicPropertySource static void database(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl); r.add("spring.datasource.username", postgres::getUsername); r.add("spring.datasource.password", postgres::getPassword);
    }
    @Autowired JdbcTemplate db;
    @Autowired MockMvc mvc;
    @MockitoBean ActorContext actor;
    UUID host, coHost, desk, event, token;
    static final String[] EVENT_MODULES = {"PLANNING_CHECKLIST", "EVENT_BUDGET", "RUN_SHEET", "CATERING_HEADCOUNT", "GUEST_CHECKIN", "LIVE_UPDATES", "RETURN_GIFTS",
        "CO_HOSTS", "EVENT_FUNCTIONS", "SHAGUN_REGISTER", "LIVE_WALL"};

    @BeforeEach void setup() {
        host = newUser(); coHost = newUser(); desk = newUser(); event = UUID.randomUUID();
        LocalDate date = LocalDate.now(ZoneId.of("Asia/Kolkata")).plusWeeks(10);
        db.update("INSERT INTO events(id,title,occasion_type,status,owner_id,host_display_name,event_date) VALUES(?,'Meera & Arjun','WEDDING','PUBLISHED',?,'Meera',?)", event, host, date);
        token = db.queryForObject("SELECT public_token FROM events WHERE id=?", UUID.class, event);
        for (String m : EVENT_MODULES) set(m, true);
        for (String m : new String[]{"RSVP", "GUESTBOOK"}) { db.update("UPDATE platform_module_controls SET enabled=TRUE WHERE module_code=?", m);
            db.update("INSERT INTO platform_host_module_grants(host_id,module_code,enabled) VALUES(?,?,TRUE) ON CONFLICT DO NOTHING", host, m); }
        db.update("INSERT INTO event_co_hosts(event_id,user_id,role,invited_by) VALUES(?,?,'CO_HOST',?),(?,?,'GIFT_DESK',?)", event, coHost, host, event, desk, host);
        as(host);
    }
    UUID newUser() {
        UUID id = UUID.randomUUID();
        db.update("INSERT INTO users(id,email,password_hash,role,full_name) VALUES(?,?,?,'CUSTOMER','Test Person')", id, id + "@example.test", "hash");
        return id;
    }
    void set(String m, boolean on) {
        db.update("UPDATE platform_module_controls SET enabled=TRUE WHERE module_code=?", m);
        db.update("INSERT INTO platform_host_module_grants(host_id,module_code,enabled) VALUES(?,?,TRUE) ON CONFLICT(host_id,module_code) DO UPDATE SET enabled=TRUE", host, m);
        db.update("INSERT INTO platform_event_module_grants(event_id,module_code,enabled) VALUES(?,?,?) ON CONFLICT(event_id,module_code) DO UPDATE SET enabled=EXCLUDED.enabled", event, m, on);
    }
    void as(UUID who) { when(actor.requireAuthenticatedUserId()).thenReturn(who); }
    String q(String s) { return s.replace('\'', '"'); }
    String base(String path) { return "/api/v1/events/" + event + path; }

    // ---- Planning checklist ----

    @Test void starterChecklistIsDatedBackFromTheWeddingAndSharedWithCoHosts() throws Exception {
        mvc.perform(post(base("/tasks/starter")).with(user("h")).with(csrf())).andExpect(status().isOk()).andExpect(jsonPath("$.added").value(12));
        mvc.perform(post(base("/tasks/starter")).with(user("h")).with(csrf())).andExpect(status().isConflict());
        mvc.perform(get(base("/tasks")).with(user("h"))).andExpect(status().isOk()).andExpect(jsonPath("$.tasks", hasSize(12)))
            .andExpect(jsonPath("$.team", hasSize(3))).andExpect(jsonPath("$.progress.total").value(12));
        // Tasks already past due when starting late are moved to a few days from now, never into the past.
        assertEquals(0, db.queryForObject("SELECT count(*) FROM event_tasks WHERE event_id=? AND due_date < CURRENT_DATE", Integer.class, event));
        as(coHost);
        mvc.perform(post(base("/tasks")).with(user("c")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content(q("{'title':'Book the band','category':'Music','assigneeId':'" + coHost + "'}"))).andExpect(status().isOk()).andExpect(jsonPath("$.assigneeName").value("Test Person"));
        UUID task = db.queryForObject("SELECT id FROM event_tasks WHERE title='Book the band'", UUID.class);
        mvc.perform(post(base("/tasks/" + task + "/done")).with(user("c")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"done\":true}"))
            .andExpect(jsonPath("$.done").value(true));
        mvc.perform(post(base("/tasks")).with(user("c")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content(q("{'title':'X','assigneeId':'" + UUID.randomUUID() + "'}"))).andExpect(status().isBadRequest());
        as(desk);
        mvc.perform(get(base("/tasks")).with(user("d"))).andExpect(status().isForbidden());
    }

    // ---- Budget ----

    @Test void budgetTracksPlannedBookedPaidAndIsOwnerOnly() throws Exception {
        String due = LocalDate.now(ZoneId.of("Asia/Kolkata")).plusDays(5).toString();
        mvc.perform(put(base("/budget/total")).with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"totalPaise\":100000000}")).andExpect(status().isOk());
        mvc.perform(post(base("/budget/lines")).with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content(q("{'category':'Food','item':'Caterer','vendorName':'Annapurna','plannedPaise':40000000,'committedPaise':45000000,'paidPaise':10000000,'dueDate':'" + due + "'}"))).andExpect(status().isOk())
            .andExpect(jsonPath("$.balancePaise").value(35000000)).andExpect(jsonPath("$.dueSoon").value(true));
        mvc.perform(post(base("/budget/lines")).with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content(q("{'category':'Decor','item':'Stage','committedPaise':1000,'paidPaise':5000}"))).andExpect(status().isBadRequest());
        mvc.perform(get(base("/budget")).with(user("h"))).andExpect(jsonPath("$.summary.committedPaise").value(45000000))
            .andExpect(jsonPath("$.summary.remainingPaise").value(55000000)).andExpect(jsonPath("$.upcoming", hasSize(1)));
        as(coHost);
        mvc.perform(get(base("/budget")).with(user("c"))).andExpect(status().isNotFound());
    }

    // ---- Run sheet ----

    @Test void runSheetSharesAReadOnlyLinkThatCanBeRotated() throws Exception {
        mvc.perform(post(base("/run-sheet/items")).with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content(q("{'startsAt':'18:30','title':'Baraat arrives','ownerName':'Ravi','location':'Main gate'}"))).andExpect(status().isOk());
        mvc.perform(post(base("/run-sheet/items")).with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content(q("{'startsAt':'17:00','title':'Decor check'}"))).andExpect(status().isOk());
        mvc.perform(get(base("/run-sheet")).with(user("h"))).andExpect(jsonPath("$.items[0].title").value("Decor check")).andExpect(jsonPath("$.shared").value(false));
        assertEquals(0, db.queryForObject("SELECT count(*) FROM event_run_sheet_links WHERE event_id=?", Integer.class, event));
        mvc.perform(post(base("/run-sheet/share")).with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"shared\":true}")).andExpect(status().isOk());
        String token1 = db.queryForObject("SELECT share_token::text FROM event_run_sheet_links WHERE event_id=?", String.class, event);
        mvc.perform(get("/api/public/run-sheet/" + token1)).andExpect(status().isOk()).andExpect(jsonPath("$.items", hasSize(2)));
        mvc.perform(post(base("/run-sheet/share/rotate")).with(user("h")).with(csrf())).andExpect(status().isOk());
        mvc.perform(get("/api/public/run-sheet/" + token1)).andExpect(status().isNotFound());
        set("RUN_SHEET", false);
        String token2 = db.queryForObject("SELECT share_token::text FROM event_run_sheet_links WHERE event_id=?", String.class, event);
        mvc.perform(get("/api/public/run-sheet/" + token2)).andExpect(status().isNotFound());
    }

    // ---- Catering headcount ----

    @Test void mealChoicesAddUpPerFunctionOnlyWhenCateringIsOn() throws Exception {
        UUID fn = UUID.randomUUID();
        db.update("INSERT INTO event_functions(id,event_id,name) VALUES(?,?,'Reception')", fn, event);
        String rsvp = "{'guestName':'%s','guestEmail':'%s@example.com','answers':[{'functionId':'" + fn + "','attendance':'YES','partySize':%d,'mealChoice':'%s'}]}";
        mvc.perform(post("/api/public/celebrations/" + token + "/functions/rsvp").contentType(MediaType.APPLICATION_JSON).content(q(rsvp.formatted("Asha", "asha", 3, "jain")))).andExpect(status().isOk());
        mvc.perform(post("/api/public/celebrations/" + token + "/functions/rsvp").contentType(MediaType.APPLICATION_JSON).content(q(rsvp.formatted("Ravi", "ravi", 2, "NON_VEG")))).andExpect(status().isOk());
        mvc.perform(post("/api/public/celebrations/" + token + "/functions/rsvp").contentType(MediaType.APPLICATION_JSON).content(q(rsvp.formatted("Bad", "bad", 1, "PIZZA")))).andExpect(status().isBadRequest());
        mvc.perform(get(base("/functions")).with(user("h"))).andExpect(jsonPath("$[0].counts.meals.JAIN").value(3)).andExpect(jsonPath("$[0].counts.meals.NON_VEG").value(2));
        set("CATERING_HEADCOUNT", false);
        mvc.perform(post("/api/public/celebrations/" + token + "/functions/rsvp").contentType(MediaType.APPLICATION_JSON).content(q(rsvp.formatted("Lata", "lata", 1, "VEG")))).andExpect(status().isOk());
        assertNull(db.queryForObject("SELECT meal_choice FROM event_function_rsvps WHERE guest_email='lata@example.com'", String.class));
    }

    // ---- Check-in ----

    @Test void giftDeskChecksInExpectedGuestsAndWalkIns() throws Exception {
        db.update("INSERT INTO event_rsvps(id,event_id,guest_name,guest_email,attendance,party_size) VALUES(?,?,'Asha Rao','a@x.com','YES',3),(?,?,'Nope','n@x.com','NO',0)",
            UUID.randomUUID(), event, UUID.randomUUID(), event);
        db.update("INSERT INTO event_guest_contacts(id,event_id,name,phone,reply_attendance,reply_party_size) VALUES(?,?,'asha  rao','+919876543210','YES',4)", UUID.randomUUID(), event);
        as(desk);
        mvc.perform(get(base("/checkin")).with(user("d"))).andExpect(status().isOk()).andExpect(jsonPath("$.guests", hasSize(1)))
            .andExpect(jsonPath("$.guests[0].expectedParty").value(4)).andExpect(jsonPath("$.expectedPeople").value(4));
        mvc.perform(post(base("/checkin")).with(user("d")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(q("{'guestName':'Asha Rao','partySize':3}"))).andExpect(status().isOk());
        mvc.perform(post(base("/checkin")).with(user("d")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(q("{'guestName':'Uncle Joseph','partySize':2,'walkIn':true}"))).andExpect(status().isOk());
        mvc.perform(post(base("/checkin")).with(user("d")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(q("{'guestName':'asha rao','partySize':4}"))).andExpect(status().isOk());
        mvc.perform(get(base("/checkin")).with(user("d"))).andExpect(jsonPath("$.arrivedGuests").value(2)).andExpect(jsonPath("$.arrivedPeople").value(6)).andExpect(jsonPath("$.walkIns").value(1));
        mvc.perform(post(base("/checkin/undo")).with(user("d")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(q("{'key':'uncle joseph'}"))).andExpect(status().isOk());
        mvc.perform(get(base("/checkin")).with(user("d"))).andExpect(jsonPath("$.arrivedPeople").value(4));
        UUID other = newUser(); as(other);
        mvc.perform(get(base("/checkin")).with(user("o"))).andExpect(status().isNotFound());
    }

    // ---- Live updates ----

    @Test void announcementsReachGuestsAndTheWall() throws Exception {
        as(coHost);
        mvc.perform(post(base("/live-updates")).with(user("c")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(q("{'message':'Dinner is served in the garden'}"))).andExpect(status().isOk());
        mvc.perform(put(base("/live-updates/stream")).with(user("c")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(q("{'url':'http://insecure.example'}"))).andExpect(status().isBadRequest());
        mvc.perform(put(base("/live-updates/stream")).with(user("c")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(q("{'url':'https://youtube.example/live','label':'Watch live'}"))).andExpect(status().isOk());
        mvc.perform(get("/api/public/events/" + token + "/live-updates")).andExpect(status().isOk())
            .andExpect(jsonPath("$.updates[0].message").value("Dinner is served in the garden")).andExpect(jsonPath("$.updates[0].postedBy").doesNotExist())
            .andExpect(jsonPath("$.stream.url").value("https://youtube.example/live"));
        db.update("INSERT INTO event_live_walls(event_id,active) VALUES(?,TRUE)", event);
        String wall = db.queryForObject("SELECT wall_token::text FROM event_live_walls WHERE event_id=?", String.class, event);
        mvc.perform(get("/api/public/live/" + wall)).andExpect(jsonPath("$.announcement").value("Dinner is served in the garden"));
        set("LIVE_UPDATES", false);
        mvc.perform(get("/api/public/events/" + token + "/live-updates")).andExpect(jsonPath("$.updates", hasSize(0)));
        mvc.perform(get("/api/public/live/" + wall)).andExpect(jsonPath("$.announcement").doesNotExist());
    }

    // ---- Return gifts ----

    @Test void returnGiftsAreTickedOffInTheRegister() throws Exception {
        as(desk);
        mvc.perform(post(base("/register")).with(user("d")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content(q("{'giverName':'Ravi','kind':'ENVELOPE','amountPaise':110000,'clientKey':'return-0001'}"))).andExpect(status().isOk());
        UUID entry = db.queryForObject("SELECT id FROM event_shagun_entries WHERE event_id=?", UUID.class, event);
        mvc.perform(post(base("/register/" + entry + "/return-gift")).with(user("d")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"given\":true}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.returnGiftAt").exists());
        mvc.perform(get(base("/register")).with(user("d"))).andExpect(jsonPath("$.returnGifts").value(true)).andExpect(jsonPath("$.returnGiftsGiven").value(1));
        set("RETURN_GIFTS", false);
        mvc.perform(post(base("/register/" + entry + "/return-gift")).with(user("d")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"given\":false}"))
            .andExpect(status().isNotFound());
        mvc.perform(get(base("/register")).with(user("d"))).andExpect(jsonPath("$.returnGifts").value(false));
    }

    @Test void everyToolNeedsItsEventSwitch() throws Exception {
        for (String[] t : new String[][]{{"PLANNING_CHECKLIST", "/tasks"}, {"EVENT_BUDGET", "/budget"}, {"RUN_SHEET", "/run-sheet"}, {"GUEST_CHECKIN", "/checkin"}, {"LIVE_UPDATES", "/live-updates"}}) {
            mvc.perform(get(base(t[1])).with(user("h"))).andExpect(status().isOk());
            set(t[0], false);
            mvc.perform(get(base(t[1])).with(user("h"))).andExpect(status().isNotFound());
        }
        mvc.perform(get("/api/v1/events/" + event + "/features").with(user("h"))).andExpect(jsonPath("$.GUEST_CHECKIN").value(false)).andExpect(jsonPath("$.RETURN_GIFTS").value(true));
    }
}
