package com.giftplatform.gift;

import com.giftplatform.gift.service.GiftManagementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.UUID;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties="gift.otp.secret-base64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
@AutoConfigureMockMvc
@Testcontainers
class GiftHttpBoundaryIntegrationTest {
    @Container static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");
    @DynamicPropertySource static void db(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
    }
    @Autowired MockMvc mvc;
    @MockitoBean GiftManagementService service;
    static final UUID EVENT = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    static final UUID GIFT = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    static final String BASE = "/api/v1/events/"+EVENT+"/gifts";
    static final String BODY = "{\"name\":\"Mixer\",\"targetAmount\":10000.00}";

    @Test void anonymousPublicationDenied() throws Exception {
        mvc.perform(put(BASE+"/"+GIFT+"/publication").with(csrf()).contentType("application/json").content("{\"visibility\":\"PUBLIC\"}"))
                .andExpect(status().isUnauthorized()); verifyNoInteractions(service);
    }
    @Test void publicationRequiresCsrf() throws Exception {
        mvc.perform(put(BASE+"/"+GIFT+"/publication").with(user("host")).contentType("application/json").content("{\"visibility\":\"PUBLIC\"}"))
                .andExpect(status().isForbidden()); verifyNoInteractions(service);
    }
    @Test void ownerCanPublish() throws Exception {
        mvc.perform(put(BASE+"/"+GIFT+"/publication").with(user("host")).with(csrf()).contentType("application/json").content("{\"visibility\":\"PUBLIC\"}"))
                .andExpect(status().isOk()); verify(service).setPublication(EVENT,GIFT,com.giftplatform.gift.entity.GiftVisibility.PUBLIC);
    }
    @Test void ownerCanUnpublish() throws Exception {
        mvc.perform(put(BASE+"/"+GIFT+"/publication").with(user("host")).with(csrf()).contentType("application/json").content("{\"visibility\":\"DRAFT\"}"))
                .andExpect(status().isOk()); verify(service).setPublication(EVENT,GIFT,com.giftplatform.gift.entity.GiftVisibility.DRAFT);
    }
    @Test void missingVisibilityRejected() throws Exception {
        mvc.perform(put(BASE+"/"+GIFT+"/publication").with(user("host")).with(csrf()).contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest()); verifyNoInteractions(service);
    }
    @Test void invalidVisibilityRejected() throws Exception {
        mvc.perform(put(BASE+"/"+GIFT+"/publication").with(user("host")).with(csrf()).contentType("application/json").content("{\"visibility\":\"HIDDEN\"}"))
                .andExpect(status().isBadRequest()); verifyNoInteractions(service);
    }
    @Test void productLinkRequiresCsrf() throws Exception {
        mvc.perform(put(BASE+"/"+GIFT+"/product-link").with(user("host")).contentType("application/json").content("{\"productUrl\":\"https://example.com/p\"}"))
                .andExpect(status().isForbidden()); verifyNoInteractions(service);
    }
    @Test void ownerCanSetProductLink() throws Exception {
        mvc.perform(put(BASE+"/"+GIFT+"/product-link").with(user("host")).with(csrf()).contentType("application/json").content("{\"productUrl\":\"https://example.com/p\"}"))
                .andExpect(status().isOk()); verify(service).setProductLink(EVENT,GIFT,"https://example.com/p");
    }
    @Test void crossHostPublicationNotFound() throws Exception {
        when(service.setPublication(eq(EVENT),eq(GIFT),any())).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));
        mvc.perform(put(BASE+"/"+GIFT+"/publication").with(user("otherHost")).with(csrf()).contentType("application/json").content("{\"visibility\":\"PUBLIC\"}"))
                .andExpect(status().isNotFound());
    }
    @Test void crossHostGetNotFound() throws Exception {
        when(service.get(EVENT,GIFT)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));
        mvc.perform(get(BASE+"/"+GIFT).with(user("otherHost"))).andExpect(status().isNotFound());
    }
    @Test void customerAuditDenied() throws Exception {
        mvc.perform(get("/api/admin/audit").param("entityType","GIFT").param("entityId",GIFT.toString()).with(user("host").roles("CUSTOMER")))
                .andExpect(status().isForbidden());
    }
    @Test void anonymousAuditDenied() throws Exception {
        mvc.perform(get("/api/admin/audit").param("entityType","GIFT").param("entityId",GIFT.toString()))
                .andExpect(status().isUnauthorized());
    }
    @Test void administratorCanQueryAudit() throws Exception {
        mvc.perform(get("/api/admin/audit").param("entityType","GIFT").param("entityId",GIFT.toString()).with(user("admin").roles("PLATFORM_ADMIN")))
                .andExpect(status().isOk());
    }
}
