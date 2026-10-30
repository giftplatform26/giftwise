
package com.giftplatform.audit.service;

import com.giftplatform.audit.entity.*;
import com.giftplatform.audit.repository.AuditLogRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuditServiceImplTest {

    private AuditLogRepository repository;
    private AuditServiceImpl service;

    private final UUID actorId = UUID.randomUUID();
    private final UUID entityId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        repository = mock(AuditLogRepository.class);
        service = new AuditServiceImpl(repository);
    }

    private void record(
            Map<String, Object> oldValues,
            Map<String, Object> newValues
    ) {
        service.recordChange(
                actorId,
                AuditActorType.CUSTOMER,
                "EVENT",
                entityId,
                AuditAction.UPDATE,
                oldValues,
                newValues,
                "request-123"
        );
    }

    private AuditLog capturedLog() {
        ArgumentCaptor<AuditLog> captor =
                ArgumentCaptor.forClass(AuditLog.class);

        verify(repository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void shouldRecordValidCustomerChange() {
        record(
                Map.of("title", "Old"),
                Map.of("title", "New")
        );

        AuditLog log = capturedLog();

        assertThat(log.getActorId()).isEqualTo(actorId);
        assertThat(log.getEntityId()).isEqualTo(entityId);
        assertThat(log.getAction()).isEqualTo(AuditAction.UPDATE);
        assertThat(log.getOldValues()).containsEntry("title", "Old");
        assertThat(log.getNewValues()).containsEntry("title", "New");
    }

    @Test
    void shouldRemoveSensitiveFields() {
        record(
                null,
                Map.of(
                        "title", "Birthday",
                        "password", "secret123",
                        "access_token", "token123",
                        "cardNumber", "123456789"
                )
        );

        Map<String, Object> values =
                capturedLog().getNewValues();

        assertThat(values)
                .containsEntry("title", "Birthday")
                .doesNotContainKeys(
                        "password",
                        "access_token",
                        "cardNumber"
                );
    }

    @Test
    void shouldAllowNullOldValuesForCreation() {
        record(null, Map.of("title", "New"));

        assertThat(capturedLog().getOldValues()).isNull();
    }

    @Test
    void shouldAllowNullNewValuesForDeletion() {
        record(Map.of("title", "Old"), null);

        assertThat(capturedLog().getNewValues()).isNull();
    }

    @Test
    void shouldRejectMissingActorId() {
        assertThatThrownBy(() ->
                service.recordChange(
                        null,
                        AuditActorType.CUSTOMER,
                        "EVENT",
                        entityId,
                        AuditAction.CREATE,
                        null,
                        Map.of("title", "Test"),
                        null
                )
        ).isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(repository);
    }

    @Test
    void shouldAllowSystemWithoutActorId() {
        service.recordChange(
                null,
                AuditActorType.SYSTEM,
                "EVENT",
                entityId,
                AuditAction.STATUS_CHANGE,
                null,
                Map.of("status", "COMPLETED"),
                null
        );

        assertThat(capturedLog().getActorType())
                .isEqualTo(AuditActorType.SYSTEM);
    }

    @Test
    void shouldRejectMissingEntityId() {
        assertThatThrownBy(() ->
                service.recordChange(
                        actorId,
                        AuditActorType.CUSTOMER,
                        "EVENT",
                        null,
                        AuditAction.CREATE,
                        null,
                        null,
                        null
                )
        ).isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(repository);
    }

    @Test
    void shouldRejectMissingAction() {
        assertThatThrownBy(() ->
                service.recordChange(
                        actorId,
                        AuditActorType.CUSTOMER,
                        "EVENT",
                        entityId,
                        null,
                        null,
                        null,
                        null
                )
        ).isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(repository);
    }

    @Test
    void shouldRejectBlankEntityType() {
        assertThatThrownBy(() ->
                service.recordChange(
                        actorId,
                        AuditActorType.CUSTOMER,
                        " ",
                        entityId,
                        AuditAction.CREATE,
                        null,
                        null,
                        null
                )
        ).isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(repository);
    }

    @Test
    void shouldPreserveRequestId() {
        record(null, Map.of("title", "Test"));

        assertThat(capturedLog().getRequestId())
                .isEqualTo("request-123");
    }

    @Test
    void shouldPropagateRepositoryFailure() {
        when(repository.save(any(AuditLog.class)))
                .thenThrow(new RuntimeException("Database unavailable"));

        assertThatThrownBy(() ->
                record(null, Map.of("title", "Test"))
        ).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Database unavailable");
    }
}
