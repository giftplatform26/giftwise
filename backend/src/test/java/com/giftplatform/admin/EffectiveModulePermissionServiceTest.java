package com.giftplatform.admin;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;
import java.util.UUID;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EffectiveModulePermissionServiceTest {
  private final JdbcTemplate db = mock(JdbcTemplate.class);
  private final EffectiveModulePermissionService service = new EffectiveModulePermissionService(db);

  @Test void missingHostFailsClosedWithoutDatabaseQuery() {
    assertFalse(service.enabled(null, "GUESTBOOK"));
    assertEquals(404, assertThrows(ResponseStatusException.class,
      () -> service.require(null, "GUESTBOOK")).getStatusCode().value());
    verifyNoInteractions(db);
  }

  @Test void missingGrantFailsClosed() {
    UUID host = UUID.randomUUID();
    when(db.queryForObject(anyString(), eq(Boolean.class), eq("GUESTBOOK"), eq(host))).thenReturn(false);
    assertEquals(404, assertThrows(ResponseStatusException.class,
      () -> service.require(host, "GUESTBOOK")).getStatusCode().value());
  }

  @Test void effectiveGrantAllowsFeature() {
    UUID host = UUID.randomUUID();
    when(db.queryForObject(anyString(), eq(Boolean.class), eq("RSVP"), eq(host))).thenReturn(true);
    assertDoesNotThrow(() -> service.require(host, "RSVP"));
  }

  @Test void nullDatabaseResultFailsClosed() {
    assertFalse(service.enabled(UUID.randomUUID(), "GUESTBOOK"));
  }

  @Test void guestbookDefaultsToAutoWhenNoSettingExists() {
    UUID event = UUID.randomUUID();
    when(db.query(anyString(), any(org.springframework.jdbc.core.RowMapper.class), eq(event)))
      .thenReturn(List.of());
    assertEquals("AUTO", service.guestbookMode(event));
  }

  @Test void approvalSettingIsRespected() {
    UUID event = UUID.randomUUID();
    when(db.query(anyString(), any(org.springframework.jdbc.core.RowMapper.class), eq(event)))
      .thenReturn(List.of("APPROVAL"));
    assertEquals("APPROVAL", service.guestbookMode(event));
  }
}
