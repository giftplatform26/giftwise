
package com.giftplatform.security;

import com.giftplatform.security.dto.AuthResponse;
import com.giftplatform.security.dto.LoginRequest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final SessionAuthenticationStrategy sessionStrategy;
    private final com.giftplatform.security.recovery.CredentialSessionVersionStore versions;

    public AuthController(
            AuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository,
            SessionAuthenticationStrategy sessionStrategy,
            com.giftplatform.security.recovery.CredentialSessionVersionStore versions
    ) {
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.sessionStrategy = sessionStrategy;
        this.versions = versions;
    }

    @PostMapping("/login")
    public AuthResponse login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse
    ) {
        Authentication authentication =
                authenticationManager.authenticate(
                        UsernamePasswordAuthenticationToken
                                .unauthenticated(
                                        request.identifier(),
                                        request.password()
                                )
                );

        sessionStrategy.onAuthentication(
                authentication,
                servletRequest,
                servletResponse
        );

        SecurityContext context =
                SecurityContextHolder.createEmptyContext();

        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        securityContextRepository.saveContext(
                context,
                servletRequest,
                servletResponse
        );
        GiftUserPrincipal signedIn = (GiftUserPrincipal) authentication.getPrincipal();
        String accountKind = signedIn.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PLATFORM_ADMIN")) ? "PLATFORM_ADMIN" : "HOST";
        servletRequest.getSession(true).setAttribute("gift.credential.epoch", versions.current(accountKind, signedIn.getUserId()));

        GiftUserPrincipal principal =
                (GiftUserPrincipal) authentication.getPrincipal();

        String role = principal.getAuthorities()
                .iterator()
                .next()
                .getAuthority()
                .replaceFirst("^ROLE_", "");

        return new AuthResponse(
                principal.getUserId(),
                principal.getUsername(),
                role
        );
    }

    @GetMapping("/me")
    public AuthResponse currentUser(Authentication authentication) {
        GiftUserPrincipal principal =
                (GiftUserPrincipal) authentication.getPrincipal();

        String role = principal.getAuthorities()
                .iterator()
                .next()
                .getAuthority()
                .replaceFirst("^ROLE_", "");

        return new AuthResponse(
                principal.getUserId(),
                principal.getUsername(),
                role
        );
    }
}
