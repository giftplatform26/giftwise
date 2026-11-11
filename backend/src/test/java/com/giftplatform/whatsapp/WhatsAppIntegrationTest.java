package com.giftplatform.whatsapp;

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

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"gift.otp.secret-base64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
    "gift.whatsapp.worker-initial-delay-ms=3600000", "gift.whatsapp.meta.verify-token=verify-token-123456",
    "gift.whatsapp.meta.app-secret=app-secret-abcdefghij"})
@Testcontainers
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
class WhatsAppIntegrationTest {
    @Container static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");
    @DynamicPropertySource static void database(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl); r.add("spring.datasource.username", postgres::getUsername); r.add("spring.datasource.password", postgres::getPassword);
    }
    @Autowired JdbcTemplate db;
    @Autowired MockMvc mvc;
    @Autowired WhatsAppService wa;
    @MockitoBean ActorContext actor;
    UUID host, event;
    String base, ph;

    @BeforeEach void setup() {
        host = UUID.randomUUID(); event = UUID.randomUUID(); base = "/api/v1/events/" + event + "/whatsapp";
        ph = "9" + String.format("%09d", (long) (Math.random() * 1_000_000_000L));
        db.update("INSERT INTO users(id,email,password_hash,role) VALUES(?,?,?,'CUSTOMER')", host, host + "@example.test", "hash");
        db.update("INSERT INTO events(id,title,occasion_type,status,owner_id,host_display_name,event_date) VALUES(?,'Meera & Arjun','WEDDING','PUBLISHED',?,'Meera',DATE '2026-12-12')", event, host);
        when(actor.requireAuthenticatedUserId()).thenReturn(host);
        db.update("UPDATE platform_module_controls SET enabled=TRUE WHERE module_code='WHATSAPP_MESSAGING'");
        db.update("INSERT INTO platform_host_module_grants(host_id,module_code,enabled) VALUES(?,'WHATSAPP_MESSAGING',TRUE)", host);
        db.update("INSERT INTO platform_event_module_grants(event_id,module_code,enabled) VALUES(?,'WHATSAPP_MESSAGING',TRUE)", event);
    }
    String q(String s) { return s.replace('\'', '"'); }
    void importGuests(String text) throws Exception {
        mvc.perform(post(base + "/contacts/import").with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content("{\"text\":" + new tools.jackson.databind.ObjectMapper().writeValueAsString(text) + "}")).andExpect(status().isOk());
    }
    String sign(String body) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256"); mac.init(new SecretKeySpec("app-secret-abcdefghij".getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return "sha256=" + HexFormat.of().formatHex(mac.doFinal(body.getBytes(StandardCharsets.UTF_8)));
    }
    String inbound(String id, String from, String text) {
        return q("{'entry':[{'changes':[{'value':{'messages':[{'id':'" + id + "','from':'" + from + "','type':'text','text':{'body':'" + text + "'}}]}}]}]}");
    }

    @Test void parsesRepliesPeopleActuallySend() {
        assertEquals(new WhatsAppService.Reply("YES", 3, false), WhatsAppService.parse("yes 3"));
        assertEquals(new WhatsAppService.Reply("YES", null, false), WhatsAppService.parse("Haan!"));
        assertEquals(new WhatsAppService.Reply("NO", 0, false), WhatsAppService.parse("No, sorry"));
        assertEquals(new WhatsAppService.Reply("MAYBE", 2, false), WhatsAppService.parse("maybe 2"));
        assertEquals(new WhatsAppService.Reply("YES", 10, false), WhatsAppService.parse("YES 40"));
        assertTrue(WhatsAppService.parse("STOP").stop());
        assertNull(WhatsAppService.parse("Congratulations to the couple"));
        assertEquals("+919876543210", wa.phone("098765-43210"));
        assertEquals("+447700900123", wa.phone("+44 7700 900123"));
        assertNull(wa.phone("12345"));
    }

    @Test void importsGuestsAndReportsBadLines() throws Exception {
        mvc.perform(post(base + "/contacts/import").with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content(q("{'text':'Asha Rao, 98765 43210\\nRavi;+91 91234 56789\\nNo phone\\nBad, 123\\nAsha Rao K, 9876543210'}")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.added").value(2)).andExpect(jsonPath("$.updated").value(1))
            .andExpect(jsonPath("$.skipped", hasSize(2)));
        mvc.perform(get(base).with(user("h"))).andExpect(status().isOk()).andExpect(jsonPath("$.provider").value("DEV_OUTBOX"))
            .andExpect(jsonPath("$.live").value(false)).andExpect(jsonPath("$.contacts", hasSize(2)))
            .andExpect(jsonPath("$.previews.INVITATION").value(containsString("Meera invites you to Meera & Arjun")));
    }

    @Test void sendingQueuesOncePerDayAndTheWorkerDelivers() throws Exception {
        importGuests("Asha, " + ph + "\nRavi, 9123456789");
        mvc.perform(post(base + "/send").with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(q("{'kind':'invitation','audience':'ALL'}")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.queued").value(2));
        mvc.perform(post(base + "/send").with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(q("{'kind':'INVITATION','audience':'ALL'}")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.queued").value(0)).andExpect(jsonPath("$.skippedAlreadyToday").value(2));
        assertEquals(2, wa.deliverBatch());
        assertEquals(2, db.queryForObject("SELECT count(*) FROM whatsapp_messages WHERE event_id=? AND status='SENT' AND provider_message_id LIKE 'dev-%'", Integer.class, event));
        mvc.perform(get(base).with(user("h"))).andExpect(jsonPath("$.stats.sent").value(2)).andExpect(jsonPath("$.contacts[0].lastStatus").value("SENT"));
    }

    @Test void unpublishedEventsCannotSendInvitations() throws Exception {
        importGuests("Asha, " + ph);
        db.update("UPDATE events SET status='DRAFT' WHERE id=?", event);
        mvc.perform(post(base + "/send").with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(q("{'kind':'INVITATION','audience':'ALL'}")))
            .andExpect(status().isConflict());
    }

    @Test void signedRepliesBecomeRsvpsAndStopOptsOutEverywhere() throws Exception {
        importGuests("Asha, " + ph);
        mvc.perform(post(base + "/send").with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(q("{'kind':'INVITATION','audience':'ALL'}")));
        wa.deliverBatch();
        String body = inbound("wamid.1" + ph, "91" + ph, "Yes 3");
        mvc.perform(post("/api/webhooks/whatsapp").contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/webhooks/whatsapp").header("X-Hub-Signature-256", "sha256=00").contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/webhooks/whatsapp").header("X-Hub-Signature-256", sign(body)).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isOk());
        mvc.perform(post("/api/webhooks/whatsapp").header("X-Hub-Signature-256", sign(body)).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isOk());
        mvc.perform(get(base).with(user("h"))).andExpect(jsonPath("$.stats.yes").value(1)).andExpect(jsonPath("$.stats.guestsComing").value(3));
        // Same phone is a guest at another event: STOP must cover both.
        UUID other = UUID.randomUUID();
        db.update("INSERT INTO events(id,title,occasion_type,status,owner_id,host_display_name) VALUES(?,'Other','BIRTHDAY','PUBLISHED',?,'Meera')", other, host);
        db.update("INSERT INTO event_guest_contacts(id,event_id,name,phone) VALUES(?,?,'Asha',?)", UUID.randomUUID(), other, "+91" + ph);
        String stop = inbound("wamid.2" + ph, "91" + ph, "STOP");
        mvc.perform(post("/api/webhooks/whatsapp").header("X-Hub-Signature-256", sign(stop)).contentType(MediaType.APPLICATION_JSON).content(stop)).andExpect(status().isOk());
        assertEquals(2, db.queryForObject("SELECT count(*) FROM event_guest_contacts WHERE phone=? AND opted_out_at IS NOT NULL", Integer.class, "+91" + ph));
        mvc.perform(post(base + "/send").with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(q("{'kind':'REMINDER','audience':'ALL'}")))
            .andExpect(jsonPath("$.queued").value(0)).andExpect(jsonPath("$.skippedOptedOut").value(1));
        // Re-adding an opted-out number keeps the opt-out.
        importGuests("Asha again, " + ph);
        assertNotNull(db.queryForObject("SELECT opted_out_at FROM event_guest_contacts WHERE event_id=? AND phone=?", Object.class, event, "+91" + ph));
    }

    @Test void deliveryReceiptsOnlyMoveForward() throws Exception {
        importGuests("Asha, " + ph);
        mvc.perform(post(base + "/send").with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(q("{'kind':'INVITATION','audience':'ALL'}")));
        wa.deliverBatch();
        String id = db.queryForObject("SELECT provider_message_id FROM whatsapp_messages WHERE event_id=?", String.class, event);
        wa.status(id, "read"); wa.status(id, "delivered");
        assertEquals("READ", db.queryForObject("SELECT status FROM whatsapp_messages WHERE event_id=?", String.class, event));
    }

    @Test void webhookSubscriptionNeedsTheVerifyToken() throws Exception {
        mvc.perform(get("/api/webhooks/whatsapp").param("hub.mode", "subscribe").param("hub.verify_token", "verify-token-123456").param("hub.challenge", "abc123"))
            .andExpect(status().isOk()).andExpect(content().string("abc123"));
        mvc.perform(get("/api/webhooks/whatsapp").param("hub.mode", "subscribe").param("hub.verify_token", "wrong").param("hub.challenge", "abc123"))
            .andExpect(status().isForbidden());
    }

    @Test void practiceRepliesWorkOnlyInOutboxModeAndOnlyForTheOwner() throws Exception {
        importGuests("Asha, " + ph);
        mvc.perform(post(base + "/send").with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(q("{'kind':'INVITATION','audience':'ALL'}")));
        wa.deliverBatch();
        UUID contact = db.queryForObject("SELECT id FROM event_guest_contacts WHERE event_id=?", UUID.class, event);
        mvc.perform(post(base + "/dev-reply").with(user("h")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(q("{'contactId':'" + contact + "','text':'maybe'}")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.result").value("RSVP_MAYBE"));
        UUID other = UUID.randomUUID();
        db.update("INSERT INTO users(id,email,password_hash,role) VALUES(?,?,?,'CUSTOMER')", other, other + "@example.test", "hash");
        when(actor.requireAuthenticatedUserId()).thenReturn(other);
        mvc.perform(get(base).with(user("o"))).andExpect(status().isNotFound());
        mvc.perform(post(base + "/send").with(user("o")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(q("{'kind':'INVITATION','audience':'ALL'}"))).andExpect(status().isNotFound());
    }
}
