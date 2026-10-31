package com.giftplatform.security.recovery;
import com.giftplatform.security.delivery.MessageDelivery;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.Base64;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
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
 @Test void verifiedCreatorCannotStartEmailChange() {
  var db=mock(JdbcTemplate.class);var challenges=mock(RecoveryChallengeStore.class);
  var versions=mock(CredentialSessionVersionStore.class);var delivery=mock(MessageDelivery.class);
  UUID token=UUID.randomUUID(),event=UUID.randomUUID(),account=UUID.randomUUID();
  var req=new org.springframework.mock.web.MockHttpServletRequest();
  req.addHeader("X-Group-Page-Id","page");
  req.getSession().setAttribute("gift.group.admin."+event,account);
  req.getSession().setAttribute("gift.group.page."+event,"page");
  req.getSession().setAttribute("gift.group.activity."+event,System.currentTimeMillis());
  req.getSession().setAttribute("gift.group.admin.epoch."+event,0L);
  when(db.query(contains("SELECT id FROM events"),any(org.springframework.jdbc.core.RowMapper.class),eq(token))).thenReturn(java.util.List.of(event));
  when(db.queryForObject(contains("SELECT count(*) FROM gift_group_accounts"),eq(Integer.class),eq(account),eq(event))).thenReturn(1);
  when(db.queryForObject(contains("email_verified_at IS NOT NULL"),eq(Boolean.class),eq(account))).thenReturn(true);
  var c=new GroupEmailEnrollmentController(db,challenges,versions,delivery,Base64.getEncoder().encodeToString(new byte[32]));
  var error=assertThrows(org.springframework.web.server.ResponseStatusException.class,()->c.start(token,new GroupEmailEnrollmentController.Start("new@example.test",null),req));
  assertEquals(409,error.getStatusCode().value());
  verifyNoInteractions(challenges,delivery);
 }
}
