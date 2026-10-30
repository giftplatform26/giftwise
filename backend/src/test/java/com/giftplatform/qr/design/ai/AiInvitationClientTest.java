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
}
