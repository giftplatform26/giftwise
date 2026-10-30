package com.giftplatform.security.recovery;
import com.giftplatform.security.delivery.MessageDelivery;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.Base64;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class GroupEmailEnrollmentControllerTest {
 private GroupEmailEnrollmentController controller(String key) {
  return new GroupEmailEnrollmentController(mock(JdbcTemplate.class),mock(RecoveryChallengeStore.class),
    mock(CredentialSessionVersionStore.class),mock(MessageDelivery.class),key);
 }
 @Test void unconfiguredKeyFailsClosed() {
  assertThrows(org.springframework.web.server.ResponseStatusException.class,()->
   controller("").start(UUID.randomUUID(),new GroupEmailEnrollmentController.Start("test@example.org",null),mock(HttpServletRequest.class)));
 }
 @Test void malformedCodeRejectedBeforeDatabaseLookup() {
  String key=Base64.getEncoder().encodeToString(new byte[32]);
  assertThrows(org.springframework.web.server.ResponseStatusException.class,()->
   controller(key).complete(UUID.randomUUID(),new GroupEmailEnrollmentController.Complete(UUID.randomUUID(),"abc",null),mock(HttpServletRequest.class)));
 }
}
