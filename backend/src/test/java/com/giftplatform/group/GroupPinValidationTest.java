package com.giftplatform.group;

import com.giftplatform.event.entity.Event;
import com.giftplatform.event.entity.EventStatus;
import com.giftplatform.event.repository.EventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class GroupPinValidationTest {
 @Test void missingPinRejectedAtRegistration(){
  var db=mock(JdbcTemplate.class);var events=mock(EventRepository.class);var event=mock(Event.class);
  UUID token=UUID.randomUUID();when(event.getStatus()).thenReturn(EventStatus.PUBLISHED);
  org.mockito.Mockito.lenient().when(event.isGiftingEnabled()).thenReturn(true);
  when(event.getId()).thenReturn(UUID.randomUUID());when(events.findByPublicToken(token)).thenReturn(Optional.of(event));
  var controller=new GroupContributionController(db,events,new BCryptPasswordEncoder(4),mock(com.giftplatform.security.recovery.CredentialSessionVersionStore.class));
  var error=assertThrows(ResponseStatusException.class,()->controller.register(token,new GroupContributionController.Credentials("admin123","validPassword123!",null,null,"Test Admin","admin@example.com","+919876543210",null,true)));
  assertEquals(HttpStatus.BAD_REQUEST,error.getStatusCode());verify(db,never()).update(anyString(),any(Object[].class));
 }
 @Test void mismatchedPinRejectedAtRegistration(){
  var db=mock(JdbcTemplate.class);var events=mock(EventRepository.class);var event=mock(Event.class);
  UUID token=UUID.randomUUID();when(event.getStatus()).thenReturn(EventStatus.PUBLISHED);
  org.mockito.Mockito.lenient().when(event.isGiftingEnabled()).thenReturn(true);
  when(event.getId()).thenReturn(UUID.randomUUID());when(events.findByPublicToken(token)).thenReturn(Optional.of(event));
  var controller=new GroupContributionController(db,events,new BCryptPasswordEncoder(4),mock(com.giftplatform.security.recovery.CredentialSessionVersionStore.class));
  var error=assertThrows(ResponseStatusException.class,()->controller.register(token,new GroupContributionController.Credentials("admin123","validPassword123!","123456","654321","Test Admin","admin@example.com","+919876543210",null,true)));
  assertEquals(HttpStatus.BAD_REQUEST,error.getStatusCode());verify(db,never()).update(anyString(),any(Object[].class));
 }
 @Test void validPinIsStoredHashed(){
  var db=mock(JdbcTemplate.class);var events=mock(EventRepository.class);var event=mock(Event.class);
  UUID token=UUID.randomUUID();when(event.getStatus()).thenReturn(EventStatus.PUBLISHED);
  org.mockito.Mockito.lenient().when(event.isGiftingEnabled()).thenReturn(true);
  when(event.getId()).thenReturn(UUID.randomUUID());when(events.findByPublicToken(token)).thenReturn(Optional.of(event));
  var controller=new GroupContributionController(db,events,new BCryptPasswordEncoder(4),mock(com.giftplatform.security.recovery.CredentialSessionVersionStore.class));
  var signup=mock(com.giftplatform.security.recovery.SignupEmailVerificationService.class);
  when(signup.consume(anyString(),anyString(),any(),anyString())).thenReturn(true);
  org.springframework.test.util.ReflectionTestUtils.setField(controller,"signupEmails",signup);
  controller.register(token,new GroupContributionController.Credentials("admin123","validPassword123!","123456","123456","Test Admin","admin@example.com","+919876543210","proof",true));
  var capture=org.mockito.ArgumentCaptor.forClass(Object.class);
  verify(db).update(contains("pin_hash"),any(),any(),any(),any(),capture.capture(),any(),any(),any());
  String stored=(String)capture.getValue();assertNotEquals("123456",stored);assertTrue(new BCryptPasswordEncoder().matches("123456",stored));
 }
}
