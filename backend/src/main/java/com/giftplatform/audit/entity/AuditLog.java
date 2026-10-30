
package com.giftplatform.audit.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "actor_id", updatable = false)
    private UUID actorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "actor_type", nullable = false,
            updatable = false, length = 30)
    private AuditActorType actorType;

    @Column(name = "entity_type", nullable = false,
            updatable = false, length = 50)
    private String entityType;

    @Column(name = "entity_id", nullable = false,
            updatable = false)
    private UUID entityId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 30)
    private AuditAction action;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "old_values",
            columnDefinition = "jsonb", updatable = false)
    private Map<String, Object> oldValues;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "new_values",
            columnDefinition = "jsonb", updatable = false)
    private Map<String, Object> newValues;

    @Column(name = "occurred_at", nullable = false,
            insertable = false, updatable = false)
    private Instant occurredAt;

    @Column(name = "request_id",
            updatable = false, length = 100)
    private String requestId;

    protected AuditLog() {
    }

    public AuditLog(
            UUID actorId,
            AuditActorType actorType,
            String entityType,
            UUID entityId,
            AuditAction action,
            Map<String, Object> oldValues,
            Map<String, Object> newValues,
            String requestId
    ) {
        this.id = UUID.randomUUID();
        this.actorId = actorId;
        this.actorType = actorType;
        this.entityType = entityType;
        this.entityId = entityId;
        this.action = action;
        this.oldValues = oldValues == null
                ? null : Map.copyOf(oldValues);
        this.newValues = newValues == null
                ? null : Map.copyOf(newValues);
        this.requestId = requestId;
    }

    public UUID getId() {
        return id;
    }

    public UUID getActorId() {
        return actorId;
    }

    public AuditActorType getActorType() {
        return actorType;
    }

    public String getEntityType() {
        return entityType;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public AuditAction getAction() {
        return action;
    }

    public Map<String, Object> getOldValues() {
        return oldValues == null
                ? null : Map.copyOf(oldValues);
    }

    public Map<String, Object> getNewValues() {
        return newValues == null
                ? null : Map.copyOf(newValues);
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public String getRequestId() {
        return requestId;
    }
}
