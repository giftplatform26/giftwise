package com.giftplatform.celebration;

import com.giftplatform.admin.EffectiveModulePermissionService;
import com.giftplatform.event.entity.Event;
import com.giftplatform.event.entity.EventStatus;
import com.giftplatform.event.entity.OccasionType;
import com.giftplatform.event.repository.EventRepository;
import com.giftplatform.security.ActorContext;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CelebrationControllerTest {
  JdbcTemplate db;
  EventRepository events;
  ActorContext actor;
  HttpServletRequest request;
  CelebrationController controller;
  UUID token;
  UUID eventId;
  UUID ownerId;
  Event event;

  @BeforeEach void setup() {
    db=mock(JdbcTemplate.class);
    events=mock(EventRepository.class);
    actor=mock(ActorContext.class);
    request=mock(HttpServletRequest.class);
    controller=new CelebrationController(db,events,actor,mock(EffectiveModulePermissionService.class));
    token=UUID.randomUUID(); eventId=UUID.randomUUID(); ownerId=UUID.randomUUID();
    event=new Event("Birthday",OccasionType.BIRTHDAY);
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");
  }
  @Test void unpublishedEventCannotAcceptRsvp() {
    when(events.findByPublicToken(token)).thenReturn(Optional.of(event));
    assertEquals(404,assertThrows(ResponseStatusException.class,()->controller.rsvp(token,
      new CelebrationController.RsvpRequest("Sam","sam@example.com","YES",1),request)).getStatusCode().value());
    verifyNoInteractions(db);
  }
  @Test void unpublishedEventCannotAcceptWishOrExposeWishes() {
    when(events.findByPublicToken(token)).thenReturn(Optional.of(event));
    assertEquals(404,assertThrows(ResponseStatusException.class,()->controller.wish(token,
      new CelebrationController.WishRequest("Sam","Happy birthday"),request)).getStatusCode().value());
    assertEquals(404,assertThrows(ResponseStatusException.class,()->controller.publicWishes(token)).getStatusCode().value());
    verifyNoInteractions(db);
  }
  @Test void invalidRsvpRejectedBeforeDatabaseWrite() {
    event.transition(EventStatus.PUBLISHED);
    when(events.findByPublicToken(token)).thenReturn(Optional.of(event));
    assertEquals(400,assertThrows(ResponseStatusException.class,()->controller.rsvp(token,
      new CelebrationController.RsvpRequest("Sam","bad-email","YES",1),request)).getStatusCode().value());
    assertEquals(400,assertThrows(ResponseStatusException.class,()->controller.rsvp(token,
      new CelebrationController.RsvpRequest("Sam","sam@example.com","YES",0),request)).getStatusCode().value());
    assertEquals(400,assertThrows(ResponseStatusException.class,()->controller.rsvp(token,
      new CelebrationController.RsvpRequest("Sam","sam@example.com","MAYBE",2),request)).getStatusCode().value());
    verifyNoInteractions(db);
  }
  @Test void invalidWishRejectedBeforeDatabaseWrite() {
    event.transition(EventStatus.PUBLISHED);
    when(events.findByPublicToken(token)).thenReturn(Optional.of(event));
    assertEquals(400,assertThrows(ResponseStatusException.class,()->controller.wish(token,
      new CelebrationController.WishRequest("Sam"," "),request)).getStatusCode().value());
    verifyNoInteractions(db);
  }
  @Test void missingOrOtherOwnersCannotReadRsvps() {
    when(actor.requireAuthenticatedUserId()).thenReturn(ownerId);
    when(events.findById(eventId)).thenReturn(Optional.empty());
    assertEquals(404,assertThrows(ResponseStatusException.class,()->controller.rsvps(eventId)).getStatusCode().value());
    assertEquals(404,assertThrows(ResponseStatusException.class,()->controller.summary(eventId)).getStatusCode().value());
    assertEquals(404,assertThrows(ResponseStatusException.class,()->controller.hostWishes(eventId)).getStatusCode().value());
    verifyNoInteractions(db);
  }
  @Test void unauthorizedHostCannotModerateWish() {
    when(actor.requireAuthenticatedUserId()).thenReturn(ownerId);
    when(events.findById(eventId)).thenReturn(Optional.of(event));
    assertEquals(404,assertThrows(ResponseStatusException.class,()->controller.moderate(eventId,UUID.randomUUID(),"APPROVED")).getStatusCode().value());
    verifyNoInteractions(db);
  }
  @Test void missingPublishedTokenReturns404() {
    when(events.findByPublicToken(token)).thenReturn(Optional.empty());
    assertEquals(404,assertThrows(ResponseStatusException.class,()->controller.publicWishes(token)).getStatusCode().value());
    verifyNoInteractions(db);
  }
}
