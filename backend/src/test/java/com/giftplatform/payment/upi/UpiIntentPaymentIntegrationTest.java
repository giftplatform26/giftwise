package com.giftplatform.payment.upi;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.giftplatform.hostpayment.qr.UpiQrPayload;
import com.giftplatform.hostpayment.security.PaymentDataProtector;
import com.giftplatform.payment.control.PaymentControlService;
import com.giftplatform.security.ActorContext;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/** Mocked UPI responses only. Real-device testing with small legitimate payments is still required before release. */
@SpringBootTest(properties = {"gift.otp.secret-base64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
    "gift.payment.profiles.enabled=true", "gift.payment.direct-transfers.enabled=true",
    "gift.payment.destination-verification.enabled=false", "gift.payment.settlement-ui.enabled=false"})
@Testcontainers
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
class UpiIntentPaymentIntegrationTest {
    @Container static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");
    @DynamicPropertySource static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl); r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
        r.add("gift.payment.encryption-key-base64", () -> Base64.getEncoder().encodeToString(new byte[32]));
        r.add("gift.payment.fingerprint-key-base64", () -> Base64.getEncoder().encodeToString(new byte[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}));
    }
    @Autowired UpiIntentPaymentService upi;
    @Autowired PaymentControlService controls;
    @Autowired PaymentDataProtector protector;
    @Autowired JdbcTemplate db;
    @Autowired MockMvc mvc;
    @MockitoBean ActorContext actor;
    UUID host, otherHost, event, token, dest, gift;

    @BeforeEach void setup() {
        host = UUID.randomUUID(); otherHost = UUID.randomUUID(); event = UUID.randomUUID(); dest = UUID.randomUUID(); gift = UUID.randomUUID();
        for (UUID u : new UUID[]{host, otherHost})
            db.update("INSERT INTO users(id,email,password_hash,role) VALUES(?,?,?,'CUSTOMER')", u, u + "@example.test", "hash");
        db.update("INSERT INTO events(id,title,occasion_type,status,owner_id,host_display_name) VALUES(?,'Asha turns 30','BIRTHDAY','PUBLISHED',?,'Asha Rao')", event, host);
        token = db.queryForObject("SELECT public_token FROM events WHERE id=?", UUID.class, event);
        db.update("INSERT INTO gifts(id,event_id,name,target_amount,status,visibility) VALUES(?,?,'Cycle',10000.00,'ACTIVE','PUBLIC')", gift, event);
        db.update("""
            INSERT INTO host_upi_accounts(id,host_id,upi_id_encrypted,upi_id_fingerprint,upi_id_masked,verification_status,host_confirmed_at)
            VALUES(?,?,?,?,'ash***@okhdfc','PENDING',now())""", dest, host, protector.encrypt(host, dest, "upi_id", "asha@okhdfc"),
            protector.fingerprint("UPI", dest.toString()));
        db.update("INSERT INTO host_upi_qr_images(host_id,upi_account_id,source,payload_sha256) VALUES(?,?,'GENERATED',?)", host, dest, "a".repeat(64));
        db.update("INSERT INTO event_payment_account_links(event_id,host_id,upi_account_id,preferred) VALUES(?,?,?,TRUE)", event, host, dest);
        when(actor.isPlatformAdmin()).thenReturn(true); when(actor.requireAuthenticatedUserId()).thenReturn(host);
        for (String channel : java.util.List.of("UPI", "BANK")) {
            long rev = controls.read("PLATFORM", null).revision();
            controls.change("PLATFORM", null, channel, new PaymentControlService.Change(rev, true, "Test"));
        }
    }

    UpiIntentPaymentService.AttemptView start(long paise) {
        return upi.start(token, new UpiIntentPaymentService.StartRequest(dest, null, paise, UUID.randomUUID().toString(), "ANDROID"));
    }
    /** UTRs are unique platform-wide and tests share one database, so each test gets its own 12-digit UTRs. */
    final String utrPrefix = String.valueOf(java.util.concurrent.ThreadLocalRandom.current().nextLong(1_000_000_000L, 10_000_000_000L));
    String utr(String suffix) { return utrPrefix + suffix; }
    UpiIntentPaymentService.ReportRequest completed(String utr) { return new UpiIntentPaymentService.ReportRequest("COMPLETED", utr, "Ravi", null, null); }
    int count(String sql, Object... args) { return db.queryForObject(sql, Integer.class, args); }

    @Test void startBuildsTheIntentFromTheHostsActiveDestinationAndServerAmount() {
        var a = start(250000);
        assertEquals("INITIATED", a.status());
        assertTrue(a.transactionRef().matches(UpiQrPayload.TRANSACTION_REF));
        assertTrue(a.paymentCode().matches("GW-[A-HJ-NP-Z2-9]{6}"));
        assertEquals("upi://pay?pa=asha@okhdfc&pn=Asha%20Rao&am=2500.00&cu=INR&tr=" + a.transactionRef() + "&tn=GiftWise%20" + a.paymentCode(), a.upiUri());
        assertNotNull(a.qrPngBase64());
        assertEquals(1, count("SELECT count(*) FROM guest_payment_status_events WHERE attempt_id=? AND new_status='INITIATED'", a.attemptId()));
        assertEquals(0, count("SELECT count(*) FROM guest_direct_transfer_claims WHERE event_id=?", event), "starting never creates a claim");
    }

    @Test void everyAttemptGetsUniqueReferencesAndRetriesAreIdempotent() {
        var a = start(10000); var b = start(10000);
        assertNotEquals(a.transactionRef(), b.transactionRef()); assertNotEquals(a.paymentCode(), b.paymentCode());
        String key = UUID.randomUUID().toString();
        var first = upi.start(token, new UpiIntentPaymentService.StartRequest(dest, null, 10000, key, "IOS"));
        var again = upi.start(token, new UpiIntentPaymentService.StartRequest(dest, null, 10000, key, "IOS"));
        assertEquals(first.attemptId(), again.attemptId());
        var tampered = assertThrows(ResponseStatusException.class, () -> upi.start(token, new UpiIntentPaymentService.StartRequest(dest, null, 1, key, "IOS")));
        assertEquals(400, tampered.getStatusCode().value());
        assertEquals(409, assertThrows(ResponseStatusException.class,
            () -> upi.start(token, new UpiIntentPaymentService.StartRequest(dest, null, 20000, key, "IOS"))).getStatusCode().value());
    }

    @Test void invalidAmountsAndIncorrectDestinationsAreRejected() {
        for (long bad : new long[]{0, 99, 100_000_001L})
            assertEquals(400, assertThrows(ResponseStatusException.class, () -> start(bad)).getStatusCode().value());
        assertEquals(404, assertThrows(ResponseStatusException.class, () -> upi.start(token,
            new UpiIntentPaymentService.StartRequest(UUID.randomUUID(), null, 10000, UUID.randomUUID().toString(), "ANDROID"))).getStatusCode().value());
        db.update("UPDATE host_upi_accounts SET enabled=FALSE WHERE id=?", dest);
        assertEquals(404, assertThrows(ResponseStatusException.class, () -> start(10000)).getStatusCode().value(), "disabled destination");
    }

    @Test void reportUsesTheAttemptAmountAndCreatesExactlyOneClaim() {
        var a = start(50000);
        var r = upi.report(token, a.attemptId(), completed(utr("01")));
        assertEquals("PAYMENT_REPORTED", r.status());
        var claim = db.queryForMap("SELECT amount_paise,payment_code,status,reported_via,attempt_id,method FROM guest_direct_transfer_claims WHERE id=?", r.claimId());
        assertEquals(50000L, claim.get("amount_paise")); assertEquals(a.paymentCode(), claim.get("payment_code"));
        assertEquals("AWAITING_VERIFICATION", claim.get("status")); assertEquals("UPI_INTENT", claim.get("reported_via"));
        assertEquals(a.attemptId(), claim.get("attempt_id")); assertEquals("UPI", claim.get("method"));
        // Double taps and replays return the same claim.
        assertEquals(r.claimId(), upi.report(token, a.attemptId(), completed(utr("01"))).claimId());
        assertEquals(1, count("SELECT count(*) FROM guest_direct_transfer_claims WHERE event_id=?", event));
        assertEquals(409, assertThrows(ResponseStatusException.class, () -> upi.report(token, a.attemptId(), completed("999999999999"))).getStatusCode().value());
        assertEquals(0, count("SELECT count(*) FROM wallet_ledger_entries WHERE event_id=?", event), "never posts to the ledger");
    }

    @Test void duplicateUtrAcrossAttemptsIsRejectedAndRollsBackCleanly() {
        var a = start(10000); var b = start(10000);
        upi.report(token, a.attemptId(), completed(utr("01")));
        int eventsBefore = count("SELECT count(*) FROM guest_payment_status_events WHERE attempt_id=?", b.attemptId());
        assertEquals(409, assertThrows(ResponseStatusException.class, () -> upi.report(token, b.attemptId(), completed(utr("01")))).getStatusCode().value());
        assertEquals("INITIATED", db.queryForObject("SELECT status FROM guest_upi_payment_attempts WHERE id=?", String.class, b.attemptId()));
        assertEquals(eventsBefore, count("SELECT count(*) FROM guest_payment_status_events WHERE attempt_id=?", b.attemptId()));
        assertEquals(1, count("SELECT count(*) FROM guest_direct_transfer_claims WHERE event_id=?", event));
    }

    @Test void appCallbackIsUntrustedDuplicateSafeAndBoundToItsAttempt() {
        var a = start(10000);
        String raw = "txnId=AXI1&responseCode=00&Status=SUCCESS&txnRef=" + a.transactionRef() + "&ApprovalRefNo=" + utr("01") + "&amount=100.00";
        var after = upi.recordResponse(token, a.attemptId(), new UpiIntentPaymentService.ResponseRequest(raw, "com.example.upi"));
        assertEquals("PENDING", after.status(), "an app SUCCESS is at most pending");
        assertEquals(utr("01"), after.suggestedReference());
        assertEquals(0, count("SELECT count(*) FROM guest_direct_transfer_claims WHERE event_id=?", event), "callback alone creates no claim");
        // Duplicate callback: no-op. Different callback: rejected.
        upi.recordResponse(token, a.attemptId(), new UpiIntentPaymentService.ResponseRequest(raw, "com.example.upi"));
        assertEquals(409, assertThrows(ResponseStatusException.class, () -> upi.recordResponse(token, a.attemptId(),
            new UpiIntentPaymentService.ResponseRequest("Status=FAILURE&txnRef=" + a.transactionRef(), null))).getStatusCode().value());
        assertEquals(1, count("SELECT count(*) FROM guest_payment_status_events WHERE attempt_id=? AND evidence='UPI_APP_RESPONSE'", a.attemptId()));
        // Replaying another attempt's response is refused.
        var b = start(10000);
        assertEquals(409, assertThrows(ResponseStatusException.class, () -> upi.recordResponse(token, b.attemptId(),
            new UpiIntentPaymentService.ResponseRequest(raw, null))).getStatusCode().value());
        // Reporting may take the UTR from the callback, still only as a guest report.
        var reported = upi.report(token, a.attemptId(), new UpiIntentPaymentService.ReportRequest("COMPLETED", null, "Ravi", null, null));
        assertEquals("PAYMENT_REPORTED", reported.status());
        assertEquals("AWAITING_VERIFICATION", db.queryForObject("SELECT status FROM guest_direct_transfer_claims WHERE id=?", String.class, reported.claimId()));
    }

    @Test void mismatchedCallbackAmountIsFlaggedForTheHost() {
        var a = start(10000);
        upi.recordResponse(token, a.attemptId(), new UpiIntentPaymentService.ResponseRequest("Status=SUCCESS&amount=1.00", null));
        assertTrue(db.queryForObject("SELECT response_amount_mismatch FROM guest_upi_payment_attempts WHERE id=?", Boolean.class, a.attemptId()));
    }

    @Test void pendingFailedAndExpiredOutcomes() {
        var a = start(10000);
        assertEquals("PENDING", upi.report(token, a.attemptId(), new UpiIntentPaymentService.ReportRequest("PENDING", null, null, null, null)).status());
        assertEquals("FAILED", upi.report(token, a.attemptId(), new UpiIntentPaymentService.ReportRequest("FAILED", null, null, null, null)).status());
        assertEquals("PAYMENT_REPORTED", upi.report(token, a.attemptId(), completed(utr("02"))).status(), "failed then actually paid");
        var b = start(10000);
        db.update("UPDATE guest_upi_payment_attempts SET expires_at=now()-interval '1 minute' WHERE id=?", b.attemptId());
        assertEquals("EXPIRED", upi.status(token, b.attemptId()).status());
        assertEquals(410, assertThrows(ResponseStatusException.class, () -> upi.report(token, b.attemptId(), completed(utr("03")))).getStatusCode().value());
    }

    @Test void attemptsAreScopedToTheirInvitation() throws Exception {
        var a = start(10000);
        assertEquals(404, assertThrows(ResponseStatusException.class, () -> upi.status(UUID.randomUUID(), a.attemptId())).getStatusCode().value());
        mvc.perform(get("/api/public/payments/upi-intent/" + token + "/attempts/" + UUID.randomUUID())).andExpect(status().isNotFound());
    }

    @Test void publicEndpointsRequireCsrf() throws Exception {
        String body = "{\"destinationId\":\"" + dest + "\",\"amountPaise\":10000,\"idempotencyKey\":\"" + UUID.randomUUID() + "\",\"platform\":\"ANDROID\"}";
        mvc.perform(post("/api/public/payments/upi-intent/" + token + "/attempts").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isForbidden());
        mvc.perform(post("/api/public/payments/upi-intent/" + token + "/attempts").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("INITIATED"))
            .andExpect(jsonPath("$.upiUri").value(org.hamcrest.Matchers.startsWith("upi://pay?pa=asha@okhdfc")));
    }

    @Test void hostConfirmationIsAuditedAndOnlyTheOwningHostCanDecide() throws Exception {
        var a = start(10000);
        var r = upi.report(token, a.attemptId(), completed(utr("01")));
        String decision = "/api/v1/events/" + event + "/transactions/claims/" + r.claimId() + "/decision";
        when(actor.requireAuthenticatedUserId()).thenReturn(otherHost);
        mvc.perform(post(decision).with(user("other")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"decision\":\"RECEIVED\"}"))
            .andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/events/" + event + "/transactions/claims/" + r.claimId()).with(user("other"))).andExpect(status().isNotFound());
        when(actor.requireAuthenticatedUserId()).thenReturn(host);
        mvc.perform(post(decision).with(user("host")).contentType(MediaType.APPLICATION_JSON).content("{\"decision\":\"RECEIVED\"}"))
            .andExpect(status().isForbidden());
        mvc.perform(post(decision).with(user("host")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"decision\":\"RECEIVED\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("HOST_CONFIRMED"));
        var row = db.queryForMap("""
            SELECT previous_status,new_status,actor_type,actor_id FROM guest_payment_status_events
            WHERE claim_id=? AND evidence='HOST_DECISION'""", r.claimId());
        assertEquals("PAYMENT_REPORTED", row.get("previous_status")); assertEquals("HOST_CONFIRMED", row.get("new_status"));
        assertEquals("HOST", row.get("actor_type")); assertEquals(host, row.get("actor_id"));
        assertTrue(count("SELECT count(*) FROM audit_logs WHERE entity_id=? AND action='STATUS_CHANGE'", r.claimId()) >= 1);
        // Same decision again is a no-op, not another history row.
        mvc.perform(post(decision).with(user("host")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"decision\":\"RECEIVED\"}")).andExpect(status().isOk());
        assertEquals(1, count("SELECT count(*) FROM guest_payment_status_events WHERE claim_id=? AND evidence='HOST_DECISION'", r.claimId()));
        mvc.perform(get("/api/v1/events/" + event + "/transactions/claims/" + r.claimId()).with(user("host")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("HOST_CONFIRMED"))
            .andExpect(jsonPath("$.reportedVia").value("UPI_INTENT")).andExpect(jsonPath("$.transactionRef").value(a.transactionRef()))
            .andExpect(jsonPath("$.history.length()").value(3));
    }

    @Test void statusHistoryIsAppendOnly() {
        var a = start(10000);
        assertThrows(Exception.class, () -> db.update("UPDATE guest_payment_status_events SET new_status='HOST_CONFIRMED' WHERE attempt_id=?", a.attemptId()));
        assertThrows(Exception.class, () -> db.update("DELETE FROM guest_payment_status_events WHERE attempt_id=?", a.attemptId()));
    }

    @Test void screenshotsAreValidatedSanitisedAndCannotBeReused() throws Exception {
        var a = start(10000); var b = start(10000);
        assertEquals(409, assertThrows(ResponseStatusException.class, () -> upi.attachEvidence(token, a.attemptId(), png())).getStatusCode().value(),
            "only after reporting");
        var ra = upi.report(token, a.attemptId(), completed(utr("01")));
        upi.report(token, b.attemptId(), completed(utr("02")));
        String url = "/api/public/payments/upi-intent/" + token + "/attempts/" + a.attemptId() + "/evidence";
        mvc.perform(multipart(url).file(new MockMultipartFile("file", "x.svg", "image/svg+xml", "<svg onload=alert(1)>".getBytes())).with(csrf()))
            .andExpect(status().isBadRequest());
        byte[] shot = png();
        mvc.perform(multipart(url).file(new MockMultipartFile("file", "shot.png", "image/png", shot)).with(csrf())).andExpect(status().isOk());
        byte[] stored = db.queryForObject("SELECT image FROM guest_payment_evidence WHERE claim_id=?", byte[].class, ra.claimId());
        assertEquals((byte) 0xFF, stored[0], "re-encoded as JPEG");
        assertEquals(409, assertThrows(ResponseStatusException.class, () -> upi.attachEvidence(token, b.attemptId(), shot)).getStatusCode().value(),
            "same screenshot cannot back another claim");
        mvc.perform(get("/api/v1/events/" + event + "/transactions/claims/" + ra.claimId() + "/evidence").with(user("host")))
            .andExpect(status().isOk()).andExpect(header().string("Content-Type", "image/jpeg"))
            .andExpect(header().string("X-Content-Type-Options", "nosniff"));
        assertEquals("AWAITING_VERIFICATION", db.queryForObject("SELECT status FROM guest_direct_transfer_claims WHERE id=?", String.class, ra.claimId()),
            "a screenshot never verifies a payment");
    }

    static byte[] png() throws Exception {
        var image = new BufferedImage(400, 800, BufferedImage.TYPE_INT_RGB);
        image.setRGB(5, 5, 0xFF0000);
        var out = new ByteArrayOutputStream(); ImageIO.write(image, "png", out); return out.toByteArray();
    }
}
