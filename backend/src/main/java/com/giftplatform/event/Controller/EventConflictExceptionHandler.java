package com.giftplatform.event.Controller;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

@RestControllerAdvice(assignableTypes = EventController.class)
public class EventConflictExceptionHandler {
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<Map<String,String>> conflict(OptimisticLockingFailureException ignored) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "Event was modified concurrently. Reload and retry."));
    }

    /**
     * Event validation and lifecycle reasons (e.g. publish readiness, invalid status change) are
     * written for hosts and contain no internal detail, so 400/402/409 reasons are returned to the
     * owner's client. Other statuses keep the default body, which hides messages in production.
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> status(ResponseStatusException ex) {
        int code = ex.getStatusCode().value();
        if ((code == 400 || code == 402 || code == 409) && ex.getReason() != null && !ex.getReason().isBlank()) {
            return ResponseEntity.status(code).body(Map.of("error", ex.getReason()));
        }
        return ResponseEntity.status(ex.getStatusCode()).build();
    }
}
