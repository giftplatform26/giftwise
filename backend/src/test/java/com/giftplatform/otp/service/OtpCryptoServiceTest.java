
package com.giftplatform.otp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class OtpCryptoServiceTest {

    private OtpCryptoService service;

    @BeforeEach
    void setUp() {
        service = new OtpCryptoService(
                "test-only-secret-key-with-32-plus-bytes"
                        .getBytes(StandardCharsets.UTF_8)
        );
    }

    @Test
    void shouldGenerateSixDigitOtp() {
        assertThat(service.generateOtp())
                .matches("[0-9]{6}");
    }

    @Test
    void shouldGenerateDifferentOtpValues() {
        Set<String> values = new HashSet<>();

        for (int i = 0; i < 100; i++) {
            values.add(service.generateOtp());
        }

        assertThat(values.size()).isGreaterThan(1);
    }

    @Test
    void shouldGenerateDeterministicDigest() {
        UUID id = UUID.randomUUID();

        assertThat(service.digest(id, "123456"))
                .isEqualTo(service.digest(id, "123456"));
    }

    @Test
    void shouldVerifyCorrectOtp() {
        UUID id = UUID.randomUUID();
        String digest = service.digest(id, "123456");

        assertThat(service.matches(
                id, "123456", digest)).isTrue();
    }

    @Test
    void shouldRejectIncorrectOtp() {
        UUID id = UUID.randomUUID();
        String digest = service.digest(id, "123456");

        assertThat(service.matches(
                id, "654321", digest)).isFalse();
    }

    @Test
    void shouldRejectDigestFromAnotherChallenge() {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();

        String digest = service.digest(first, "123456");

        assertThat(service.matches(
                second, "123456", digest)).isFalse();
    }

    @Test
    void shouldRejectMalformedOtp() {
        UUID id = UUID.randomUUID();
        String digest = service.digest(id, "123456");

        assertThat(service.matches(
                id, "12345", digest)).isFalse();

        assertThat(service.matches(
                id, "abcdef", digest)).isFalse();

        assertThat(service.matches(
                id, null, digest)).isFalse();
    }

    @Test
    void shouldRejectMalformedDigest() {
        assertThat(service.matches(
                UUID.randomUUID(),
                "123456",
                "not-a-valid-digest"
        )).isFalse();
    }

    @Test
    void shouldRejectShortSecret() {
        assertThatThrownBy(() ->
                new OtpCryptoService(
                        "short".getBytes(StandardCharsets.UTF_8)
                )
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectNullSecret() {
        assertThatThrownBy(() ->
                new OtpCryptoService(null)
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldProduceDifferentDigestsForDifferentCodes() {
        UUID id = UUID.randomUUID();

        assertThat(service.digest(id, "123456"))
                .isNotEqualTo(service.digest(id, "654321"));
    }

    @Test
    void shouldProduceDifferentDigestsForDifferentKeys() {
        UUID id = UUID.randomUUID();

        OtpCryptoService another = new OtpCryptoService(
                "another-test-secret-key-with-32-bytes"
                        .getBytes(StandardCharsets.UTF_8)
        );

        assertThat(service.digest(id, "123456"))
                .isNotEqualTo(another.digest(id, "123456"));
    }
}
