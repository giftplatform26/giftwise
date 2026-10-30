package com.giftplatform.payment.settlement;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.server.ResponseStatusException;
class PayoutReservationServiceTest {
    private final JdbcTemplate jdbc=mock(JdbcTemplate.class);
    private final SettlementBalanceService balances=mock(SettlementBalanceService.class);
    private final PayoutReservationService service=new PayoutReservationService(jdbc,balances);
    @Test void nullRequestRejected() {
        assertThrows(IllegalArgumentException.class,()->service.reserve(null));
        verifyNoInteractions(jdbc,balances);
    }
    @Test void missingRequestRejected() {
        when(jdbc.query(contains("FROM host_payout_requests"),any(RowMapper.class),any(UUID.class)))
            .thenReturn(List.of());
        assertThrows(ResponseStatusException.class,()->service.reserve(UUID.randomUUID()));
        verifyNoInteractions(balances);
    }
}
