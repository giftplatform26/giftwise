package com.giftplatform.security.recovery;

import com.giftplatform.security.GiftUserPrincipal;
import com.giftplatform.security.delivery.MessageDelivery;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UnverifiedEmailUpdateTest {
    final JdbcTemplate db=mock(JdbcTemplate.class);
    final RecoveryChallengeStore challenges=mock(RecoveryChallengeStore.class);
    final UUID id=UUID.randomUUID();
    final EmailRecoveryController controller=new EmailRecoveryController(db,challenges,
            mock(CredentialSessionVersionStore.class),mock(PasswordEncoder.class),mock(MessageDelivery.class),
            Base64.getEncoder().encodeToString(new byte[32]));

    Authentication account(String role,String currentEmail,boolean verified) throws Exception {
        var principal=new GiftUserPrincipal(id,"old@example.test","hash",role,true);
        var rs=mock(ResultSet.class);
        when(rs.getObject("id",UUID.class)).thenReturn(id);
        when(rs.getString("email")).thenReturn(currentEmail);
        when(rs.getTimestamp("email_verified_at")).thenReturn(verified?Timestamp.from(Instant.now()):null);
        when(db.query(contains("WHERE id=? AND role=?"),any(RowMapper.class),eq(id),eq(role)))
                .thenAnswer(call->java.util.List.of(((RowMapper<?>)call.getArgument(1)).mapRow(rs,0)));
        return new UsernamePasswordAuthenticationToken(principal,"",principal.getAuthorities());
    }
    @Test void hostAndAdminCanUpdateTheirOwnUnverifiedEmail() throws Exception {
        for(String role:java.util.List.of("CUSTOMER","PLATFORM_ADMIN")) {
            var auth=account(role,"old@example.test",false);
            when(db.update(startsWith("UPDATE users SET email="),eq("new@example.test"),eq(id))).thenReturn(1);
            assertThat(controller.updateEmail(new EmailRecoveryController.UpdateEmail(" NEW@example.test "),auth).message()).contains("Email updated");
            verify(db).update(contains("SET consumed_at=now()"),eq(role.equals("CUSTOMER")?"HOST":"PLATFORM_ADMIN"),eq(id));
        }
    }
    @Test void verifiedEmailCannotBeChanged() throws Exception {
        var auth=account("PLATFORM_ADMIN","verified@example.test",true);
        assertThatThrownBy(()->controller.updateEmail(new EmailRecoveryController.UpdateEmail("new@example.test"),auth))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("Verified email addresses cannot be changed");
        verify(db,never()).update(anyString(),any(Object[].class));
    }
    @Test void anonymousCannotUpdateEmail() {
        assertThatThrownBy(()->controller.updateEmail(new EmailRecoveryController.UpdateEmail("new@example.test"),null))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("401");
        verifyNoInteractions(db);
    }
    @Test void malformedEmailIsRejected() throws Exception {
        var auth=account("CUSTOMER","old@example.test",false);
        assertThatThrownBy(()->controller.updateEmail(new EmailRecoveryController.UpdateEmail("junk"),auth))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("valid email");
    }
    @Test void duplicateEmailIsReportedWithoutDatabaseDetails() throws Exception {
        var auth=account("CUSTOMER","old@example.test",false);
        when(db.update(startsWith("UPDATE users SET email="),anyString(),eq(id))).thenThrow(new DuplicateKeyException("private database details"));
        assertThatThrownBy(()->controller.updateEmail(new EmailRecoveryController.UpdateEmail("new@example.test"),auth))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("already in use").hasMessageNotContaining("private database");
    }
    @Test void concurrentVerificationPreventsUpdate() throws Exception {
        var auth=account("CUSTOMER","old@example.test",false);
        when(db.update(contains("AND email_verified_at IS NULL"),anyString(),eq(id))).thenReturn(0);
        assertThatThrownBy(()->controller.updateEmail(new EmailRecoveryController.UpdateEmail("new@example.test"),auth))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("already verified");
    }
    @Test void statusUsesAccountIdAfterEmailChanges() throws Exception {
        var auth=account("CUSTOMER","new@example.test",false);
        assertThat(controller.emailStatus(auth).email()).isEqualTo("new@example.test");
    }
    @Test void oldVerificationCodeCannotVerifyNewAddress() throws Exception {
        var auth=account("CUSTOMER","new@example.test",false);
        assertThatThrownBy(()->controller.verifyEmail(new EmailRecoveryController.VerifyEmail(UUID.randomUUID(),"123456"),auth))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("Invalid or expired code");
        verify(db,never()).update(startsWith("UPDATE users SET email_verified_at"),any(),any());
    }
}
