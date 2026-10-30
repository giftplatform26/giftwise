package com.giftplatform.hostpayment.verification.account;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.Map;

@Component
public class AccountVerificationClient {
    private final boolean enabled;
    private final String environment,clientId,clientSecret;
    private final ObjectMapper mapper;
    private final HttpClient http;
    public AccountVerificationClient(ObjectMapper mapper,
        @Value("${gift.payment.account-verification.enabled:true}") boolean enabled,
        @Value("${gift.payment.account-verification.environment:SANDBOX}") String environment,
        @Value("${gift.cashfree.secureid.client-id:}") String clientId,
        @Value("${gift.cashfree.secureid.client-secret:}") String clientSecret) {
        if(!java.util.Set.of("SANDBOX","PRODUCTION").contains(environment))
            throw new IllegalArgumentException("Invalid account verification environment");
        this.mapper=mapper;this.enabled=enabled;this.environment=environment;
        this.clientId=clientId;this.clientSecret=clientSecret;
        this.http=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3))
            .followRedirects(HttpClient.Redirect.NEVER).build();
    }
    public String environment(){return environment;}
    public boolean available(){return enabled&&!clientId.isBlank()&&!clientSecret.isBlank();}
    public JsonNode verify(String kind,Map<String,String> payload) {
        if(!available())throw new IllegalStateException("Account verification credentials are not configured");
        String path=switch(kind){case "BANK"->"/bank-account/sync";case "UPI"->"/upi";default->throw new IllegalArgumentException("Invalid kind");};
        String base=environment.equals("SANDBOX")?"https://sandbox.cashfree.com/verification":"https://api.cashfree.com/verification";
        try {
            var request=HttpRequest.newBuilder(URI.create(base+path)).timeout(Duration.ofSeconds(10))
                .header("Content-Type","application/json").header("Accept","application/json")
                .header("x-client-id",clientId).header("x-client-secret",clientSecret)
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(payload))).build();
            var response=http.send(request,HttpResponse.BodyHandlers.ofInputStream());
            try(var stream=response.body()){
                byte[] bytes=stream.readNBytes(65537);
                if(response.statusCode()!=200||bytes.length>65536)throw new IllegalStateException("Account verification provider unavailable");
                return mapper.readTree(bytes);
            }
        }catch(InterruptedException ex){Thread.currentThread().interrupt();throw new IllegalStateException("Account verification interrupted");}
        catch(java.io.IOException ex){throw new IllegalStateException("Account verification provider unavailable");}
    }
    public record Decision(String outcome,String providerStatus,String providerReference,boolean nameMatched) {}
    public static Decision interpret(JsonNode result,String kind,String reference,String upi,String expectedName,boolean sandbox){
        String ref=result.path("reference_id").asText("");
        String status=result.path(kind.equals("BANK")?"account_status":"status").asText("");
        boolean bound=kind.equals("BANK") || (reference.equals(result.path("verification_id").asText())&&upi.equalsIgnoreCase(result.path("vpa").asText()));
        boolean name=normalize(expectedName).equals(normalize(result.path("name_at_bank").asText()))&&!normalize(expectedName).isBlank();
        if(kind.equals("BANK"))name=name&&"DIRECT_MATCH".equals(result.path("name_match_result").asText());
        String outcome=!bound||!ref.matches("[A-Za-z0-9_-]{1,120}")?"INCONCLUSIVE":status.equals("INVALID")||status.equals("EXPIRED")?"REJECTED":
            !status.equals("VALID")?"INCONCLUSIVE":!name?"NAME_MISMATCH":sandbox?"SANDBOX_VALID":"VERIFIED";
        return new Decision(outcome,status.matches("[A-Z_]{1,40}")?status:"UNKNOWN",ref.matches("[A-Za-z0-9_-]{1,120}")?ref:null,name);
    }
    private static String normalize(String value){return value==null?"":value.replaceAll("[^\\p{L}\\p{N}]","").toUpperCase(java.util.Locale.ROOT);}
}
