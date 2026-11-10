package com.giftplatform.group;

import com.giftplatform.event.entity.Event;
import com.giftplatform.event.entity.EventStatus;
import com.giftplatform.event.repository.EventRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import java.util.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest(GroupContributionController.class)
@org.springframework.context.annotation.Import(com.giftplatform.security.SecurityConfig.class)
class GroupContributionValidCsrfHttpTest {
 @MockitoBean com.giftplatform.monetization.CommercialAccessService commerce;
 @MockitoBean com.giftplatform.security.verification.VerificationSettings verificationSettings;
 @MockitoBean GroupReadinessService readiness;
 @MockitoBean com.giftplatform.admin.EffectiveModulePermissionService modulePermissions;
 @Autowired MockMvc mvc;
 @MockitoBean com.giftplatform.security.GiftUserDetailsService users;
 @MockitoBean com.giftplatform.security.recovery.CredentialSessionVersionStore versions;
 @MockitoBean com.giftplatform.security.recovery.EffectiveRecoveryPolicyService policies;
 @MockitoBean EventRepository events;
 @MockitoBean JdbcTemplate db;
 @MockitoBean com.giftplatform.security.recovery.SignupEmailVerificationService signupEmails;
 UUID token,eventId,accountId;
 String base;
 @BeforeEach void init(){
  when(signupEmails.consume(anyString(),anyString(),any(),anyString())).thenReturn(true);
  token=UUID.randomUUID(); eventId=UUID.randomUUID(); accountId=UUID.randomUUID();
  base="/api/public/groups/"+token;
  Event event=mock(Event.class);
  when(event.getStatus()).thenReturn(EventStatus.PUBLISHED);
  org.mockito.Mockito.lenient().when(event.isGiftingEnabled()).thenReturn(true);
  when(event.getId()).thenReturn(eventId);
  when(events.findByPublicToken(token)).thenReturn(Optional.of(event));
  when(policies.effective(null,eventId)).thenReturn(new com.giftplatform.security.recovery.EffectiveRecoveryPolicyService.Policy(true,true,true,true,true));
 }
 private RequestPostProcessor csrf(){
  return req -> {
   CookieCsrfTokenRepository repo=CookieCsrfTokenRepository.withHttpOnlyFalse();
   CsrfToken t=repo.generateToken(req);
   req.setCookies(new Cookie("XSRF-TOKEN",t.getToken()));
   req.addHeader("X-XSRF-TOKEN",t.getToken());
   return req;
  };
 }
 @Test void validCsrfRegistrationReachesController() throws Exception {
  mvc.perform(post(base+"/register").with(csrf()).contentType(MediaType.APPLICATION_JSON)
    .content("{\"username\":\"groupguest1\",\"password\":\"SecurePassword123!\",\"pin\":\"123456\",\"confirmPin\":\"123456\",\"fullName\":\"Test Creator\",\"email\":\"creator@example.com\",\"mobile\":\"+919876543210\",\"emailVerificationToken\":\"proof\",\"termsAccepted\":true}"))
    .andExpect(status().isCreated());
  verify(db).update(contains("INSERT INTO gift_group_accounts"),any(),any(),eq("groupguest1"),any(),any(),any(),any(),any());
 }
 @Test void invalidCsrfRegistrationIsRejected() throws Exception {
  mvc.perform(post(base+"/register").contentType(MediaType.APPLICATION_JSON)
    .content("{\"username\":\"groupguest1\",\"password\":\"SecurePassword123!\",\"pin\":\"123456\",\"confirmPin\":\"123456\",\"fullName\":\"Test Creator\",\"email\":\"creator@example.com\",\"mobile\":\"+919876543210\",\"emailVerificationToken\":\"proof\",\"termsAccepted\":true}"))
    .andExpect(status().isForbidden());
  verify(db,never()).update(anyString(),any(Object[].class));
 }
 @Test void validCsrfSetupStillRequiresLogin() throws Exception {
  mvc.perform(post(base+"/setup").with(csrf()).contentType(MediaType.APPLICATION_JSON)
    .content("{\"name\":\"Friends\",\"creatorName\":\"Alice\",\"creatorMobile\":\"9876543210\"}"))
    .andExpect(status().isUnauthorized());
 }
 @Test void draftEventCannotRegisterEvenWithCsrf() throws Exception {
  Event draft=mock(Event.class);
  when(draft.getStatus()).thenReturn(EventStatus.DRAFT);
  when(events.findByPublicToken(token)).thenReturn(Optional.of(draft));
  mvc.perform(post(base+"/register").with(csrf()).contentType(MediaType.APPLICATION_JSON)
    .content("{\"username\":\"groupguest1\",\"password\":\"SecurePassword123!\",\"pin\":\"123456\",\"confirmPin\":\"123456\",\"fullName\":\"Test Creator\",\"email\":\"creator@example.com\",\"mobile\":\"+919876543210\",\"emailVerificationToken\":\"proof\",\"termsAccepted\":true}"))
    .andExpect(status().isNotFound());
 }

 @Test void optionalIndependentFlagDoesNotBypassLogin() throws Exception {
  for(String flag:List.of(",\"independentGift\":null",",\"independentGift\":false",",\"independentGift\":true")){
   mvc.perform(post(base+"/setup").with(csrf()).contentType(MediaType.APPLICATION_JSON)
     .content("{\"name\":\"Friends\",\"creatorName\":\"Alice\",\"creatorMobile\":\"9876543210\""+flag+"}"))
     .andExpect(status().isUnauthorized());
  }
  verify(db,never()).update(anyString(),any(Object[].class));
 }
 @Test void missingOrNullIndependentFlagDefaultsToExistingCollectionBehaviour(){
  org.junit.jupiter.api.Assertions.assertFalse(new GroupContributionController.Setup("Friends","Alice","9876543210",null,null,null,null,null).independentGift());
  org.junit.jupiter.api.Assertions.assertFalse(new GroupContributionController.Setup("Friends","Alice","9876543210",null,null,null,null,null,null,null).independentGift());
 }
}
