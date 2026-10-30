package com.giftplatform.group;

import com.giftplatform.event.entity.Event;
import com.giftplatform.event.entity.EventStatus;
import com.giftplatform.event.repository.EventRepository;
import com.giftplatform.security.recovery.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class GroupPolicyFlowTest {
 final JdbcTemplate db=mock(JdbcTemplate.class);
 final EventRepository events=mock(EventRepository.class);
 final EffectiveRecoveryPolicyService policies=mock(EffectiveRecoveryPolicyService.class);
 final CredentialSessionVersionStore versions=mock(CredentialSessionVersionStore.class);
 final UUID token=UUID.randomUUID(),eventId=UUID.randomUUID(),groupId=UUID.randomUUID();
 final GroupContributionController controller=new GroupContributionController(db,events,new BCryptPasswordEncoder(4),versions,policies);
 @BeforeEach void setup(){
  var signup=mock(SignupEmailVerificationService.class);when(signup.consume(anyString(),anyString(),any(),anyString())).thenReturn(true);org.springframework.test.util.ReflectionTestUtils.setField(controller,"signupEmails",signup);
  Event event=mock(Event.class);when(event.getId()).thenReturn(eventId);when(event.getStatus()).thenReturn(EventStatus.PUBLISHED);
  when(events.findByPublicToken(token)).thenReturn(Optional.of(event));
  when(policies.effective(null,eventId)).thenReturn(new EffectiveRecoveryPolicyService.Policy(true,true,true,true,false));
  when(db.queryForObject("SELECT count(*) FROM gift_groups WHERE id=? AND event_id=?",Integer.class,groupId,eventId)).thenReturn(1);
  when(db.queryForList(contains("SELECT name,description"),eq(groupId),eq(eventId))).thenReturn(List.of(Map.of("status","PUBLISHED","join_policy","AUTO_JOIN")));
 }
 @Test void disabledPinStillAllowsPasswordRegistration(){
  controller.register(token,new GroupContributionController.Credentials("creator","Password123!",null,null,"Test Creator","creator@example.com","+919876543210","proof"));
  verify(db).update(contains("INSERT INTO gift_group_accounts"),any(),eq(eventId),eq("creator"),any(),isNull(),eq("Test Creator"),eq("creator@example.com"),eq("+919876543210"));
 }
 @Test void disabledPinStillAllowsMemberRegistration(){
  controller.join(token,groupId,new GroupContributionController.JoinRequest("Test Member","+919876543210","Password123!","Password123!",null,null,"member@example.test","proof"));
  verify(db).update(contains("INSERT INTO gift_group_members"),any(),eq(groupId),eq("Test Member"),eq("test member"),any(),any(),eq("APPROVED"),isNull());
 }
 @Test void disabledPinRejectsCreatorLoginBeforeHashLookup(){
  var error=assertThrows(ResponseStatusException.class,()->controller.login(token,new GroupContributionController.Credentials("creator",null,"123456",null),new MockHttpServletRequest()));
  assertEquals(403,error.getStatusCode().value());verifyNoInteractions(db);
 }
 @Test void disabledPinRejectsMemberLoginBeforeHashLookup(){
  var error=assertThrows(ResponseStatusException.class,()->controller.memberLogin(token,groupId,new GroupContributionController.MemberLogin("Test Member",null,"123456"),new MockHttpServletRequest()));
  assertEquals(403,error.getStatusCode().value());
  verify(db,never()).queryForList(contains("SELECT id,password_hash"),any(UUID.class),anyString());
 }
 @Test void passwordLoginUsesSingleAccountLookupWhenPinDisabled(){
  var request=new MockHttpServletRequest();request.setRemoteAddr(UUID.randomUUID().toString());request.addHeader("X-Group-Page-Id",UUID.randomUUID().toString());
  String hash=new BCryptPasswordEncoder(4).encode("Password123!");UUID id=UUID.randomUUID();
  when(db.queryForList(contains("WHERE event_id=? AND username=?"),eq(eventId),eq("creator"))).thenReturn(List.of(Map.of("id",id,"password_hash",hash)));
  controller.login(token,new GroupContributionController.Credentials("creator","Password123!"),request);
  assertEquals(id,request.getSession().getAttribute("gift.group.admin."+eventId));
 }
 @Test void namedPinLoginChecksOnlyTheSelectedAccount(){
  when(policies.effective(null,eventId)).thenReturn(new EffectiveRecoveryPolicyService.Policy(true,true,true,true,true));
  var request=new MockHttpServletRequest();request.setRemoteAddr(UUID.randomUUID().toString());request.addHeader("X-Group-Page-Id",UUID.randomUUID().toString());UUID id=UUID.randomUUID();
  when(db.queryForList(contains("WHERE event_id=? AND username=?"),eq(eventId),eq("creator"))).thenReturn(List.of(Map.of("id",id,"password_hash","unused","pin_hash",new BCryptPasswordEncoder(4).encode("123456"))));
  controller.login(token,new GroupContributionController.Credentials("creator",null,"123456",null),request);
  assertEquals(id,request.getSession().getAttribute("gift.group.admin."+eventId));
 }
 @Test void creatorPinWithoutUsernameIsRejectedBeforeAccountLookup(){
  when(policies.effective(null,eventId)).thenReturn(new EffectiveRecoveryPolicyService.Policy(true,true,true,true,true));
  var error=assertThrows(ResponseStatusException.class,()->controller.login(token,new GroupContributionController.Credentials("",null,"123456",null),new MockHttpServletRequest()));
  assertEquals(400,error.getStatusCode().value());verifyNoInteractions(db);
 }
 @Test void memberPinWithoutUsernameIsRejectedBeforeAccountLookup(){
  when(policies.effective(null,eventId)).thenReturn(new EffectiveRecoveryPolicyService.Policy(true,true,true,true,true));
  var error=assertThrows(ResponseStatusException.class,()->controller.memberLogin(token,groupId,new GroupContributionController.MemberLogin(null,null,"123456"),new MockHttpServletRequest()));
  assertEquals(400,error.getStatusCode().value());
  verify(db,never()).queryForList(contains("SELECT id,password_hash"),any(UUID.class));
  verify(db,never()).queryForList(contains("SELECT id,password_hash"),any(UUID.class),anyString());
 }
 @Test void memberUsernameAndPinAuthenticateOnlyTheSelectedAccount(){
  when(policies.effective(null,eventId)).thenReturn(new EffectiveRecoveryPolicyService.Policy(true,true,true,true,true));
  UUID id=UUID.randomUUID();var request=new MockHttpServletRequest();request.setRemoteAddr(UUID.randomUUID().toString());
  when(db.queryForList(contains("WHERE group_id=? AND username=?"),eq(groupId),eq("test member"))).thenReturn(List.of(Map.of("id",id,"password_hash","unused","pin_hash",new BCryptPasswordEncoder(4).encode("123456"),"status","APPROVED")));
  controller.memberLogin(token,groupId,new GroupContributionController.MemberLogin(null,null,"123456","Test Member"),request);
  assertEquals(id,request.getSession().getAttribute("gift.group.member."+groupId));
  verify(db,never()).queryForList(contains("pin_hash IS NOT NULL"),any(UUID.class));
 }
 @Test void correctPinForAnotherUsernameCannotAuthenticateCreator(){
  when(policies.effective(null,eventId)).thenReturn(new EffectiveRecoveryPolicyService.Policy(true,true,true,true,true));
  var request=new MockHttpServletRequest();request.setRemoteAddr(UUID.randomUUID().toString());
  assertEquals(401,assertThrows(ResponseStatusException.class,()->controller.login(token,new GroupContributionController.Credentials("unknown",null,"123456",null),request)).getStatusCode().value());
  assertNull(request.getSession(false));
  verify(db).queryForList(contains("WHERE event_id=? AND username=?"),eq(eventId),eq("unknown"));
  verify(db,never()).queryForList(contains("SELECT id,password_hash"),eq(eventId));
 }
 @Test void revokedMemberSessionCannotBeUsedForContributions(){
  var request=new MockHttpServletRequest();UUID member=UUID.randomUUID();request.getSession().setAttribute("gift.group.member."+groupId,member);request.getSession().setAttribute("gift.group.member.epoch."+groupId,0L);
  when(versions.current("GROUP_MEMBER",member)).thenReturn(1L);
  assertEquals(401,assertThrows(ResponseStatusException.class,()->controller.member(groupId,request)).getStatusCode().value());
  assertNull(request.getSession().getAttribute("gift.group.member."+groupId));
 }
}
