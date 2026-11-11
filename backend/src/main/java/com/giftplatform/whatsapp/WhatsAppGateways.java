package com.giftplatform.whatsapp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.databind.JsonNode;

import java.time.Duration;
import java.util.*;

/**
 * Chooses the WhatsApp provider from {@code gift.whatsapp.provider}:
 * DEV_OUTBOX (default, nothing leaves the server) or META_CLOUD (WhatsApp Business Cloud API with approved templates).
 */
@Configuration
public class WhatsAppGateways {

    @Bean
    WhatsAppGateway whatsAppGateway(@Value("${gift.whatsapp.provider:DEV_OUTBOX}") String provider,
                                    @Value("${gift.whatsapp.meta.phone-number-id:}") String phoneNumberId,
                                    @Value("${gift.whatsapp.meta.access-token:}") String accessToken,
                                    @Value("${gift.whatsapp.meta.api-version:v21.0}") String apiVersion,
                                    @Value("${gift.whatsapp.templates.invitation:giftwise_invitation}") String invitation,
                                    @Value("${gift.whatsapp.templates.reminder:giftwise_reminder}") String reminder,
                                    @Value("${gift.whatsapp.templates.thank-you:giftwise_thank_you}") String thankYou,
                                    @Value("${gift.whatsapp.templates.language:en}") String language) {
        if ("META_CLOUD".equalsIgnoreCase(provider)) {
            if (!phoneNumberId.matches("[0-9]{5,30}") || accessToken.isBlank() || !apiVersion.matches("v[0-9]+\\.[0-9]+"))
                throw new IllegalStateException("gift.whatsapp.provider=META_CLOUD needs gift.whatsapp.meta.phone-number-id and access-token");
            return new MetaCloud(phoneNumberId, accessToken, apiVersion, Map.of("INVITATION", invitation, "REMINDER", reminder, "THANK_YOU", thankYou), language);
        }
        if (!"DEV_OUTBOX".equalsIgnoreCase(provider)) throw new IllegalStateException("Unknown gift.whatsapp.provider: " + provider);
        return new DevOutbox();
    }

    /** Records messages as sent without contacting anyone. Lets hosts and testers try the full flow safely. */
    static final class DevOutbox implements WhatsAppGateway {
        public String name() { return "DEV_OUTBOX"; }
        public boolean live() { return false; }
        public Result send(String phone, String kind, List<String> params) { return Result.ok("dev-" + UUID.randomUUID()); }
    }

    static final class MetaCloud implements WhatsAppGateway {
        private final RestClient client;
        private final String phoneNumberId, language;
        private final Map<String, String> templates;

        MetaCloud(String phoneNumberId, String token, String version, Map<String, String> templates, String language) {
            var transport = java.net.http.HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5))
                .followRedirects(java.net.http.HttpClient.Redirect.NEVER).build();
            var requests = new org.springframework.http.client.JdkClientHttpRequestFactory(transport);
            requests.setReadTimeout(Duration.ofSeconds(15));
            this.client = RestClient.builder().requestFactory(requests).baseUrl("https://graph.facebook.com/" + version)
                .defaultHeader("Authorization", "Bearer " + token).build();
            this.phoneNumberId = phoneNumberId; this.templates = templates; this.language = language;
        }
        public String name() { return "META_CLOUD"; }
        public boolean live() { return true; }
        public Result send(String phone, String kind, List<String> params) {
            var parameters = params.stream().map(p -> Map.of("type", "text", "text", p)).toList();
            var body = Map.of("messaging_product", "whatsapp", "to", phone.substring(1), "type", "template",
                "template", Map.of("name", templates.get(kind), "language", Map.of("code", language),
                    "components", List.of(Map.of("type", "body", "parameters", parameters))));
            try {
                JsonNode res = client.post().uri("/{id}/messages", phoneNumberId).contentType(MediaType.APPLICATION_JSON).body(body)
                    .retrieve().body(JsonNode.class);
                JsonNode id = res == null ? null : res.path("messages").path(0).path("id");
                return id == null || id.isMissingNode() ? Result.fail("NO_MESSAGE_ID", true) : Result.ok(id.asString());
            } catch (RestClientResponseException e) {
                int status = e.getStatusCode().value();
                return Result.fail("HTTP_" + status, status == 429 || status >= 500);
            } catch (Exception e) {
                return Result.fail("NETWORK", true);
            }
        }
    }
}
