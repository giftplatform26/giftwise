package com.giftplatform.security;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CsrfBootstrapController {
    @GetMapping("/api/auth/csrf")
    public ResponseEntity<Void> csrf(CsrfToken csrfToken) {
        csrfToken.getToken(); // Force deferred cookie token creation.
        return ResponseEntity.noContent().cacheControl(CacheControl.noStore()).build();
    }
}
