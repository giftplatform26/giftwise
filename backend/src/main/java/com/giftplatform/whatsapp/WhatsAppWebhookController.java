package com.giftplatform.whatsapp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

/**
 * WhatsApp Cloud API webhook: subscription check (GET) and signed notifications (POST) for replies and delivery
 * receipts. Disabled (404) unless both the verify token and the app secret are configured.
 */
@RestController
@RequestMapping("/api/webhooks/whatsapp")
public class WhatsAppWebhookController {
    private final WhatsAppService wa;
    private final ObjectMapper json;
    private final String verifyToken, appSecret;

    public WhatsAppWebhookController(WhatsAppService wa, ObjectMapper json,
                                     @Value("${gift.whatsapp.meta.verify-token:}") String verifyToken,
                                     @Value("${gift.whatsapp.meta.app-secret:}") String appSecret) {
        this.wa = wa; this.json = json; this.verifyToken = verifyToken; this.appSecret = appSecret;
    }

    private boolean enabled() { return verifyToken.length() >= 16 && appSecret.length() >= 16; }

    @GetMapping(produces = "text/plain")
    public ResponseEntity<String> verify(@RequestParam(name = "hub.mode", required = false) String mode,
                                         @RequestParam(name = "hub.verify_token", required = false) String token,
                                         @RequestParam(name = "hub.challenge", required = false) String challenge) {
        if (!enabled()) return ResponseEntity.notFound().build();
        if (!"subscribe".equals(mode) || token == null || challenge == null || !challenge.matches("[A-Za-z0-9_-]{1,100}")
            || !MessageDigest.isEqual(token.getBytes(StandardCharsets.UTF_8), verifyToken.getBytes(StandardCharsets.UTF_8)))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(challenge);
    }

    @PostMapping(consumes = "application/json")
    public ResponseEntity<Void> receive(@RequestHeader(name = "X-Hub-Signature-256", required = false) String signature, @RequestBody byte[] raw) {
        if (!enabled()) return ResponseEntity.notFound().build();
        if (raw.length > 512_000 || !validSignature(signature, raw)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        JsonNode root;
        try { root = json.readTree(raw); } catch (Exception e) { return ResponseEntity.badRequest().build(); }
        for (JsonNode entry : root.path("entry")) for (JsonNode change : entry.path("changes")) {
            JsonNode value = change.path("value");
            for (JsonNode m : value.path("messages")) {
                String text = m.path("text").path("body").asString(null);
                if (text == null) text = m.path("button").path("text").asString(null);
                if (text == null) text = m.path("interactive").path("button_reply").path("title").asString(null);
                wa.inbound(m.path("id").asString(null), m.path("from").asString(null), text);
            }
            for (JsonNode s : value.path("statuses")) wa.status(s.path("id").asString(null), s.path("status").asString(null));
        }
        // Always 200 for valid signed payloads so the provider does not retry endlessly.
        return ResponseEntity.ok().build();
    }

    boolean validSignature(String header, byte[] raw) {
        if (header == null || !header.startsWith("sha256=")) return false;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(appSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] expected = mac.doFinal(raw);
            byte[] given = HexFormat.of().parseHex(header.substring(7));
            return MessageDigest.isEqual(expected, given);
        } catch (Exception e) { return false; }
    }
}
