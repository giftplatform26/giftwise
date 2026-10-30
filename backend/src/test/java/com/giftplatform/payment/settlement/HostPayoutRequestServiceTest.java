package com.giftplatform.payment.settlement;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

class HostPayoutRequestServiceTest {
    private final JdbcTemplate jdbc=mock(JdbcTemplate.class);
    private final HostPayoutRequestService service=new HostPayoutRequestService(jdbc);
    @Test void invalidInputsRejectedWithoutDbAccess() {
        UUID id=UUID.randomUUID();
        assertThrows(ResponseStatusException.class,()->service.request(id,id,id,0,"x"));
        assertThrows(ResponseStatusException.class,()->service.request(id,id,id,1," "));
        assertThrows(ResponseStatusException.class,()->service.request(null,id,id,1,"x"));
        verifyNoInteractions(jdbc);
    }
    @Test void unverifiedDestinationCannotBeRequested() {
        UUID h=UUID.randomUUID(),e=UUID.randomUUID(),b=UUID.randomUUID();
        when(jdbc.query(anyString(),any(org.springframework.jdbc.core.RowMapper.class),
                        eq(h),eq("key"))).thenReturn(java.util.List.of());
        when(jdbc.queryForObject(contains("SELECT COUNT(*)"),eq(Integer.class),
                                 eq(e),eq(h),eq(b))).thenReturn(0);
        assertThrows(ResponseStatusException.class,()->service.request(h,e,b,500,"key"));
        verify(jdbc,never()).update(contains("INSERT INTO host_payout_requests"),any(Object[].class));
    }
    @Test void verifiedDestinationStillProducesBlockedRequest() {
        UUID h=UUID.randomUUID(),e=UUID.randomUUID(),b=UUID.randomUUID();
        when(jdbc.query(anyString(),any(org.springframework.jdbc.core.RowMapper.class),
                        eq(h),eq("key"))).thenReturn(java.util.List.of());
        when(jdbc.queryForObject(contains("SELECT COUNT(*)"),eq(Integer.class),
                                 eq(e),eq(h),eq(b))).thenReturn(1);
        var result=service.request(h,e,b,500,"key");
        assertEquals("BLOCKED",result.state());
        assertEquals("LIVE_PAYOUTS_DISABLED",result.reason());
        verify(jdbc).update(contains("INSERT INTO host_payout_requests"),
                            any(UUID.class),eq(h),eq(e),eq(b),eq(500L),eq("key"));
    }
}
