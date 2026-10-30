package com.giftplatform.payment.settlement;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
class SettlementBalanceServiceTest {
    private final JdbcTemplate jdbc=mock(JdbcTemplate.class);
    private final SettlementBalanceService service=new SettlementBalanceService(jdbc);
    @Test void rejectsNullEvent() {
        assertThrows(IllegalArgumentException.class,()->service.read(null));
        verifyNoInteractions(jdbc);
    }
    @Test void onlyAvailableMinusHeldCanBeWithdrawn() {
        UUID event=UUID.randomUUID();
        when(jdbc.queryForObject(contains("wallet_ledger_entries"),eq(Long.class),eq(event))).thenReturn(1000L);
        when(jdbc.queryForObject(contains("payout_reservations"),eq(Long.class),eq(event))).thenReturn(400L);
        var b=service.read(event);
        assertEquals(1000,b.availablePaise());assertEquals(400,b.heldPaise());
        assertEquals(600,b.withdrawablePaise());
    }
    @Test void zeroBalanceDoesNotBecomeAvailable() {
        UUID event=UUID.randomUUID();
        when(jdbc.queryForObject(anyString(),eq(Long.class),eq(event))).thenReturn(0L);
        assertEquals(0,service.read(event).withdrawablePaise());
    }
    @Test void negativeLedgerIsRejected() {
        UUID event=UUID.randomUUID();
        when(jdbc.queryForObject(contains("wallet_ledger_entries"),eq(Long.class),eq(event))).thenReturn(-1L);
        when(jdbc.queryForObject(contains("payout_reservations"),eq(Long.class),eq(event))).thenReturn(0L);
        assertThrows(IllegalStateException.class,()->service.read(event));
    }
}
