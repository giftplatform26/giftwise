package com.giftplatform.event;

import com.giftplatform.event.service.EventManagementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "gift.otp.secret-base64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
@AutoConfigureMockMvc
@Testcontainers
class EventConflictHttpIntegrationTest {
    @Container static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");
    @DynamicPropertySource static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
    }
    @Autowired MockMvc mvc;
    @MockitoBean EventManagementService service;
    private static final UUID ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    @Test void staleUpdateReturns409() throws Exception {
        when(service.update(eq(ID), any())).thenThrow(new OptimisticLockingFailureException("stale"));
        mvc.perform(put("/api/v1/events/"+ID).with(user("host")).with(csrf())
                .contentType("application/json")
                .content("{\"title\":\"Birthday\",\"occasionType\":\"BIRTHDAY\",\"hostName\":\"Fixture host\",\"addressLine1\":\"1 Fixture Street\",\"city\":\"Bengaluru\",\"state\":\"Karnataka\",\"postalCode\":\"560001\",\"country\":\"India\",\"contactName\":\"Fixture contact\",\"contactPhone\":\"+919876543210\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }
    @Test void stalePublishReturns409() throws Exception {
        when(service.changeStatus(eq(ID), any())).thenThrow(new OptimisticLockingFailureException("stale"));
        mvc.perform(post("/api/v1/events/"+ID+"/publish").with(user("host")).with(csrf()))
                .andExpect(status().isConflict());
    }
    @Test void noCsrfStill403() throws Exception {
        mvc.perform(put("/api/v1/events/"+ID).with(user("host"))
                .contentType("application/json")
                .content("{\"title\":\"Birthday\",\"occasionType\":\"BIRTHDAY\",\"hostName\":\"Fixture host\",\"addressLine1\":\"1 Fixture Street\",\"city\":\"Bengaluru\",\"state\":\"Karnataka\",\"postalCode\":\"560001\",\"country\":\"India\",\"contactName\":\"Fixture contact\",\"contactPhone\":\"+919876543210\"}"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }
}
