package com.giftplatform.group;

import com.giftplatform.event.entity.Event;
import com.giftplatform.event.entity.EventStatus;
import com.giftplatform.event.repository.EventRepository;
import com.giftplatform.security.recovery.CredentialSessionVersionStore;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Regression: a reset must invalidate existing group admin/member sessions. */
class GroupCredentialRevocationTest {
    JdbcTemplate db;
    EventRepository events;
    CredentialSessionVersionStore versions;
    GroupContributionController controller;
    UUID token, eventId, accountId;

    @BeforeEach void setup() {
        db = mock(JdbcTemplate.class);
        events = mock(EventRepository.class);
        versions = mock(CredentialSessionVersionStore.class);
        controller = new GroupContributionController(db, events, new BCryptPasswordEncoder(4), versions);
        token = UUID.randomUUID(); eventId = UUID.randomUUID(); accountId = UUID.randomUUID();
        Event event = mock(Event.class);
        when(event.getId()).thenReturn(eventId);
        when(event.getStatus()).thenReturn(EventStatus.PUBLISHED);
  org.mockito.Mockito.lenient().when(event.isGiftingEnabled()).thenReturn(true);
        when(events.findByPublicToken(token)).thenReturn(Optional.of(event));
    }

    private MockHttpServletRequest adminRequest(long sessionEpoch) {
        var request = new MockHttpServletRequest();
        String pageId = UUID.randomUUID().toString();
        request.addHeader("X-Group-Page-Id", pageId);
        HttpSession session = request.getSession(true);
        session.setAttribute("gift.group.admin." + eventId, accountId);
        session.setAttribute("gift.group.page." + eventId, pageId);
        session.setAttribute("gift.group.activity." + eventId, System.currentTimeMillis());
        session.setAttribute("gift.group.admin.epoch." + eventId, sessionEpoch);
        return request;
    }

    @Test void revokedAdminSessionIsRejectedBeforeDatabaseReads() {
        var request = adminRequest(0L);
        when(versions.current("GROUP_ADMIN", accountId)).thenReturn(1L);
        var error = assertThrows(ResponseStatusException.class, () -> controller.dashboard(token, request));
        assertEquals(401, error.getStatusCode().value());
        assertNull(request.getSession(false).getAttribute("gift.group.admin." + eventId));
        verify(db, never()).queryForObject(anyString(), eq(Integer.class), any(), any());
    }

    @Test void currentAdminEpochPassesSecurityGate() {
        var request = adminRequest(3L);
        when(versions.current("GROUP_ADMIN", accountId)).thenReturn(3L);
        when(db.queryForObject(anyString(), eq(Integer.class), eq(accountId), eq(eventId))).thenReturn(1);
        when(db.query(anyString(), any(org.springframework.jdbc.core.RowMapper.class), eq(accountId)))
            .thenReturn(java.util.Collections.emptyList());
        assertEquals(true, controller.dashboard(token, request).get("setupRequired"));
    }

    @Test void revokedMemberSessionIsRejected() throws Exception {
        var request = new MockHttpServletRequest();
        HttpSession session = request.getSession(true);
        session.setAttribute("gift.group.member." + eventId, accountId);
        session.setAttribute("gift.group.member.epoch." + eventId, 0L);
        when(versions.current("GROUP_MEMBER", accountId)).thenReturn(1L);
        var method = GroupContributionController.class.getDeclaredMethod("member", UUID.class, jakarta.servlet.http.HttpServletRequest.class);
        method.setAccessible(true);
        var ex = assertThrows(InvocationTargetException.class, () -> method.invoke(controller, eventId, request));
        assertInstanceOf(ResponseStatusException.class, ex.getCause());
        assertEquals(401, ((ResponseStatusException) ex.getCause()).getStatusCode().value());
        assertNull(session.getAttribute("gift.group.member." + eventId));
        verify(db, never()).queryForObject(anyString(), eq(Integer.class), any(), any());
    }

    @Test void missingMemberEpochFailsClosed() throws Exception {
        var request = new MockHttpServletRequest();
        request.getSession(true).setAttribute("gift.group.member." + eventId, accountId);
        var method = GroupContributionController.class.getDeclaredMethod("member", UUID.class, jakarta.servlet.http.HttpServletRequest.class);
        method.setAccessible(true);
        var ex = assertThrows(InvocationTargetException.class, () -> method.invoke(controller, eventId, request));
        assertEquals(401, ((ResponseStatusException) ex.getCause()).getStatusCode().value());
    }
}
