package com.giftplatform.audit;

import com.giftplatform.audit.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.Page;
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
class AdminAuditSecurityIntegrationTest {
    @Container static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");
    @DynamicPropertySource static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
    }
    @Autowired MockMvc mvc;
    @MockitoBean AuditLogRepository repository;
    static final String URL = "/api/admin/audit?entityType=EVENT&entityId=aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";
    @Test void anonymousDenied() throws Exception { mvc.perform(get(URL)).andExpect(status().isUnauthorized()); verifyNoInteractions(repository); }
    @Test void customerDenied() throws Exception { mvc.perform(get(URL).with(user("host").roles("CUSTOMER"))).andExpect(status().isForbidden()); verifyNoInteractions(repository); }
    @Test void platformAdminAllowed() throws Exception {
        when(repository.findByEntityTypeAndEntityIdOrderByOccurredAtDesc(eq("EVENT"), any(UUID.class), any())).thenReturn(Page.empty());
        mvc.perform(get(URL).with(user("admin").roles("PLATFORM_ADMIN")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content").isArray());
        verify(repository).findByEntityTypeAndEntityIdOrderByOccurredAtDesc(eq("EVENT"), any(UUID.class), any());
    }
    @Test void adminInvalidPageRejected() throws Exception {
        mvc.perform(get(URL+"&size=101").with(user("admin").roles("PLATFORM_ADMIN")))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(repository);
    }
}
