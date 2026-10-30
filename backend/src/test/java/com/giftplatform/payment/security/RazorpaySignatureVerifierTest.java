package com.giftplatform.payment.security;

import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.*;

class RazorpaySignatureVerifierTest {

    private final RazorpaySignatureVerifier verifier =
            new RazorpaySignatureVerifier();

    private static final String SECRET = "test-secret-value";

    private String sign(String message, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"));

        return HexFormat.of().formatHex(
                mac.doFinal(message.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void validCheckoutSignatureIsAccepted() throws Exception {
        String signature = sign("order_123|pay_456", SECRET);

        assertTrue(verifier.verifyCheckout(
                "order_123", "pay_456", signature, SECRET));
    }

    @Test
    void incorrectCheckoutSignatureIsRejected() throws Exception {
        String signature = sign("order_123|pay_wrong", SECRET);

        assertFalse(verifier.verifyCheckout(
                "order_123", "pay_456", signature, SECRET));
    }

    @Test
    void tamperedOrderIsRejected() throws Exception {
        String signature = sign("order_123|pay_456", SECRET);

        assertFalse(verifier.verifyCheckout(
                "order_999", "pay_456", signature, SECRET));
    }

    @Test
    void incorrectSecretIsRejected() throws Exception {
        String signature = sign("order_123|pay_456", SECRET);

        assertFalse(verifier.verifyCheckout(
                "order_123", "pay_456", signature, "wrong-secret"));
    }

    @Test
    void validWebhookSignatureIsAccepted() throws Exception {
        String body = "{\"event\":\"payment.captured\"}";
        String signature = sign(body, SECRET);

        assertTrue(verifier.verifyWebhook(body, signature, SECRET));
    }

    @Test
    void modifiedWebhookBodyIsRejected() throws Exception {
        String body = "{\"event\":\"payment.captured\"}";
        String signature = sign(body, SECRET);

        assertFalse(verifier.verifyWebhook(
                body + " ", signature, SECRET));
    }

    @Test
    void checkoutSignatureCannotBeUsedAsWebhookSignature()
            throws Exception {
        String signature = sign("order_123|pay_456", SECRET);

        assertFalse(verifier.verifyWebhook(
                "{\"event\":\"payment.captured\"}",
                signature, SECRET));
    }

    @Test
    void malformedSignatureIsRejected() {
        assertFalse(verifier.verifyCheckout(
                "order_123", "pay_456", "invalid", SECRET));
    }

    @Test
    void missingInputsAreRejected() {
        assertFalse(verifier.verifyCheckout(
                null, "pay_456", "abc", SECRET));

        assertFalse(verifier.verifyCheckout(
                "order_123", null, "abc", SECRET));

        assertFalse(verifier.verifyWebhook(
                null, "abc", SECRET));

        assertFalse(verifier.verifyWebhook(
                "body", null, SECRET));
    }

    @Test
    void emptySecretIsRejected() throws Exception {
        String signature = sign("order_123|pay_456", SECRET);

        assertFalse(verifier.verifyCheckout(
                "order_123", "pay_456", signature, ""));
    }

    @Test
    void differentWebhookSecretIsRejected() throws Exception {
        String body = "{\"event\":\"payment.captured\"}";
        String signature = sign(body, SECRET);

        assertFalse(verifier.verifyWebhook(
                body, signature, "different-secret"));
    }
}
