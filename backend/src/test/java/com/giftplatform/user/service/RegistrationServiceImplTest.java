
package com.giftplatform.user.service;

import com.giftplatform.user.dto.RegisterRequest;
import com.giftplatform.user.entity.User;
import com.giftplatform.user.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RegistrationServiceImplTest {

    private UserRepository repository;
    private RegistrationServiceImpl service;
    private PasswordEncoder encoder;

    @BeforeEach
    void setUp() {
        repository = mock(UserRepository.class);
        encoder = new BCryptPasswordEncoder(4);
        var policies=mock(com.giftplatform.security.recovery.EffectiveRecoveryPolicyService.class);
        service = new RegistrationServiceImpl(repository, encoder, policies);

        when(repository.saveAndFlush(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private RegisterRequest request(String username,String email,String password){
        return new RegisterRequest("Test Host",username,email,"+919876543210",password,null,true);
    }

    private RegisterRequest validRequest() {
        return request(
                "customer123",
                "customer@example.com",
                "SecurePassword123!"
        );
    }

    @Test
    void shouldRegisterCustomer() {
        var response = service.register(validRequest());

        assertThat(response.id()).isNotNull();
        assertThat(response.username()).isEqualTo("customer123");
        assertThat(response.email()).isEqualTo("customer@example.com");
        assertThat(response.role()).isEqualTo("CUSTOMER");
    }

    @Test
    void shouldHashPasswordBeforeSaving() {
        service.register(validRequest());

        ArgumentCaptor<User> captor =
                ArgumentCaptor.forClass(User.class);

        verify(repository).saveAndFlush(captor.capture());

        String hash = captor.getValue().getPasswordHash();

        assertThat(hash).isNotEqualTo("SecurePassword123!");
        assertThat(encoder.matches("SecurePassword123!", hash))
                .isTrue();
    }

    @Test
    void shouldNormalizeEmail() {
        var response = service.register(
                request(
                        "customer123",
                        " CUSTOMER@EXAMPLE.COM ",
                        "SecurePassword123!"
                )
        );

        assertThat(response.email())
                .isEqualTo("customer@example.com");
    }

    @Test
    void shouldRejectDuplicateEmail() {
        when(repository.existsByEmailIgnoreCase(
                "customer@example.com"
        )).thenReturn(true);

        assertThatThrownBy(() -> service.register(validRequest()))
                .isInstanceOf(DuplicateEmailException.class);

        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void shouldRejectNullRequest() {
        assertThatThrownBy(() -> service.register(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectNullEmail() {
        assertThatThrownBy(() -> service.register(
                request("customer123", null, "SecurePassword123!")
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectInvalidEmail() {
        assertThatThrownBy(() -> service.register(
                request("customer123", "invalid-email", "SecurePassword123!")
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectShortPassword() {
        assertThatThrownBy(() -> service.register(
                request("customer123", "valid@example.com", "Short123")
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldAcceptMinimumPasswordLength() {
        var response = service.register(
                request("customer123", "min@example.com", "Abcdef1!")
        );

        assertThat(response.id()).isNotNull();
    }

    @Test
    void shouldAcceptMaximumPasswordLength() {

        var response = service.register(
                request(
                        "customer123",
                        "max@example.com",
                        "Aa1!" + "A".repeat(68)
                )
        );

        assertThat(response.id()).isNotNull();
    }

    @Test
    void shouldRejectPasswordOverMaximumLength() {
        assertThatThrownBy(() -> service.register(
                request(
                        "customer123",
                        "long@example.com",
                        "A".repeat(73)
                )
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldAlwaysAssignCustomerRole() {
        var response = service.register(validRequest());

        assertThat(response.role())
                .isEqualTo("CUSTOMER");
    }
    @Test
    void shouldRejectPasswordExceedingUtf8ByteLimit() {

        String password = "é".repeat(40);

        assertThat(password.length()).isEqualTo(40);

        assertThatThrownBy(() -> service.register(
                request(
                        "customer123",
                        "unicode@example.com",
                        password
                )
        )).isInstanceOf(IllegalArgumentException.class);

        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void rejectsMissingConsentWithoutSaving() {
        var request = new RegisterRequest("Test Host", "customer123", "valid@example.com", "+919876543210", "Abcdef1!", null, false);
        assertThatThrownBy(() -> service.register(request)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("agree");
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void acceptsAlphanumericStartAndMixedCaseUsername() {
        assertThat(service.register(request("1Host_name", "valid@example.com", "Abcdef1!")).username()).isEqualTo("1Host_name");
    }

    @Test
    void reportsConcurrentDuplicateUsername() {
        when(repository.saveAndFlush(any())).thenThrow(new org.springframework.dao.DataIntegrityViolationException("write", new RuntimeException("ux_users_username_normalized")));
        assertThatThrownBy(() -> service.register(validRequest())).isInstanceOf(DuplicateUsernameException.class);
    }
}
