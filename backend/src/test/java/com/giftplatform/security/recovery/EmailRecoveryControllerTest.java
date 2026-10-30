package com.giftplatform.security.recovery;

import com.giftplatform.security.delivery.MessageDelivery;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import java.util.Base64;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmailRecoveryControllerTest {
    private EmailRecoveryController controller() {
        return new EmailRecoveryController(mock(JdbcTemplate.class),mock(RecoveryChallengeStore.class),
                mock(CredentialSessionVersionStore.class),mock(PasswordEncoder.class),
                mock(MessageDelivery.class),Base64.getEncoder().encodeToString(new byte[32]));
    }
    @Test void unknownAccountGetsGenericResponse() {
        var req=mock(HttpServletRequest.class);when(req.getRemoteAddr()).thenReturn("127.0.0.42");
        var result=controller().request(new EmailRecoveryController.RequestCode("UNRECOGNIZED","absent@example.test","RESET_PASSWORD"),req);
        assertNotNull(result.challengeId());
        assertTrue(result.message().contains("If an eligible"));
    }
    @Test void badCodeCannotChangeCredential() {
        assertThrows(ResponseStatusException.class,()->controller().complete(new EmailRecoveryController.Reset(
                java.util.UUID.randomUUID(),"HOST","host@example.test","RESET_PASSWORD","not-an-otp","NewPass123!","NewPass123!")));
    }
    @Test void emailVerificationRequiresSignedInAccount() {
        assertThrows(ResponseStatusException.class,()->controller().verifyEmail(new EmailRecoveryController.VerifyEmail(
                java.util.UUID.randomUUID(),"123456"),null));
    }
}
