
package com.giftplatform.audit.service;

import com.giftplatform.audit.entity.*;
import com.giftplatform.audit.repository.AuditLogRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class AuditServiceImpl implements AuditService {

    private static final Set<String> SENSITIVE_FIELDS = Set.of(
            "password",
            "token",
            "accesstoken",
            "refreshtoken",
            "secret",
            "cvv",
            "cardnumber",
            "accountnumber"
    );

    private final AuditLogRepository repository;

    public AuditServiceImpl(AuditLogRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void recordChange(
            UUID actorId,
            AuditActorType actorType,
            String entityType,
            UUID entityId,
            AuditAction action,
            Map<String, Object> oldValues,
            Map<String, Object> newValues,
            String requestId
    ) {
        if (actorType == null || action == null
                || entityId == null || entityType == null
                || entityType.isBlank()) {
            throw new IllegalArgumentException(
                    "Required audit fields are missing"
            );
        }

        if (actorType != AuditActorType.SYSTEM
                && actorId == null) {
            throw new IllegalArgumentException(
                    "Actor ID is required for non-system actions"
            );
        }

        AuditLog auditLog = new AuditLog(
                actorId,
                actorType,
                entityType,
                entityId,
                action,
                sanitize(oldValues),
                sanitize(newValues),
                requestId
        );

        repository.save(auditLog);
    }

    private Map<String, Object> sanitize(
            Map<String, Object> values
    ) {
        if (values == null) {
            return null;
        }

        Map<String, Object> safe = new HashMap<>();

        values.forEach((key, value) -> {
            String normalized = key.toLowerCase(Locale.ROOT)
                    .replace("_", "")
                    .replace("-", "");

            if (!SENSITIVE_FIELDS.contains(normalized)) {
                safe.put(key, value);
            }
        });

        return safe;
    }
}
