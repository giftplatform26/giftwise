package com.giftplatform.payment.settlement;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
class VerifiedBankFallbackServiceTest {
    JdbcTemplate jdbc=mock(JdbcTemplate.class);
    VerifiedBankFallbackService service=new VerifiedBankFallbackService(jdbc);
    @Test void missingHostRejected(){
        assertThrows(IllegalArgumentException.class,()->service.eligible(null));verifyNoInteractions(jdbc);
    }
    @Test void onlyVerifiedEnabledBanksQueriedInPreferenceOrder(){
        UUID host=UUID.randomUUID();
        when(jdbc.query(anyString(),any(RowMapper.class),eq(host))).thenReturn(List.of());
        assertTrue(service.eligible(host).isEmpty());
        verify(jdbc).query(argThat((String sql)->sql.contains("verification_status='VERIFIED'")
            && sql.contains("enabled=TRUE") && sql.contains("preferred DESC")),
            any(RowMapper.class),eq(host));
    }
}
