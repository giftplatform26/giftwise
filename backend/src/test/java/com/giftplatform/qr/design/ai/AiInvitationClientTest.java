package com.giftplatform.qr.design.ai;

import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class AiInvitationClientTest {
    private final ObjectMapper mapper=new ObjectMapper();
    private Map<String,Object> blueprint(){return Map.of("designName","Garden joy","headline","Celebrate Together","message","A lovely day with our favourite people.","closing","Your presence means the world.","palette","SAGE","layout","AIRY","font","SERIF");}
    private String response(Map<String,Object> blueprint){return mapper.writeValueAsString(Map.of("status","completed","output",List.of(Map.of("type","message","content",List.of(Map.of("type","output_text","text",mapper.writeValueAsString(blueprint)))))));}
    @Test void callsResponsesWithStrictSchemaAndStorageDisabled(){
        var builder=RestClient.builder().baseUrl("https://api.openai.com/v1");var server=MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://api.openai.com/v1/responses")).andExpect(method(HttpMethod.POST)).andExpect(header("Authorization","Bearer test-api-key"))
            .andExpect(request->{var body=mapper.readTree(((MockClientHttpRequest)request).getBodyAsString());assertFalse(body.path("store").asBoolean());assertEquals("json_schema",body.path("text").path("format").path("type").asText());assertTrue(body.path("text").path("format").path("strict").asBoolean());})
            .andRespond(withSuccess(response(blueprint()),MediaType.APPLICATION_JSON));
        var client=new AiInvitationClient(mapper,builder.build(),true,"test-api-key","gpt-4.1-mini");
        assertEquals("SAGE",client.generate(Map.of("eventTitle","Garden party")).palette());server.verify();
    }
    @Test void refusesUnconfiguredProviderWithoutCallingNetwork(){
        var client=new AiInvitationClient(mapper,RestClient.create(),true,"","gpt-4.1-mini");
        assertFalse(client.available());assertEquals(503,assertThrows(ResponseStatusException.class,()->client.generate(Map.of())).getStatusCode().value());
    }
    @Test void rejectsIncompleteRefusedUnsafeAndUnsupportedResults(){
        var client=new AiInvitationClient(mapper,RestClient.create(),true,"test","model");
        assertThrows(IllegalArgumentException.class,()->client.parse(mapper.readTree("{\"status\":\"incomplete\"}")));
        assertEquals(422,assertThrows(ResponseStatusException.class,()->client.parse(mapper.readTree("{\"status\":\"completed\",\"output\":[{\"content\":[{\"type\":\"refusal\"}]}]}"))).getStatusCode().value());
        var invalid=new HashMap<>(blueprint());invalid.put("palette","url(javascript:alert(1))");String unsupported=response(invalid);assertThrows(IllegalArgumentException.class,()->client.parse(mapper.readTree(unsupported)));
        invalid=new HashMap<>(blueprint());invalid.put("headline","<script>bad</script>");String unsafe=response(invalid);assertThrows(IllegalArgumentException.class,()->client.parse(mapper.readTree(unsafe)));
        invalid=new HashMap<>(blueprint());invalid.put("headline","x".repeat(61));String longText=response(invalid);assertThrows(IllegalArgumentException.class,()->client.parse(mapper.readTree(longText)));
    }

    @Test void providerBillingRateLimitAndConfigurationErrorsHaveDistinctSafeCodes(){
        record Failure(HttpStatus status,String code,String type,int expected,String category){}
        for(var sample:List.of(new Failure(HttpStatus.TOO_MANY_REQUESTS,"credit_balance_exhausted","insufficient_quota",402,"AI_CREDITS_EXHAUSTED"),new Failure(HttpStatus.TOO_MANY_REQUESTS,"insufficient_quota","insufficient_quota",402,"AI_CREDITS_EXHAUSTED"),new Failure(HttpStatus.TOO_MANY_REQUESTS,"rate_limit_exceeded","requests",503,"AI_PROVIDER_RATE_LIMITED"),new Failure(HttpStatus.UNAUTHORIZED,"invalid_api_key","invalid_request_error",503,"AI_PROVIDER_CONFIGURATION"),new Failure(HttpStatus.INTERNAL_SERVER_ERROR,"server_error","server_error",502,"AI_PROVIDER_UNAVAILABLE"))){
            var builder=RestClient.builder().baseUrl("https://api.openai.com/v1");var server=MockRestServiceServer.bindTo(builder).build();
            server.expect(requestTo("https://api.openai.com/v1/responses")).andRespond(withStatus(sample.status()).contentType(MediaType.APPLICATION_JSON).body(mapper.writeValueAsString(Map.of("error",Map.of("code",sample.code(),"type",sample.type(),"message","PRIVATE_PROVIDER_MESSAGE")))));
            var failure=assertThrows(AiInvitationFailure.class,()->new AiInvitationClient(mapper,builder.build(),true,"private-api-key","gpt-4.1-mini").generate(Map.of("eventTitle","Celebration")));
            assertEquals(sample.expected(),failure.getStatusCode().value());assertEquals(sample.category(),failure.code());assertFalse(failure.getReason().contains("PRIVATE_PROVIDER_MESSAGE"));assertFalse(failure.getReason().contains("private-api-key"));server.verify();
        }
    }
    @Test void structuredTextLengthsMatchTheApplicationValidation(){
        var properties=(Map<String,Object>)AiInvitationClient.schema().get("properties");
        assertEquals(60,((Map<?,?>)properties.get("headline")).get("maxLength"));
        assertEquals(1,((Map<?,?>)properties.get("closing")).get("minLength"));
    }
    @Test void billingProblemResponseContainsOnlySafeDetails(){
        var failure=new AiInvitationFailure(HttpStatus.PAYMENT_REQUIRED,"AI_CREDITS_EXHAUSTED","AI design credits have run out.",new IllegalStateException("PRIVATE_PROVIDER_MESSAGE"));
        var response=new AiInvitationController(null).failure(failure);
        assertEquals(402,response.getStatusCode().value());assertEquals("AI_CREDITS_EXHAUSTED",response.getBody().getProperties().get("code"));
        assertFalse(mapper.writeValueAsString(response.getBody()).contains("PRIVATE_PROVIDER_MESSAGE"));
    }
}
