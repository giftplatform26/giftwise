package com.giftplatform.security.recovery;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.time.Instant;
import java.util.UUID;

class RecoveryChallengeStoreTest {
    @Test void rejectsInvalidBindingBeforeDatabaseAccess() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        RecoveryChallengeStore store = new RecoveryChallengeStore(jdbc);
        UUID id = UUID.randomUUID();
        assertThrows(IllegalArgumentException.class, () -> store.create(id,"INVALID",id,"EMAIL","RESET_PASSWORD","a".repeat(64),"b".repeat(64),Instant.now().plusSeconds(300)));
        assertThrows(IllegalArgumentException.class, () -> store.create(id,"HOST",id,"EMAIL","RESET_PASSWORD","bad","b".repeat(64),Instant.now().plusSeconds(300)));
        assertFalse(store.verifyAndConsume(id,"HOST",id,"RESET_PASSWORD","a".repeat(64),"bad"));
        verifyNoInteractions(jdbc);
    }
    @Test void acceptsValidChallengeMetadata() {
        JdbcTemplate jdbc=mock(JdbcTemplate.class);
        RecoveryChallengeStore store=new RecoveryChallengeStore(jdbc);
        UUID id=UUID.randomUUID();
        store.create(id,"HOST",id,"EMAIL","RESET_PASSWORD","a".repeat(64),"b".repeat(64),Instant.now().plusSeconds(300));
        verify(jdbc).update(startsWith("INSERT INTO account_contact_challenges"),any(Object[].class));
    }
}
