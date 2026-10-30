package com.giftplatform.group;

import com.giftplatform.event.entity.Event;
import com.giftplatform.event.entity.EventStatus;
import com.giftplatform.event.repository.EventRepository;
import jakarta.servlet.http.HttpSession;
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

class GroupContributionSessionTest {
  JdbcTemplate db;
  EventRepository events;
  GroupContributionController controller;
  UUID token, eventId, accountId;

  @BeforeEach void setup() {
    db=mock(JdbcTemplate.class); events=mock(EventRepository.class);
    controller=new GroupContributionController(db,events,new BCryptPasswordEncoder(4),mock(com.giftplatform.security.recovery.CredentialSessionVersionStore.class));
    token=UUID.randomUUID(); eventId=UUID.randomUUID(); accountId=UUID.randomUUID();
    Event e=mock(Event.class);
    when(e.getStatus()).thenReturn(EventStatus.PUBLISHED);
    when(e.getId()).thenReturn(eventId);
    when(events.findByPublicToken(token)).thenReturn(Optional.of(e));
  }

  @Test void noSessionCannotReadDashboard() {
    var error=assertThrows(ResponseStatusException.class,()->controller.dashboard(token,new MockHttpServletRequest()));
    assertEquals(401,error.getStatusCode().value());
  }

  @Test void unrelatedSessionCannotReadDashboard() {
    MockHttpServletRequest request=new MockHttpServletRequest();
    request.getSession(true).setAttribute("gift.group.admin."+UUID.randomUUID(),accountId);
    assertEquals(401,assertThrows(ResponseStatusException.class,()->controller.dashboard(token,request)).getStatusCode().value());
  }

  @Test void forgedAccountSessionCannotReadDashboard() {
    MockHttpServletRequest request=new MockHttpServletRequest();
    request.getSession(true).setAttribute("gift.group.admin."+eventId,accountId);
    when(db.queryForObject(anyString(),eq(Integer.class),eq(accountId),eq(eventId))).thenReturn(0);
    assertEquals(401,assertThrows(ResponseStatusException.class,()->controller.dashboard(token,request)).getStatusCode().value());
  }

  @Test void logoutClearsOnlyEventScopedGroupLogin() {
    MockHttpServletRequest request=new MockHttpServletRequest();
    HttpSession session=request.getSession(true);
    session.setAttribute("gift.group.admin."+eventId,accountId);
    String other="gift.group.admin."+UUID.randomUUID();
    session.setAttribute(other,UUID.randomUUID());
    controller.logout(token,request);
    assertNull(session.getAttribute("gift.group.admin."+eventId));
    assertNotNull(session.getAttribute(other));
  }

  @Test void setupRequiresSessionBeforeWritingAnything() {
    var data=new GroupContributionController.Setup("Friends","Alice","9876543210",null,null,null,null,null);
    assertEquals(401,assertThrows(ResponseStatusException.class,()->controller.setup(token,data,new MockHttpServletRequest())).getStatusCode().value());
    verify(db,never()).update(anyString(),any(Object[].class));
  }
  @Test void validPageIdentityAllowsDashboard() {
    MockHttpServletRequest request=new MockHttpServletRequest();
    String page=UUID.randomUUID().toString();
    request.addHeader("X-Group-Page-Id",page);
    HttpSession session=request.getSession(true);
    session.setAttribute("gift.group.admin."+eventId,accountId);
    session.setAttribute("gift.group.page."+eventId,page);
    session.setAttribute("gift.group.activity."+eventId,System.currentTimeMillis());
    session.setAttribute("gift.group.admin.epoch."+eventId,0L);
    when(db.queryForObject(anyString(),eq(Integer.class),eq(accountId),eq(eventId))).thenReturn(1);
    when(db.query(anyString(),any(org.springframework.jdbc.core.RowMapper.class),eq(accountId))).thenReturn(java.util.Collections.emptyList());
    assertEquals(true,controller.dashboard(token,request).get("setupRequired"));
  }

  @Test void differentPageCannotReuseSession() {
    MockHttpServletRequest request=new MockHttpServletRequest();
    request.addHeader("X-Group-Page-Id",UUID.randomUUID().toString());
    HttpSession session=request.getSession(true);
    session.setAttribute("gift.group.admin."+eventId,accountId);
    session.setAttribute("gift.group.page."+eventId,UUID.randomUUID().toString());
    session.setAttribute("gift.group.activity."+eventId,System.currentTimeMillis());
    assertEquals(401,assertThrows(ResponseStatusException.class,()->controller.dashboard(token,request)).getStatusCode().value());
    verify(db,never()).queryForObject(anyString(),eq(Integer.class),any(),any());
  }

  @Test void expiredPageCannotAccessDashboard() {
    MockHttpServletRequest request=new MockHttpServletRequest();
    String page=UUID.randomUUID().toString();
    request.addHeader("X-Group-Page-Id",page);
    HttpSession session=request.getSession(true);
    session.setAttribute("gift.group.admin."+eventId,accountId);
    session.setAttribute("gift.group.page."+eventId,page);
    session.setAttribute("gift.group.activity."+eventId,System.currentTimeMillis()-11*60*1000L);
    assertEquals(401,assertThrows(ResponseStatusException.class,()->controller.dashboard(token,request)).getStatusCode().value());
    assertNull(session.getAttribute("gift.group.admin."+eventId));
    assertNull(session.getAttribute("gift.group.page."+eventId));
  }

  @Test void logoutRejectsDifferentPageIdentity() {
    MockHttpServletRequest request=new MockHttpServletRequest();
    request.addHeader("X-Group-Page-Id",UUID.randomUUID().toString());
    HttpSession session=request.getSession(true);
    session.setAttribute("gift.group.admin."+eventId,accountId);
    session.setAttribute("gift.group.page."+eventId,UUID.randomUUID().toString());
    assertEquals(401,assertThrows(ResponseStatusException.class,()->controller.logout(token,request)).getStatusCode().value());
    assertNotNull(session.getAttribute("gift.group.admin."+eventId));
  }

}
