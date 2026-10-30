package com.giftplatform.admin;

import com.giftplatform.security.ActorContext;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PlatformAdminConsoleControllerTest {
  private final JdbcTemplate db = mock(JdbcTemplate.class);
  private final ActorContext actor = mock(ActorContext.class);
  private final PlatformAdminConsoleController controller = new PlatformAdminConsoleController(db, actor);

  @Test void nonAdminCannotViewModules() {
    assertEquals(403, assertThrows(ResponseStatusException.class,
      controller::modules).getStatusCode().value());
    verifyNoInteractions(db);
  }

  @Test void nonAdminCannotTogglePlatformModule() {
    assertEquals(403, assertThrows(ResponseStatusException.class,
      () -> controller.platformModule("GUESTBOOK", new PlatformAdminConsoleController.Toggle(true)))
      .getStatusCode().value());
    verifyNoInteractions(db);
  }

  @Test void nonAdminCannotToggleHostModule() {
    assertEquals(403, assertThrows(ResponseStatusException.class,
      () -> controller.hostModule(UUID.randomUUID(), "RSVP", new PlatformAdminConsoleController.Toggle(true)))
      .getStatusCode().value());
    verifyNoInteractions(db);
  }

  @Test void unknownModuleRejectedBeforeWrite() {
    when(actor.isPlatformAdmin()).thenReturn(true);
    when(actor.requireAuthenticatedUserId()).thenReturn(UUID.randomUUID());
    assertEquals(400, assertThrows(ResponseStatusException.class,
      () -> controller.platformModule("PAYMENT_VERIFIED", new PlatformAdminConsoleController.Toggle(true)))
      .getStatusCode().value());
    verifyNoInteractions(db);
  }

  @Test void nonexistentHostCannotReceiveGrant() {
    when(actor.isPlatformAdmin()).thenReturn(true);
    when(actor.requireAuthenticatedUserId()).thenReturn(UUID.randomUUID());
    when(db.queryForObject(anyString(), eq(Integer.class), any(UUID.class))).thenReturn(0);
    assertEquals(404, assertThrows(ResponseStatusException.class,
      () -> controller.hostModule(UUID.randomUUID(), "GUESTBOOK", new PlatformAdminConsoleController.Toggle(true)))
      .getStatusCode().value());
    verify(db, never()).update(anyString(), any(Object[].class));
  }
}
