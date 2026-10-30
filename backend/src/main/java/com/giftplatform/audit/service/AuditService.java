
package com.giftplatform.audit.service;

import com.giftplatform.audit.entity.AuditAction;
import com.giftplatform.audit.entity.AuditActorType;

import java.util.Map;
import java.util.UUID;

public interface AuditService {

    void recordChange(
            UUID actorId,
            AuditActorType actorType,
            String entityType,
            UUID entityId,
            AuditAction action,
            Map<String, Object> oldValues,
            Map<String, Object> newValues,
            String requestId
    );
}
