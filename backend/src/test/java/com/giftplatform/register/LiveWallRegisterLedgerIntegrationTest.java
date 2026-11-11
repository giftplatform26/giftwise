package com.giftplatform.register;

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
class LiveWallRegisterLedgerIntegrationTest {
    @Container static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");
    @DynamicPropertySource static void database(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl); r.add("spring.datasource.username", postgres::getUsername); r.add("spring.datasource.password", postgres::getPassword);
    }
    @Autowired JdbcTemplate db;
    @Autowired MockMvc mvc;
    @MockitoBean ActorContext actor;
    UUID host, event, token;

    @BeforeEach void setup() {
        host = UUID.randomUUID(); event = UUID.randomUUID();
        db.update("INSERT INTO users(id,email,password_hash,role) VALUES(?,?,?,'CUSTOMER')", host, host + "@example.test", "hash");
        db.update("INSERT INTO events(id,title,occasion_type,status,owner_id,host_display_name,event_date) VALUES(?,'Meera & Arjun','WEDDING','PUBLISHED',?,'Meera',DATE '2026-12-12')", event, host);
        token = db.queryForObject("SELECT public_token FROM events WHERE id=?", UUID.class, event);
        when(actor.requireAuthenticatedUserId()).thenReturn(host);
        for (String m : new String[]{"LIVE_WALL", "SHAGUN_REGISTER", "GIFT_LEDGER", "GUESTBOOK"}) grant(m, true, true, true);
    }
    void grant(String module, boolean platform, boolean hostOn, boolean eventOn) {
        db.update("UPDATE platform_module_controls SET enabled=? WHERE module_code=?", platform, module);
        db.update("INSERT INTO platform_host_module_grants(host_id,module_code,enabled) VALUES(?,?,?) ON CONFLICT(host_id,module_code) DO UPDATE SET enabled=EXCLUDED.enabled", host, module, hostOn);
        if (!module.equals("GIFT_LEDGER") && !module.equals("GUESTBOOK"))
            db.update("INSERT INTO platform_event_module_grants(event_id,module_code,enabled) VALUES(?,?,?) ON CONFLICT(event_id,module_code) DO UPDATE SET enabled=EXCLUDED.enabled", event, module, eventOn);
    }
    String json(String s) { return s.replace('\'', '"'); }
    void wish(String name, String message, String status) {
        db.update("INSERT INTO event_guestbook(id,event_id,guest_name,message,status) VALUES(?,?,?,?,?)", UUID.randomUUID(), event, name, message, status);
    }

    // ---- Switches ----

    @Test void everyFeatureNeedsPlatformHostAndEventSwitches() throws Exception {
        String wall = "/api/v1/events/" + event + "/live-wall", register = "/api/v1/events/" + event + "/register";
        mvc.perform(get(wall).with(user("h"))).andExpect(status().isOk());
        grant("LIVE_WALL", true, true, false);
        mvc.perform(get(wall).with(user("h"))).andExpect(status().isNotFound());
        grant("LIVE_WALL", true, false, true);
        mvc.perform(get(wall).with(user("h"))).andExpect(status().isNotFound());
        grant("LIVE_WALL", false, true, true);
        mvc.perform(get(wall).with(user("h"))).andExpect(status().isNotFound());
        grant("SHAGUN_REGISTER", true, true, false);
        mvc.perform(get(register).with(user("h"))).andExpect(status().isNotFound());
        grant("GIFT_LEDGER", true, false, false);
        mvc.perform(get("/api/v1/me/ledger").with(user("h"))).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/me/ledger")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/events/" + event + "/features").with(user("h"))).andExpect(status().isOk())
            .andExpect(jsonPath("$.LIVE_WALL").value(false)).andExpect(jsonPath("$.SHAGUN_REGISTER").value(false));
    }

    @Test void adminCanToggleTheNewEventModules() throws Exception {
        when(actor.isPlatformAdmin()).thenReturn(true);
        db.update("DELETE FROM platform_event_module_grants WHERE event_id=?", event);
        mvc.perform(get("/api/admin/console/events/" + event + "/modules").with(user("a").roles("PLATFORM_ADMIN"))).andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.module=='LIVE_WALL')].eventEnabled").value(false));
        mvc.perform(post("/api/admin/console/events/" + event + "/modules/LIVE_WALL").with(user("a").roles("PLATFORM_ADMIN")).with(csrf())
            .contentType(MediaType.APPLICATION_JSON).content("{\"enabled\":true}")).andExpect(status().isOk());
        mvc.perform(get("/api/admin/console/events/" + event + "/modules").with(user("a").roles("PLATFORM_ADMIN")))
            .andExpect(jsonPath("$[?(@.module=='LIVE_WALL')].effectiveEnabled").value(true));
        mvc.perform(post("/api/admin/console/modules/GIFT_LEDGER").with(user("a").roles("PLATFORM_ADMIN")).with(csrf())
            .contentType(MediaType.APPLICATION_JSON).content("{\"enabled\":false}")).andExpect(status().isOk());
    }

    // ---- Live wall ----

    @Test void liveWallShowsOnlyApprovedWishesAndRotatesItsLink() throws Exception {
        String base = "/api/v1/events/" + event + "/live-wall";
        wish("Asha", "Congratulations!", "APPROVED"); wish("Spammer", "buy now", "PENDING"); wish("Hidden", "nope", "HIDDEN");
        mvc.perform(put(base).with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content(json("{'active':true,'headline':'Love & laughter','theme':'festive','showMessages':true,'showPhotos':true,'showJoinQr':true,'slideSeconds':6,'version':0}")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.theme").value("FESTIVE")).andExpect(jsonPath("$.version").value(1));
        String wallToken = db.queryForObject("SELECT wall_token::text FROM event_live_walls WHERE event_id=?", String.class, event);
        mvc.perform(get("/api/public/live/" + wallToken)).andExpect(status().isOk())
            .andExpect(jsonPath("$.items", hasSize(1))).andExpect(jsonPath("$.items[0].guestName").value("Asha"))
            .andExpect(jsonPath("$.invitationPath").value("/e/" + token)).andExpect(jsonPath("$.slideSeconds").value(6));
        mvc.perform(put(base).with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content(json("{'active':true,'theme':'NIGHT','showMessages':true,'showPhotos':true,'showJoinQr':true,'slideSeconds':6,'version':0}")))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.error").exists());
        mvc.perform(put(base).with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content(json("{'active':true,'theme':'NIGHT','showMessages':false,'showPhotos':false,'showJoinQr':true,'slideSeconds':6,'version':1}")))
            .andExpect(status().isBadRequest());
        mvc.perform(post(base + "/rotate").with(user("h")).with(csrf())).andExpect(status().isOk());
        mvc.perform(get("/api/public/live/" + wallToken)).andExpect(status().isNotFound());
        String fresh = db.queryForObject("SELECT wall_token::text FROM event_live_walls WHERE event_id=?", String.class, event);
        mvc.perform(get("/api/public/live/" + fresh)).andExpect(status().isOk());
        grant("LIVE_WALL", true, true, false);
        mvc.perform(get("/api/public/live/" + fresh)).andExpect(status().isNotFound());
    }

    @Test void pausedWallOrUnpublishedEventIsHidden() throws Exception {
        db.update("INSERT INTO event_live_walls(event_id,active) VALUES(?,FALSE)", event);
        String wallToken = db.queryForObject("SELECT wall_token::text FROM event_live_walls WHERE event_id=?", String.class, event);
        mvc.perform(get("/api/public/live/" + wallToken)).andExpect(status().isNotFound());
        db.update("UPDATE event_live_walls SET active=TRUE WHERE event_id=?", event);
        db.update("UPDATE events SET status='DRAFT' WHERE id=?", event);
        mvc.perform(get("/api/public/live/" + wallToken)).andExpect(status().isNotFound());
    }

    // ---- Shagun register ----

    @Test void registerRecordsEntriesOnceAndKeepsTotals() throws Exception {
        String base = "/api/v1/events/" + event + "/register";
        String entry = json("{'giverName':' Ravi  Kumar ','giverPhone':'+91 98765-43210','side':'Bride side','kind':'envelope','amountPaise':210100,'clientKey':'abcd1234-0001'}");
        mvc.perform(post(base).with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(entry))
            .andExpect(status().isOk()).andExpect(jsonPath("$.giverName").value("Ravi  Kumar")).andExpect(jsonPath("$.giverPhone").value("+919876543210"));
        // Double tap on a slow connection: same client key, no duplicate.
        mvc.perform(post(base).with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(entry)).andExpect(status().isOk());
        mvc.perform(post(base).with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content(json("{'giverName':'Lata','kind':'GIFT_ITEM','itemDescription':'Silver lamp','clientKey':'abcd1234-0002'}"))).andExpect(status().isOk());
        mvc.perform(post(base).with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content(json("{'giverName':'Nobody','kind':'CASH','clientKey':'abcd1234-0003'}")))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").value("Add an amount or describe the gift"));
        mvc.perform(get(base).with(user("h"))).andExpect(status().isOk())
            .andExpect(jsonPath("$.entries", hasSize(2))).andExpect(jsonPath("$.totals.inPersonPaise").value(210100))
            .andExpect(jsonPath("$.totals.itemGifts").value(1)).andExpect(jsonPath("$.totals.givers").value(2))
            .andExpect(jsonPath("$.sides[?(@.side=='Bride side')].amountPaise").value(210100));
    }

    @Test void registerEditsAreVersionedAndDeletesCanBeUndone() throws Exception {
        String base = "/api/v1/events/" + event + "/register";
        mvc.perform(post(base).with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content(json("{'giverName':'Ravi','kind':'CASH','amountPaise':50100,'clientKey':'abcd1234-0010'}"))).andExpect(status().isOk());
        UUID id = db.queryForObject("SELECT id FROM event_shagun_entries WHERE event_id=?", UUID.class, event);
        String edit = "{'giverName':'Ravi K','kind':'CASH','amountPaise':110100,'version':%d}";
        mvc.perform(put(base + "/" + id).with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(json(edit.formatted(1))))
            .andExpect(status().isOk()).andExpect(jsonPath("$.version").value(2));
        mvc.perform(put(base + "/" + id).with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(json(edit.formatted(1))))
            .andExpect(status().isConflict());
        mvc.perform(post(base + "/" + id + "/delete").with(user("h")).with(csrf())).andExpect(status().isOk());
        mvc.perform(get(base).with(user("h"))).andExpect(jsonPath("$.entries", hasSize(0)));
        mvc.perform(post(base + "/" + id + "/restore").with(user("h")).with(csrf())).andExpect(status().isOk());
        mvc.perform(get(base).with(user("h"))).andExpect(jsonPath("$.entries", hasSize(1)));
    }

    @Test void registerIncludesOnlyConfirmedOnlineGiftsAndExportsSafeCsv() throws Exception {
        UUID bank = UUID.randomUUID();
        db.update("""
            INSERT INTO host_bank_accounts(id,host_id,account_number_encrypted,account_fingerprint,account_number_masked,ifsc_encrypted,ifsc_masked,account_holder_encrypted,verification_status,host_confirmed_at)
            VALUES(?,?,'enc',?,'XXXX1234','enc','HDFC0****','enc','PENDING',now())""", bank, host, bank.toString().replace("-", ""));
        for (String[] c : new String[][]{{"HOST_CONFIRMED", "Confirmed Guest"}, {"AWAITING_VERIFICATION", "Waiting Guest"}})
            db.update("INSERT INTO guest_direct_transfer_claims(id,event_id,destination_id,method,amount_paise,guest_name,reference_hash,client_key,status) VALUES(?,?,?,'UPI',150000,?,?,?,?)",
                UUID.randomUUID(), event, bank, c[1], UUID.randomUUID().toString().replace("-", "") + "00000000000000000000000000000000".substring(0, 32), UUID.randomUUID().toString(), c[0]);
        String base = "/api/v1/events/" + event + "/register";
        mvc.perform(post(base).with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content(json("{'giverName':'=HYPERLINK(1)','kind':'CASH','amountPaise':10000,'clientKey':'abcd1234-0020'}"))).andExpect(status().isOk());
        mvc.perform(get(base).with(user("h"))).andExpect(jsonPath("$.online", hasSize(1)))
            .andExpect(jsonPath("$.online[0].giverName").value("Confirmed Guest")).andExpect(jsonPath("$.totals.onlinePaise").value(150000));
        mvc.perform(get(base + "/export.csv").with(user("h"))).andExpect(status().isOk())
            .andExpect(content().string(containsString("\"'=HYPERLINK(1)\""))).andExpect(content().string(containsString("Confirmed Guest")))
            .andExpect(content().string(not(containsString("Waiting Guest"))));
    }

    @Test void anotherHostCannotReadOrWriteTheRegister() throws Exception {
        UUID other = UUID.randomUUID();
        db.update("INSERT INTO users(id,email,password_hash,role) VALUES(?,?,?,'CUSTOMER')", other, other + "@example.test", "hash");
        db.update("INSERT INTO platform_host_module_grants(host_id,module_code,enabled) VALUES(?,'SHAGUN_REGISTER',TRUE)", other);
        when(actor.requireAuthenticatedUserId()).thenReturn(other);
        String base = "/api/v1/events/" + event + "/register";
        mvc.perform(get(base).with(user("o"))).andExpect(status().isNotFound());
        mvc.perform(post(base).with(user("o")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content(json("{'giverName':'X','kind':'CASH','amountPaise':10000,'clientKey':'abcd1234-0030'}"))).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/events/" + event + "/live-wall").with(user("o"))).andExpect(status().isNotFound());
    }

    // ---- Personal ledger ----

    @Test void ledgerMatchesPeopleAcrossGivenAndReceived() throws Exception {
        mvc.perform(post("/api/v1/events/" + event + "/register").with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content(json("{'giverName':'Ravi Kumar','kind':'CASH','amountPaise':510000,'clientKey':'abcd1234-0040'}"))).andExpect(status().isOk());
        mvc.perform(post("/api/v1/me/ledger/given").with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content(json("{'recipientName':'ravi  kumar.','occasion':'Son’s wedding','occasionDate':'2024-02-10','kind':'ENVELOPE','amountPaise':310000}"))).andExpect(status().isOk());
        mvc.perform(post("/api/v1/me/ledger/given").with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content(json("{'recipientName':'Anil','kind':'GIFT_ITEM','itemDescription':'Watch'}"))).andExpect(status().isOk());
        mvc.perform(get("/api/v1/me/ledger").with(user("h"))).andExpect(status().isOk())
            .andExpect(jsonPath("$.givenPaise").value(310000)).andExpect(jsonPath("$.receivedPaise").value(510000))
            .andExpect(jsonPath("$.people[0].key").value("ravi kumar")).andExpect(jsonPath("$.people[0].givenPaise").value(310000))
            .andExpect(jsonPath("$.people[0].receivedPaise").value(510000));
        mvc.perform(get("/api/v1/me/ledger/person").param("name", "RAVI KUMAR").with(user("h"))).andExpect(status().isOk())
            .andExpect(jsonPath("$.given", hasSize(1))).andExpect(jsonPath("$.received", hasSize(1)))
            .andExpect(jsonPath("$.received[0].eventTitle").value("Meera & Arjun"));
    }

    @Test void ledgerIsPrivateToItsOwner() throws Exception {
        mvc.perform(post("/api/v1/me/ledger/given").with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content(json("{'recipientName':'Anil','kind':'CASH','amountPaise':10000}"))).andExpect(status().isOk());
        UUID id = db.queryForObject("SELECT id FROM user_gifts_given WHERE user_id=?", UUID.class, host);
        UUID other = UUID.randomUUID();
        db.update("INSERT INTO users(id,email,password_hash,role) VALUES(?,?,?,'CUSTOMER')", other, other + "@example.test", "hash");
        db.update("INSERT INTO platform_host_module_grants(host_id,module_code,enabled) VALUES(?,'GIFT_LEDGER',TRUE)", other);
        when(actor.requireAuthenticatedUserId()).thenReturn(other);
        mvc.perform(get("/api/v1/me/ledger").with(user("o"))).andExpect(jsonPath("$.given", hasSize(0))).andExpect(jsonPath("$.receivedPaise").value(0));
        mvc.perform(post("/api/v1/me/ledger/given/" + id + "/delete").with(user("o")).with(csrf())).andExpect(status().isNotFound());
        mvc.perform(put("/api/v1/me/ledger/given/" + id).with(user("o")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content(json("{'recipientName':'X','kind':'CASH','amountPaise':10000,'version':1}"))).andExpect(status().isNotFound());
    }
}
