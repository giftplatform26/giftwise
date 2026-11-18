package com.giftplatform.admin;

import com.giftplatform.security.ActorContext;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AdminOversightControllerTest {
  private final JdbcTemplate db = mock(JdbcTemplate.class);
  private final ActorContext actor = mock(ActorContext.class);
  private final AdminOversightController controller = new AdminOversightController(db, actor);

  private void signInAsAdmin() {
    when(actor.isPlatformAdmin()).thenReturn(true);
    when(actor.requireAuthenticatedUserId()).thenReturn(UUID.randomUUID());
  }

  @Test void nonAdminCannotReadAnything() {
    assertEquals(403, assertThrows(ResponseStatusException.class, () -> controller.users("", "ALL", "ALL", 0)).getStatusCode().value());
    assertEquals(403, assertThrows(ResponseStatusException.class, controller::verificationInbox).getStatusCode().value());
    assertEquals(403, assertThrows(ResponseStatusException.class, () -> controller.groupPayments("", 0)).getStatusCode().value());
    verifyNoInteractions(db);
  }

  @Test void unknownFiltersAndLongSearchesAreRejectedBeforeQuerying() {
    signInAsAdmin();
    assertEquals(400, assertThrows(ResponseStatusException.class, () -> controller.users("", "ROOT", "ALL", 0)).getStatusCode().value());
    assertEquals(400, assertThrows(ResponseStatusException.class, () -> controller.users("", "ALL", "SUSPENDED", 0)).getStatusCode().value());
    assertEquals(400, assertThrows(ResponseStatusException.class, () -> controller.users("x".repeat(101), "ALL", "ALL", 0)).getStatusCode().value());
    assertEquals(400, assertThrows(ResponseStatusException.class, () -> controller.groupPayments("x".repeat(101), 0)).getStatusCode().value());
    verifyNoInteractions(db);
  }

  @Test void userSearchEscapesWildcardsAndClampsThePage() {
    signInAsAdmin();
    when(db.queryForObject(anyString(), eq(Long.class), any(Object[].class))).thenReturn(30L);
    when(db.queryForList(anyString(), any(Object[].class))).thenReturn(List.of());
    Map<String, Object> result = controller.users("50%_off", "customer", "active", 99);
    assertEquals(1, result.get("page"));
    assertEquals(2, result.get("totalPages"));
    ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<Object[]> args = ArgumentCaptor.forClass(Object[].class);
    verify(db).queryForList(sql.capture(), args.capture());
    assertTrue(sql.getValue().contains("u.role=?"));
    assertTrue(sql.getValue().contains("u.enabled AND u.deleted_at IS NULL"));
    assertFalse(sql.getValue().contains("phone_number"), "phone numbers are never returned");
    assertFalse(sql.getValue().contains("password_hash"));
    assertEquals("%50\\%\\_off%", args.getValue()[0]);
    assertEquals("CUSTOMER", args.getValue()[3]);
    assertEquals(AdminOversightController.PAGE_SIZE, args.getValue()[4]);
    assertEquals(AdminOversightController.PAGE_SIZE, args.getValue()[5]);
  }

  @Test void inboxListsEveryQueueWithoutGuestIdentifiers() {
    signInAsAdmin();
    when(db.queryForMap(anyString())).thenReturn(new HashMap<>(Map.of("count", 0L)));
    when(db.queryForList(anyString())).thenReturn(List.of());
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> queues = (List<Map<String, Object>>) controller.verificationInbox().get("queues");
    assertEquals(List.of("GUEST_TRANSFERS", "GROUP_REPORTS", "HOST_ACCOUNTS", "GROUP_ACCOUNTS", "SHOWCASE"), queues.stream().map(q -> q.get("key")).toList());
    ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
    verify(db, times(5)).queryForList(sql.capture());
    for (String query : sql.getAllValues()) {
      for (String hidden : List.of("guest_name", "guest_contact", "transaction_reference", "reference_hash", "ciphertext", "upi_id_encrypted"))
        assertFalse(query.contains(hidden), hidden + " must not be exposed");
    }
    verify(db, never()).update(anyString(), any(Object[].class));
  }
}
