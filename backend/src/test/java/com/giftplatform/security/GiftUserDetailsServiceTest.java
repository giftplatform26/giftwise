
package com.giftplatform.security;

import com.giftplatform.user.entity.User;
import com.giftplatform.user.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class GiftUserDetailsServiceTest {

    private UserRepository repository;
    private GiftUserDetailsService service;

    @BeforeEach
    void setUp() {
        repository = mock(UserRepository.class);
        service = new GiftUserDetailsService(repository);
    }

    @Test
    void shouldLoginUsingUsername() {
        User user = new User(
                "customer123",
                "customer@example.com",
                "hashed-password"
        );

        when(repository.findByUsernameIgnoreCase("customer123"))
                .thenReturn(Optional.of(user));

        var details = service.loadUserByUsername("customer123");

        assertThat(details.getUsername())
                .isEqualTo("customer@example.com");
    }

    @Test
    void shouldLoginUsingEmail() {
        User user = new User(
                "customer123",
                "customer@example.com",
                "hashed-password"
        );

        when(repository.findByEmailIgnoreCase("customer@example.com"))
                .thenReturn(Optional.of(user));

        var details = service.loadUserByUsername(
                "customer@example.com"
        );

        assertThat(details.getUsername())
                .isEqualTo("customer@example.com");
    }

    @Test
    void shouldNormalizeUsername() {
        User user = new User(
                "customer123",
                "customer@example.com",
                "hashed-password"
        );

        when(repository.findByUsernameIgnoreCase("customer123"))
                .thenReturn(Optional.of(user));

        service.loadUserByUsername(" CUSTOMER123 ");

        verify(repository)
                .findByUsernameIgnoreCase("customer123");
    }

    @Test
    void shouldRejectUnknownUsername() {
        assertThatThrownBy(() ->
                service.loadUserByUsername("unknownuser")
        ).isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void shouldRejectUnknownEmail() {
        assertThatThrownBy(() ->
                service.loadUserByUsername("unknown@example.com")
        ).isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void shouldRejectBlankIdentifier() {
        assertThatThrownBy(() ->
                service.loadUserByUsername(" ")
        ).isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void shouldRejectNullIdentifier() {
        assertThatThrownBy(() ->
                service.loadUserByUsername(null)
        ).isInstanceOf(UsernameNotFoundException.class);
    }
}
