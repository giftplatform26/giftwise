package com.giftplatform.hostpayment.verification;

import com.giftplatform.security.ActorContext;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HostDestinationVerificationInboxControllerTest {
    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final ActorContext actor = mock(ActorContext.class);
    private final UUID id = UUID.randomUUID();

    @Test void invalidKindDoesNotQueryDatabase() {
        var api = new HostDestinationVerificationInboxController(jdbc, actor, false);
        assertThrows(ResponseStatusException.class, () -> api.request("other", id));
        verifyNoInteractions(jdbc, actor);
    }

    @Test void enabledFlagCannotPretendProviderIsIntegrated() {
        var api = new HostDestinationVerificationInboxController(jdbc, actor, true);
        when(actor.requireAuthenticatedUserId()).thenReturn(UUID.randomUUID());
        var ex = assertThrows(ResponseStatusException.class, () -> api.request("upi", id));
        assertEquals(503, ex.getStatusCode().value());
        verifyNoInteractions(jdbc);
    }

    @Test void otherHostsDestinationIsNotVisible() {
        var api = new HostDestinationVerificationInboxController(jdbc, actor, false);
        when(actor.requireAuthenticatedUserId()).thenReturn(UUID.randomUUID());
        when(jdbc.queryForObject(anyString(), eq(Long.class), eq(id), any(UUID.class))).thenReturn(0L);
        var ex = assertThrows(ResponseStatusException.class, () -> api.history("bank", id, 20, 0));
        assertEquals(404, ex.getStatusCode().value());
    }

    @Test void invalidPaginationRejectedBeforeDatabaseRead() {
        var api = new HostDestinationVerificationInboxController(jdbc, actor, false);
        assertThrows(ResponseStatusException.class, () -> api.history("upi", id, 101, 0));
        verifyNoInteractions(jdbc, actor);
    }
}
