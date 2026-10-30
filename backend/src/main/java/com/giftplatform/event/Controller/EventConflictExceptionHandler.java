package com.giftplatform.event.Controller;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;

@RestControllerAdvice(assignableTypes = EventController.class)
public class EventConflictExceptionHandler {
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<Map<String,String>> conflict(OptimisticLockingFailureException ignored) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "Event was modified concurrently. Reload and retry."));
    }
}
