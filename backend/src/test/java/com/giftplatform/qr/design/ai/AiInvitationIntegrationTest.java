package com.giftplatform.qr.design.ai;

import com.giftplatform.security.ActorContext;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties="gift.otp.secret-base64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
@AutoConfigureMockMvc
@Testcontainers
class AiInvitationIntegrationTest {
    @Container static PostgreSQLContainer postgres=new PostgreSQLContainer("postgres:17");
    @DynamicPropertySource static void db(DynamicPropertyRegistry r){r.add("spring.datasource.url",postgres::getJdbcUrl);r.add("spring.datasource.username",postgres::getUsername);r.add("spring.datasource.password",postgres::getPassword);}
    @Autowired MockMvc mvc;@Autowired JdbcTemplate db;
    @MockitoBean ActorContext actor;@MockitoBean AiInvitationClient client;
    UUID host,event;
    String input="{\"visualStyle\":\"FLORAL\",\"tone\":\"WARM\",\"language\":\"English\",\"publicVenue\":\"Public Garden\"}";
    String path(){return "/api/v1/events/"+event+"/qr/ai-design";}
    @BeforeEach void setup(){
        host=UUID.randomUUID();event=UUID.randomUUID();
        db.update("INSERT INTO users(id,email,password_hash,role) VALUES (?,?,?,'CUSTOMER')",host,host+"@example.test","test-hash");
        db.update("INSERT INTO events(id,title,occasion_type,owner_id,host_display_name) VALUES (?,'Garden party','BIRTHDAY',?,'Jane')",event,host);
        when(actor.requireAuthenticatedUserId()).thenReturn(host);when(client.available()).thenReturn(true);
        when(client.generate(anyMap())).thenReturn(new AiInvitationClient.Blueprint("Garden joy","Join us","A day with our favourite people","Your presence is our gift","SAGE","AIRY","SERIF"));
    }
    @Test void anonymousAndMissingCsrfCannotGenerate() throws Exception {
        mvc.perform(post(path()).with(csrf()).contentType("application/json").content(input)).andExpect(status().isUnauthorized());
        mvc.perform(post(path()).with(user("host").roles("CUSTOMER")).contentType("application/json").content(input)).andExpect(status().isForbidden());
        verify(client,never()).generate(anyMap());
    }
    @Test void ownedEventReturnsPreviewAndDoesNotPublish() throws Exception {
        mvc.perform(post(path()).with(user("host").roles("CUSTOMER")).with(csrf()).contentType("application/json").content(input))
            .andExpect(status().isOk()).andExpect(jsonPath("$.blueprint.palette").value("SAGE")).andExpect(jsonPath("$.eventTitle").value("Garden party"));
        assertEquals("DRAFT",db.queryForObject("SELECT status FROM events WHERE id=?",String.class,event));
        assertEquals(1,db.queryForObject("SELECT count(*) FROM invitation_ai_requests WHERE host_id=? AND status='COMPLETED'",Integer.class,host));
    }
    @Test void anotherHostCannotGenerate() throws Exception {
        when(actor.requireAuthenticatedUserId()).thenReturn(UUID.randomUUID());
        mvc.perform(post(path()).with(user("other").roles("CUSTOMER")).with(csrf()).contentType("application/json").content(input)).andExpect(status().isNotFound());verify(client,never()).generate(anyMap());
    }
    @Test void malformedPreferencesCannotCallProvider() throws Exception {
        mvc.perform(post(path()).with(user("host").roles("CUSTOMER")).with(csrf()).contentType("application/json").content(input.replace("FLORAL","<script>"))).andExpect(status().isBadRequest());verify(client,never()).generate(anyMap());
    }
    @Test void missingConfigurationReportsUnavailableWithoutUsingQuota() throws Exception {
        when(client.available()).thenReturn(false);
        mvc.perform(get(path()).with(user("host").roles("CUSTOMER"))).andExpect(status().isOk()).andExpect(jsonPath("$.available").value(false));
        mvc.perform(post(path()).with(user("host").roles("CUSTOMER")).with(csrf()).contentType("application/json").content(input)).andExpect(status().isServiceUnavailable());
        assertEquals(0,db.queryForObject("SELECT count(*) FROM invitation_ai_requests WHERE host_id=?",Integer.class,host));
    }
    @Test void quotaPreventsAdditionalProviderCalls() throws Exception {
        for(int i=0;i<10;i++)db.update("INSERT INTO invitation_ai_requests(id,host_id,event_id,status) VALUES(?,?,?,'COMPLETED')",UUID.randomUUID(),host,event);
        mvc.perform(post(path()).with(user("host").roles("CUSTOMER")).with(csrf()).contentType("application/json").content(input)).andExpect(status().isTooManyRequests());verify(client,never()).generate(anyMap());
    }

    @Test void eventContextPrefillsSavedDetailsAndRequiresOwnership() throws Exception {
        db.update("UPDATE events SET description='Birthday with our favourite people',address_line1='123 Garden Road',address_city='Kochi',address_state='Kerala' WHERE id=?",event);
        mvc.perform(get(path()+"/context").with(user("host").roles("CUSTOMER")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.title").value("Garden party"))
            .andExpect(jsonPath("$.hostName").value("Jane")).andExpect(jsonPath("$.visualStyle").value("PLAYFUL"))
            .andExpect(jsonPath("$.venue").value("123 Garden Road, Kochi, Kerala"))
            .andExpect(jsonPath("$.contactPhone").doesNotExist()).andExpect(jsonPath("$.contactEmail").doesNotExist());
        when(actor.requireAuthenticatedUserId()).thenReturn(UUID.randomUUID());
        mvc.perform(get(path()+"/context").with(user("other").roles("CUSTOMER"))).andExpect(status().isNotFound());
        verify(client,never()).generate(anyMap());
    }
}
