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

/** Batch E2: travel & stay for outstation guests, and the shared photo album guests fill by scanning a QR. */
@SpringBootTest(properties = {"gift.otp.secret-base64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=", "gift.whatsapp.worker-initial-delay-ms=3600000", "gift.shared-album.dir=${java.io.tmpdir}/gift-album-it"})
@Testcontainers
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
class TravelAndAlbumIntegrationTest {
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

    // ---- Travel & stay ----

    @Test void travelIsOffUntilSwitchedOn() throws Exception {
        mvc.perform(get(base + "/travel").with(user("h"))).andExpect(status().isNotFound());
        enable("GUEST_TRAVEL");
        mvc.perform(get(base + "/travel").with(user("h"))).andExpect(status().isOk()).andExpect(jsonPath("$.guests", hasSize(0)));
        eventSwitch("GUEST_TRAVEL", false);
        mvc.perform(get(base + "/travel").with(user("h"))).andExpect(status().isNotFound());
    }

    @Test void hostPlansStaysAndArrivalsAndGuestsFillTheirOwnDetails() throws Exception {
        enable("GUEST_TRAVEL");
        String stay = body(json(post(base + "/travel/stays"), "{'name':'Hotel Lotus','address':'MG Road','phone':'+91 80 1234 5678','mapUrl':'https://maps.example/lotus','checkIn':'2026-12-11','checkOut':'2026-12-13','roomsTotal':10}"));
        UUID stayId = UUID.fromString(om.readTree(stay).path("id").asText());
        mvc.perform(json(post(base + "/travel/stays"), "{'name':'Bad','checkIn':'2026-12-13','checkOut':'2026-12-11'}")).andExpect(status().isBadRequest());
        mvc.perform(json(post(base + "/travel/stays"), "{'name':'Bad','mapUrl':'http://insecure'}")).andExpect(status().isBadRequest());

        String g = body(json(post(base + "/travel/guests"), "{'guestName':'Sharma family','partySize':4,'phone':'+919876543210'}"));
        var node = om.readTree(g);
        UUID travelId = UUID.fromString(node.path("id").asText()), form = UUID.fromString(node.path("formToken").asText());
        mvc.perform(json(post(base + "/travel/guests"), "{'guestName':'sharma FAMILY'}")).andExpect(status().isConflict());
        mvc.perform(json(put(base + "/travel/guests/" + travelId), "{'guestName':'Sharma family','partySize':4,'stayId':'" + stayId + "','roomLabel':'204','rooms':2,"
            + "'arrival':{'at':'2026-12-11T14:30','mode':'flight','ref':'6E 512','point':'BLR T1'},'pickupNeeded':true,'version':" + node.path("version").asLong() + "}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.stayName").value("Hotel Lotus")).andExpect(jsonPath("$.arrival.mode").value("FLIGHT"));
        mvc.perform(json(put(base + "/travel/guests/" + travelId), "{'guestName':'Sharma family','version':1}")).andExpect(status().isConflict());

        // The guest opens their private link (no account) and adds the return journey.
        mvc.perform(get("/api/public/travel/" + form)).andExpect(status().isOk()).andExpect(jsonPath("$.stay.name").value("Hotel Lotus"))
            .andExpect(jsonPath("$.stay.room").value("204")).andExpect(jsonPath("$.arrival.ref").value("6E 512")).andExpect(jsonPath("$.submitted").value(false));
        mvc.perform(put("/api/public/travel/" + form).contentType(MediaType.APPLICATION_JSON).content(q("{'partySize':5,'arrival':{'at':'2026-12-11T14:30','mode':'FLIGHT','ref':'6E 512','point':'BLR T1'},"
            + "'departure':{'at':'2026-12-10T10:00','mode':'TRAIN'}}"))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").value("Departure can't be before arrival"));
        mvc.perform(put("/api/public/travel/" + form).contentType(MediaType.APPLICATION_JSON).content(q("{'partySize':5,'arrival':{'at':'2026-12-11T14:30','mode':'FLIGHT','ref':'6E 512','point':'BLR T1'},"
            + "'departure':{'at':'2026-12-13T21:15','mode':'TRAIN','ref':'12658'},'pickupNeeded':true,'dropNeeded':true,'guestNote':'One wheelchair'}")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.submitted").value(true)).andExpect(jsonPath("$.partySize").value(5));

        mvc.perform(get(base + "/travel").with(user("h"))).andExpect(jsonPath("$.totals.people").value(5)).andExpect(jsonPath("$.totals.pickups").value(1))
            .andExpect(jsonPath("$.totals.drops").value(1)).andExpect(jsonPath("$.totals.submitted").value(1))
            .andExpect(jsonPath("$.stays[0].roomsUsed").value(2)).andExpect(jsonPath("$.stays[0].people").value(5))
            .andExpect(jsonPath("$.days[0].date").value("2026-12-11")).andExpect(jsonPath("$.days[0].arrivals[0].transport").value(true))
            .andExpect(jsonPath("$.days[1].departures[0].ref").value("12658"));
        String csv = mvc.perform(get(base + "/travel/manifest.csv").with(user("h"))).andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(csv.contains("2026-12-11 14:30") && csv.contains("Hotel Lotus") && csv.contains("One wheelchair"), csv);

        // A new link retires the old one; deleting the stay keeps the guest but clears their hotel.
        mvc.perform(post(base + "/travel/guests/" + travelId + "/new-link").with(user("h")).with(csrf())).andExpect(status().isOk());
        mvc.perform(get("/api/public/travel/" + form)).andExpect(status().isNotFound());
        mvc.perform(post(base + "/travel/stays/" + stayId + "/delete").with(user("h")).with(csrf())).andExpect(status().isOk());
        mvc.perform(get(base + "/travel").with(user("h"))).andExpect(jsonPath("$.guests[0].stayId").doesNotExist()).andExpect(jsonPath("$.stays", hasSize(0)));

        // Switched off: the guest link stops working too.
        UUID fresh = db.queryForObject("SELECT form_token FROM event_guest_travel WHERE id=?", UUID.class, travelId);
        mvc.perform(get("/api/public/travel/" + fresh)).andExpect(status().isOk());
        eventSwitch("GUEST_TRAVEL", false);
        mvc.perform(get("/api/public/travel/" + fresh)).andExpect(status().isNotFound());
    }

    @Test void outstationHouseholdsAreSuggestedAndAddedInOneTap() throws Exception {
        enable("GUEST_TRAVEL");
        mvc.perform(post(base + "/travel/guests/from-households").with(user("h")).with(csrf())).andExpect(status().isBadRequest());
        enable("GUEST_HOUSEHOLDS");
        mvc.perform(json(post(base + "/households"), "{'name':'Iyer family','outstation':true,'members':[{'name':'A'},{'name':'B'},{'name':'C','attending':false}]}"));
        mvc.perform(json(post(base + "/households"), "{'name':'Local Lal','outstation':false}"));
        mvc.perform(json(post(base + "/households"), "{'name':'Declined Das','outstation':true,'rsvp':'NO'}"));
        mvc.perform(get(base + "/travel").with(user("h"))).andExpect(jsonPath("$.suggestions", contains("Iyer family")));
        mvc.perform(post(base + "/travel/guests/from-households").with(user("h")).with(csrf())).andExpect(jsonPath("$.created").value(1));
        mvc.perform(post(base + "/travel/guests/from-households").with(user("h")).with(csrf())).andExpect(jsonPath("$.created").value(0));
        mvc.perform(get(base + "/travel").with(user("h"))).andExpect(jsonPath("$.guests[0].partySize").value(2)).andExpect(jsonPath("$.suggestions", hasSize(0)));
    }

    // ---- Shared album ----

    static byte[] jpeg() throws Exception {
        var img = new java.awt.image.BufferedImage(1200, 800, java.awt.image.BufferedImage.TYPE_INT_RGB);
        var out = new java.io.ByteArrayOutputStream();
        javax.imageio.ImageIO.write(img, "jpg", out);
        return out.toByteArray();
    }
    UUID albumToken() throws Exception {
        return UUID.fromString(om.readTree(body(get(base + "/shared-album").with(user("h")))).path("uploadToken").asText());
    }

    @Test void guestsAddPhotosByQrAndHostModeratesAndDownloads() throws Exception {
        mvc.perform(get(base + "/shared-album").with(user("h"))).andExpect(status().isNotFound());
        enable("SHARED_ALBUM");
        UUID token = albumToken();
        mvc.perform(get(base + "/shared-album/qr.png").with(user("h"))).andExpect(status().isOk()).andExpect(content().contentType(MediaType.IMAGE_PNG));
        mvc.perform(get("/api/public/albums/" + token)).andExpect(status().isOk()).andExpect(jsonPath("$.uploadsOpen").value(true)).andExpect(jsonPath("$.photos", hasSize(0)));

        mvc.perform(multipart("/api/public/albums/" + token + "/photos").file(new MockMultipartFile("photo", "a.jpg", "image/jpeg", jpeg())).param("name", "Asha").param("caption", "Haldi!"))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("APPROVED"));
        mvc.perform(multipart("/api/public/albums/" + token + "/photos").file(new MockMultipartFile("photo", "x.txt", "text/plain", "hello".getBytes())))
            .andExpect(status().isUnsupportedMediaType());
        String view = body(get("/api/public/albums/" + token));
        UUID photo = UUID.fromString(om.readTree(view).path("photos").get(0).path("id").asText());
        assertEquals("Asha", om.readTree(view).path("photos").get(0).path("uploaderName").asText());
        byte[] full = mvc.perform(get("/api/public/albums/" + token + "/photos/" + photo)).andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray();
        byte[] thumb = mvc.perform(get("/api/public/albums/" + token + "/photos/" + photo + "/thumb")).andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray();
        assertTrue(thumb.length < full.length);

        // Review mode: new photos wait for approval and are invisible to guests until then.
        mvc.perform(json(put(base + "/shared-album/settings"), "{'uploadsOpen':true,'moderation':'REVIEW','guestsCanView':true}")).andExpect(jsonPath("$.moderation").value("REVIEW"));
        String up = mvc.perform(multipart("/api/public/albums/" + token + "/photos").file(new MockMultipartFile("photo", "b.jpg", "image/jpeg", jpeg())))
            .andExpect(jsonPath("$.status").value("PENDING")).andReturn().getResponse().getContentAsString();
        UUID pending = UUID.fromString(om.readTree(up).path("id").asText());
        mvc.perform(get("/api/public/albums/" + token + "/photos/" + pending)).andExpect(status().isNotFound());
        mvc.perform(get(base + "/shared-album/photos/" + pending).with(user("h"))).andExpect(status().isOk());
        mvc.perform(get(base + "/shared-album").with(user("h"))).andExpect(jsonPath("$.approved").value(1)).andExpect(jsonPath("$.pending").value(1));
        mvc.perform(json(post(base + "/shared-album/photos/" + pending + "/status"), "{'status':'APPROVED'}")).andExpect(jsonPath("$.status").value("APPROVED"));
        mvc.perform(json(post(base + "/shared-album/photos/" + photo + "/status"), "{'status':'HIDDEN'}")).andExpect(status().isOk());
        mvc.perform(get("/api/public/albums/" + token)).andExpect(jsonPath("$.total").value(1)).andExpect(jsonPath("$.photos[0].id").value(pending.toString()));

        byte[] zip = mvc.perform(get(base + "/shared-album/download.zip").with(user("h"))).andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray();
        int entries = 0;
        try (var in = new java.util.zip.ZipInputStream(new java.io.ByteArrayInputStream(zip))) { while (in.getNextEntry() != null) entries++; }
        assertEquals(1, entries);

        // Deleting removes the files; closing stops uploads; guests-can't-view hides the gallery; a new link retires the old QR.
        String file = db.queryForObject("SELECT file_name FROM event_shared_photos WHERE id=?", String.class, photo);
        Path dir = Path.of(System.getProperty("java.io.tmpdir"), "gift-album-it");
        assertTrue(Files.exists(dir.resolve(file)));
        mvc.perform(post(base + "/shared-album/photos/" + photo + "/delete").with(user("h")).with(csrf())).andExpect(status().isOk());
        assertFalse(Files.exists(dir.resolve(file)));
        mvc.perform(json(put(base + "/shared-album/settings"), "{'uploadsOpen':false,'moderation':'AUTO','guestsCanView':false}")).andExpect(status().isOk());
        mvc.perform(multipart("/api/public/albums/" + token + "/photos").file(new MockMultipartFile("photo", "c.jpg", "image/jpeg", jpeg()))).andExpect(status().isConflict());
        mvc.perform(get("/api/public/albums/" + token)).andExpect(jsonPath("$.guestsCanView").value(false)).andExpect(jsonPath("$.photos", hasSize(0)));
        mvc.perform(get("/api/public/albums/" + token + "/photos/" + pending + "/thumb")).andExpect(status().isNotFound());
        mvc.perform(post(base + "/shared-album/new-link").with(user("h")).with(csrf())).andExpect(status().isOk());
        mvc.perform(get("/api/public/albums/" + token)).andExpect(status().isNotFound());
    }

    @Test void albumIsHiddenWhenTheEventIsUnpublishedOrSwitchedOff() throws Exception {
        enable("SHARED_ALBUM");
        UUID token = albumToken();
        eventSwitch("SHARED_ALBUM", false);
        mvc.perform(get("/api/public/albums/" + token)).andExpect(status().isNotFound());
        eventSwitch("SHARED_ALBUM", true);
        db.update("UPDATE events SET status='DRAFT' WHERE id=?", event);
        mvc.perform(get("/api/public/albums/" + token)).andExpect(status().isNotFound());
    }
}
