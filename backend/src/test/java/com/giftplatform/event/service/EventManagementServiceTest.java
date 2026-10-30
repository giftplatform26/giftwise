package com.giftplatform.event.service;

import com.giftplatform.audit.entity.*;
import com.giftplatform.audit.service.AuditService;
import com.giftplatform.event.dto.*;
import com.giftplatform.event.entity.*;
import com.giftplatform.event.repository.EventRepository;
import com.giftplatform.security.ActorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EventManagementServiceTest {
    private final EventRepository repo = mock(EventRepository.class);
    private final ActorContext actors = mock(ActorContext.class);
    private final AuditService audit = mock(AuditService.class);
    private final EventPublishReadiness readiness = mock(EventPublishReadiness.class);
    private final EventManagementService service = new EventManagementService(repo, actors, audit, readiness);
    private final UUID user = UUID.randomUUID();
    private final UUID other = UUID.randomUUID();
    private final EventRequest request = new EventRequest(" Wedding ", "Details", OccasionType.WEDDING, LocalDate.of(2027, 2, 14), "123 Test Road", "Kochi", "Kerala", "682001", "India", "Test Host", "+919876543210", "host@example.com");

    @BeforeEach void setup() {
        reset(repo, actors, audit, readiness);
        when(readiness.canPublish(any())).thenReturn(true);
        when(actors.requireAuthenticatedUserId()).thenReturn(user);
        when(repo.saveAndFlush(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));
        when(repo.findForUpdate(any())).thenAnswer(inv -> repo.findById(inv.getArgument(0)));
    }

    private Event owned() {
        Event e = new Event("Initial", OccasionType.BIRTHDAY);
        e.assignOwner(user);
        when(repo.findById(e.getId())).thenReturn(Optional.of(e));
        return e;
    }

    @Test void createAssignsOwnerTrimsTitleAndAudits() {
        EventResponse response = service.create(request);
        assertThat(response.ownerId()).isEqualTo(user);
        assertThat(response.title()).isEqualTo("Wedding");
        assertThat(response.status()).isEqualTo(EventStatus.DRAFT);
        verify(repo).saveAndFlush(any(Event.class));
        verify(audit).recordChange(eq(user), eq(AuditActorType.CUSTOMER), eq("EVENT"), eq(response.id()),
                eq(AuditAction.CREATE), isNull(), anyMap(), isNull());
    }

    @Test void getReturnsOwnedEvent() {
        Event e = owned();
        assertThat(service.get(e.getId()).id()).isEqualTo(e.getId());
    }

    @Test void getRejectsAnotherUsersEventWithoutLeakingExistence() {
        Event e = new Event("Private", OccasionType.BIRTHDAY);
        e.assignOwner(other);
        when(repo.findById(e.getId())).thenReturn(Optional.of(e));
        assertThatThrownBy(() -> service.get(e.getId())).isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
    }

    @Test void getMissingEventReturns404() {
        UUID id = UUID.randomUUID();
        when(repo.findById(id)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.get(id)).isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
    }

    @Test void listQueriesOnlyCurrentOwnersEvents() {
        Event e = owned();
        when(repo.findByOwnerIdOrderByCreatedAtDesc(user)).thenReturn(List.of(e));
        assertThat(service.list()).extracting(EventResponse::id).containsExactly(e.getId());
        verify(repo).findByOwnerIdOrderByCreatedAtDesc(user);
    }

    @Test void updateDraftRecordsOldAndNewSnapshots() {
        Event e = owned();
        EventResponse result = service.update(e.getId(), request);
        assertThat(result.title()).isEqualTo("Wedding");
        verify(audit).recordChange(eq(user), eq(AuditActorType.CUSTOMER), eq("EVENT"), eq(e.getId()),
                eq(AuditAction.UPDATE), argThat(m -> "Initial".equals(m.get("title"))),
                argThat(m -> "Wedding".equals(m.get("title"))), isNull());
    }

    @Test void updatePublishedEventRejectedWithoutSaveOrAudit() {
        Event e = owned(); e.transition(EventStatus.PUBLISHED);
        assertThatThrownBy(() -> service.update(e.getId(), request)).isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("409");
        verify(repo, never()).saveAndFlush(any());
        verifyNoInteractions(audit);
    }

    @Test void publishDraftAndAuditStatusChange() {
        Event e = owned();
        assertThat(service.changeStatus(e.getId(), EventStatus.PUBLISHED).status()).isEqualTo(EventStatus.PUBLISHED);
        verify(audit).recordChange(eq(user), eq(AuditActorType.CUSTOMER), eq("EVENT"), eq(e.getId()),
                eq(AuditAction.STATUS_CHANGE), argThat(m -> "DRAFT".equals(m.get("status"))),
                argThat(m -> "PUBLISHED".equals(m.get("status"))), isNull());
    }

    @Test void missingPreferredAccountBlocksPublishingWithoutMutation() {
        Event e = owned();
        when(readiness.canPublish(e)).thenReturn(false);
        assertThat(service.get(e.getId()).canPublish()).isFalse();
        assertThatThrownBy(() -> service.changeStatus(e.getId(), EventStatus.PUBLISHED))
            .isInstanceOf(ResponseStatusException.class).hasMessageContaining("preferred");
        assertThat(e.getStatus()).isEqualTo(EventStatus.DRAFT);
        verify(repo, never()).saveAndFlush(any());
        verifyNoInteractions(audit);
    }

    @Test void completePublishedEvent() {
        Event e = owned(); e.transition(EventStatus.PUBLISHED);
        assertThat(service.changeStatus(e.getId(), EventStatus.COMPLETED).status()).isEqualTo(EventStatus.COMPLETED);
    }

    @Test void cancelDraftEvent() {
        Event e = owned();
        assertThat(service.changeStatus(e.getId(), EventStatus.CANCELLED).status()).isEqualTo(EventStatus.CANCELLED);
    }

    @Test void rejectInvalidTransitionWithoutAudit() {
        Event e = owned();
        assertThatThrownBy(() -> service.changeStatus(e.getId(), EventStatus.COMPLETED))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("409");
        verifyNoInteractions(audit);
    }

    @Test void rejectCrossUserUpdateWithoutAudit() {
        Event e = new Event("Private", OccasionType.BIRTHDAY); e.assignOwner(other);
        when(repo.findById(e.getId())).thenReturn(Optional.of(e));
        assertThatThrownBy(() -> service.update(e.getId(), request))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("404");
        verifyNoInteractions(audit);
    }

    @Test void auditFailurePropagatesToTransactionalCaller() {
        doThrow(new IllegalStateException("audit unavailable")).when(audit).recordChange(
                any(), any(), anyString(), any(), any(), any(), any(), any());
        assertThatThrownBy(() -> service.create(request)).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("audit unavailable");
    }
    @Test
    void unpublishPublishedEventAndAuditStatusChange() {
        Event event = owned();
        event.transition(EventStatus.PUBLISHED);

        UUID originalToken = event.getPublicToken();

        EventResponse result =
                service.changeStatus(event.getId(), EventStatus.DRAFT);

        assertThat(result.status()).isEqualTo(EventStatus.DRAFT);
        assertThat(event.getPublicToken()).isEqualTo(originalToken);

        verify(audit).recordChange(
                eq(user),
                eq(AuditActorType.CUSTOMER),
                eq("EVENT"),
                eq(event.getId()),
                eq(AuditAction.STATUS_CHANGE),
                argThat(m -> "PUBLISHED".equals(m.get("status"))),
                argThat(m -> "DRAFT".equals(m.get("status"))),
                isNull()
        );
    }

    @Test
    void unpublishAnotherUsersEventReturns404WithoutAudit() {
        Event event = new Event("Private", OccasionType.BIRTHDAY);
        event.assignOwner(other);
        event.transition(EventStatus.PUBLISHED);

        when(repo.findById(event.getId()))
                .thenReturn(Optional.of(event));

        assertThatThrownBy(
                () -> service.changeStatus(event.getId(), EventStatus.DRAFT)
        ).isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");

        verify(repo, never()).saveAndFlush(any());
        verifyNoInteractions(audit);
    }
}
