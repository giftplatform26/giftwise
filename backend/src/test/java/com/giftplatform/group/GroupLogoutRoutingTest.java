package com.giftplatform.group;

import com.giftplatform.event.entity.*;
import com.giftplatform.event.repository.EventRepository;
import com.giftplatform.security.*;
import com.giftplatform.security.recovery.*;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GroupContributionController.class)
@Import(SecurityConfig.class)
class GroupLogoutRoutingTest {
 @MockitoBean com.giftplatform.admin.EffectiveModulePermissionService modulePermissions;
 @Autowired MockMvc mvc;
 @MockitoBean SignupEmailVerificationService signupEmails;
 @MockitoBean JdbcTemplate db;
 @MockitoBean EventRepository events;
 @MockitoBean GiftUserDetailsService users;
 @MockitoBean CredentialSessionVersionStore versions;
 @MockitoBean EffectiveRecoveryPolicyService policies;
 UUID token=UUID.randomUUID(),eventId=UUID.randomUUID(),groupId=UUID.randomUUID();
 @BeforeEach void setup(){
  Event event=mock(Event.class);when(event.getId()).thenReturn(eventId);when(event.getStatus()).thenReturn(EventStatus.PUBLISHED);
  when(events.findByPublicToken(token)).thenReturn(Optional.of(event));
  when(db.queryForObject("SELECT count(*) FROM gift_groups WHERE id=? AND event_id=?",Integer.class,groupId,eventId)).thenReturn(1);
  when(db.queryForList("SELECT name,description,gift_name,gift_description,gift_link,target_paise,join_policy,status FROM gift_groups WHERE id=? AND event_id=?",groupId,eventId)).thenReturn(List.of(Map.of("status","PUBLISHED")));
  when(db.queryForObject(org.mockito.ArgumentMatchers.contains("a.email_verified_at"),eq(Boolean.class),eq(groupId))).thenReturn(true);
  when(db.queryForObject(org.mockito.ArgumentMatchers.contains("payment_destinations"),eq(Boolean.class),eq(groupId))).thenReturn(true);
  when(policies.effective(null,eventId)).thenReturn(new EffectiveRecoveryPolicyService.Policy(true,true,true,true,true));
 }
 @Test void csrfProtectedLogoutClearsOnlyThisMemberSession() throws Exception {
  MockHttpSession session=new MockHttpSession();session.setAttribute("gift.group.member."+groupId,UUID.randomUUID());session.setAttribute("gift.group.member.epoch."+groupId,0L);session.setAttribute("other-session","keep");
  mvc.perform(post("/api/public/groups/"+token+"/member-logout/"+groupId).session(session).with(req->{var t=CookieCsrfTokenRepository.withHttpOnlyFalse().generateToken(req);req.setCookies(new Cookie("XSRF-TOKEN",t.getToken()));req.addHeader("X-XSRF-TOKEN",t.getToken());return req;})).andExpect(status().isOk());
  assertNull(session.getAttribute("gift.group.member."+groupId));assertNull(session.getAttribute("gift.group.member.epoch."+groupId));assertEquals("keep",session.getAttribute("other-session"));
 }
 @Test void logoutWithoutCsrfIsStillForbidden() throws Exception {
  mvc.perform(post("/api/public/groups/"+token+"/member-logout/"+groupId)).andExpect(status().isForbidden());
 }
 @Test void metricsAreNotPublic() throws Exception {
  mvc.perform(get("/actuator/metrics/http.server.requests")).andExpect(status().isUnauthorized());
 }
}
