package com.giftplatform.hostpayment.verification;

import com.giftplatform.security.ActorContext;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** HTTP mapping tests with mocked persistence. Not a substitute for full security/PostgreSQL integration. */
class HostVerificationHttpContractTest {
    final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    final ActorContext actor = mock(ActorContext.class);
    final UUID host = UUID.randomUUID(), destination = UUID.randomUUID();

    @Test void rejectsInvalidKindOverHttp() throws Exception {
        var mvc = MockMvcBuilders.standaloneSetup(new HostDestinationVerificationInboxController(jdbc, actor, false)).build();
        mvc.perform(post("/api/v1/host/payment-profiles/verification/invalid/"+destination+"/request"))
            .andExpect(status().isBadRequest());
        verifyNoInteractions(jdbc,actor);
    }

    @Test void failsClosedWhenLiveFlagSet() throws Exception {
        when(actor.requireAuthenticatedUserId()).thenReturn(host);
        var mvc = MockMvcBuilders.standaloneSetup(new HostDestinationVerificationInboxController(jdbc, actor, true)).build();
        mvc.perform(post("/api/v1/host/payment-profiles/verification/upi/"+destination+"/request"))
            .andExpect(status().isServiceUnavailable());
        verifyNoInteractions(jdbc);
    }

    @Test void rejectsCrossHostHistoryOverHttp() throws Exception {
        when(actor.requireAuthenticatedUserId()).thenReturn(host);
        when(jdbc.queryForObject(anyString(),eq(Long.class),eq(destination),eq(host))).thenReturn(0L);
        var mvc = MockMvcBuilders.standaloneSetup(new HostDestinationVerificationInboxController(jdbc, actor, false)).build();
        mvc.perform(get("/api/v1/host/payment-profiles/verification/bank/"+destination+"/attempts"))
            .andExpect(status().isNotFound());
    }

    @Test void rejectsCrossHostRequestOverHttp() throws Exception {
        when(actor.requireAuthenticatedUserId()).thenReturn(host);
        when(jdbc.queryForObject(anyString(),eq(Long.class),eq(destination),eq(host))).thenReturn(0L);
        var mvc = MockMvcBuilders.standaloneSetup(new HostDestinationVerificationInboxController(jdbc, actor, false)).build();
        mvc.perform(post("/api/v1/host/payment-profiles/verification/upi/"+destination+"/request"))
            .andExpect(status().isNotFound());
        verify(jdbc,never()).update(anyString(),any(Object[].class));
    }

    @Test void rejectsInvalidPaginationOverHttp() throws Exception {
        var mvc = MockMvcBuilders.standaloneSetup(new HostDestinationVerificationInboxController(jdbc, actor, false)).build();
        mvc.perform(get("/api/v1/host/payment-profiles/verification/upi/"+destination+"/attempts?limit=101"))
            .andExpect(status().isBadRequest());
        verifyNoInteractions(jdbc,actor);
    }
}
