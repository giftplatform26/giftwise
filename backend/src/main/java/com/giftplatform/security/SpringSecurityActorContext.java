
package com.giftplatform.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SpringSecurityActorContext implements ActorContext {

    @Override
    public UUID requireAuthenticatedUserId() {

        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal()
                instanceof GiftUserPrincipal principal)) {

            throw new AccessDeniedException(
                    "Authentication required"
            );
        }

        return principal.getUserId();
    }

    @Override
    public boolean isPlatformAdmin() {

        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal()
                instanceof GiftUserPrincipal)) {
            return false;
        }

        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        "ROLE_PLATFORM_ADMIN".equals(
                                authority.getAuthority()
                        )
                );
    }
}
