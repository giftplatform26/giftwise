package com.giftplatform.audit.api;

import com.giftplatform.audit.entity.AuditLog;
import com.giftplatform.audit.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/audit")
public class AdminAuditController {
    private final AuditLogRepository repository;
    public AdminAuditController(AuditLogRepository repository) { this.repository = repository; }

    @GetMapping
    public Page<AuditEntry> byEntity(@RequestParam String entityType,
                                      @RequestParam UUID entityId,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "20") int size) {
        if (entityType == null || !entityType.matches("[A-Z_]{2,50}") || page < 0 || size < 1 || size > 100)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid audit query");
        return repository.findByEntityTypeAndEntityIdOrderByOccurredAtDesc(entityType, entityId,
                PageRequest.of(page, size)).map(AuditEntry::from);
    }

    public record AuditEntry(UUID id, UUID actorId, String actorType, String entityType,
                             UUID entityId, String action, Map<String,Object> oldValues,
                             Map<String,Object> newValues, Instant occurredAt, String requestId) {
        public static AuditEntry from(AuditLog a) {
            return new AuditEntry(a.getId(), a.getActorId(), a.getActorType().name(),
                    a.getEntityType(), a.getEntityId(), a.getAction().name(),
                    a.getOldValues(), a.getNewValues(), a.getOccurredAt(), a.getRequestId());
        }
    }
}
