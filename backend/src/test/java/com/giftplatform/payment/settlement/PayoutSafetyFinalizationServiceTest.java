package com.giftplatform.payment.settlement;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.server.ResponseStatusException;
class PayoutSafetyFinalizationServiceTest {
    JdbcTemplate jdbc=mock(JdbcTemplate.class);
    PayoutSafetyFinalizationService service=new PayoutSafetyFinalizationService(jdbc);
    @Test void invalidRequestNeverTouchesDatabase(){
        assertThrows(IllegalArgumentException.class,()->service.cancel(null,UUID.randomUUID()));
        assertThrows(IllegalArgumentException.class,()->service.recordInternalObservation(UUID.randomUUID(),"EXECUTED"));
        verifyNoInteractions(jdbc);
    }
    @Test void otherHostCannotCancel(){
        UUID host=UUID.randomUUID(),request=UUID.randomUUID();
        when(jdbc.query(contains("FOR UPDATE"),any(RowMapper.class),eq(request)))
            .thenReturn(List.<Object[]>of(new Object[]{UUID.randomUUID(),UUID.randomUUID(),"BLOCKED",null}));
        assertThrows(ResponseStatusException.class,()->service.cancel(host,request));
        verify(jdbc,never()).update(anyString(),any(Object[].class));
    }
    @Test void cancelledRequestIsIdempotent(){
        UUID host=UUID.randomUUID(),request=UUID.randomUUID();
        when(jdbc.query(contains("FOR UPDATE"),any(RowMapper.class),eq(request)))
            .thenReturn(List.<Object[]>of(new Object[]{host,UUID.randomUUID(),"BLOCKED",new java.sql.Timestamp(1)}));
        assertTrue(service.cancel(host,request).alreadyCancelled());
        verify(jdbc,never()).update(anyString(),any(Object[].class));
    }
    @Test void blockedRequestReleasesHolds(){
        UUID host=UUID.randomUUID(),request=UUID.randomUUID(),event=UUID.randomUUID();
        when(jdbc.query(contains("FOR UPDATE"),any(RowMapper.class),eq(request)))
            .thenReturn(List.<Object[]>of(new Object[]{host,event,"BLOCKED",null}));
        when(jdbc.update(contains("UPDATE payout_reservations"),eq(request),eq(event))).thenReturn(1);
        when(jdbc.update(contains("UPDATE host_payout_requests"),eq(request))).thenReturn(1);
        var result=service.cancel(host,request);
        assertFalse(result.alreadyCancelled()); assertEquals(1,result.releasedHolds());
    }
    @Test void observationNeverMutatesPayout(){
        UUID request=UUID.randomUUID();
        when(jdbc.queryForObject(contains("SELECT COUNT(*)"),eq(Integer.class),eq(request))).thenReturn(1);
        var result=service.recordInternalObservation(request,"UNKNOWN");
        assertEquals("INTERNAL_REVIEW",result.source());
        verify(jdbc).update(contains("INSERT INTO payout_reconciliation_observations"),
            any(UUID.class),eq(request),eq("UNKNOWN"));
        verify(jdbc,never()).update(contains("UPDATE host_payout_requests"),any(Object[].class));
    }
}
