package com.giftplatform.payment.controller;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/public/payments")
public class PublicPaymentContextController {
    private final JdbcTemplate jdbc;
    public PublicPaymentContextController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @GetMapping("/context/{token}")
    public Map<String, UUID> context(@PathVariable UUID token) {
        var ids = jdbc.query("SELECT id FROM events WHERE public_token = ? AND status = 'PUBLISHED'",
                (rs, i) -> rs.getObject(1, UUID.class), token);
        if (ids.size() != 1) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation unavailable");
        return Map.of("eventId", ids.get(0));
    }
}
