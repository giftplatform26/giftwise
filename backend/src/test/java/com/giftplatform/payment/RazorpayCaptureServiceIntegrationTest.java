package com.giftplatform.payment;

import com.giftplatform.payment.provider.RazorpayClient;
import com.giftplatform.payment.security.RazorpaySignatureVerifier;
import com.giftplatform.payment.service.RazorpayCaptureService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.server.ResponseStatusException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {
        "gift.otp.secret-base64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
        "gift.payment.razorpay.key-secret=test_capture_secret",
        "spring.jpa.hibernate.ddl-auto=validate"
})
@Testcontainers
class RazorpayCaptureServiceIntegrationTest {
    private static final String SECRET = "test_capture_secret";

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired RazorpayCaptureService service;
    @Autowired JdbcTemplate jdbc;
    @Autowired RazorpaySignatureVerifier signatures;
    @MockitoBean RazorpayClient razorpay;

    record Fixture(UUID eventId, UUID giftId, UUID orderId,
                   UUID contributionId, String providerOrderId,
                   String paymentId, long amount) {}

    private UUID event() {
        UUID user = UUID.randomUUID();
        UUID event = UUID.randomUUID();
        jdbc.update("INSERT INTO users(id,email,password_hash,role) VALUES (?,?,?,?)",
                user, user + "@example.test", "test-hash", "CUSTOMER");
        jdbc.update("INSERT INTO events(id,title,occasion_type,status,owner_id,host_display_name) VALUES (?,?,?,?,?,'Test Host')",
                event, "Capture Test", "BIRTHDAY", "PUBLISHED", user);
        return event;
    }

