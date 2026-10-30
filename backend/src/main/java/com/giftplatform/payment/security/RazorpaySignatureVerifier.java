package com.giftplatform.payment.security;

import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@Component
public class RazorpaySignatureVerifier {

    public boolean verifyCheckout(
            String orderId,
            String paymentId,
            String signature,
            String keySecret) {

        if (blank(orderId) || blank(paymentId)
                || blank(signature) || blank(keySecret)) {
            return false;
        }

        return verify(
                orderId + "|" + paymentId,
                signature,
                keySecret);
    }

    public boolean verifyWebhook(
            String rawBody,
            String signature,
            String webhookSecret) {

        if (rawBody == null || blank(signature)
                || blank(webhookSecret)) {
            return false;
        }

        return verify(rawBody, signature, webhookSecret);
    }

    private boolean verify(
            String message,
            String signature,
            String secret) {

        try {
            if (!signature.matches("(?i)[0-9a-f]{64}")) {
                return false;
            }

            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"));

            byte[] expected = mac.doFinal(
                    message.getBytes(StandardCharsets.UTF_8));

            byte[] supplied = HexFormat.of()
                    .parseHex(signature.toLowerCase(java.util.Locale.ROOT));

            return MessageDigest.isEqual(expected, supplied);

        } catch (Exception ex) {
            return false;
        }
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
