package com.giftplatform.group;

import com.giftplatform.event.entity.Event;
import com.giftplatform.event.entity.EventStatus;
import com.giftplatform.event.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GroupContributionSecurityTest {
  JdbcTemplate db;
  EventRepository events;
  GroupContributionController controller;
  UUID token;
  UUID eventId;

  @BeforeEach void setUp() {
    db = mock(JdbcTemplate.class);
    events = mock(EventRepository.class);
    controller = new GroupContributionController(db, events, new BCryptPasswordEncoder(4), mock(com.giftplatform.security.recovery.CredentialSessionVersionStore.class));
    token = UUID.randomUUID();
    eventId = UUID.randomUUID();
    Event event = mock(Event.class);
    when(event.getStatus()).thenReturn(EventStatus.PUBLISHED);
  org.mockito.Mockito.lenient().when(event.isGiftingEnabled()).thenReturn(true);
    when(event.getId()).thenReturn(eventId);
    when(events.findByPublicToken(token)).thenReturn(Optional.of(event));
  }

  @Test void anonymousDashboardIsRejected() {
    ResponseStatusException error = assertThrows(ResponseStatusException.class,
      () -> controller.dashboard(token, new MockHttpServletRequest()));
    assertEquals(HttpStatus.UNAUTHORIZED.value(), error.getStatusCode().value());
    verify(db, never()).queryForObject(anyString(), eq(Integer.class), any(), any());
  }

  @Test void anonymousSetupIsRejected() {
    var setup = new GroupContributionController.Setup("Friends", "Alice", "9876543210", null, null, null, null, null);
    ResponseStatusException error = assertThrows(ResponseStatusException.class,
      () -> controller.setup(token, setup, new MockHttpServletRequest()));
    assertEquals(HttpStatus.UNAUTHORIZED.value(), error.getStatusCode().value());
    verify(db, never()).update(anyString(), any(Object[].class));
  }

  @Test void weakPasswordIsRejectedBeforeInsert() {
    var credentials = new GroupContributionController.Credentials("alice123", "short");
    ResponseStatusException error = assertThrows(ResponseStatusException.class,
      () -> controller.register(token, credentials));
    assertEquals(HttpStatus.BAD_REQUEST.value(), error.getStatusCode().value());
    verify(db, never()).update(anyString(), any(Object[].class));
  }

  @Test void invalidUsernameIsRejectedBeforeInsert() {
    var credentials = new GroupContributionController.Credentials("bad name", "securePassword123");
    ResponseStatusException error = assertThrows(ResponseStatusException.class,
      () -> controller.register(token, credentials));
    assertEquals(HttpStatus.BAD_REQUEST.value(), error.getStatusCode().value());
  }

  @Test void unpublishedEventIsNotAccessible() {
    Event draft = mock(Event.class);
    when(draft.getStatus()).thenReturn(EventStatus.DRAFT);
    when(events.findByPublicToken(token)).thenReturn(Optional.of(draft));
    ResponseStatusException error = assertThrows(ResponseStatusException.class,
      () -> controller.dashboard(token, new MockHttpServletRequest()));
    assertEquals(HttpStatus.NOT_FOUND.value(), error.getStatusCode().value());
  }
}
