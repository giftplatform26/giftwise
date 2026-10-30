package com.giftplatform.security.recovery;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CredentialSessionVersionStoreTest {
    @Test void rejectsUnknownKinds() {
        var store = new CredentialSessionVersionStore(mock(JdbcTemplate.class));
        assertThrows(IllegalArgumentException.class, () -> store.revokeAll("OTHER", UUID.randomUUID()));
        assertThrows(IllegalArgumentException.class, () -> store.current("HOST", null));
    }
    @Test void incrementsEpoch() {
        var jdbc = mock(JdbcTemplate.class);
        var store = new CredentialSessionVersionStore(jdbc);
        var id = UUID.randomUUID();
        when(jdbc.queryForObject(contains("ON CONFLICT"), eq(Long.class), eq("HOST"), eq(id))).thenReturn(2L);
        assertEquals(2L, store.revokeAll("HOST", id));
        verify(jdbc).queryForObject(contains("RETURNING version"), eq(Long.class), eq("HOST"), eq(id));
    }
}
