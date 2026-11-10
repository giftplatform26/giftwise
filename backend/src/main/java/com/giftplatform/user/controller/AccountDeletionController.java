package com.giftplatform.user.controller;

import com.giftplatform.security.ActorContext;
import com.giftplatform.user.service.AccountDeletionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

/** Self-service account deletion (required for app-store distribution). Covered by /api/auth/profile/** authentication. */
@RestController
@RequestMapping("/api/auth/profile/delete-account")
public class AccountDeletionController {
    public record DeletionRequest(String password, String confirmation) {}

    private final AccountDeletionService deletion;
    private final ActorContext actor;

    public AccountDeletionController(AccountDeletionService deletion, ActorContext actor) {
        this.deletion = deletion;
        this.actor = actor;
    }

    @PostMapping
    public ResponseEntity<Void> delete(@RequestBody(required = false) DeletionRequest input, HttpServletRequest request) {
        deletion.delete(actor.requireAuthenticatedUserId(),
            input == null ? null : input.password(), input == null ? null : input.confirmation());
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }

    /** Deletion reasons are written for the account owner, so they are returned even when server messages are hidden. */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> reason(ResponseStatusException ex) {
        int code = ex.getStatusCode().value();
        if ((code == 400 || code == 403 || code == 409) && ex.getReason() != null)
            return ResponseEntity.status(code).body(Map.of("error", ex.getReason()));
        return ResponseEntity.status(ex.getStatusCode()).build();
    }
}
