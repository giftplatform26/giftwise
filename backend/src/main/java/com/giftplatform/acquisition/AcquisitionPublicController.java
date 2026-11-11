package com.giftplatform.acquisition;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Platform-level switches public pages need before anyone signs in (try-without-account, showcase).
 * Only these codes are exposed; host and event switches are never revealed here.
 */
@RestController
public class AcquisitionPublicController {
    static final String[] PUBLIC_CODES = {"TRY_WITHOUT_ACCOUNT", "CELEBRATION_SHOWCASE", "GUEST_FIRST_FREE"};
    private final JdbcTemplate db;
    public AcquisitionPublicController(JdbcTemplate db) { this.db = db; }

    @GetMapping("/api/public/acquisition-modules")
    public ResponseEntity<Map<String, Boolean>> modules() {
        Map<String, Boolean> out = new LinkedHashMap<>();
        for (String code : PUBLIC_CODES) out.put(code, platformOn(db, code));
        return ResponseEntity.ok().cacheControl(CacheControl.maxAge(Duration.ofSeconds(30))).body(out);
    }

    static boolean platformOn(JdbcTemplate db, String code) {
        return Boolean.TRUE.equals(db.query("SELECT enabled FROM platform_module_controls WHERE module_code=?", (rs, i) -> rs.getBoolean(1), code)
            .stream().findFirst().orElse(false));
    }
}