    private UUID gift(UUID event) {
        UUID gift = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO gifts(id,event_id,name,target_amount,status,visibility)
                VALUES (?,?,?,?,?,?)
                """, gift, event, "Capture Gift", new BigDecimal("10000.00"), "ACTIVE", "PUBLIC");
        return gift;
    }

    private Fixture fixture(UUID event, UUID gift, long amount) {
        UUID order = UUID.randomUUID();
        UUID contribution = UUID.randomUUID();
        String providerOrder = "order_" + compact();
        String payment = "pay_" + compact();
        jdbc.update("""
                INSERT INTO payment_orders
                    (id,event_id,gift_id,provider_order_id,contribution_type,
                     amount_paise,idempotency_key,status)
                VALUES (?,?,?,?,?,?,?,'PENDING')
                """, order, event, gift, providerOrder,
                gift == null ? "EVENT_WALLET" : "GIFT_PARTIAL",
                amount, UUID.randomUUID().toString());
        jdbc.update("""
                INSERT INTO contributions
                    (id,payment_order_id,event_id,gift_id,guest_name,amount_paise)
                VALUES (?,?,?,?,?,?)
                """, contribution, order, event, gift, "Guest", amount);
        return new Fixture(event, gift, order, contribution, providerOrder, payment, amount);
    }

    private Fixture fixture(UUID gift, long amount) {
        UUID event = event();
        return fixture(event, gift, amount);
    }

    private static String compact() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private void provider(Fixture f) {
        provider(f, f.paymentId(), f.providerOrderId(), f.amount(), "INR", "captured");
    }

    private void provider(Fixture f, String id, String orderId,
                          long amount, String currency, String status) {
        when(razorpay.fetchPayment(f.paymentId()))
                .thenReturn(new RazorpayClient.ProviderPayment(
                        id, orderId, amount, currency, status));
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {"card", "upi", "netbanking"})
    void storesProviderMethodAndBankReferencesAfterVerifiedCapture(String method) {
        Fixture f = fixture(null, 50000);
        when(razorpay.fetchPayment(f.paymentId())).thenReturn(new RazorpayClient.ProviderPayment(
                f.paymentId(), f.providerOrderId(), f.amount(), "INR", "captured",
                method, "HDFC", "bank123", "123456789012", "upi123"));

        confirm(f);
        confirm(f); // Retries must preserve one transaction and one allocation.

        var details = jdbc.queryForMap("""
            SELECT payment_method,bank_code,bank_transaction_id,rrn,upi_transaction_id
            FROM payment_transactions WHERE payment_order_id=?
            """, f.orderId());
        assertEquals(method, details.get("payment_method"));
        assertEquals("HDFC", details.get("bank_code"));
        assertEquals("bank123", details.get("bank_transaction_id"));
        assertEquals("123456789012", details.get("rrn"));
        assertEquals("upi123", details.get("upi_transaction_id"));
        assertEquals(1, count("payment_transactions", "payment_order_id=?", f.orderId()));
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(booleans = {true, false})
    void unreadableBankDoesNotHideValidUpi(boolean unreadableBank) {
        UUID event = event();
        UUID host = jdbc.queryForObject("SELECT owner_id FROM events WHERE id=?", UUID.class, event);
        UUID token = jdbc.queryForObject("SELECT public_token FROM events WHERE id=?", UUID.class, event);
        byte[] key = new byte[32], fingerprintKey = new byte[32], oldKey = new byte[32];
        java.util.Arrays.fill(key, (byte) 1);
        java.util.Arrays.fill(fingerprintKey, (byte) 2);
        java.util.Arrays.fill(oldKey, (byte) 3);
        var protector = new com.giftplatform.hostpayment.security.PaymentDataProtector(key, fingerprintKey);
        var bankProtector = unreadableBank
                ? new com.giftplatform.hostpayment.security.PaymentDataProtector(oldKey, fingerprintKey) : protector;
        UUID upi = UUID.randomUUID(), bank = UUID.randomUUID();
        jdbc.update("""
            INSERT INTO host_upi_accounts(id,host_id,upi_id_encrypted,upi_id_fingerprint,upi_id_masked,verification_status)
            VALUES(?,?,?,?,?,'VERIFIED')
            """, upi, host, protector.encrypt(host,upi,"upi_id","recipient@bank"),
                protector.fingerprint("UPI",upi.toString()), "rec***@bank");
        jdbc.update("""
            INSERT INTO host_upi_qr_images(host_id,upi_account_id,source,payload_sha256)
            VALUES(?,?,'GENERATED',?)
            """,host,upi,"a".repeat(64));
        jdbc.update("""
            INSERT INTO host_bank_accounts(id,host_id,account_number_encrypted,account_fingerprint,
                account_number_masked,ifsc_encrypted,ifsc_masked,account_holder_encrypted,verification_status)
            VALUES(?,?,?,?,?,?,?,?,'VERIFIED')
            """,bank,host,bankProtector.encrypt(host,bank,"account_number","1234567890"),
                protector.fingerprint("BANK",bank.toString()),"****7890",
                bankProtector.encrypt(host,bank,"ifsc","HDFC0001234"),"HDFC*******",
                bankProtector.encrypt(host,bank,"account_holder","Recipient"));
        jdbc.update("INSERT INTO event_payment_account_links(event_id,host_id,upi_account_id,preferred) VALUES(?,?,?,TRUE)",event,host,upi);
        jdbc.update("INSERT INTO event_payment_account_links(event_id,host_id,bank_account_id,preferred) VALUES(?,?,?,TRUE)",event,host,bank);
        var service = new com.giftplatform.payment.direct.GuestVerifiedDestinationService(jdbc,protector,true,false);
        var destinations = service.list(token);
        assertEquals(unreadableBank ? 1 : 2, destinations.size());
        var actor=mock(com.giftplatform.security.ActorContext.class);
        when(actor.requireAuthenticatedUserId()).thenReturn(host);
        var health=new com.giftplatform.hostpayment.controller.ReceivingAccountHealthController(jdbc,actor,protector,true).health(event).getBody();
        assertNotNull(health);
        var bankHealth=health.stream().filter(h->h.method().equals("BANK")).findFirst().orElseThrow();
        assertEquals(unreadableBank?"DETAILS_UNAVAILABLE":"READY",bankHealth.status());
        assertEquals(!unreadableBank,bankHealth.guestVisible());
        assertTrue(health.stream().anyMatch(h->h.method().equals("UPI")&&h.guestVisible()));
        assertEquals(2,jdbc.queryForObject("SELECT count(*) FROM event_payment_account_links WHERE event_id=? AND preferred=TRUE",Integer.class,event));
        when(actor.requireAuthenticatedUserId()).thenReturn(UUID.randomUUID());
        assertEquals(404,assertThrows(ResponseStatusException.class,()->new com.giftplatform.hostpayment.controller.ReceivingAccountHealthController(jdbc,actor,protector,true).health(event)).getStatusCode().value());
        assertEquals("recipient@bank", destinations.stream().filter(d->d.method().equals("UPI")).findFirst().orElseThrow().upiId());
        if (!unreadableBank) assertEquals("1234567890", destinations.stream().filter(d->d.method().equals("BANK")).findFirst().orElseThrow().accountNumber());
    }

    private String signature(Fixture f) {
        return signature(f.providerOrderId(), f.paymentId());
    }

    private String signature(String order, String payment) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(
                    (order + "|" + payment).getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private RazorpayCaptureService.CaptureResult confirm(Fixture f) {
        return service.confirmCheckout(f.orderId(), f.providerOrderId(),
                f.paymentId(), signature(f));
    }

    private long amount(String table, String where, Object... args) {
        Long result = jdbc.queryForObject("SELECT COALESCE(SUM(amount_paise),0) FROM " + table
                + " WHERE " + where, Long.class, args);
        return result == null ? 0 : result;
    }

    private int count(String table, String where, Object... args) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM " + table
                + " WHERE " + where, Integer.class, args);
    }

    private void assertUnposted(Fixture f) {
        assertEquals("PENDING", jdbc.queryForObject(
                "SELECT status FROM payment_orders WHERE id=?", String.class, f.orderId()));
        assertEquals(0, count("payment_transactions", "payment_order_id=?", f.orderId()));
        assertEquals(0, count("wallet_ledger_entries", "contribution_id=?", f.contributionId()));
        assertEquals(0, count("contribution_allocation_postings", "contribution_id=?", f.contributionId()));
    }

    private void assertRejected(Fixture f, HttpStatus expected) {
        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> confirm(f));
        assertEquals(expected, e.getStatusCode());
        assertUnposted(f);
    }

    @Test
    void capturedWalletPaymentPostsBalancedPendingFunds() {
        Fixture f = fixture(null, 50000);
        provider(f);
        var result = confirm(f);
        assertEquals(0, result.giftPaise());
        assertEquals(50000, result.walletPaise());
        assertFalse(result.alreadyProcessed());
        assertEquals("CAPTURED", jdbc.queryForObject(
                "SELECT status FROM payment_orders WHERE id=?", String.class, f.orderId()));
        assertEquals(1, count("payment_transactions", "payment_order_id=? AND status='CAPTURED' AND verified_at IS NOT NULL", f.orderId()));
        assertEquals(50000, amount("wallet_ledger_entries", "contribution_id=? AND account_code='HOST_PENDING'", f.contributionId()));
        assertEquals(0, amount("wallet_ledger_entries", "contribution_id=?", f.contributionId()));
        assertEquals(0, count("wallet_ledger_entries", "contribution_id=? AND account_code='HOST_AVAILABLE'", f.contributionId()));
    }

    @Test
    void capturedGiftPaymentReservesGiftFunds() {
        UUID event = event();
        Fixture f = fixture(event, gift(event), 200000);
        provider(f);
        var result = confirm(f);
        assertEquals(200000, result.giftPaise());
        assertEquals(0, result.walletPaise());
        assertEquals(200000, amount("wallet_ledger_entries", "contribution_id=? AND account_code='GIFT_RESERVED'", f.contributionId()));
        assertEquals(0, amount("wallet_ledger_entries", "contribution_id=?", f.contributionId()));
    }

    @Test
    void overfundingSplitsGiftAndHostPending() {
        UUID event = event();
        UUID gift = gift(event);
        Fixture first = fixture(event, gift, 700000);
        provider(first);
        confirm(first);
        Fixture second = fixture(event, gift, 400000);
        provider(second);
        var result = confirm(second);
        assertEquals(300000, result.giftPaise());
        assertEquals(100000, result.walletPaise());
        assertEquals(300000, amount("wallet_ledger_entries", "contribution_id=? AND account_code='GIFT_RESERVED'", second.contributionId()));
        assertEquals(100000, amount("wallet_ledger_entries", "contribution_id=? AND account_code='HOST_PENDING'", second.contributionId()));
        assertEquals(0, amount("wallet_ledger_entries", "contribution_id=?", second.contributionId()));
    }

    @Test
    void repeatedConfirmationDoesNotPostAgain() {
        Fixture f = fixture(null, 50000);
        provider(f);
        var first = confirm(f);
        var second = confirm(f);
        assertFalse(first.alreadyProcessed());
        assertTrue(second.alreadyProcessed());
        assertEquals(first.giftPaise(), second.giftPaise());
        assertEquals(first.walletPaise(), second.walletPaise());
        assertEquals(1, count("payment_transactions", "payment_order_id=?", f.orderId()));
        assertEquals(1, count("contribution_allocation_postings", "contribution_id=?", f.contributionId()));
        assertEquals(2, count("wallet_ledger_entries", "contribution_id=?", f.contributionId()));
    }

    @Test
    void invalidSignatureIsRejectedBeforeProviderLookup() {
        Fixture f = fixture(null, 50000);
        ResponseStatusException e = assertThrows(ResponseStatusException.class, () ->
                service.confirmCheckout(f.orderId(), f.providerOrderId(), f.paymentId(), "not-a-signature"));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
        verify(razorpay, never()).fetchPayment(anyString());
        assertUnposted(f);
    }

    @Test
    void rejectsPaymentWithWrongProviderOrder() {
        Fixture f = fixture(null, 50000);
        provider(f, f.paymentId(), "order_" + compact(), f.amount(), "INR", "captured");
        assertRejected(f, HttpStatus.CONFLICT);
    }

    @Test
    void rejectsPaymentWithWrongProviderPaymentId() {
        Fixture f = fixture(null, 50000);
        provider(f, "pay_" + compact(), f.providerOrderId(), f.amount(), "INR", "captured");
        assertRejected(f, HttpStatus.CONFLICT);
    }

    @Test
    void rejectsPaymentWithWrongAmount() {
        Fixture f = fixture(null, 50000);
        provider(f, f.paymentId(), f.providerOrderId(), 49999, "INR", "captured");
        assertRejected(f, HttpStatus.CONFLICT);
    }

    @Test
    void rejectsPaymentWithWrongCurrency() {
        Fixture f = fixture(null, 50000);
        provider(f, f.paymentId(), f.providerOrderId(), f.amount(), "USD", "captured");
        assertRejected(f, HttpStatus.CONFLICT);
    }

    @Test
    void rejectsAuthorizedButNotCapturedPayment() {
        Fixture f = fixture(null, 50000);
        provider(f, f.paymentId(), f.providerOrderId(), f.amount(), "INR", "authorized");
        assertRejected(f, HttpStatus.CONFLICT);
    }

    @Test
    void rejectsRefundedPayment() {
        Fixture f = fixture(null, 50000);
        provider(f, f.paymentId(), f.providerOrderId(), f.amount(), "INR", "refunded");
        assertRejected(f, HttpStatus.CONFLICT);
    }

    @Test
    void missingContributionRollsBack() {
        Fixture f = fixture(null, 50000);
        jdbc.update("DELETE FROM contributions WHERE id=?", f.contributionId());
        provider(f);
        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> confirm(f));
        assertEquals(HttpStatus.CONFLICT, e.getStatusCode());
        assertEquals("PENDING", jdbc.queryForObject("SELECT status FROM payment_orders WHERE id=?", String.class, f.orderId()));
        assertEquals(0, count("payment_transactions", "payment_order_id=?", f.orderId()));
    }

    @Test
    void differentCapturedPaymentForSameOrderIsRejected() {
        Fixture f = fixture(null, 50000);
        provider(f);
        confirm(f);
        String secondPaymentId = "pay_" + compact();
        when(razorpay.fetchPayment(secondPaymentId)).thenReturn(
                new RazorpayClient.ProviderPayment(secondPaymentId, f.providerOrderId(), f.amount(), "INR", "captured"));
        ResponseStatusException e = assertThrows(ResponseStatusException.class, () ->
                service.confirmCheckout(f.orderId(), f.providerOrderId(), secondPaymentId,
                        signature(f.providerOrderId(), secondPaymentId)));
        assertEquals(HttpStatus.CONFLICT, e.getStatusCode());
        assertEquals(1, count("payment_transactions", "payment_order_id=?", f.orderId()));
        assertEquals(1, count("contribution_allocation_postings", "contribution_id=?", f.contributionId()));
    }

    @Test
    void providerFailureDoesNotWriteFinancialRecords() {
        Fixture f = fixture(null, 50000);
        when(razorpay.fetchPayment(f.paymentId())).thenThrow(new IllegalStateException("Provider unavailable"));
        assertThrows(IllegalStateException.class, () -> confirm(f));
        assertUnposted(f);
    }

    @Test
    void concurrentCallbacksPostExactlyOnce() throws Exception {
        Fixture f = fixture(null, 50000);
        provider(f);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Callable<RazorpayCaptureService.CaptureResult> action = () -> {
                start.await(10, TimeUnit.SECONDS);
                return confirm(f);
            };
            Future<RazorpayCaptureService.CaptureResult> a = pool.submit(action);
            Future<RazorpayCaptureService.CaptureResult> b = pool.submit(action);
            start.countDown();
            var one = a.get(30, TimeUnit.SECONDS);
            var two = b.get(30, TimeUnit.SECONDS);
            assertNotEquals(one.alreadyProcessed(), two.alreadyProcessed());
            assertEquals(1, count("payment_transactions", "payment_order_id=?", f.orderId()));
            assertEquals(1, count("contribution_allocation_postings", "contribution_id=?", f.contributionId()));
            assertEquals(0, amount("wallet_ledger_entries", "contribution_id=?", f.contributionId()));
        } finally {
            pool.shutdownNow();
        }
    }
}
