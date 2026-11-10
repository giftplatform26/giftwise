package com.giftplatform.payment.settlement;

import com.giftplatform.security.GiftUserPrincipal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.postgresql.PostgreSQLContainer;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties="gift.payment.settlement-ui.enabled=true")
@AutoConfigureMockMvc
class HostSettlementHttpIntegrationTest {
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17")
        .withDatabaseName("gift_5ef_http").withUsername("test").withPassword("test");
    static { postgres.start(); }
    @DynamicPropertySource static void db(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
    }
    @Autowired MockMvc mvc;
    @MockitoBean HostSettlementUiService service;
    final UUID event=UUID.randomUUID(), bank=UUID.randomUUID(), request=UUID.randomUUID();
    String base(){return "/api/v1/host/settlement/events/"+event;}
    UsernamePasswordAuthenticationToken login() {
        var principal=new GiftUserPrincipal(UUID.randomUUID(),"host@example.com","unused","CUSTOMER",true);
        return new UsernamePasswordAuthenticationToken(principal,null,principal.getAuthorities());
    }
    @Test void anonymousWalletRejected() throws Exception {
        mvc.perform(get(base()+"/wallet")).andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }
    @Test void anonymousPostWithCsrfRejected() throws Exception {
        mvc.perform(post(base()+"/payouts").with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content("{}" )).andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }
    @Test void authenticatedPostWithoutCsrfRejected() throws Exception {
        mvc.perform(post(base()+"/payouts").with(authentication(login())).sessionAttr("gift.credential.epoch",0L)
            .contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }
    @Test void invalidCsrfRejected() throws Exception {
        mvc.perform(post(base()+"/payouts").with(authentication(login())).sessionAttr("gift.credential.epoch",0L)
            .with(csrf().useInvalidToken()).contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }
    @Test void authenticatedWalletUsesServiceAndNoStore() throws Exception {
        when(service.wallet(event)).thenReturn(new HostSettlementUiService.Wallet(event,1000,400,600,false));
        mvc.perform(get(base()+"/wallet").with(authentication(login())).sessionAttr("gift.credential.epoch",0L))
            .andExpect(status().isOk()).andExpect(jsonPath("$.withdrawablePaise").value(600))
            .andExpect(jsonPath("$.livePayoutsEnabled").value(false))
            .andExpect(header().string("Cache-Control",org.hamcrest.Matchers.containsString("no-store")));
    }
    @Test void payoutPostRemainsBlocked() throws Exception {
        when(service.request(event,bank,600,"key-1"))
            .thenReturn(new HostPayoutRequestService.Result(request,"BLOCKED","LIVE_PAYOUTS_DISABLED"));
        mvc.perform(post(base()+"/payouts").with(authentication(login())).sessionAttr("gift.credential.epoch",0L).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"destinationId\":\""+bank+"\",\"amountPaise\":600,\"idempotencyKey\":\"key-1\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.state").value("BLOCKED"))
            .andExpect(jsonPath("$.reason").value("LIVE_PAYOUTS_DISABLED"));
        verify(service).request(event,bank,600,"key-1");
    }
    @Test void cancellationRequiresCsrf() throws Exception {
        mvc.perform(post(base()+"/payouts/"+request+"/cancel").with(authentication(login())).sessionAttr("gift.credential.epoch",0L))
            .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }
    @Test void authenticatedCancellationReturnsResult() throws Exception {
        when(service.cancel(event,request)).thenReturn(new PayoutSafetyFinalizationService.Cancellation(request,false,0));
        mvc.perform(post(base()+"/payouts/"+request+"/cancel").with(authentication(login())).sessionAttr("gift.credential.epoch",0L).with(csrf()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.alreadyCancelled").value(false));
    }
    @Test void internalReservationNotExposed() throws Exception {
        mvc.perform(post("/api/v1/host/settlement/reservations").with(authentication(login())).sessionAttr("gift.credential.epoch",0L).with(csrf()))
            .andExpect(status().isNotFound());
    }
}
