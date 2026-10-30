package com.giftplatform.audit.api;

import java.util.Map;
import java.util.UUID;

public interface AuditRecorder {

    void recordEventCreated(
            UUID eventId,
            Map<String, Object> eventSnapshot
    );
}