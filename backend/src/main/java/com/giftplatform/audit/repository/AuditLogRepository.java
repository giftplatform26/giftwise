
package com.giftplatform.audit.repository;

import com.giftplatform.audit.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AuditLogRepository
        extends JpaRepository<AuditLog, UUID> {

    Page<AuditLog> findByEntityTypeAndEntityIdOrderByOccurredAtDesc(
            String entityType,
            UUID entityId,
            Pageable pageable
    );

    Page<AuditLog> findByActorIdOrderByOccurredAtDesc(
            UUID actorId,
            Pageable pageable
    );
}
