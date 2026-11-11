package com.giftplatform.eventops;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

/** Shared validation and host-readable error bodies for the event-management tools. */
abstract class EventOpsSupport {
    static final long MAX_PAISE = 100_000_000_000L;

    static String text(String value, int max, String label) {
        if (value == null || value.isBlank()) return null;
        String v = value.strip();
        if (v.length() > max) throw bad(label + " must be at most " + max + " characters");
        if (v.chars().anyMatch(c -> Character.isISOControl(c) && c != '\n')) throw bad(label + " contains unsupported characters");
        return v;
    }
    static String required(String value, int max, String label) {
        String v = text(value, max, label);
        if (v == null) throw bad("Add " + label.toLowerCase(java.util.Locale.ROOT));
        return v;
    }
    static long paise(Long value, String label) {
        long v = value == null ? 0 : value;
        if (v < 0 || v > MAX_PAISE) throw bad(label + " must be between ₹0 and ₹1,00,00,00,000");
        return v;
    }
    static String https(String value, String label) {
        String v = text(value, 500, label);
        if (v != null && !v.matches("https://[^\\s<>\"]+")) throw bad(label + " must start with https://");
        return v;
    }
    static ResponseStatusException bad(String m) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, m); }
    static ResponseStatusException conflict(String m) { return new ResponseStatusException(HttpStatus.CONFLICT, m); }
    static ResponseStatusException notFound() { return new ResponseStatusException(HttpStatus.NOT_FOUND); }
    static ResponseStatusException changed() { return conflict("This changed in another tab. Reload and try again."); }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> reason(ResponseStatusException ex) {
        int code = ex.getStatusCode().value();
        if ((code == 400 || code == 409 || code == 429) && ex.getReason() != null) return ResponseEntity.status(code).body(Map.of("error", ex.getReason()));
        if (code == 403 && ex.getReason() != null) return ResponseEntity.status(code).body(Map.of("error", ex.getReason()));
        return ResponseEntity.status(ex.getStatusCode()).build();
    }
}
