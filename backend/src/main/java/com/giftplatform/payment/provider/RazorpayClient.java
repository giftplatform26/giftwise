package com.giftplatform.payment.provider;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class RazorpayClient {

    private final RestClient client;
    private final ObjectMapper mapper;
    private final String keyId;
    private final String keySecret;

    public RazorpayClient(
            ObjectMapper mapper,
            @Value("${gift.payment.razorpay.key-id:}") String keyId,
            @Value("${gift.payment.razorpay.key-secret:}") String keySecret) {

        var transport = java.net.http.HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(5))
                .followRedirects(java.net.http.HttpClient.Redirect.NEVER).build();
        var requests = new org.springframework.http.client.JdkClientHttpRequestFactory(transport);
        requests.setReadTimeout(java.time.Duration.ofSeconds(20));
        this.client = RestClient.builder().requestFactory(requests)
                .baseUrl("https://api.razorpay.com/v1")
                .build();

        this.mapper = mapper;
        this.keyId = keyId;
        this.keySecret = keySecret;
    }

    private void requireCredentials() {
        if (!keyId.startsWith("rzp_test_")
                || keySecret.isBlank()) {
            throw new IllegalStateException(
                    "Only Razorpay Test Mode credentials are permitted");
        }
    }

    private String authorization() {
        String credentials = keyId + ":" + keySecret;
        return "Basic " + Base64.getEncoder().encodeToString(
                credentials.getBytes(StandardCharsets.UTF_8));
    }

    public record ProviderOrder(
            String id,
            long amount,
            String currency,
            String status
    ) {}

    public record ProviderPayment(
            String id,
            String orderId,
            long amount,
            String currency,
            String status,
            String method,
            String bank,
            String bankTransactionId,
            String rrn,
            String upiTransactionId,
            Long feePaise,
            Long taxPaise
    ) {
        public ProviderPayment(String id,String orderId,long amount,String currency,String status,String method,String bank,String bankTransactionId,String rrn,String upiTransactionId) {
            this(id,orderId,amount,currency,status,method,bank,bankTransactionId,rrn,upiTransactionId,null,null);
        }
        public ProviderPayment(String id, String orderId, long amount, String currency, String status) {
            this(id, orderId, amount, currency, status, null, null, null, null, null);
        }
    }

    private <T> T providerCall(java.util.function.Supplier<T> operation) {
        try { return operation.get(); }
        catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 401 || ex.getStatusCode().value() == 403) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Razorpay test credentials were rejected. Check backend test key ID and secret.");
            }
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                "Razorpay is temporarily unavailable. No payment has been confirmed.");
        }
    }

    public ProviderOrder createOrder(
            long amountPaise,
            String receipt) {

        requireCredentials();

        if (amountPaise <= 0 || receipt == null
                || receipt.isBlank() || receipt.length() > 40) {
            throw new IllegalArgumentException(
                    "Invalid Razorpay order request");
        }

        JsonNode result = providerCall(() -> client.post()
                .uri("/orders")
                .header(HttpHeaders.AUTHORIZATION, authorization())
                .contentType(MediaType.APPLICATION_JSON)
                .body(java.util.Map.of(
                        "amount", amountPaise,
                        "currency", "INR",
                        "receipt", receipt))
                .retrieve()
                .body(JsonNode.class));

        if (result == null) {
            throw new IllegalStateException(
                    "Empty Razorpay order response");
        }

        String id = result.path("id").asText();
        long amount = result.path("amount").asLong();
        String currency = result.path("currency").asText();
        String status = result.path("status").asText();

        if (id.isBlank() || amount != amountPaise
                || !"INR".equals(currency)
                || !"created".equals(status)) {
            throw new IllegalStateException(
                    "Invalid Razorpay order response");
        }

        return new ProviderOrder(id, amount, currency, status);
    }

    private static String optionalText(JsonNode value) {
        return value.isMissingNode() || value.isNull() ? null : value.asText();
    }

    private static Long optionalAmount(JsonNode value) {
        if(value.isMissingNode()||value.isNull())return null;
        if(!value.isIntegralNumber()||!value.canConvertToLong()||value.asLong()<0)throw new IllegalStateException("Invalid provider charge amount");
        return value.asLong();
    }
    public ProviderPayment fetchPayment(String paymentId) {
        requireCredentials();

        if (paymentId == null ||
                !paymentId.matches("pay_[A-Za-z0-9]+")) {
            throw new IllegalArgumentException(
                    "Invalid Razorpay payment ID");
        }

        JsonNode result = providerCall(() -> client.get()
                .uri("/payments/{id}", paymentId)
                .header(HttpHeaders.AUTHORIZATION, authorization())
                .retrieve()
                .body(JsonNode.class));

        if (result == null) {
            throw new IllegalStateException(
                    "Empty Razorpay payment response");
        }

        return new ProviderPayment(
                result.path("id").asText(),
                result.path("order_id").asText(),
                result.path("amount").asLong(),
                result.path("currency").asText(),
                result.path("status").asText(),
                optionalText(result.path("method")),
                optionalText(result.path("bank")),
                optionalText(result.path("acquirer_data").path("bank_transaction_id")),
                optionalText(result.path("acquirer_data").path("rrn")),
                optionalText(result.path("acquirer_data").path("upi_transaction_id")),
                optionalAmount(result.path("fee")),optionalAmount(result.path("tax"))
        );
    }
}
