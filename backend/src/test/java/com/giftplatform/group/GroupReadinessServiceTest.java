package com.giftplatform.group;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class GroupReadinessServiceTest {
 private final JdbcTemplate db=mock(JdbcTemplate.class);
 private final GroupReadinessService service=new GroupReadinessService(db);
 private final UUID group=UUID.randomUUID();
 @Test void invitationRequiresEmailAndAccountIndependently(){
  when(db.queryForObject(contains("a.email_verified_at"),eq(Boolean.class),eq(group))).thenReturn(false);
  when(db.queryForObject(contains("payment_destinations"),eq(Boolean.class),eq(group))).thenReturn(false);
  assertThat(service.invitation(group).enabled()).isFalse();
  assertThat(service.invitation(group).reasons()).containsExactly("Verify your email in Account","Add and enable a receiving account");
  when(db.queryForObject(contains("a.email_verified_at"),eq(Boolean.class),eq(group))).thenReturn(true);
  assertThat(service.invitation(group).reasons()).containsExactly("Add and enable a receiving account");
  when(db.queryForObject(contains("payment_destinations"),eq(Boolean.class),eq(group))).thenReturn(true);
  assertThat(service.invitation(group).enabled()).isTrue();
 }
 @Test void missingVerificationEvidenceFailsClosed(){assertThat(service.invitation(group).enabled()).isFalse();}
}
