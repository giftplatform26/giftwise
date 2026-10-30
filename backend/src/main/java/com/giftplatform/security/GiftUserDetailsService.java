
package com.giftplatform.security;

import com.giftplatform.user.entity.User;
import com.giftplatform.user.repository.UserRepository;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class GiftUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public GiftUserDetailsService(
            UserRepository userRepository
    ) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String identifier)
            throws UsernameNotFoundException {

        if (identifier == null || identifier.isBlank()) {
            throw new UsernameNotFoundException(
                    "Invalid credentials"
            );
        }

        String normalized = identifier.trim()
                .toLowerCase(Locale.ROOT);

        User user;

        if (normalized.contains("@")) {
            user = userRepository
                    .findByEmailIgnoreCase(normalized)
                    .orElseThrow(() ->
                            new UsernameNotFoundException(
                                    "Invalid credentials"
                            )
                    );
        } else {
            user = userRepository
                    .findByUsernameIgnoreCase(normalized)
                    .orElseThrow(() ->
                            new UsernameNotFoundException(
                                    "Invalid credentials"
                            )
                    );
        }

        return new GiftUserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getRole().name(),
                user.isEnabled()
        );
    }
}
