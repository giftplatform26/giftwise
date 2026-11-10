package com.giftplatform.security.delivery;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Configuration
public class EmailDeliveryConfiguration {
    @org.springframework.beans.factory.annotation.Autowired(required=false) private com.giftplatform.localization.LocalizationService localization;
    @org.springframework.beans.factory.annotation.Autowired(required=false) private com.giftplatform.notification.email.BrandedEmailService branded;
    private String localized(String text){return localization==null?text:localization.localizeRendered(text,org.springframework.context.i18n.LocaleContextHolder.getLocale().toLanguageTag());}
    @Bean("otpEmailDelivery")
    MessageDelivery emailDelivery(
            @Value("${gift.otp.email.provider:disabled}") String provider,
            @Value("${gift.otp.email.resend.api-key:}") String apiKey,
            @Value("${gift.otp.email.from:}") String from) {
        return transport(provider,apiKey,from,"Gift Platform security code");
    }
    @Bean("usernameReminderDelivery")
    MessageDelivery usernameReminderDelivery(
            @Value("${gift.otp.email.provider:disabled}") String provider,
            @Value("${gift.otp.email.resend.api-key:}") String apiKey,
            @Value("${gift.otp.email.from:}") String from) {
        return transport(provider,apiKey,from,"Your Gift Platform username");
    }
    private MessageDelivery transport(String provider,String apiKey,String from,String subject) {
        if ("disabled".equals(provider)||("resend".equals(provider)&&(apiKey.isBlank()||from.isBlank()))) return new MessageDelivery() {
            @Override public boolean isAvailable() { return false; }
            @Override public void send(String to,String body) { throw new IllegalStateException("Email OTP delivery is not configured"); }
        };
        if (!"resend".equals(provider)) throw new IllegalStateException("Unsupported email OTP provider");
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        return (to, body) -> {
            if (to == null || !to.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) throw new IllegalArgumentException("Invalid recipient");
            if (branded != null && branded.available()) {
                branded.send(to, branded.security(localized(subject.replace("Gift Platform", "GiftWise")), localized(body)), "giftwise-security-"+java.util.UUID.randomUUID());
                return;
            }
            String json = "{\"from\":\"" + escape(from) + "\",\"to\":[\"" + escape(to) + "\"],\"subject\":\"" + escape(localized(subject)) + "\",\"text\":\"" + escape(localized(body)) + "\"}";
            HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.resend.com/emails"))
                    .timeout(Duration.ofSeconds(10))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json)).build();
            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() < 200 || response.statusCode() >= 300)
                    throw EmailDeliveryException.rejected(response.statusCode(),response.body());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new EmailDeliveryException("Email delivery was interrupted. Please try again.");
            } catch (java.io.IOException e) {
                throw new EmailDeliveryException("Could not connect to the email provider. Please try again later.");
            }
        };
    }
    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}
